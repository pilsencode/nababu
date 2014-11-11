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
    private Player me;

    public PlayingFieldView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (-1 == x) {
            x = getWidth();
            y = getHeight();
            me = new Player();
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        me.draw(canvas);
    }

    public void move(int incX, int incY) {
        me.move(incX, incY);
        invalidate();
    }
}
