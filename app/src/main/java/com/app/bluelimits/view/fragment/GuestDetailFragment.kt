package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestDetailBinding
import com.app.bluelimits.model.GuestData
import com.app.bluelimits.model.SpaceType
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.view.GuestDetailAdapter
import com.app.bluelimits.view.VisitorDetailAdapter
import com.google.gson.Gson


/**
 * A fragment representing a list of Items.
 */
class GuestDetailFragment : Fragment() {

    private lateinit var binding: FragmentGuestDetailBinding
    private lateinit var guestDetailAdapter: GuestDetailAdapter
    private lateinit var guestDetail: GuestData
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            guestDetail = it.getParcelable("gDetails")!!
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGuestDetailBinding.inflate(layoutInflater)
        return binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        setHomeNavigation(context as Activity, VisitorDetailFragmentDirections.actionNavToHome())
        setDetails()
    }

    private fun setVisitorList() {

        guestDetailAdapter = context?.let { GuestDetailAdapter(arrayListOf(), it) }!!

        binding.rvGuests.addItemDecoration(
            DividerItemDecoration(
                binding.rvGuests.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvGuests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestDetailAdapter

        }

        guestDetail.guests?.let { guestDetailAdapter.setGuestDetail(it) }
    }


    private fun setDetails() {
        binding.tvChkoutDate.setText(getString(R.string.check_out) + " : " + guestDetail.to)
        binding.tvReservDate.setText(getString(R.string.reserv_date) + " : " + guestDetail.from)
        binding.tvNoOfDays.setText(getString(R.string.no_of_days) + " " + guestDetail.no_of_day)
        binding.tvNoOfGuests.setText(getString(R.string.no_of_guests) + " " + guestDetail.no_of_guest)
        binding.tvReference.setText(getString(R.string.reference_by) + "\n" + guestDetail.ref_name)
        binding.tvUnit.setText(getString(R.string.setup_unit) + " " + guestDetail.unit_no)

        setServiceName(guestDetail.unit_no!!)
        setVisitorList()

    }

    private fun setServiceName(space: String) {
        var serviceText = ""
        var unit: SpaceType? = null
        val gson = Gson()

        if (space.contains("LOFT")) {
            val json: String? = prefsHelper.getData(Constants.LOFT_SPACE)
            unit = gson.fromJson(json, SpaceType::class.java)
            serviceText = "The Loft Unit for " + unit.accomodation + " members"

        } else if (space.contains("VILLA") || space.contains("G")) {
            val json: String? = prefsHelper.getData(Constants.VILLA_SPACE)
            unit = gson.fromJson(json, SpaceType::class.java)
            serviceText = "The Villa Unit for " + unit.accomodation + " members"

        }
        else if (space.contains("SUITE") || space.contains("B")) {
            val json: String? = prefsHelper.getData(Constants.SUITE_SPACE)
            unit = gson.fromJson(json, SpaceType::class.java)
            serviceText = "The Suite Space for " + unit.accomodation + " members"

        }

        binding.tvService.setText(getString(R.string.serv_name) + " " + serviceText)
        binding.tvPrice.setText(getString(R.string.price) + " : " + unit?.start_pirce)

    }
}