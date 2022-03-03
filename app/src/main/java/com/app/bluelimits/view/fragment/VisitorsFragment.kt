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
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentVisitorsListBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.VisitorResult
import com.app.bluelimits.model.VisitorsData
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.VisitorsListAdapter
import com.app.bluelimits.viewmodel.VisitorsViewModel
import com.google.gson.Gson

/**
 * A fragment representing a list of Items.
 */
class VisitorsFragment : Fragment() {

    private lateinit var viewModel: VisitorsViewModel
    private lateinit var binding: FragmentVisitorsListBinding
    private lateinit var visitorListAdapter: VisitorsListAdapter
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVisitorsListBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this).get(VisitorsViewModel::class.java)
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        getVisitors()

        return binding.root

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setHomeNavigation(context as Activity, VisitorsFragmentDirections.actionNavToHome())



    }

    private fun getVisitors() {
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data = gson.fromJson(data_string, Data::class.java)
        val token = data.token

        binding.progressBar.progressbar.visibility = View.VISIBLE
        token?.let {
            viewModel.getVisitors(it)
            observeVisitorVM()
        }
    }

    fun observeVisitorVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })

        viewModel.visitors.observe(viewLifecycleOwner, Observer { visitors ->
            binding.progressBar.progressbar.visibility = View.GONE
            visitors?.let {
                setVisitorList(it.invitees.data)
                setVisitorData(it)
            }

        })
    }

    private fun setVisitorList(data: ArrayList<VisitorResult>) {

        visitorListAdapter = context?.let { VisitorsListAdapter(arrayListOf(), it, this) }!!

        binding.rvVisitors.addItemDecoration(
            DividerItemDecoration(
                binding.rvVisitors.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvVisitors.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorListAdapter

        }

        visitorListAdapter.setVisitorList(data)
    }

    private fun setVisitorData(data: VisitorsData)
    {
        binding.tvTotal.setText(data.total_invitees.toString())
        binding.tvToday.setText(data.today_invitees.toString())

    }

}