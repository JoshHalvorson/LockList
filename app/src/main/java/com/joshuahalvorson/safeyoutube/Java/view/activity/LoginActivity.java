package com.joshuahalvorson.safeyoutube.Java.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.joshuahalvorson.safeyoutube.R;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private TextView passwordText;
    private EditText enteredPasswordText;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.submit_password_button);
        passwordText = findViewById(R.id.login_text);
        enteredPasswordText = findViewById(R.id.password_edit_text);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final String password = sharedPref.getString(getString(R.string.account_key), "");

        if (password != null) {
            if (password.equals("")) {
                passwordText.setText("Create password");
                loginButton.setHint("Create a password");
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!enteredPasswordText.getText().toString().equals("")) {
                            sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.account_key), enteredPasswordText.getText().toString());
                            editor.apply();
                            Intent result = new Intent();
                            setResult(RESULT_OK, result);
                            finish();
                        }
                    }
                });
            } else {
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (password.equals(enteredPasswordText.getText().toString())) {
                            Intent result = new Intent();
                            setResult(RESULT_OK, result);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong password",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }
}
