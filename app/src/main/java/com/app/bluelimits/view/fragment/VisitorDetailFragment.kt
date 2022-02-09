package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentHomeBinding
import com.app.bluelimits.databinding.FragmentVisitorDetailBinding
import com.app.bluelimits.databinding.FragmentVisitorInviteBinding
import com.app.bluelimits.databinding.FragmentVisitorsListBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.VisitorRequest
import com.app.bluelimits.model.VisitorResult
import com.app.bluelimits.model.VisitorsData
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.AddVisitorsAdapter
import com.app.bluelimits.view.ResortListAdapter
import com.app.bluelimits.view.VisitorDetailAdapter
import com.app.bluelimits.view.VisitorsListAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.HomeViewModel
import com.app.bluelimits.viewmodel.VisitorInviteViewModel
import com.app.bluelimits.viewmodel.VisitorsViewModel
import com.google.gson.Gson

/**
 * A fragment representing a list of Items.
 */
class VisitorDetailFragment : Fragment() {

    private lateinit var binding: FragmentVisitorDetailBinding
    private lateinit var visitorDetailAdapter: VisitorDetailAdapter
    private lateinit var visitorDetail: VisitorResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            visitorDetail = it.getParcelable("vDetails")!!
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVisitorDetailBinding.inflate(layoutInflater)
        return binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomeNavigation(context as Activity, VisitorDetailFragmentDirections.actionNavToHome())
        setDetails()
    }

    private fun setVisitorList() {

        visitorDetailAdapter = context?.let { VisitorDetailAdapter(arrayListOf(), it) }!!

        binding.rvVisitors.addItemDecoration(
            DividerItemDecoration(
                binding.rvVisitors.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvVisitors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorDetailAdapter

        }

        visitorDetail.visitors?.let { visitorDetailAdapter.setVisitorDetail(it) }
    }


    private fun setDetails()
    {
        binding.tvDate.setText(getString(R.string.date_time) + ": " + visitorDetail.visiting_date_time)
        binding.tvNoOfVisitors.setText(getString(R.string.no_of_visitors) + ": " +visitorDetail.no_of_visitor)

        setVisitorList()

    }

}