package org.pilsencode.nababu;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by veny on 5.11.14.
 */
public class PlayingFieldActivity extends Activity implements SensorEventListener {

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

//        //setContentView(R.layout.activity_playing_field);
//
////        final View controlsView = findViewById(R.id.fullscreen_content_controls);
////        final View contentView = findViewById(R.id.fullscreen_content);
////
////        // Upon interacting with UI controls, delay any scheduled hide()
////        // operations to prevent the jarring behavior of controls going away
////        // while interacting with the UI.
////        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(displayMetrics);
//        int x = displayMetrics.widthPixels;
//        int y = displayMetrics.heightPixels;
//
//        Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888); //??
//        Canvas canvas = new Canvas(bg);
//        canvas.drawRGB(255, 255, 255);
//
//        Paint paint = new Paint();
//        paint.setColor(Color.parseColor("#00FFFF"));
//        int radius = 20;
//
//        canvas.drawCircle(x / 2, y / 2, radius, paint);
//        LinearLayout ll = (LinearLayout) findViewById(R.id.rect);
//        ll.setBackgroundDrawable(new BitmapDrawable(bg)); //??
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
    public void onSensorChanged(SensorEvent e) {
        float x = e.values[0];
        float y = e.values[1];
        float z = e.values[2];

        // http://www.anddev.org/code-snippets-for-android-f33/convert-android-accelerometer-values-and-get-tilt-from-accel-t6595.html
        // http://www.hobbytronics.co.uk/accelerometer-info

        double accX = -x/SensorManager.GRAVITY_EARTH;
        double accY = -y/SensorManager.GRAVITY_EARTH;
        double accZ = z/SensorManager.GRAVITY_EARTH;
        double totAcc = Math.sqrt((accX*accX)+(accY*accY)+(accZ*accZ));
        // tiltXYZ: returned angle is in the range -pi/2 through pi/2
        double tiltX = Math.asin(accX/totAcc);
        double tiltY = Math.asin(accY/totAcc);
        double tiltZ = Math.asin(accZ/totAcc);
        Log.d("nababu", "values: tiltX: " + tiltX + ", tiltY: " + tiltY + ", tiltZ: " + tiltZ);

        int incX = 0;
        int incY = 0;
        if (tiltX < -0.5f) {
            incX = -10;
        }
        if (tiltX > 0.5f) {
            incX = 10;
        }
        if (tiltY < -0.5f) {
            incY = 10;
        }
        if (tiltY > 0.5f) {
            incY = -10;
        }
        if (0 != incX || 0 != incY) {
            mView.move(incX, incY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // currently not used
    }

}
