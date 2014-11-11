package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by veny on 11.11.14.
 */
public class Player implements Drawable {

    private String name;

    private int spotRadius;

    private Point spotCoordinates;

    private Color spotColor; // = Color.parseColor("#0000AA");

    private boolean baba = false;

    public Player() {
        spotCoordinates = new Point();
        spotCoordinates.x = 200;
        spotCoordinates.y = 200;
        spotRadius = 30;
    }

    public void move(int incX, int incY) {
        spotCoordinates.x += incX;
        spotCoordinates.y += incY;
    }

    Paint paint = new Paint();

    @Override
    public void draw(Canvas canvas) {
        paint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawCircle(spotCoordinates.x, spotCoordinates.y, spotRadius, paint);
    }


    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
