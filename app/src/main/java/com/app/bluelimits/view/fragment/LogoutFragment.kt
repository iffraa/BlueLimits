package com.app.bluelimits.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.app.bluelimits.R
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.view.activity.MainActivity

class LogoutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.logout_msg))
            .setPositiveButton(getString(R.string.yes)) { _,_ ->
                (activity as DashboardActivity).changeLoginDisplay(false)
                clearData()

                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)

            }
            .setNegativeButton(getString(R.string.no)) { _,_ ->
                dismiss()
            }
            .create()

    private fun clearBackStack() {
        val fragmentManager: FragmentManager = requireActivity().getSupportFragmentManager()
        for (fragment in fragmentManager.getFragments()) {
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }

    private fun clearData()
    {
        val prefsHelper = context?.let { SharedPreferencesHelper(it) }
        prefsHelper?.clearPrefs()
        Constants.isLoggedIn = false

    }
}