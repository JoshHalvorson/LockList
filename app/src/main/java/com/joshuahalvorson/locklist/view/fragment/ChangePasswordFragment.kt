package com.joshuahalvorson.locklist.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import com.joshuahalvorson.locklist.util.removeErrorOnTextChange
import kotlinx.android.synthetic.main.fragment_change_password.*

class ChangePasswordFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPrefsHelper = SharedPrefsHelper(activity?.getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))
        val password = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_KEY, "")

        old_password_edit_text.removeErrorOnTextChange(old_password_edit_text_layout)
        new_password_edit_text.removeErrorOnTextChange(new_password_edit_text_layout)

        change_password_button.setOnClickListener {
            if (old_password_edit_text.text.toString() == password) {
                if (new_password_edit_text.text.toString() != password) {
                    sharedPrefsHelper.put(SharedPrefsHelper.ACCOUNT_KEY, new_password_edit_text.text.toString())
                    Toast.makeText(context, "Password changed", Toast.LENGTH_LONG).show()
                    dismiss()
                } else {
                    new_password_edit_text_layout.error = "New password matches old password"
                }
            } else {
                old_password_edit_text_layout.error = "Old password doesn't match current password"
            }
        }
    }
}
