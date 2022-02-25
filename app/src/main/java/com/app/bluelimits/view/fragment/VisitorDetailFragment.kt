package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentVisitorDetailBinding
import com.app.bluelimits.model.VisitorResult
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.view.VisitorDetailAdapter

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