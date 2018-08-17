package com.example.acitsec.hook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import bean.WebsocketRequestBean;
import utils.FileUtils;
import utils.SPUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText orgId;
    private EditText secret;
    private Button confirm;
    private EditText ip_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orgId = findViewById(R.id.orgId);
        secret = findViewById(R.id.secret);
        confirm = findViewById(R.id.confirm);
        ip_port = findViewById(R.id.ip_port);

        orgId.setText(SPUtils.getInstance().getString(Constance.ORGID));
        secret.setText(SPUtils.getInstance().getString(Constance.KEY));
        ip_port.setText(SPUtils.getInstance().getString(Constance.ip_port));

        String[] permission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission[0])) {
                ActivityCompat.requestPermissions(this, permission, 0);
            }
        }


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String torgId = orgId.getText().toString().trim();
                String tsecret = secret.getText().toString().trim();
                String tip_port = ip_port.getText().toString().trim();
                if (!TextUtils.isEmpty(torgId)) {
                    SPUtils.getInstance().setString(Constance.ORGID, torgId);
                    Constance.orgId = torgId;
                    if (!TextUtils.isEmpty(tsecret)) {
                        Constance.key = tsecret;
                        SPUtils.getInstance().setString(Constance.KEY, tsecret);
                        if(!TextUtils.isEmpty(tip_port)){
                            Constance.IP_PORT = tip_port;
                            SPUtils.getInstance().setString(Constance.ip_port, tip_port);
                            FileUtils.getFileUtils().setConfig(torgId,tsecret,tip_port);
                            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.eg.android.AlipayGphone");
                            startActivity(LaunchIntent);
                            MainActivity.this.finish();
                        }else{
                            Toast.makeText(MainActivity.this, "请输入IP地址和端口号", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "请输入密钥", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请输入商户ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
