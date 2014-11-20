package org.pilsencode.nababu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by veny on 13.11.14.
 */
public class EntryPointActivity extends Activity {

    private EditText txtUserName;

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

    /**
     * Called when the user clicks the 'Host Game' button.
     */
    public void hostGame(View view) {
        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getBaseContext(), HostGameActivity.class);
        intent.putExtra("USERNAME", txtUserName.getText().toString().trim());
        startActivity(intent);
    }


    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    /**
     * Called when the user clicks the 'Join Game' button.
     */
    public void joinGame(View view) {
        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getBaseContext(), JoinGameActivity.class);
        intent.putExtra("USERNAME", txtUserName.getText().toString().trim());
        startActivity(intent);
    }

}
