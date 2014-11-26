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
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Activity representing step of hosting the game as a server.
 *
 * Created by veny on 19.11.14.
 */
public class HostGameActivity extends AbstractBTActivity implements Game.GameEventObserver {

    private ServerThread serverThread;
    private ArrayAdapter<String> playersListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_host_game);

        playersListAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView pairedListView = (ListView) findViewById(R.id.list_of_players);
        pairedListView.setAdapter(playersListAdapter);


        Game.getInstance().setServer(true);

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
    protected void onStart() {
        // Called just before the activity becomes visible to the user.
        super.onStart();
showToast("ON_START");

        // reset the game, maybe coming back from PlayingField
        Game.getInstance().reset();
        // register itself as game observer
        Game.getInstance().registerEventObserver(this);

        // clear joined players
        playersListAdapter.clear();
        // and add myself
        playersListAdapter.add(getUsername() + " [me]");

        if (getBluetoothAdapter().isEnabled()) {
            btPrepared4Server();
        } else { // enable BT discoverability
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIMEOUT);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT_CODE);
        }
    }

    @Override
    protected void onStop() {
        // Called when the activity is no longer visible to the user.
        // This may happen because it is being destroyed,
        // or because another activity (either an existing one or a new one)
        // has been resumed and is covering it.
        super.onStop();
showToast("ON_STOP");

        // remove itself as game observer
        Game.getInstance().removeEventObserver();

        // stop listening for incoming connection
        if (null != serverThread) {
            serverThread.cancel();
            serverThread = null;
        }
    }


    @Override
    protected void btPrepared4Server() {
        // start listening for incoming connections
        serverThread = new ServerThread();
        serverThread.start();

        Game.getInstance().setMe(new Player(getUsername()));
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
        startActivity(intent);
    }

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(ActionEnum action, String... params) {
        switch (action) {
            case JOIN:
                playersListAdapter.add(params[0]);
                break;
        }
    }


    // ------------------------------------------------------------------------

    private class ServerThread extends Thread {

        private BluetoothServerSocket serverSocket;
        private boolean running = true;

        public ServerThread() {
            try {
                serverSocket = getBluetoothAdapter().listenUsingRfcommWithServiceRecord(
                        AbstractBTActivity.BT_APP_NAME, AbstractBTActivity.BT_APP_UUID);
            } catch (IOException e) {
                cancel();
                handleCaughtException("server socked failed", e);
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket;

            while (running && null != serverSocket) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    if (Game.D) { Log.d(Game.TAG, "failed to accept socket", e); }
                    socket = null;
                }
                // if a connection was accepted
                if (null != socket) {
                    ConnectedClientThread th = new ConnectedClientThread(socket);
                    th.start();
                    socket = null;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish.
         */
        public void cancel() {
            running = false;
            try {
                // this produces exception on 'serverSocket.accept()'
                if (null != serverSocket) { serverSocket.close(); }
            } catch (IOException e) {
                Log.e("nababu", "failed to close server socket", e);
            }
            serverSocket = null;
        }

    }

    // ------------------------------------------------------------------------

    protected class ConnectedClientThread extends Thread implements Communicator {

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
                finish();
                handleCaughtException("failed to initialize reader/writer", e);
            }
        }

        public void run() {
            Player player = new Player();
            player.setCommunicator(this);
            Game.getInstance().addPlayer(player);

            byte[] buffer = new byte[1024];
            while (null != socket) {
                try {
//                    String packet = reader.readLine();

                        int len = socket.getInputStream().read(buffer);
                        String packet = new String(buffer, 0, len - 1, "UTF-8");

                    final String parts[] = packet.split(":");
                    final String username = parts[1];
                    player.setName(username);
                    Game.getInstance().getHandler().obtainMessage(ActionEnum.JOIN.ordinal(), username).sendToTarget();
                    // response with JOINED packet
                    sendMessage(encodePacket(ActionEnum.JOINED, username));

                    // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();
                } catch (Exception e) {
                    finish();
                    handleCaughtException("failed to read from socket", e);
                }
            }
        }

        @Override
        public void sendMessage(String packet) {
            writer.println(packet);
            writer.flush();
//
//            try {
//                socket.getOutputStream().write(packet.getBytes());
//            } catch (IOException e) {
//                Log.e("nababu", "failed to send data", e);
//                cancel();
//showToast("ERR: " + e.toString());
//            }
        }

        @Override
        public void finish() {
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
