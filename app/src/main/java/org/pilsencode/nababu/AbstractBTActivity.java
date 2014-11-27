package org.pilsencode.nababu;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.UUID;


/**
 * Base class of an <code>Activity</code> working with Bluetooth Adapter.
 *
 * Created by veny on 19.11.14.
 */
public abstract class AbstractBTActivity extends Activity {

    public static final int REQUEST_DISCOVERABLE_BT_CODE = 42;
    public static final int REQUEST_ENABLE_BT_CODE = 43;
    public static final int BT_DISCOVERABLE_TIMEOUT = 300;

    public static final String BT_APP_NAME = "Nababu";
    // Unique UUID for this application
    public static final UUID BT_APP_UUID = UUID.fromString("97a335c2-a01a-33ee-56ca-e802200c9a77");

    private BluetoothAdapter bluetoothAdapter;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        username = extras.getString(EntryPointActivity.USERNAME);
        Toast.makeText(this, "USERNAME " + username, Toast.LENGTH_LONG);

        // init Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == bluetoothAdapter) {
            Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    btPrepared4Client();
                } // TODO [veny] implement 'else'
                break;

            case REQUEST_DISCOVERABLE_BT_CODE:
                if (resultCode == BT_DISCOVERABLE_TIMEOUT) {
                    btPrepared4Server();
                } // TODO [veny] implement 'else'
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected abstract void btPrepared4Server();
    protected abstract void btPrepared4Client();

    protected BluetoothAdapter getBluetoothAdapter() {
        if (null == bluetoothAdapter) {
            throw new NullPointerException("adapter is null");
        }
        return bluetoothAdapter;
    }

    /**
     * Gets username of player.
     * @return username
     */
    protected String getUsername() {
        return username;
    }

    /**
     * Shows message box with information about a caught exception.
     * @param action context description
     * @param e caught exception
     */
    protected void handleCaughtException(String action, Exception e) {
        if (Game.D) { Log.e(Game.TAG, action + ": " + e.getMessage(), e); }

        final AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
        messageBox.setTitle("Error");
        messageBox.setMessage(action + "\n" + e.getClass().getSimpleName() + "\n" + e.getMessage());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        runOnUiThread(new Runnable() {
            public void run() {
                messageBox.show();
            }
        });
    }

    /**
     * Allows to display <code>Toast</code> messages from an another thread.
     * @param toast message
     */
    protected void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(AbstractBTActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected String encodePacket(ActionEnum action, Object... args) {
        StringBuilder rslt = new StringBuilder(action.toString());
        for (Object arg : args) {
            rslt.append(':').append(arg.toString());
        }
        return rslt.toString();
    }

    // ------------------------------------------------------------------------

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    protected class ConnectedThread extends Thread implements Communicator {

        private BluetoothSocket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ConnectedThread(BluetoothSocket socket) {
            if (null == socket) { throw new NullPointerException("socket cannot be null"); }
            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                if (Game.D) { Log.d(Game.TAG, "created ConnectedThread"); }
            } catch (IOException e) {
                finish();
                handleCaughtException("failed to initialize reader/writer", e);
            }
        }

        public void run() {
            final Player player = new Player();
            player.setCommunicator(this);

            if (Game.getInstance().isServer()) {
                // we do not know the name now, wait for JOIN packet
                Game.getInstance().addPlayer(player);
            } else {
                player.setName(getUsername());
                Game.getInstance().setMe(player);
                // send username to server
                sendMessage(encodePacket(ActionEnum.JOIN, getUsername()));
            }

            while (null != socket) {
                try {
                    String packet = reader.readLine();
                    if (Game.D) { Log.d(Game.TAG, "packet received: " + packet); }

                    final String parts[] = packet.split(":");
                    final ActionEnum action = ActionEnum.valueOf(parts[0]);

                    Game.GameEvent event = new Game.GameEvent(action, player, Arrays.copyOfRange(parts, 1, parts.length));
                    Game.getInstance().getHandler().obtainMessage(action.ordinal(), event).sendToTarget();
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
