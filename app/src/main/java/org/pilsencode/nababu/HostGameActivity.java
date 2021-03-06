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

/**
 * Activity representing step of hosting the game as a server.
 *
 * Created by veny on 19.11.14.
 */
public class HostGameActivity extends AbstractBTActivity implements Game.GameEventObserver {

    private AcceptThread acceptThread;
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

        // remove itself as game observer
        Game.getInstance().removeEventObserver(this);

        // stop listening for incoming connection
        if (null != acceptThread && acceptThread.isAlive()) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }


    @Override
    protected void btPrepared4Server() {
        // start listening for incoming connections
        acceptThread = new AcceptThread();
        acceptThread.start();

        Player me = new Player(getUsername());
        Game.getInstance().setMe(me);
        // server is always the Baba
        me.setBaba(true);
    }

    @Override
    protected void btPrepared4Client() {
        throw new IllegalStateException("not relevant for server");
    }

    /**
     * Called when the user clicks the 'Start' button. (on the server side)
     */
    public void startGame(View view) {
        String babaName = Game.getInstance().getMe().getName();

        Intent intent = new Intent(this, PlayingFieldActivity.class);
        intent.putExtra(PlayingFieldActivity.BABANAME, babaName);
        startActivity(intent);

        // inform all clients that the game starts
        Game.getInstance().sendToOtherPlayers(new Game.GameEvent(ActionEnum.START_GAME, babaName));
    }

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case JOIN:
                // JOIN action is sent only from the client to the server
                if (Game.getInstance().isServer()) {
                    String name = event.params[0];
                    // on server the player name was unknown until now
                    // check if the players name already exists, if yes - return info that the player must change his name
                    if (Game.getInstance().isNameFree(name)) {
                        // name of the player is free - set it (it was unknow until now)
                        event.player.setName(name);

                        // add new player into displayed list of players
                        playersListAdapter.add(name);

                        // send all currently joined players names to all players (players must ignore duplicities)
                        for (Player p : Game.getInstance().getOtherPlayers()) {
                            Game.getInstance().sendToOtherPlayers(new Game.GameEvent(event.player, ActionEnum.JOINED, p.getName()));
                        }

                        // send the name of server player to currently joined player
                        Game.getInstance().sendToOtherPlayers(new Game.GameEvent(ActionEnum.JOINED, Game.getInstance().getMe().getName()));

                        // 1 - I guess it does not matter what is send.. just send any integer
//                        Game.getInstance().getHandler().obtainMessage(1, new Game.GameEvent(event.player, ActionEnum.JOINED, name)).sendToTarget();
                        // send the name of server player to all players
//                        Game.getInstance().getHandler().obtainMessage(1, new Game.GameEvent(event.player, ActionEnum.JOINED, Game.getInstance().getMe().getName())).sendToTarget();
                    } else {
                        // TODO send info that player exists and user must change his name - he has to send join packet again
                        showToast("Player name "+ name +" already exists and user must change his name - he has to send join packet again (TODO implement)");
                    }
                }
                // response with JOINED packet
//                event.player.getCommunicator().sendPacket(encodePacket(ActionEnum.JOINED, name));
                // response with me to add me on client side
//                event.player.getCommunicator().sendPacket(encodePacket(ActionEnum.JOINED, Game.getInstance().getMe().getName()));
                break;
        }
    }


    // ------------------------------------------------------------------------

    /**
     * This thread runs while listening for incoming connections.
     * It runs until cancelled.
     */
    private class AcceptThread extends Thread {

        private BluetoothServerSocket serverSocket;
        private boolean running = true;

        public AcceptThread() {
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
                    if (Game.D) { Log.i(Game.TAG, "socket connection accepted"); }
                } catch (IOException e) {
                    if (Game.D) { Log.d(Game.TAG, "failed to accept socket", e); }
                    socket = null;
                }
                // if a connection was accepted
                if (null != socket) {
                    ConnectedThread th = new ConnectedThread(socket);
                    th.start();
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

}
