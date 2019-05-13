package com.joshuahalvorson.safeyoutube.Java.view.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.joshuahalvorson.safeyoutube.R;

public class ChangePasswordDialogFragment extends DialogFragment {
    private EditText oldPasswordEditText, newPasswordEditText;
    private Button changePasswordButton;
    private SharedPreferences sharedPref;

    public ChangePasswordDialogFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        oldPasswordEditText = view.findViewById(R.id.old_password_edit_text);
        newPasswordEditText = view.findViewById(R.id.new_password_edit_text);
        changePasswordButton = view.findViewById(R.id.change_password_button);

        sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final String password = sharedPref.getString(getString(R.string.account_key), "");

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPasswordEditText.getText().toString().equals(password) &&
                    !newPasswordEditText.getText().toString().equals(password)){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.account_key), newPasswordEditText.getText().toString());
                    editor.apply();
                    Toast.makeText(getContext(), "Password changed", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });

    }
}
