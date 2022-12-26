package com.example.appaudioplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PauseService extends Service {

    public int onStartCommand(Intent data,int sid, int flag)
    {
        GlobalMedia.mp.pause(); // pause thay jase


        return START_NOT_STICKY;
                // return START_STICKY; // for sticky service
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
}