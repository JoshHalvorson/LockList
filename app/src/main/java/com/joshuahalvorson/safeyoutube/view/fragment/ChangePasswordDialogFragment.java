package com.joshuahalvorson.safeyoutube.view.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshuahalvorson.safeyoutube.R;

public class ChangePasswordDialogFragment extends Fragment {


    public ChangePasswordDialogFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password_dialog, container, false);
    }

}
