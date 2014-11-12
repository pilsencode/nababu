package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by veny on 11.11.14.
 */
public class Game implements Drawable {

    public static final int BORDER_WIDTH = 10;
    public static final int BORDER_COLOR = Color.RED;
    public static final int BG_COLOR = Color.GRAY;

    private Player me = new Player("veny");
    private Set<Player> players = new HashSet<Player>();

    private int fieldSizeX;
    private int fieldSizeY;

    public void setFieldSize(int x, int y) {
        fieldSizeX = x;
        fieldSizeY = y;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void moveMe(int incX, int incY) {
        Point coordinates = me.getCoordinates();
        coordinates.x += incX;
        coordinates.y += incY;
        if (coordinates.x < me.getRadius()) {
            coordinates.x = me.getRadius();
        }
        if (coordinates.y < me.getRadius()) {
            coordinates.y = me.getRadius();
        }
        int innerSize = Math.min(fieldSizeX, fieldSizeY) - (2 * BORDER_WIDTH);
        if (coordinates.x > (innerSize - me.getRadius())) {
            coordinates.x = innerSize - me.getRadius();
        }
        if (coordinates.y > (innerSize - me.getRadius())) {
            coordinates.y = innerSize - me.getRadius();
        }
    }

    // --------------------------------------------------------- Drawable Stuff

    private Paint paint = new Paint();

    @Override
    public void draw(Canvas canvas) {
        int rectSize = Math.min(fieldSizeX, fieldSizeY);
        int top = (fieldSizeY - rectSize) / 2;

        // field border
        paint.setColor(BORDER_COLOR);
        paint.setStrokeWidth(BORDER_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, top, rectSize, top + rectSize, paint);

        // field background
        paint.setColor(BG_COLOR);
        paint.setStrokeWidth(BORDER_WIDTH);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(
                BORDER_WIDTH, top + BORDER_WIDTH,
                rectSize - BORDER_WIDTH, top + rectSize - BORDER_WIDTH, paint);

        // draw 'me'
        paint.setColor(me.getColor());
        canvas.drawCircle(
                BORDER_WIDTH + me.getCoordinates().x,
                top + BORDER_WIDTH + me.getCoordinates().y,
                me.getRadius(), paint);
        // symbol on 'me'
        int fontSize = me.getRadius() - 10;
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(fontSize);
        canvas.drawText(
                me.getSymbol(),
                BORDER_WIDTH + me.getCoordinates().x - (fontSize / 3),
                top + BORDER_WIDTH + me.getCoordinates().y + (fontSize / 3), paint);
    }

}
