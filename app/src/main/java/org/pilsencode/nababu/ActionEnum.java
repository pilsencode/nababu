package org.pilsencode.nababu;

/**
 * Enumeration representing list of actions communicated via remote connection.
 *
 * Created by veny on 20.11.14.
 */
public enum ActionEnum {

    // request to the server that player wants to join the game
    // params: username
    JOIN,

    // info to all players about newly joined user - all clients should register this player
    // params: username
    JOINED,

    // info about player move
    // params: username, incX, incY
    MOVE,

    // info that game starts - all clients should show playing field
    // params: -no params-
    START_GAME,

    // info from the server that game ends - all clients should end game and render base activity
    // params: -no params-
    END_GAME,

    // info to players that some player disconnected - all players should remove him from
    // list of players
    // params: username
    QUIT

}
