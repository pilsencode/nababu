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

    private boolean intialized = false;

    public PlayingFieldView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        PlayingFieldActivity ctx = (PlayingFieldActivity) getContext();
        Game game = ctx.getGame();

        if (!intialized) {
            // set size of playing filed
            game.setFieldSize(getWidth(), getHeight());

            // draw background
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            intialized = true;
        }

        game.draw(canvas);
    }

}