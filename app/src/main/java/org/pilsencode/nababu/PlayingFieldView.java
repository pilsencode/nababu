package org.pilsencode.nababu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by veny on 7.11.14.
 */
public class PlayingFieldView extends View {

    public int x = -1;
    public int y = -1;
    public int radius = 100;

    public PlayingFieldView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (-1 == x) {
            x = getWidth();
            y = getHeight();
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        paint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawCircle(x / 2, y / 2, radius, paint);
    }

    public void move(int incX, int incY) {
        x += incX;
        y += incY;
        invalidate();
    }
}
