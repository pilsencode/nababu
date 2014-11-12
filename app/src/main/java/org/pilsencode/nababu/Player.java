package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

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
        this.name = name;
        coordinates = new Point();
        coordinates.x = radius;
        coordinates.y = radius;
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

}
