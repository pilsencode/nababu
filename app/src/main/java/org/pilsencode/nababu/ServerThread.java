package org.pilsencode.nababu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by veny on 16.11.14.
 */
public class ServerThread extends Thread {

    private BluetoothServerSocket serverSocket = null;

    public ServerThread(BluetoothAdapter adapter) {
        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                    EntryPointActivity.BT_NAME, EntryPointActivity.BT_UUID);
        } catch (IOException e) {
            // TODO [veny] an android corresponding reaction
            Log.e("nababu", "server socked failed", e);
        }
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }
            // if a connection was accepted
            if (null != socket) {
                // Do work to manage the connection (in a separate thread)
                //manageConnectedSocket(socket);
                //mmServerSocket.close();
                break;
            }
        }
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish.
     */
    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) { }
    }

}