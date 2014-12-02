package org.pilsencode.nababu;

/**
 * This interface represents a component for remote communication between two devices.
 *
 * Created by veny on 23.11.14.
 */
public interface Communicator {

    /**
     * Sends packet to other device.
     *
     * @param packet message to be sent
     */
    void sendPacket(String packet);


    /**
     * Closes the communication infrastructure and releases corresponding resources.
     */
    void finish();

}
