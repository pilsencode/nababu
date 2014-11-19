package org.pilsencode.nababu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

                try {
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();

                    byte[] buffer = new byte[1024];
                    int bytes = in.read(buffer);
                    out.write("NAZDAR".getBytes());

                    socket.close();
                } catch (IOException closeException) { }
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