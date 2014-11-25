package org.pilsencode.nababu;

import android.graphics.Color;
import android.graphics.Point;

/**
 * This class represents a player of the game.
 *
 * Created by veny on 11.11.14.
 */
public class Player {

    private String name;

    private int radius = 22;

    private Point coordinates;

    private int color;

    private boolean baba = false;

    public Player() {
        color = getRandomColor();
        coordinates = new Point();
        coordinates.x = radius;
        coordinates.y = radius;
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    public Player(String name, int color) {
        this(name);
        this.color = color;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public int getRadius() {
        return radius;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isBaba() {
        return baba;
    }
    public void setBaba(boolean baba) {
        this.baba = baba;
    }

    public String getSymbol() {
        return name.substring(0, 1).toUpperCase();
    }

    /**
     * An user is 'activated' when he call JOIN action on server.
     * After that the sent username is set.
     */
    public boolean isActivated() {
        return (null != name);
    }

    private int getRandomColor() {
        return Color.rgb(0, (int)(Math.random()*256), (int)(Math.random()*256));
    }

}
