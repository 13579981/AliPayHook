package com.example.acitsec.hook;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MyService extends Service {
    private static Dialog dialog;
    private static final String TAG = "MyService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
    }


    public static class MyBroadCastReceiver1 extends BroadcastReceiver {

        public MyBroadCastReceiver1() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            String money = intent.getStringExtra("money");
            String beizhu = intent.getStringExtra("beizhu");
            String url = intent.getStringExtra("url");
            if (!"com.action.hook".equals(action)) {
                return;
            }
            if ((money == null) || (beizhu == null) || (url == null)) {
                return;
            }

            if(dialog == null) {
                dialog = new Dialog(context);
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }

                View view = LayoutInflater.from(context).inflate(R.layout.money, null);
                Window window = dialog.getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                window.setGravity(Gravity.TOP);
                window.setAttributes(lp);
                dialog.setContentView(view);
            }
            ((TextView)dialog.findViewById(R.id.money)).setText(money);
            ((TextView)dialog.findViewById(R.id.beizhu)).setText(beizhu);
            ((TextView)dialog.findViewById(R.id.url)).setText(url);
            dialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        if(dialog.isShowing()){
                            dialog.dismiss();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


//            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//            layoutParams.flags = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;
//            layoutParams.format = PixelFormat.TRANSLUCENT;
//            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//            final WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            mWindowManager.addView(view, layoutParams);

            dialog.findViewById(R.id.money).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.beizhu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.url).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
//            InterfaceClass.GetInfo getInfo = InterfaceClass.getInterfaceClass().getGetInfo();
//            if (getInfo != null) {
//                getInfo.getInfo(money, beizhu, url);
//            } else {
//                Log.e(TAG, "onReceive: getInfo is null");
//            }
        }
    }
}
