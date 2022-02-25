package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.bluelimits.view.GuestListAdapter
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestsBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.GuestData
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.util.showSuccessDialog
import com.app.bluelimits.viewmodel.GuestsViewModel
import com.google.gson.Gson

/**
 * A fragment representing a list of Items.
 */
class GuestsFragment : Fragment() {

    private lateinit var viewModel: GuestsViewModel
    private lateinit var binding: FragmentGuestsBinding
    private lateinit var guestListAdapter: GuestListAdapter
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGuestsBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this).get(GuestsViewModel::class.java)
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        getGuests()

        return binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomeNavigation(context as Activity, VisitorsFragmentDirections.actionNavToHome())

    }

    private fun getGuests() {
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data = gson.fromJson(data_string, Data::class.java)
        val token = data.token

        binding.progressBar.progressbar.visibility = View.VISIBLE
        token?.let {
            viewModel.getGuests(it)
            observeVisitorVM()
        }
    }

    private fun observeVisitorVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })

        viewModel.guests.observe(viewLifecycleOwner, Observer { guests ->
            binding.progressBar.progressbar.visibility = View.GONE
            guests?.let {
                setGuestList(it)
            }

        })
    }

    private fun setGuestList(data: ArrayList<GuestData>) {

        guestListAdapter = context?.let { GuestListAdapter(arrayListOf(), it, this) }!!

        binding.rvGuests.addItemDecoration(
            DividerItemDecoration(
                binding.rvGuests.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvGuests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestListAdapter

        }

        guestListAdapter.setGuestList(data)
    }

}