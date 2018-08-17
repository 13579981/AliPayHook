package utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by ACITSEC on 2018/6/6.
 */

public class AlarmUtils {
    private static AlarmUtils instance;

    public static AlarmUtils getInstance(){
        if(instance == null){
            synchronized (AlarmUtils.class)
            {
                instance = new AlarmUtils();
            }
        }
        return instance;
    }


    public void warnning(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //发出警报
//                for (int i = 0; i < 7; i++) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone rt = RingtoneManager.getRingtone(context, uri);
                    rt.play();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
            }
        }).start();
    }
}
