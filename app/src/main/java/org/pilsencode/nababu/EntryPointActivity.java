package org.pilsencode.nababu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by veny on 13.11.14.
 */
public class EntryPointActivity extends Activity {

    private EditText txtUserName;
    private Button btnJoinGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_entry_point);

        txtUserName = (EditText) findViewById(R.id.edit_username);
//        btnJoinGame = (Button) findViewById(R.id.button_join_game);
//
//        btnJoinGame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(EntryPointActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    /** Called when the user clicks the 'Host Game' button */
    public void hostGame(View view) {
        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!initBT()) { // failed initialization
            return;
        }
        setContentView(R.layout.layout_host_game);
        // hides the soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        // http://javatechig.com/android/android-listview-tutorial
        String[] values = new String[] { "veny", "tomor", "ondra" };
        // use your custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        ListView listPlayers = (ListView) findViewById(R.id.list_of_players);
        listPlayers.setAdapter(adapter);


        serverThread = new ServerThread(bluetoothAdapter);
//        serverThread.start();

        Toast.makeText(this, "OK", Toast.LENGTH_LONG);
    }


    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    /** Called when the user clicks the 'Join Game' button */
    public void joinGame(View view) {
        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!initBT()) { // failed initialization
            return;
        }
        setContentView(R.layout.layout_join_game);
        // hides the soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_in_list);

        ListView pairedListView = (ListView) findViewById(R.id.list_of_paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        // get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }

//        setProgressBarIndeterminateVisibility(true);
//        // If we're already discovering, stop it
//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//        }
//        // Request discover from BluetoothAdapter
//        bluetoothAdapter.startDiscovery();
    }

    /** Called when the user clicks the 'Start' button */
    public void startGame(View view) {
        Intent intent = new Intent(this, PlayingFieldActivity.class);
        startActivity(intent);
    }

    public void notImplementedYet(View view) {
        Toast.makeText(EntryPointActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }


    // -------------------------------------------------------- Bluetooth Stuff

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 3;
    public static final String BT_NAME = "Nababu";
    // Unique UUID for this application
    public static final UUID BT_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a77");
    private ServerThread serverThread;
    public boolean initBT() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == bluetoothAdapter) {
            Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_LONG);
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }

        return true;
    }

}
