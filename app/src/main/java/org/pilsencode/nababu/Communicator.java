package org.pilsencode.nababu;

/**
 * This interface represents a component for remote communication between two devices.
 *
 * Created by veny on 23.11.14.
 */
public interface Communicator {

    /**
     * Sends message to other device.
     *
     * @param packet message to be sent
     */
    void sendMessage(String packet);


    /**
     * Closes the communication infrastructure and releases corresponding resources.
     */
    void finish();

    /**
     * Gets player associated with this communicator.
     *
     * @return associated player
     */
    Player getPlayer();
}
