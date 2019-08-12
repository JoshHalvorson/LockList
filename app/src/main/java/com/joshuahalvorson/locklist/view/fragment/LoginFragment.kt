package com.joshuahalvorson.locklist.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import com.joshuahalvorson.locklist.util.removeErrorOnTextChange
import com.joshuahalvorson.locklist.view.activity.SettingsActivity
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPrefsHelper = SharedPrefsHelper(activity?.getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))
        val password = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_KEY, "")

        password_edit_text.removeErrorOnTextChange(password_edit_text_layout)
        confirm_password_edit_text.removeErrorOnTextChange(confirm_password_edit_text_layout)

        if (password.equals("")) {
            log_in_frag_text.text = "Create a password"
            password_edit_text_layout.hint = "Create a password"
            confirm_password_edit_text.visibility = View.VISIBLE
            log_in_button.setOnClickListener {
                if (password_edit_text.text.toString() != "" && confirm_password_edit_text.text.toString() != "") {
                    if (password_edit_text.text.toString() == confirm_password_edit_text.text.toString()) {
                        sharedPrefsHelper.put(SharedPrefsHelper.ACCOUNT_KEY, password_edit_text.text.toString())
                        startSettingsFragment()
                    } else {
                        confirm_password_edit_text_layout.error = "Passwords do not match"
                    }
                }

                if (password_edit_text.text.toString() == "") {
                    password_edit_text_layout.error = "Field is empty"
                }

                if (confirm_password_edit_text.text.toString() == "") {
                    confirm_password_edit_text_layout.error = "Field is empty"
                }
            }
        } else {
            confirm_password_edit_text.visibility = View.GONE
            log_in_button.setOnClickListener {
                if (password == password_edit_text.text.toString()) {
                    startSettingsFragment()
                } else {
                    password_edit_text_layout.error = "Wrong password"
                }
            }
        }
    }

    private fun startSettingsFragment() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
        dismiss()
    }
}
