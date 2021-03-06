package org.pilsencode.nababu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * This activity represents playing field of the game.
 *
 * Created by veny on 5.11.14.
 */
public class PlayingFieldActivity extends Activity implements SensorEventListener, Game.GameEventObserver {

    /**
     * Key for passing baba name to this activity via Intent
     */
    public final static String BABANAME = "org.pilsencode.nababu.BABANAME";

    /**
     * Sensor refresh limit
     */
    private static final long SENSOR_REFRESH_LIMIT = 40; // [ms]

    RelativeLayout layout;
    private PlayingFieldView boardView;
    private TextView caughtCounterView;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private long lastSensorEvent = System.currentTimeMillis();

    private final String babatextBase = "\n\n your baba quantity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boardView = new PlayingFieldView(this);
        caughtCounterView = new TextView(this);
        caughtCounterView.append(babatextBase + "0");
        caughtCounterView.setTextSize(24);

        // Create a Layout in which to add the Views

        layout = new RelativeLayout(this);

        layout.addView(caughtCounterView);
        layout.addView(boardView);

        setContentView(layout);


        // enforce the Portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // to prevent the sleep mode on this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister the sensor to save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // register itself as game observer
        Game.getInstance().registerEventObserver(this);
        // start the game
        Bundle extras = getIntent().getExtras();
        String babaName = extras.getString(PlayingFieldActivity.BABANAME);

        Game.getInstance().start(babaName);
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
                .setMessage("Are you sure you want to leave running game?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // send info that I want to quit
                        //Game.getInstance().getHandler().obtainMessage(1, new Game.GameEvent(ActionEnum.QUIT, Game.getInstance().getMe().getName())).sendToTarget();
                        if (Game.getInstance().isServer()) {
                            Game.getInstance().sendToOtherPlayers(new Game.GameEvent(ActionEnum.END_GAME));
                        } else {
                            // TODO send info - now problems with sockets
                            //Game.getInstance().sendToServer(new Game.GameEvent(ActionEnum.QUIT, Game.getInstance().getMe().getName()));
                        }

                        PlayingFieldActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Method to stop the game - move from playing field to the starting activity
     */
    private void stopGameOnClient() {
        this.finish();
    }

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case MOVE:

                // when player moved, invalidate view - render new position
                boardView.invalidate();
                break;

            case BABA:
                String caught = event.params[0];
                Toast.makeText(this, "BABA! The looser is: " + caught, Toast.LENGTH_SHORT).show();

                // refresh baba counter - show your baba-size ;)
                caughtCounterView.setText(this.babatextBase + String.valueOf(Game.getInstance().getMe().getCaughtCounter()));

                break;
            case QUIT:
                //String quitName = event.params[0];
                //Toast.makeText(this, "Player " + quitName + " left the game", Toast.LENGTH_SHORT).show();

                break;
            case END_GAME:
                Toast.makeText(this, "Game finished", Toast.LENGTH_LONG).show();
                stopGameOnClient(); // go back to Host/JoinGame -> onStart -> Game#reset()
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
