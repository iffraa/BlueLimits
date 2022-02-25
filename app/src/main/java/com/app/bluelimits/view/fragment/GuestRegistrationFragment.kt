package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import java.util.*
import androidx.lifecycle.Lifecycle
import com.app.bluelimits.databinding.FragmentGuestRegistrationBinding
import com.app.bluelimits.view.UnitListAdapter
import com.app.bluelimits.viewmodel.GuestUnitsViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [GuestRegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GuestRegistrationFragment : Fragment() {

    private lateinit var viewModel: GuestUnitsViewModel
    private var _binding: FragmentGuestRegistrationBinding? = null

    private lateinit var unitListAdapter: UnitListAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGuestRegistrationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(GuestUnitsViewModel::class.java)

        binding.progressBar.progressbar.visibility = View.VISIBLE

        arguments?.let {
            val resort = it.getParcelable<Resort>("resort")!!
            viewModel.getGuestUnits(resort.id.toString())
            observeViewModel()
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun observeViewModel() {
        viewModel.units.observe(viewLifecycleOwner, Observer { user ->
            binding.progressBar.progressbar.visibility = View.GONE
            user?.let {
                //prevents on souccess code from being called twice
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    viewModel.units.value?.let { it1 ->

                        unitListAdapter = UnitListAdapter(arrayListOf(), requireContext())
                        binding.rvUnits.visibility = View.VISIBLE

                        binding.rvUnits.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = unitListAdapter
                        }

                        unitListAdapter.updateUnitList(it1.data)

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