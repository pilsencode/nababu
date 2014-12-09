package org.pilsencode.nababu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;


/**
 * This activity represents playing field of the game.
 *
 * Created by veny on 5.11.14.
 */
public class PlayingFieldActivity extends Activity implements SensorEventListener, Game.GameEventObserver {

    /**
     * Sensor refresh limit
     */
    private static final long SENSOR_REFRESH_LIMIT = 40; // [ms]

    private PlayingFieldView mView;
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private long lastSensorEvent = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new PlayingFieldView(this);
        setContentView(mView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        Game.getInstance().addAI();
//
//        // temporary code till BT connection is ready - test observer
//        // TODO - replace with "game.update(null, player)" - player with new coordinates
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                Game.getInstance().moveAI();
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule(task, new Date(), 300);
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
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_GAME);
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
                        // is this ok? todo solved?
                        Game.getInstance().sendToOtherPlayers(new Game.GameEvent(ActionEnum.END_GAME));

                        PlayingFieldActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Method to stop the game - move from playing field to the starting activity
     */
    private void stopGame() {
        Intent intent = new Intent(getBaseContext(), EntryPointActivity.class);
        startActivity(intent);
    }

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case MOVE:
                // decode params of move action
                String name = event.params[0];

                // if other player moved, change his position
                if (!name.equals(Game.getInstance().getMe().getName())) {
                    // find object of player who moved
                    // On the server this player is also in event.player, but on the client side not...
                    int positionX = Integer.valueOf(event.params[1]);
                    int positionY = Integer.valueOf(event.params[2]);

                    Player player = Game.getInstance().getPlayer(name);

                    // move by the player (update his coordinates)
                    // TODO this should be somewhere? - same code should move by me and others
                    player.getCoordinates().x = positionX;
                    player.getCoordinates().y = positionY;
                }

                //Toast.makeText(this, "ddd", Toast.LENGTH_SHORT).show();

                mView.invalidate();
                break;
            case END_GAME:
                Toast.makeText(this, "END_GAME received, going to Entry Activity", Toast.LENGTH_LONG).show();
                stopGame();
                break;
        }
    }

    // ---------------------------------------------- SensorEventListener Stuff

    @Override
    public void onSensorChanged(SensorEvent e) {
        // make sure all players have the same speed
        long now = System.currentTimeMillis();
        if ((now - lastSensorEvent) < SENSOR_REFRESH_LIMIT) {
            return;
        }

        lastSensorEvent = now;

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

        // TODO [veny] there should be Strategy design pattern to calculate the speed of movement
        // (int)(tiltX * 2 / Math.PI) -  is increment (-1 to 1, 0 is no move)
        double speedX = (tiltX * 2 / Math.PI);
        double speedY = (-tiltY * 2 / Math.PI);
        if (0 != speedX || 0 != speedY) {
            Game.getInstance().moveMe(speedX, speedY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // currently not used
    }

}
