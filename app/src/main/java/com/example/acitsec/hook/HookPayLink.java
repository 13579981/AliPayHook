package com.example.acitsec.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xutils.x;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import bean.WebsocketErrorRequestBean;
import bean.WebsocketRequestBean;
import bean.WebsocketResponseBean;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import utils.AlarmUtils;
import utils.FileUtils;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by ACITSEC on 2018/7/25.
 */

public class HookPayLink implements IXposedHookLoadPackage {
    private static final String TAG = "HookPayLink";
    private String qr_money = null;
    private String beizhu = null;
    private String qrCodeUrl = null;

    private WebSocket webSocket;
    private String url = "";//TODO;
    private Request request = null;

    public static final String RET_CODE_SUCCESS = "00";
    public static final String RET_CODE_TOO_FAST = "01";
    public static final String RET_CODE_FAIL = "02";
    public static final String RET_CODE_OTHER_ERROR = "03";
    public static final String RET_CODE_NETWORK_ERROR = "04";

    private static final String TXNTYPE_CODE = "T01";
    private static final String TXNTYPE_ERROR = "T11";

    private String orgId;
    private String ip;
    private String key;

    WebSocketListener webSocketListener;
    OkHttpClient mOkHttpClient;

    private static final int RECALL_SERVER = 2;
    private static final int NOTIFY_SERVER = 3;
    private static final int CONNECT_SERVER = 1;
    private static final int DEVICE_IS_NOT_ACTIVE = 4;
    private static final int LOW_POWER = 5;

    private WebsocketResponseBean websocketResponseBean;

    private Context context;
    private static boolean isActive = true;//检测是否活跃
    private static long isActive_timeStamp;//检测是否活跃
    private boolean isSetMoney = true;//是否设置金额页
    private boolean hasSetMoneyAndReason = false;//是否已经设置好金额和理由
    private Button comfirm;//确认按钮

