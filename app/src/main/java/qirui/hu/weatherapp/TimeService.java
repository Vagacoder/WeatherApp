package qirui.hu.weatherapp;

import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;

public class TimeService extends JobIntentService {

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        boolean start = intent.getBooleanExtra("start", false);

        while(true){
            sleep(20000);
            Date now = new Date();
            String localDate = now.toLocaleString();
            String[] d = localDate.split("\\s(?=\\d\\d:\\d\\d:\\d\\d)");
            String time = d[1].substring(0, 5);
            broadcastTime(time);
        }

    }

    private void broadcastTime(String time){
        Intent timeIntent = new Intent("TimeService");
        timeIntent.putExtra("currentTime", time);
        LocalBroadcastManager.getInstance(this).sendBroadcast(timeIntent);
    }

    private void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
