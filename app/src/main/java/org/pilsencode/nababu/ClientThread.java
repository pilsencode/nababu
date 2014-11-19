package org.pilsencode.nababu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by veny on 16.11.14.
 */
public class ClientThread extends Thread {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device;

    public ClientThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;

        // get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // BT_UUID is the app's UUID string, also used by the server code
            socket = device.createRfcommSocketToServiceRecord(EntryPointActivity.BT_UUID);
        } catch (IOException e) {
            // TODO [veny] an android corresponding reaction
            Log.e("nababu", "server socked failed", e);
        }
    }

    @Override
    public void run() {
        // cancel discovery because it will slow down the connection
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket.
            // This will block until it succeeds or throws an exception
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                socket.close();
            } catch (IOException closeException) { }
            return;
        }

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(out);

            pw.println("CLIENT");
            pw.flush();
            byte[] buffer = new byte[1024];
            int bytes = in.read(buffer);

            socket.close();
        } catch (IOException e) {
            Log.e("nababu", "communication failed", e);
        }
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish.
     */
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }

}