    private MyReceiver myReceiver;

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_SERVER:
                    if (context != null) {
                        Toast.makeText(context, "已经连上服务器", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case RECALL_SERVER:
                    Log.e(TAG, "handleMessage: 重新连接服务器");
                    if (request == null) {
                        request = new Request.Builder().url(url).build();
                    }
                    if (mOkHttpClient != null) {
                        mOkHttpClient.newWebSocket(request, webSocketListener);//重新连接webSocket
                    }
                    break;
                case NOTIFY_SERVER:
                    if (webSocket != null) {
                        Log.e(TAG, "handleMessage: " + "{\"orgId\":\"" + orgId + "\",\"txnType\":" + "\"T21\"" + "}");
                        boolean send = webSocket.send("{\"orgId\":\"" + orgId + "\",\"txnType\":" + "\"T21\"" + "}");
                        if (!send) {
                            Message message = new Message();
                            message.what = RECALL_SERVER;
                            myHandler.sendMessage(message);
                        }
                    }
                    break;
                case DEVICE_IS_NOT_ACTIVE:
                    WebsocketErrorRequestBean websocketRequestBean = new WebsocketErrorRequestBean();
                    websocketRequestBean.setOrgId("");
                    websocketRequestBean.setRetCode(RET_CODE_FAIL);
                    if (context != null) {
                        AlarmUtils.getInstance().warnning(context);
                    }
                    if (websocketResponseBean != null) {
                        websocketRequestBean.setRetMsg("支付宝没有在工作" + websocketResponseBean.getTotalAmount() + " reason : " + websocketResponseBean.getReason());
                    } else {
                        websocketRequestBean.setRetMsg("支付宝没有在工作");
                    }
                    websocketRequestBean.setTxnType(TXNTYPE_ERROR);
                    websocketRequestBean.setSign(key);
                    Gson gson = new Gson();
                    String s = gson.toJson(websocketRequestBean);
                    Log.e(TAG, "upLoadUrl: " + s);
                    if (webSocket != null) {
                        webSocket.send(s);
                    }
                    break;
                case LOW_POWER:
                    if (HookPayLink.this.context != null) {
                        AlarmUtils.getInstance().warnning(HookPayLink.this.context);
                    }
                    if (HookPayLink.this.webSocket != null) {
                        WebsocketErrorRequestBean websocketRequestBean1 = new WebsocketErrorRequestBean();//发送低电量通知
                        if (websocketResponseBean != null) {
                            websocketRequestBean1.setOrgId(websocketResponseBean.getOrgId());
                        } else {
                            websocketRequestBean1.setOrgId(orgId);
                        }
                        websocketRequestBean1.setRetCode(RET_CODE_OTHER_ERROR);
                        websocketRequestBean1.setRetMsg("低电量");
                        websocketRequestBean1.setTxnType(TXNTYPE_ERROR);
                        websocketRequestBean1.setSign(key);
                        Gson gson1 = new Gson();
                        String s1 = gson1.toJson(websocketRequestBean1);
                        HookPayLink.this.webSocket.send(s1);
                        Log.e(TAG, "upLoadUrl: " + s1);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        //hook支付链接的回调
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if (AndroidAppHelper.currentApplication() != null) {
                    Intent intent = new Intent();
                    intent.setClassName("com.alipay.mobile.payee.ui", "PayeeQRSetMoneyActivity");
                    AndroidAppHelper.currentApplication().startActivity(intent);
                } else {
                    Log.e(TAG, "handleLoadPackage:  AndroidAppHelper.currentApplication() is null");
                }

                if (methodHookParam != null) {
                    Object[] args = methodHookParam.args;
                    if (args != null) {
                        if (args.length > 1) {
                            if (args[0].equals("qr_money")) {
                                //这是金额
                                qr_money = (String) args[1];
                            }
                            if (args[0].equals("beiZhu")) {
                                //这是备注
                                beizhu = (String) args[1];
                            }
                            if (args[0].equals("qrCodeUrl")) {
                                qrCodeUrl = (String) args[1];
                                if (qr_money != null && beizhu != null) {
                                    Log.e(TAG, "afterHookedMethod: " + qr_money + beizhu + "wangic");
                                    if (websocketResponseBean != null && websocketResponseBean.isCorrectResponse(key) && webSocket != null) {
                                        if (qr_money.equals(websocketResponseBean.getTotalAmount()) && beizhu.equals(websocketResponseBean.getReason())) {
                                            WebsocketRequestBean websocketRequestBean = new WebsocketRequestBean();
                                            websocketRequestBean.setOrgId(orgId);
                                            websocketRequestBean.setPayUrl(qrCodeUrl);
                                            websocketRequestBean.setReason(websocketResponseBean.getReason());
                                            websocketRequestBean.setRetMsg("成功");
                                            websocketRequestBean.setRetCode(RET_CODE_SUCCESS);
                                            websocketRequestBean.setTotalAmount(websocketResponseBean.getTotalAmount());
                                            websocketRequestBean.setTxnType(TXNTYPE_CODE);
                                            websocketRequestBean.setSign(key);
                                            Gson gson = new Gson();
                                            String s = gson.toJson(websocketRequestBean);
                                            Log.e(TAG, "upLoadUrl: " + s);
                                            webSocket.send(s);
                                            isActive_timeStamp = System.currentTimeMillis();
                                            isActive = true;
                                        } else {
                                            //金额或者备注对不上
                                            WebsocketErrorRequestBean websocketRequestBean = new WebsocketErrorRequestBean();
                                            websocketRequestBean.setOrgId(orgId);
                                            websocketRequestBean.setRetCode(RET_CODE_FAIL);
                                            websocketRequestBean.setRetMsg("金额或者备注对不上" + " totalAmount : " + websocketResponseBean.getTotalAmount() + " reason : " + websocketResponseBean.getReason());
                                            websocketRequestBean.setTxnType(TXNTYPE_ERROR);
                                            websocketRequestBean.setSign(key);
                                            Gson gson = new Gson();
                                            String s = gson.toJson(websocketRequestBean);
                                            Log.e(TAG, "upLoadUrl: " + s);
                                            webSocket.send(s);
                                            isActive_timeStamp = System.currentTimeMillis();
                                            isActive = true;
                                        }
                                    }
                                    qr_money = null;
                                    beizhu = null;
                                    qrCodeUrl = null;
                                } else {
                                    Log.e(TAG, "afterHookedMethod: wangic" + "getUrl is null");
                                    //金额或者备注对不上
                                    WebsocketErrorRequestBean websocketRequestBean = new WebsocketErrorRequestBean();
                                    websocketRequestBean.setOrgId("");
                                    websocketRequestBean.setRetCode(RET_CODE_FAIL);
                                    if (websocketResponseBean != null) {
                                        websocketRequestBean.setRetMsg("金额或者备注对不上" + " totalAmount : " + websocketResponseBean.getTotalAmount() + " reason : " + websocketResponseBean.getReason());
                                    } else {
                                        websocketRequestBean.setRetMsg("金额或者备注对不上");
                                    }
                                    websocketRequestBean.setTxnType(TXNTYPE_ERROR);
                                    websocketRequestBean.setSign(key);
                                    Gson gson = new Gson();
                                    String s = gson.toJson(websocketRequestBean);
                                    Log.e(TAG, "upLoadUrl: " + s);
                                    if (webSocket != null) {
                                        webSocket.send(s);
                                        isActive_timeStamp = System.currentTimeMillis();
                                        isActive = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        //获取二维码页面回调
        XC_MethodHook index = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context context = (Context) param.thisObject;
                if (context != null) {
                    if (context.getClass().getName() != null) {
                        Log.e(TAG, "afterHookedMethod: " + context.getClass().getName());
                        if (context.getClass().getName().equals("com.alipay.mobile.payee.ui.PayeeQRActivity")) {
                            //hook可以跳转到设置金额页面
                            if (context != null) {
                                HookPayLink.this.context = context;
                                Toast.makeText(context, "已经开启hook了", Toast.LENGTH_SHORT).show();
                                String config = FileUtils.getFileUtils().getConfig();
                                if (!TextUtils.isEmpty(config)) {
                                    String[] split = config.split(">0<");
                                    Log.e(TAG, "afterHookedMethod: " + split.length);
                                    for (int i = 0; i < split.length; i++) {
                                        Log.e(TAG, "afterHookedMethod: " + split[i]);
                                    }
                                    if (split.length == 3) {
                                        orgId = split[0];
                                        key = split[1];
                                        ip = split[2];
//                                        connectOkhttpSocket(context);
                                        FileUtils.getFileUtils().deleteConfig();
                                    } else {
                                        Toast.makeText(context, "配置信息不正确，请重新打开hook插件配置", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(context, "配置信息已清除，请重新打开hook插件配置登录", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.e(TAG, "afterHookedMethod: context is null");
                            }
                        }
                    }
                }
            }
        };

        //设置金额页面回调
        XC_MethodHook QRSetMoney = new XC_MethodHook(){
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;
                Context context = ((Context) args[0]);
                if (context != null) {
                    if (context.getClass().getName().equals("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity")) {
                        final Button button = (Button) param.thisObject;
                        Log.e(TAG, "afterHookedMethod: " + button.getText());
                        if (button.getText().toString().equals("确定")) {
                            comfirm = button;
                        }
                    }
                }
            }
        };

        //输入回调
        XC_MethodHook inPutMoney = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;
                Context context = ((Context) args[0]);
                if (context != null) {
                    if (context.getClass().getName().equals("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity")) {
                        final EditText editText = (EditText) param.thisObject;
                        if (isSetMoney) {
                            x.task().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (websocketResponseBean != null) {
                                        editText.setText(websocketResponseBean.getTotalAmount());
                                        x.task().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (hasSetMoneyAndReason) {
                                                    comfirm.performClick();
                                                    hasSetMoneyAndReason = false;
                                                }
                                            }
                                        }, 1);
                                    } else {

                                    }
                                }
                            }, 3);
                            isSetMoney = false;
                        } else {
                            if (websocketResponseBean != null) {
                                editText.setText(websocketResponseBean.getReason());
                                hasSetMoneyAndReason = true;
                                isSetMoney = true;
                            } else {

                            }
                        }
                    }
                }
            }
        };

        // 可以排除非当前包名
        if (lpparam.packageName.equals("com.eg.android.AlipayGphone")) {
            Log.e(TAG, "找到支付宝开始挂钩子！");

            //hook 支付链接
            findAndHookMethod(Intent.class, "putExtra", String.class, String.class, hook);

            //hook 二维码页面
            findAndHookMethod(Activity.class, "onCreate", Bundle.class, index);

            //hook 金额页面回调
            findAndHookConstructor(Button.class, Context.class, AttributeSet.class, int.class, int.class, QRSetMoney);

            //hook 输入
            findAndHookConstructor(EditText.class, Context.class, AttributeSet.class, int.class, int.class, inPutMoney);

//            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                }
//
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                }
//            });
//            //hook设置金额页面
            findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.e(TAG, "wangic: PayeeQRSetMoneyActivity" );
//                    Context context = (Context) param.thisObject;
//                    Class<?> aClass = param.thisObject.getClass();
                    XposedHelpers.callMethod(param.thisObject,"a");
                }
            });
        }
    }

    //连接后台服务器，接收指令
    private void connectOkhttpSocket(final Context context) {
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(30000, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(30000, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(30000, TimeUnit.SECONDS)//设置连接超时时间
                .build();

        url = "ws://" + ip + "/mrPay/rtQrCodePay?orgId=" + orgId;
        Log.i(TAG, "connectOkhttpSocket: " + url);
        request = new Request.Builder().url(url).build();

        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                HookPayLink.this.webSocket = webSocket;
                Log.e(TAG, "onOpen: ");
                Message message = new Message();
                message.what = CONNECT_SERVER;
                if (myHandler != null) {
                    myHandler.sendMessage(message);
                }
                myReceiver = new MyReceiver();
                context.registerReceiver(myReceiver, new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED));//监听低电量
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
//                Log.e(TAG, "onMessage: "+text);
                Gson gson = new Gson();
                websocketResponseBean = gson.fromJson(text, WebsocketResponseBean.class);
                websocketResponseBean.setResponseTime("" + (System.currentTimeMillis() / 1000));
                if (websocketResponseBean != null) {
                    if (websocketResponseBean.isCorrectResponse(key)) {
                        Log.e(TAG, "onMessage: " + "correct request" + websocketResponseBean.getTotalAmount() + "  " + websocketResponseBean.getReason());
                        //启动设置金额页面
                        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
                        Log.e(TAG, "afterHookedMethod: " + name);
                        if (!name.equals("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity")) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.setClassName(context, "com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity");
                            context.getApplicationContext().startActivity(intent);
                            isActive_timeStamp = System.currentTimeMillis();
                            isActive = false;
                        } else {
                            Log.e(TAG, "afterHookedMethod: " + "频率太快了");
                        }
                    }
                } else {
                    WebsocketErrorRequestBean websocketRequestBean = new WebsocketErrorRequestBean();
                    websocketRequestBean.setOrgId(orgId);
                    websocketRequestBean.setRetCode(RET_CODE_OTHER_ERROR);
                    websocketRequestBean.setRetMsg("服务器请求不合法");
                    websocketRequestBean.setTxnType(TXNTYPE_ERROR);
                    websocketRequestBean.setSign(key);
                    gson = new Gson();
                    String s = gson.toJson(websocketRequestBean);
                    Log.e(TAG, "error: " + s);
                    webSocket.send(s);
                    websocketResponseBean = null;
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                AlarmUtils.getInstance().warnning(context);
                x.task().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = RECALL_SERVER;
                        myHandler.sendMessage(message);
                    }
                }, 5000);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                AlarmUtils.getInstance().warnning(context);
                x.task().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = RECALL_SERVER;
                        myHandler.sendMessage(message);
                    }
                }, 5000);
            }
        };
        mOkHttpClient.newWebSocket(request, webSocketListener);//连接webSocket

        //验证服务器没有断开
        x.task().run(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //定期发送websocket信息，保证连接
                        Thread.sleep(1000 * 60);
                        Message message = new Message();
                        message.what = NOTIFY_SERVER;
                        myHandler.sendMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //验证支付宝页面有没有响应
        x.task().run(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000 * 10);
                        if ((!isActive) && (System.currentTimeMillis() - isActive_timeStamp > 10 * 1000)) {
                            Message message = new Message();
                            message.what = DEVICE_IS_NOT_ACTIVE;
                            myHandler.sendMessage(message);
                        }
//                        isActive = false;
                    } catch (InterruptedException e) {
                    }
                }
            }
        });


    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
            int levelPercent = (int) (((float) level / scale) * 100);//当前电量
            if (levelPercent < 20) {
                Message message = new Message();
                message.what = LOW_POWER;
                if (myHandler != null) {
                    myHandler.sendMessage(message);
                }
            }
        }
    }
}
