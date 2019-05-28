package com.joshuahalvorson.safeyoutube.view.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.view.activity.SettingsActivity
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val password = sharedPref?.getString(getString(R.string.account_key), "")

        if (password.equals("")) {
            log_in_frag_text.text = "Create a password"
            password_edit_text.hint = "Create a password"
            confirm_password_edit_text.visibility = View.VISIBLE
            log_in_button.setOnClickListener {
                if (password_edit_text.text.toString() != "" && confirm_password_edit_text.text.toString() != "") {
                    if (password_edit_text.text.toString() == confirm_password_edit_text.text.toString()) {
                        val editor: SharedPreferences.Editor? = sharedPref?.edit()
                        editor?.putString(getString(R.string.account_key), password_edit_text.text.toString())
                        editor?.apply()
                        startSettingsFragment()
                    } else {
                        makeToast("Passwords do not match")
                    }
                } else {
                    makeToast("One of the fields is empty")
                }
            }
        } else {
            confirm_password_edit_text.visibility = View.GONE
            log_in_button.setOnClickListener {
                if (password == password_edit_text.text.toString()) {
                    startSettingsFragment()
                } else {
                    makeToast("Wrong password")
                }
            }
        }
    }

    private fun startSettingsFragment() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
        dismiss()
    }

    private fun makeToast(toastText: String) {
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val activity = activity
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }
}
