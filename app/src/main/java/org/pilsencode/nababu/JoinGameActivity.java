package org.pilsencode.nababu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;

/**
 * Activity representing step of joining the game as a client.
 *
 * Created by veny on 20.11.14.
 */
public class JoinGameActivity extends AbstractBTActivity implements Game.GameEventObserver {

    private ConnectThread connectThread;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.layout_join_game);

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView pairedListView = (ListView) findViewById(R.id.list_of_paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(deviceListClickListener);

        newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView newListView = (ListView) findViewById(R.id.list_of_new_devices);
        newListView.setAdapter(newDevicesArrayAdapter);
        newListView.setOnItemClickListener(deviceListClickListener);

        Game.getInstance().setServer(false);
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
        // register itself as game observer
        Game.getInstance().registerEventObserver(this);

        // clear listed devices
        pairedDevicesArrayAdapter.clear();
        newDevicesArrayAdapter.clear();

        if (getBluetoothAdapter().isEnabled()) {
            btPrepared4Client();
        } else { // enable BT
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_CODE);
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

        // stop connecting thread
        if (null != connectThread && connectThread.isAlive()) {
            connectThread.finish();
            connectThread = null;
        }
    }

    @Override
    protected void btPrepared4Server() {
        throw new IllegalStateException("not relevant for client");
    }

    @Override
    protected void btPrepared4Client() {
        Button scan = (Button) findViewById(R.id.button_scan);
        scan.setEnabled(true);

        // get list of currently paired devices
        Set<BluetoothDevice> pairedDevices = getBluetoothAdapter().getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_devices_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * Called when the user clicks the 'Scan' button.
     */
    public void scanDevices(View view) {
        // register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastsDiscoveryReceiver, filter);
        // TODO [veny] deregister receiver

        // register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(broadcastsDiscoveryReceiver, filter);
        // TODO [veny] deregister receiver

        setProgressBarIndeterminateVisibility(true);
        // stop discovering if already running
        if (getBluetoothAdapter().isDiscovering()) {
            getBluetoothAdapter().cancelDiscovery();
        }
        // start discovering from BluetoothAdapter
        getBluetoothAdapter().startDiscovery();
    }

    // BroadcastReceiver for discovering of Bluetooth devices
    private final BroadcastReceiver broadcastsDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
//                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_devices_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    // the on-click listener for all devices in the ListViews
    private final AdapterView.OnItemClickListener deviceListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // cancel discovery because it's costly and we're about to connect
            getBluetoothAdapter().cancelDiscovery();

            // get the device MAC address, which is the second line
            String info = ((TextView) v).getText().toString();
            String address = info.split("\\n")[1];

            connectThread = new ConnectThread(address);
            connectThread.start();
        }
    };

    // ------------------------------------------- Game.GameEventObserver Stuff

    @Override
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case JOINED:
                //playersListAdapter.add(params[0]);
                break;
        }
    }

    // ------------------------------------------------------------------------

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {

        private BluetoothSocket socket;

        public ConnectThread(String address) {
            // get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);
                // BT_UUID is the app's UUID string, also used by the server code
                socket = device.createRfcommSocketToServiceRecord(AbstractBTActivity.BT_APP_UUID);
            } catch (IOException e) {
                Log.e("nababu", "failed to acquire socket", e);
                finish();
showToast("ERR: " + e.toString());
            }
        }

        @Override
        public void run() {
            if (null != socket) { // unless constructor failed
                // cancel discovery because it will slow down the connection
                getBluetoothAdapter().cancelDiscovery();

                try {
                    socket.connect();
                } catch (IOException e) {
                    Log.e("nababu", "failed to connect", e);
                    finish();
showToast("ERR: " + e.toString());
                }
            }

            if (null != socket) {
                ConnectedThread th = new ConnectedThread(socket);
                th.start();
            }
        }

        public void finish() {
            try {
                if (null != socket) { socket.close(); }
            } catch (IOException e) {
                Log.e("nababu", "failed to close socket", e);
            }
            socket = null;
        }

    }

}
