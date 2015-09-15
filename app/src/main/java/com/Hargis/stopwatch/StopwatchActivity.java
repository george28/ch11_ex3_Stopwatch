package com.Hargis.stopwatch;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StopwatchActivity extends Activity implements OnClickListener {

    private TextView hoursTextView;
    private TextView minsTextView;
    private TextView secsTextView;
    private TextView tenthsTextView;

    private Button resetButton;
    private Button startStopButton;
    
    private long startTimeMillis;
    private long elapsedTimeMillis;

    private int elapsedHours;
    private int elapsedMins;
    private int elapsedSecs;
    private int elapsedTenths;

    private Timer timer;
    private NumberFormat number;

    private NotificationManager notification;
    private SharedPreferences prefs;
    private boolean stopwatchOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);
        
        // get references to widgets
        hoursTextView = (TextView) findViewById(R.id.textViewHoursValue);
        minsTextView = (TextView) findViewById(R.id.textViewMinsValue);
        secsTextView = (TextView) findViewById(R.id.textViewSecsValue);
        tenthsTextView = (TextView) findViewById(R.id.textViewTenthsValue);
        resetButton = (Button) findViewById(R.id.buttonReset);
        startStopButton = (Button) findViewById(R.id.buttonStartStop);
        
        // set listeners
        resetButton.setOnClickListener(this);
        startStopButton.setOnClickListener(this);

        // get preferences
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);


        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);



    }

    @Override
    protected void onPause() {
        super.onPause();
        
        Editor edit = prefs.edit();
        edit.putBoolean("stopwatchOn", stopwatchOn);
        edit.putLong("startTimeMillis", startTimeMillis);
        edit.putLong("elapsedTimeMillis", elapsedTimeMillis);
        edit.commit();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        stopwatchOn = prefs.getBoolean("stopwatchOn", false);
        startTimeMillis = prefs.getLong("startTimeMillis", System.currentTimeMillis());
        elapsedTimeMillis = prefs.getLong("elapsedTimeMillis", 0);

        if (stopwatchOn) {
            start();
        }
        else {
            updateViews(elapsedTimeMillis);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartStop:
                if (stopwatchOn) {
                    stop();
                }
                else {
                    start();
                }        
                break;
            case R.id.buttonReset:
                reset();
                break;
        }
    }

    private void start() {
        // make sure old timer thread has been cancelled 
        if (timer != null) {
            timer.cancel();
        }

        // if stopped or reset, set new start time
        if (stopwatchOn == false) {
            startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
        }
        
        // update variables and UI
        stopwatchOn = true;
        startStopButton.setText(R.string.stop);

        // start new timer thread
        TimerTask task = new TimerTask() {
            
            @Override
            public void run() {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                updateViews(elapsedTimeMillis);
            }
        };
        timer = new Timer(true);
        timer.schedule(task, 0, 100);
        
        // create pending intent here


        Intent notificationIntent = new Intent(this, ItemsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);
        // build the notification here
            int icon = R.drawable.ic_launcher;
            CharSequence tickerText = "Stopwatch is on";
            CharSequence contentTitle = getText(R.string.app_name);
            CharSequence contentText =getText(R.string.app_description);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // notify the user here

        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        manager.notify(NOTIFICATION_ID, notification);

    }
    
    private void stop() {
        // stop timer
        stopwatchOn = false;
        if (timer != null) {
            timer.cancel();
        }
        startStopButton.setText(R.string.start);
        
        // update views
        updateViews(elapsedTimeMillis);    
        
        // turn off notification here
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        manager.cancel(NOTIFICATION_ID);

    }

    private void reset() {
        // stop timer & turn off notification
        this.stop();
        
        // reset millis and update views
        elapsedTimeMillis = 0;
        updateViews(elapsedTimeMillis);
    }
    
    private void updateViews(final long elapsedMillis) {
        elapsedTenths = (int) ((elapsedMillis/100) % 10);
        elapsedSecs = (int) ((elapsedMillis/1000) % 60);
        elapsedMins = (int) ((elapsedMillis/(60*1000)) % 60);
        elapsedHours = (int) (elapsedMillis/(60*60*1000));

        if (elapsedHours > 0) {
            updateView(hoursTextView, elapsedHours, 1);
        }
        updateView(minsTextView, elapsedMins, 2);
        updateView(secsTextView, elapsedSecs, 2);
        updateView(tenthsTextView, elapsedTenths, 1);
    }
    
    private void updateView(final TextView textView,
            final long elapsedTime, final int minIntDigits) {

        number = NumberFormat.getInstance();
        
        // UI changes need to be run on the UI thread
        textView.post(new Runnable() {

            @Override
            public void run() {
                number.setMinimumIntegerDigits(minIntDigits);
                textView.setText(number.format(elapsedTime));
            }
        });
    }

    private class ItemsActivity {
    }
}