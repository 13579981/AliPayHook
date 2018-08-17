package com.example.acitsec.hook;

import android.os.Environment;

/**
 * Created by ACITSEC on 2018/8/14.
 */

public class Constance {
    public static String IP_PORT = "120.78.196.14:9060";
    public static String orgId = "A0000423";
    public static String key = "123456";


    /**
     * file
     */
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath()+"/AliPayHook/";
    public static final String FILE_NAME = "AliPayConfig.txt";



    /**
     * SP
     */

    public static final String ORGID = "orgId";
    public static final String KEY = "key";
    public static final String ip_port = "ip_port";



}
