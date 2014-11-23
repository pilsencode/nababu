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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Activity representing step of joining the game as a client.
 *
 * Created by veny on 20.11.14.
 */
public class JoinGameActivity extends AbstractBTActivity {

    private ClientThread clientThread;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.layout_join_game);

        if (getBluetoothAdapter().isEnabled()) {
            btPrepared4Client();
        } else { // enable BT
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_CODE);
        }

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView pairedListView = (ListView) findViewById(R.id.list_of_paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(deviceListClickListener);

        newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        ListView newListView = (ListView) findViewById(R.id.list_of_new_devices);
        newListView.setAdapter(newDevicesArrayAdapter);
        newListView.setOnItemClickListener(deviceListClickListener);

        // get a set of currently paired devices
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.cancel();
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

            // get the device MAC address, which is the last second line
            String info = ((TextView) v).getText().toString();
            String address = info.split("\\n")[1];

            clientThread = new ClientThread(address);
            clientThread.start();
        }
    };

    // ------------------------------------------------------------------------

    private class ClientThread extends Thread {

        private BluetoothSocket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientThread(String address) {
            // get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);
                // BT_UUID is the app's UUID string, also used by the server code
                socket = device.createRfcommSocketToServiceRecord(AbstractBTActivity.BT_APP_UUID);
            } catch (IOException e) {
                Log.e("nababu", "failed to acquire socket", e);
                cancel();
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
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream());
                } catch (IOException e) {
                    Log.e("nababu", "failed to connect", e);
                    cancel();
showToast("ERR: " + e.toString());
                }
            }

            // send username to server
            sendMessage(encodePacket(ActionEnum.JOIN, getUsername()));

//            try{
//            socket.getOutputStream().write(encodePacket(ActionEnum.JOIN, getUsername()).getBytes());
//                socket.getOutputStream().flush();
                //socket.getOutputStream().flush();
            // and wait reading forever (writing comes from another threads)
            while (null != socket) {
                try {
                    String packet = new String(reader.readLine());
                    // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e("nababu", "failed to read from socket", e);
                    cancel();
showToast("ERR: " + e.toString());
                }
            }
//cancel();
        }

        /**
         * Call this from another thread or the main activity to send data to the remote device.
         */
        public void sendMessage(String packet) {
            writer.println(packet);
            writer.flush();
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish.
         */
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
