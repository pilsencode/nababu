package org.pilsencode.nababu;

import android.graphics.Color;
import android.graphics.Point;

import java.security.InvalidParameterException;

/**
 * Created by veny on 11.11.14.
 */
public class Player {

    private String name;

    private int radius = 30;

    private Point coordinates;

    private int color = Color.rgb(0, 0, 255);

    private boolean baba = false;

    public Player(String name) {
        if (null == name || 0 == name.trim().length()) {
            throw new InvalidParameterException("name must be provided");
            // TODO [veny] exception handling in android?
        }
        this.name = name;
        this.color = getRandomColor();

        coordinates = new Point();
        coordinates.x = radius;
        coordinates.y = radius;
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

    public boolean isBaba() {
        return baba;
    }

    public String getSymbol() {
        return name.substring(0, 1).toUpperCase();
    }

    // ----------------------------------------------------------- Object Stuff

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private int getRandomColor() {
        return Color.rgb(0, (int)(Math.random()*256), (int)(Math.random()*256));
    }
}
