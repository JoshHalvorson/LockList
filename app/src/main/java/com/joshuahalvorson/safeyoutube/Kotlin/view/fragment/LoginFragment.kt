package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_playlists_list.*

class LoginFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val password = sharedPref?.getString(getString(R.string.account_key), "")

        if(password.equals("")){
            log_in_frag_text.text = "Create a password"
            password_edit_text.hint = "Create a password"
            confirm_password_edit_text.visibility = View.VISIBLE
            log_in_button.setOnClickListener {
                if(password_edit_text.text.toString() != "" && confirm_password_edit_text.text.toString() != ""){
                    if (password_edit_text.text.toString() == confirm_password_edit_text.text.toString()){
                        val editor: SharedPreferences.Editor? = sharedPref?.edit()
                        editor?.putString(getString(R.string.account_key), password_edit_text.text.toString())
                        editor?.apply()
                        //close dialog and
                        //launch settings frag
                    }else{
                        //passwords do not match
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                    }
                }else{
                    //one of the password fields is empty
                    Toast.makeText(context, "One of the fields is empty", Toast.LENGTH_LONG).show()
                }
            }
        }else{
            log_in_button.setOnClickListener {
                if(password == password_edit_text.text.toString()){
                    //close dialog, launch settings frag
                }else{
                    //password is wrong
                    Toast.makeText(context, "Wrong password", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}
