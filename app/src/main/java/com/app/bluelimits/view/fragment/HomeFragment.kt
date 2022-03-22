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
import com.app.bluelimits.databinding.FragmentHomeBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.util.*
import com.app.bluelimits.view.ResortListAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val resortListAdapter = ResortListAdapter(arrayListOf())

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE

        hideKeyboard(context as Activity)
        (activity as DashboardActivity).onCustomTBIconClick(null)

        viewModel.getMemberResorts()

        binding.rvResorts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = resortListAdapter
        }

        observeViewModel()

        context?.let { loadGif(binding.ivBg, R.raw.white_bg, it) }

      /*  binding.ivMarine.setOnClickListener(View.OnClickListener {
            val resort = Resort(0, Constants.MARINE, "")
            val action = HomeFragmentDirections.actionNavHomeToResortInfoFrag(resort)
            Navigation.findNavController(it).navigate(action)

        })*/

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addMarine(resorts: ArrayList<Resort>) {

        val marineResort = Resort(
            90,
            "Marine",
            "http://saudiaweb.com/bluelimits/public/uploads/resort/file_1629272985_marine.png"
        )
        resorts.add(marineResort)
        /* Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (binding.rvResorts.isVisible)
                    // This method will be executed once the timer is over
                        binding.ivMarine.visibility = View.VISIBLE
                    else {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {

                                // This method will be executed once the timer is over
                                binding.ivMarine.visibility = View.VISIBLE
                            },
                            2000 // value in milliseconds
                        )
                    }
                },
                1000 // value in milliseconds
            )*/
    }

    fun observeViewModel() {
        viewModel.resorts.observe(viewLifecycleOwner, Observer { user ->
            binding.progressBar.progressbar.visibility = View.GONE
            user?.let {
                //prevents on souccess code from being called twice
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                   // binding.rvResorts.visibility = View.VISIBLE

                    viewModel.resorts.value?.let {

                        val iterator = it.iterator()
                        while (iterator.hasNext()) {
                            val resort = iterator.next()
                            if (resort.name.equals("BOHO Resort")) {
                                iterator.remove()
                            }
                            else if(resort.name.equals("OIA Resort") && Constants.isLoggedIn)
                            {
                                iterator.remove()

                            }
                        }

                        addMarine(it)
                        //binding.ivMarine.visibility = View.VISIBLE
                        resortListAdapter.updateResortList(it)
                    }
                }

            }

        })

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

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

    }


}