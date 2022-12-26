package com.example.appaudioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import dalvik.system.InMemoryDexClassLoader;

public class MainActivity extends Activity {
    ImageButton btpa,btop,btpl,btst;
    TextView tv;
    SeekBar sb;
    int duration = 0, current=0;
    boolean pauseFinish=false,finish=false; //thread ne start karva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btop = findViewById(R.id.btop);
        btpl = findViewById(R.id.btpl);
        btst = findViewById(R.id.btst);
        btpa = findViewById(R.id.btpa);

        tv = findViewById(R.id.tv);
        sb = findViewById(R.id.sb);

        //game tya seekbar hate to jode song b badlay
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(GlobalMedia.mp!=null) {
                    current = sb.getProgress();
                    current = current * 1000;
                    GlobalMedia.mp.seekTo(current);
                    tv.setText(""+(current/1000)+"/"+duration);

                }


            }
        });


        btpa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GlobalMedia.mp!=null) {
                    GlobalMedia.mp.pause();
                    pauseFinish = true;
                }

            }
        });

        btpl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GlobalMedia.mp!=null){
                    GlobalMedia.mp.start();
                    pauseFinish = false;
                }
            }
        });

        btst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GlobalMedia.mp!=null){
                    GlobalMedia.mp.stop();
                    pauseFinish=true;
                    finish=true;
                }
            }
        });

        btop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i1 = new Intent(Intent.ACTION_GET_CONTENT);
                i1.setType("audio/*");
                startActivityForResult(Intent.createChooser(i1,"Select your song"),151); // song avse a badha ahoya avse ne 151 code lagse ane
                // kasu pan lai ne pachu avanu hoy koi b data to startactivityresult levu pade

            }
        });
    }

    @Override
    protected  void onActivityResult(int reqCode,int resCode,Intent data)
    {
        if (reqCode==151 && resCode == RESULT_OK); // kasu lai ne avse to ahiya khber padse ne nai lai ne ave to code nai ave atle khyaal ave a check karva mate a levanu
        {
            Uri uri = data.getData(); //user je lai ne ayo a data ahioya leva mate uri ave
            GlobalMedia.mp = MediaPlayer.create(getApplicationContext(),uri);
            GlobalMedia.mp.start();

            notifyMe(uri); // step one for notification

            duration = GlobalMedia.mp.getDuration();
            duration = (duration/1000); //second ma convert karyu
            tv.setText("0/"+duration); // time batavse
            sb.setMax(duration); //song ni and seekbar ni length ne same karva mate

            finish=false;   //jyare navu song open kariye fari false thay jase
            pauseFinish = false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!finish) //permanently band karva mate while &&  [ temporary mate if]
                    {
                        //stop par click karso atle seekbar farvanu bandh thay jase
                        try {Thread.sleep(1000);}
                        catch (Exception e){}

                        if(!pauseFinish){
                            current = GlobalMedia.mp.getCurrentPosition();
                            current=(current/1000); // sec ma karyu
                            sb.setProgress(current);
                            tv.setText(""+current+"/"+duration);

                            if(current>=duration){
                                pauseFinish = true;
                                finish = true;
                                tv.setText("0/0");
                                sb.setProgress(0);
                            }
                        }
                    }

                    pauseFinish = true;
                    finish = true;
                    tv.setText("0/0");
                    sb.setProgress(0);
                    GlobalMedia.mp.stop();
                    GlobalMedia.mp = null;

                }
            }).start();  //run pachi sidhu j start thay jase .start thi

        }
    }

    //notification mate nu function ane ana mate ne call upar onactivityresult ma
    void notifyMe(Uri uri)
    {
        String path = uri.getPath(); //akho path apse
        int p = path.lastIndexOf("/"); //chello slash ne sodhi apse ana pachi name hase song nu
        String song = path.substring(p+1,path.length()); //name lese a string pela path mathi slash pachi nu name

        //notification parthi touch kari ne app par java mate
        Intent i2 = new Intent(getApplicationContext(),MainActivity.class);
        //notification parthi touch kari ne app par java mate
        PendingIntent pi2 = PendingIntent.getActivity(this,0,i2,PendingIntent.FLAG_MUTABLE);


        //pause service ne call karva notificatiuon ma
        Intent i3 = new Intent(getApplicationContext(),PauseService.class);
        PendingIntent pi3 = PendingIntent.getService(this, 0, i3, PendingIntent.FLAG_MUTABLE);

        Intent i4 = new Intent(getApplicationContext(),PlayService.class);
        PendingIntent pi4 = PendingIntent.getService(this, 0, i4, PendingIntent.FLAG_MUTABLE);

        //channel generation
        NotificationChannel channel = new NotificationChannel("1001","Compactsong", NotificationManager.IMPORTANCE_HIGH);

        //notificatiuon ma kai b joye to builder ma mukvu pade
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"1001")
                .setContentTitle("Soothing")
                .setContentText(song)
                .setContentIntent(pi2)  // notification par click kare to pending intent ne call kare
                .setSmallIcon(R.drawable.logo)
                .addAction(R.drawable.pause,"pause",pi3) //notification ma pause mate
                .addAction(R.drawable.playbtn,"play",pi4);

        NotificationManager man = getSystemService(NotificationManager.class);  //avil notification service from clients device
        man.createNotificationChannel(channel);

        man.notify(123,builder.build());    // an calll karya pachi dekhase

    }
}