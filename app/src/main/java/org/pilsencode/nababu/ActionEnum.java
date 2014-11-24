package org.pilsencode.nababu;

/**
 * Enumeration representing list of actions communicated via remote connection.
 *
 * Created by veny on 20.11.14.
 */
public enum ActionEnum {

    OK,
    JOIN, // (username)
    MOVE, // (username, incX, incY)
    QUIT // (username)

}
