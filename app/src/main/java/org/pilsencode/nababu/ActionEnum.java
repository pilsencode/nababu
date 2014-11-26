package org.pilsencode.nababu;

/**
 * Enumeration representing list of actions communicated via remote connection.
 *
 * Created by veny on 20.11.14.
 */
public enum ActionEnum {

    JOIN,       // (username) - request for join the game
    JOINED,     // (username) - info about newly joined player
    MOVE, // (username, incX, incY)
    START,
    QUIT // (username)

}
