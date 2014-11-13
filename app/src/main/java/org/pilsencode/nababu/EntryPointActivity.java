package org.pilsencode.nababu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
        btnJoinGame = (Button) findViewById(R.id.button_join_game);

        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EntryPointActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Called when the user clicks the 'Host Game' button */
    public void hostGame(View view) {
        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            setContentView(R.layout.layout_host_game);

            // http://javatechig.com/android/android-listview-tutorial
            String[] values = new String[] { "veny", "tomor", "ondra" };
            // use your custom layout
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
            ListView listPlayers = (ListView) findViewById(R.id.list_of_players);
            listPlayers.setAdapter(adapter);
        }
    }

    /** Called when the user clicks the 'Start' button */
    public void startGame(View view) {
        Intent intent = new Intent(this, PlayingFieldActivity.class);
        startActivity(intent);
    }

}
