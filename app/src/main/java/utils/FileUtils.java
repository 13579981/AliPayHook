package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.acitsec.hook.Constance.FILE_NAME;
import static com.example.acitsec.hook.Constance.FILE_PATH;

/**
 * Created by ACITSEC on 2018/8/14.
 */

public class FileUtils {
    private static FileUtils fileUtils;

    public static FileUtils getFileUtils() {
        if (fileUtils == null) {
            synchronized (FileUtils.class) {
                fileUtils = new FileUtils();
            }
        }
        return fileUtils;
    }


    public void setConfig(String orgId, String key, String ip) {
        File file = new File(FILE_PATH + FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        File file1 = new File(FILE_PATH);
        file1.mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            String string = orgId + ">0<" + key + ">0<" + ip;
            fos.write(string.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfig(){
        String config = "";
        File file = new File(FILE_PATH + FILE_NAME);
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len;

                while ((len = fileInputStream.read(bytes)) > 0){
                    config = new String(bytes, 0, len);
                }
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }


}
