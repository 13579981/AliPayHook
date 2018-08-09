package com.example.acitsec.hook;

import android.app.AndroidAppHelper;
import android.content.Intent;
import android.util.Log;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by ACITSEC on 2018/7/25.
 */

public class HookPayLink implements IXposedHookLoadPackage {
    private static final String TAG = "HookPayLink";
    private String qr_money = null;
    private String beizhu = null;
    private String qrCodeUrl = null;


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.e(TAG, "获得数据");
                if (methodHookParam != null) {
                    Object[] args = methodHookParam.args;
                    if (args != null) {
                        for (int i = 0; i < args.length; i++) {
                            Log.e(TAG, "afterHookedMethod: " + args[i]);
                        }
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
                                    if (AndroidAppHelper.currentApplication() != null) {
                                        Intent i = new Intent("com.action.hook");
                                        i.putExtra("money", qr_money);
                                        i.putExtra("beizhu", beizhu);
                                        i.putExtra("url", qrCodeUrl);
                                        AndroidAppHelper.currentApplication().sendBroadcast(i);
                                    } else {
                                        Log.e(TAG, "afterHookedMethod: wangic currentApplication is null");
                                    }
                                    qr_money = null;
                                    beizhu = null;
                                    qrCodeUrl = null;
                                } else {
                                    Log.e(TAG, "afterHookedMethod: wangic" + "getUrl is null");
                                }
                            } else {
                                Log.e(TAG, "afterHookedMethod: " + qr_money + beizhu + "wangic1");
                            }
                        }
                    }
                }
            }
        };

        // 可以排除非当前包名
        if (lpparam.packageName.equals("com.eg.android.AlipayGphone")) {
            Log.e(TAG, "找到支付宝开始挂钩子！");
            findAndHookMethod(Intent.class, "putExtra", String.class, String.class, hook);
        }
    }
}
