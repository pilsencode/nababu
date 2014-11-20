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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Activity representing step of hosting the game as a server.
 *
 * Created by veny on 19.11.14.
 */
public class HostGameActivity extends AbstractBTActivity {

    private ServerThread serverThread;


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

        // hides the soft keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        // http://javatechig.com/android/android-listview-tutorial
        String[] values = new String[] { "veny", "tomor", "ondra" };
        // use your custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        ListView listPlayers = (ListView) findViewById(R.id.list_of_players);
        listPlayers.setAdapter(adapter);
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
        intent.putExtra("USERNAME", getUsername());
        startActivity(intent);
    }

    // ------------------------------------------------------------------------

    private class ServerThread extends Thread {

        private BluetoothServerSocket serverSocket = null;

        public ServerThread() {
            try {
                serverSocket = getBluetoothAdapter().listenUsingRfcommWithServiceRecord(
                        AbstractBTActivity.BT_APP_NAME, AbstractBTActivity.BT_APP_UUID);
            } catch (IOException e) {
                // TODO [veny] an android corresponding reaction
                Log.e("nababu", "server socked failed", e);
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
showToast("BEFORE ACCEPT");
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
showToast("MSG: " + new String(buffer, "UTF-8"));
                        out.write("NAZDAR".getBytes());

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
                serverSocket.close();
            } catch (IOException e) { }
        }

    }

}
