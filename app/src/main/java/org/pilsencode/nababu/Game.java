package org.pilsencode.nababu;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by veny on 11.11.14.
 */
public class Game {

    private Set<Player> players = new HashSet<Player>();

    private int fieldSizeX;
    private int fieldSizeY;

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
            // fire change event
        }
    }


}
