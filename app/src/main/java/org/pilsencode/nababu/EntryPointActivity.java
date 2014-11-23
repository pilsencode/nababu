package org.pilsencode.nababu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This activity represents entry point into the game.
 *
 * Created by veny on 13.11.14.
 */
public class EntryPointActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_entry_point);

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
        nextActivity(HostGameActivity.class);
    }

    /**
     * Called when the user clicks the 'Join Game' button.
     */
    public void joinGame(View view) {
        nextActivity(JoinGameActivity.class);
    }

    private void nextActivity(Class activity) {
        EditText txtUserName = (EditText) findViewById(R.id.edit_username);

        if (0 == txtUserName.getText().toString().trim().length()) {
            Toast.makeText(EntryPointActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getBaseContext(), activity);
        intent.putExtra("USERNAME", txtUserName.getText().toString().trim());
        startActivity(intent);
    }

    /**
     * Exit application confirmation dialog
     *
     * @Override
     */
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EntryPointActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}
