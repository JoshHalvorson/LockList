package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.fragment_change_password.*

class ChangePasswordFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val password = sharedPref?.getString(getString(R.string.account_key), "")

        change_password_button.setOnClickListener {
            if (old_password_edit_text.text.toString() == password) {
                if (new_password_edit_text.text.toString() != password) {
                    val editor = sharedPref.edit()
                    editor.putString(getString(R.string.account_key), new_password_edit_text.text.toString())
                    editor.apply()
                    Toast.makeText(context, "Password changed", Toast.LENGTH_LONG).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "New password is the same as the old password", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Old password is not the same as the current password", Toast.LENGTH_LONG).show()
            }
        }
    }
}
