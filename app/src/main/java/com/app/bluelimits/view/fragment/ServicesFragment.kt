package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentServicesBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.util.*
import com.app.bluelimits.view.ServiceListAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.ServicesViewModel
import com.google.gson.Gson

class ServicesFragment : Fragment() {

    private lateinit var viewModel: ServicesViewModel
    private var _binding: FragmentServicesBinding? = null
    private val resortListAdapter = ServiceListAdapter(arrayListOf())

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentServicesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ServicesViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE

        hideKeyboard(context as Activity)
        (activity as DashboardActivity).onCustomTBIconClick(null)

        val prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data = gson.fromJson(data_string, Data::class.java)
        val token = data.token
        token?.let { viewModel.getAllServices(it,"100", "1") }

        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = resortListAdapter
        }

        observeViewModel()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun observeViewModel() {
        viewModel.servicesResponse.observe(viewLifecycleOwner, Observer { user ->
            binding.progressBar.progressbar.visibility = View.GONE
            user?.let {
                //prevents on souccess code from being called twice
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                    viewModel.servicesResponse.value?.let {

                        resortListAdapter.setServicesList(it.data, requireContext())
                    }
                }

            }

        })

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

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

    }


}