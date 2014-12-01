package org.pilsencode.nababu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This activity represents playing field of the game.
 *
 * Created by veny on 5.11.14.
 */
public class PlayingFieldActivity extends Activity implements SensorEventListener, Game.GameEventObserver {

    private PlayingFieldView mView;
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new PlayingFieldView(this);
        setContentView(mView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Game.getInstance().addAI();

        // temporary code till BT connection is ready - test observer
        // TODO - replace with "game.update(null, player)" - player with new coordinates
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Game.getInstance().moveAI();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, new Date(), 300);
        // temporary code end

//        //setContentView(R.layout.activity_playing_field);
//
////        final View controlsView = findViewById(R.id.fullscreen_content_controls);
////        final View contentView = findViewById(R.id.fullscreen_content);
////
////        // Upon interacting with UI controls, delay any scheduled hide()
////        // operations to prevent the jarring behavior of controls going away
////        // while interacting with the UI.
////        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(displayMetrics);
//        int x = displayMetrics.widthPixels;
//        int y = displayMetrics.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister the sensor to save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // register itself as game observer
        Game.getInstance().registerEventObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove itself as game observer
        Game.getInstance().removeEventObserver(this);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to leave the game?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO [veny] send STOP to all clients if I am server
                        PlayingFieldActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case MOVE:
                int incX = Integer.valueOf(event.params[1]);
                int incY = Integer.valueOf(event.params[2]);
                Player p;
                if (Game.getInstance().isServer()) {
                    p = event.player;
                } else {
                    String playerName = event.params[0];
                    p = Game.getInstance().getPlayer(playerName);
                }
                Point coordinates = p.getCoordinates();
                coordinates.x += incX;
                coordinates.y += incY;
                mView.invalidate();
                break;
        }
    }

    // ---------------------------------------------- SensorEventListener Stuff

    @Override
    public void onSensorChanged(SensorEvent e) {
        float x = e.values[0];
        float y = e.values[1];
        float z = e.values[2];

        // http://www.anddev.org/code-snippets-for-android-f33/convert-android-accelerometer-values-and-get-tilt-from-accel-t6595.html
        // http://www.hobbytronics.co.uk/accelerometer-info

        double accX = -x / SensorManager.GRAVITY_EARTH;
        double accY = -y / SensorManager.GRAVITY_EARTH;
        double accZ = z / SensorManager.GRAVITY_EARTH;
        double totAcc = Math.sqrt((accX * accX) + (accY * accY) + (accZ * accZ));
        // tiltXYZ: returned angle is in the range -pi/2 through pi/2
        double tiltX = Math.asin(accX / totAcc);
        double tiltY = Math.asin(accY / totAcc);
        // double tiltZ = Math.asin(accZ / totAcc);
        //Log.d("nababu", "values: tiltX: " + tiltX + ", tiltY: " + tiltY + ", tiltZ: " + tiltZ);

        // TODO [veny] there should be Strategy design pattern to calculate the speed of movement
        int speedX = (int)(tiltX * 2 / Math.PI * 25);
        int speedY = (int)(-tiltY * 2 / Math.PI * 25);
        if (0 != speedX || 0 != speedY) {
            Game.getInstance().moveMe(speedX, speedY);
            mView.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // currently not used
    }

}
