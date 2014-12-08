package org.pilsencode.nababu;

/**
 * Enumeration representing list of actions communicated via remote connection.
 *
 * Created by veny on 20.11.14.
 */
public enum ActionEnum {

    // request to the server that player wants to join the game
    // params: username
    // direction: client -> server
    JOIN,

    // info to clients about newly joined user - all clients should register this player to the internal list of players
    // duplicate names must be ignored (server does not remember what he already sent to who)
    // params: username
    // direction: server -> client
    JOINED,

    // info about player move
    // params: username, positionX, positionY
    // direction: both (server -> client, client -> server)
    MOVE,

    // info that game starts - all clients should show playing field
    // params: -no params-
    // direction: server -> client
    START_GAME,

    // info from the server that game ends - all clients should end game and render base activity
    // params: -no params-
    // direction: server -> client
    END_GAME,

    // info to players that some player disconnected - all players should remove him from the
    // list of players. When player knows he will end, he should sent QUIT command to the server.
    // server sends this info to other connected clients
    // params: username
    // direction: both (client -> server, server -> client)
    QUIT

}
