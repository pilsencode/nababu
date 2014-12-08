package org.pilsencode.nababu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * This view renders visual part of the running game.
 *
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

        Game game = Game.getInstance();

        if (!intialized) {
            // set size of playing filed
            game.setBoardSize(getWidth(), getHeight());

            // draw background
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Game.SCREEN_BG_COLOR);
            canvas.drawPaint(paint);

            intialized = true;
        }

        game.draw(canvas);
    }

}
