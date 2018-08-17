package utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.acitsec.hook.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.acitsec.hook.Constance.FILE_NAME;
import static com.example.acitsec.hook.Constance.FILE_PATH;

/**
 * Created by ACITSEC on 2018/5/31.
 */

public class SPUtils {
    private static SPUtils instance;
    private static String SP = "AliPlugin";
    private static SharedPreferences sharedPreferences;

    public static SPUtils getInstance() {
        if (instance == null) {
            synchronized (SPUtils.class) {
                instance = new SPUtils();
            }
        }
        sharedPreferences = MyApplication.getMyApplication().getSharedPreferences(SP, Context.MODE_PRIVATE);
        return instance;
    }

    public void setString(String name, String value) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(name, value);
            edit.commit();
        }
    }

    public String getString(String name) {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(name,"");
        }
        return null;
    }


}
