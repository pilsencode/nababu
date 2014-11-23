package org.pilsencode.nababu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Activity representing step of hosting the game as a server.
 *
 * Created by veny on 19.11.14.
 */
public class HostGameActivity extends AbstractBTActivity {

    private ServerThread serverThread;
    private ArrayAdapter<String> playersListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_host_game);

        if (getBluetoothAdapter().isEnabled()) {
            btPrepared4Server();
        } else { // enable BT discoverability
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIMEOUT);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT_CODE);
        }

        playersListAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView pairedListView = (ListView) findViewById(R.id.list_of_players);
        pairedListView.setAdapter(playersListAdapter);

        // hides the soft keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        // fill list view with static data
//        // http://javatechig.com/android/android-listview-tutorial
//        String[] values = new String[] { "veny", "tomor", "ondra" };
//        // use your custom layout
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
//        ListView listPlayers = (ListView) findViewById(R.id.list_of_players);
//        listPlayers.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            serverThread.cancel();
        }
    }

    @Override
    protected void btPrepared4Server() {
        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    protected void btPrepared4Client() {
        throw new IllegalStateException("not relevant for server");
    }

    /**
     * Called when the user clicks the 'Start' button.
     */
    public void startGame(View view) {
        Intent intent = new Intent(this, PlayingFieldActivity.class);
        intent.putExtra(EntryPointActivity.USERNAME, getUsername());
        startActivity(intent);
    }

    // ------------------------------------------------------------------------

    private class ServerThread extends Thread {

        private BluetoothServerSocket serverSocket;

        public ServerThread() {
            try {
                serverSocket = getBluetoothAdapter().listenUsingRfcommWithServiceRecord(
                        AbstractBTActivity.BT_APP_NAME, AbstractBTActivity.BT_APP_UUID);
            } catch (IOException e) {
                Log.e("nababu", "server socked failed", e);
                cancel();
showToast("ERR: " + e.toString());
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket;

            while (null != serverSocket) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    // TODO [veny] exception handling
                    break;
                }
                // if a connection was accepted
                if (null != socket) {
//                    ConnectedClientThread th = new ConnectedClientThread(socket);
//                    th.start();
//                    socket = null;

                    try {
                        InputStream in = socket.getInputStream();

                        byte[] buffer = new byte[1024];
                        int len = in.read(buffer);
                        String packet = new String(buffer, 0, len - 1, "UTF-8");
                        final String parts[] = packet.split(":");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                playersListAdapter.add(parts[1]);
                            }
                        });
showToast("MSG: " + packet);

                        socket.close();
                    } catch (IOException e) {
showToast("ERR: " + e.toString());
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish.
         */
        public void cancel() {
            try {
                if (null != serverSocket) { serverSocket.close(); }
            } catch (IOException e) {
                Log.e("nababu", "failed to close server socket", e);
            }
            serverSocket = null;
        }

    }

    // ------------------------------------------------------------------------

    protected class ConnectedClientThread extends Thread {

        private BluetoothSocket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ConnectedClientThread(BluetoothSocket socket) {
            if (null == socket) { throw new NullPointerException("socket cannot be null"); }
            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                Log.e("nababu", "failed to initialize reader/writer", e);
                cancel();
showToast("ERR: " + e.toString());
            }
        }

        public void run() {
            while (null != socket) {
                try {
                    String packet = new String(reader.readLine());
                    // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();
                } catch (IOException e) {
                    Log.e("nababu", "failed to read from socket", e);
                    cancel();
showToast("ERR: " + e.toString());
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String packet) {
            writer.println(packet);
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                if (null != reader) { reader.close(); }
                if (null != writer) { writer.close(); }
            } catch (IOException e) {
                Log.e("nababu", "failed to close reader/writer", e);
            }
            try {
                if (null != socket) { socket.close(); }
            } catch (IOException e) {
                Log.e("nababu", "failed to close socket", e);
            }
            socket = null;
        }

    }

}
