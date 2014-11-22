package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by veny on 11.11.14.
 */
public class Game implements Drawable, Observer {

    private static Game instance = null;

    public static final int BORDER_WIDTH = 10;
    public static final int BORDER_COLOR = Color.RED;
    public static final int BG_COLOR = Color.GRAY;

    private Player me;
    private Map<String, Player> players = new HashMap<String, Player>();

    private int fieldSizeX;
    private int fieldSizeY;

    /**
     * Private constructor to defeat instantiation of singleton.
     */
    private Game() {
        me = new Player("veny");

        // add another player - temporary
        addPlayer(new Player("AI"));
    }

    /**
     * Gets singleton instance of game.
     * @return the game
     */
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public void setFieldSize(int x, int y) {
        fieldSizeX = x;
        fieldSizeY = y;
    }

    public void addPlayer(Player player) {
        if (!players.containsKey(player.getName())) {
            players.put(player.getName(), player);
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

        // draw 'app players'
        drawAllPlayers(canvas, rectSize, top);
    }

    /**
     * Render all players from the set
     *
     * @param canvas
     * @param rectSize
     * @param top
     */
    private void drawAllPlayers(Canvas canvas, int rectSize, int top) {
        drawPlayer(canvas, me, rectSize, top);
        // render other players
        for (Player player : players.values()) {
            drawPlayer(canvas, player, rectSize, top);
        }
    }

    /**
     * Draw one player
     *
     * @param canvas
     * @param player
     * @param rectSize
     * @param top
     */
    private void drawPlayer(Canvas canvas, Player player, int rectSize, int top) {
        // draw player
        paint.setColor(player.getColor());
        canvas.drawCircle(
                BORDER_WIDTH + player.getCoordinates().x,
                top + BORDER_WIDTH + player.getCoordinates().y,
                player.getRadius(), paint);

        // symbol on 'player'
        int fontSize = player.getRadius() - 10;
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(fontSize);
        canvas.drawText(
                player.getSymbol(),
                BORDER_WIDTH + player.getCoordinates().x - (fontSize / 3),
                top + BORDER_WIDTH + player.getCoordinates().y + (fontSize / 3), paint);

    }

    // temporary code till BT connection is ready - test observer
    public void moveAI() {
        Point point = this.players.get("AI").getCoordinates();

        // create player with same nama to test update method
        Player updated = new Player("AI");
        updated.setCoordinates(new Point(point.x+2, point.y+2));

        this.update(null, updated);
    }

    @Override
    public void update(Observable observable, Object argument) {
        // Currently I expect that updated player will come as paramater - TODO consult with vasy
        Player updatedPlayer = (Player) argument;

        // find player and update his coorinates
        if (this.players.containsKey(updatedPlayer.getName())) {
            this.players.get(updatedPlayer.getName()).setCoordinates(updatedPlayer.getCoordinates());
        }

    }
}
