package com.joshuahalvorson.safeyoutube.view.activity;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.database.Account;
import com.joshuahalvorson.safeyoutube.database.AccountDatabase;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private TextView passwordText;
    private EditText enteredPasswordText;
    private AccountDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = Room.databaseBuilder(getApplicationContext(),
                AccountDatabase.class, "database-accounts").build();
        loginButton = findViewById(R.id.submit_password_button);
        passwordText = findViewById(R.id.login_text);
        enteredPasswordText = findViewById(R.id.password_edit_text);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(db.accountDao().getAll().size() == 0){
                    passwordText.setText("Create password");
                    loginButton.setHint("Create a password");
                    loginButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    db.accountDao().insertAll(
                                            new Account(enteredPasswordText.getText().toString()));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent result = new Intent();
                                            setResult(RESULT_OK, result);
                                            finish();
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                }else{
                    loginButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Account account = db.accountDao().getAccountById(1);
                                    if(account != null && account.accountPassword.equals(
                                            enteredPasswordText.getText().toString())){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent result = new Intent();
                                                setResult(RESULT_OK, result);
                                                finish();
                                            }
                                        });
                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Wrong password",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        
                                    }
                                }
                            }).start();
                        }
                    });
                }
            }
        }).start();
    }
}
