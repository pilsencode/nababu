package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * This class represents game and all associated players.
 *
 * Created by veny on 11.11.14.
 */
public class Game implements Drawable, Observer {

    // debugging
    public static final boolean D = true;
    public static final String TAG = "nababu";

    private static Game instance = null;

    public static final int BORDER_WIDTH = 10;
    public static final int BORDER_COLOR = Color.RED;
    public static final int BG_COLOR = Color.GRAY;

    private Player me;
    private List<Player> players = new ArrayList<Player>();

    private int fieldSizeX;
    private int fieldSizeY;

    /**
     * Private constructor to defeat instantiation of singleton.
     */
    private Game() { }

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

    public void setMe(Player player) {
        me = player;
        // I'm baba
        me.setBaba(true);
    }

    public void addAI() {
        // add another player - temporary
        addPlayer(new Player("AI"));
    }

    public void addPlayer(Player player) {
        if (null == player.getName()) {
            throw new NullPointerException("username cannot be null");
        }
        players.add(player);
    }

    public Player getPlayer(String name) {
        for (Player p : players) {
            if (name.equals(p.getName())) {
                return p;
            }
        }
        return null;
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

    public void reset() {
        me = null;
        players.clear();
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
     * Renders all players.
     */
    private void drawAllPlayers(Canvas canvas, int rectSize, int top) {
        drawPlayer(canvas, me, rectSize, top);
        for (Player player : players) {
            drawPlayer(canvas, player, rectSize, top);
        }
    }

    /**
     * Draws one player.
     */
    private void drawPlayer(Canvas canvas, Player player, int rectSize, int top) {
        if (!player.isActivated()) {
            return;
        }

        // draw player
        paint.setColor(player.getColor());
        canvas.drawCircle(
                BORDER_WIDTH + player.getCoordinates().x,
                top + BORDER_WIDTH + player.getCoordinates().y,
                player.getRadius(), paint);

        // symbol on 'player'
        int fontSize = player.getRadius() - 10;
        // mark baba with red color
        if (player.isBaba()) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.BLACK);
        }

        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(fontSize);
        canvas.drawText(
                player.getSymbol(),
                BORDER_WIDTH + player.getCoordinates().x - (fontSize / 3),
                top + BORDER_WIDTH + player.getCoordinates().y + (fontSize / 3), paint);

    }

    // temporary code till BT connection is ready - test observer
    public void moveAI() {
        Player ai = getPlayer("AI");
        if (null == ai) {
            return;
        }

        Point point = ai.getCoordinates();

        // create player with same nama to test update method
        Player updated = new Player("AI");
        updated.setCoordinates(new Point(point.x+2, point.y+2));

        this.update(null, updated);
    }

    @Override
    public void update(Observable observable, Object argument) {
        // Currently I expect that updated player will come as paramater - TODO consult with vasy
        Player updatedPlayer = (Player) argument;

//        // find player and update his coordinates
//        if (this.players.containsKey(updatedPlayer.getName())) {
//            this.players.get(updatedPlayer.getName()).setCoordinates(updatedPlayer.getCoordinates());
//        }
    }

}
