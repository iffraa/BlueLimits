package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestSpaceBinding
import com.app.bluelimits.databinding.FragmentLoginBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.SpaceType
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.viewmodel.GuestSpaceViewModel
import com.app.bluelimits.viewmodel.HomeViewModel
import com.google.gson.Gson

class GuestSpaceFragment : Fragment() {

    private lateinit var binding: FragmentGuestSpaceBinding
    private lateinit var viewModel: GuestSpaceViewModel
    private lateinit var spaceList: ArrayList<SpaceType>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuestSpaceBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(GuestSpaceViewModel::class.java)

        setHomeNavigation(context as Activity, AboutUsFragmentDirections.actionNavToHome())

        getUnitTypes()

    }

    private fun getUnitTypes()
    {
        val prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data = gson.fromJson(data_string, Data::class.java)

        data.token?.let { viewModel.getUnitTypes(it) }
        observeViewModel()
    }

    fun observeViewModel() {
        viewModel.unitTypes.observe(viewLifecycleOwner, Observer { user ->
        //    binding.progressBar.progressbar.visibility = View.GONE
            user?.let {
                //prevents on souccess code from being called twice
                if(viewLifecycleOwner.lifecycle.currentState== Lifecycle.State.RESUMED){
                    viewModel.unitTypes.value?.let {

                        spaceList = viewModel.unitTypes.value!!
                        val iterator = it.iterator()
                        while(iterator.hasNext()){
                            val space = iterator.next()
                           when(space.name)
                           {
                               "LOFT SPACE" -> setLoftDetail(space)
                               "VILLA SPACE" -> setVillaDetail(space)
                               "SUITE SPACE" -> setSuiteDetail(space)
                           }
                        }

                    }
                }

            }

        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
               // binding.progressBar.progressbar.visibility = View.GONE
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
//                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

    }

    private fun setLoftDetail(unit: SpaceType)
    {
        binding.tvSpace.setText(unit.name)
        binding.tvAccom.setText("Accomodation for " + unit.accomodation)
        binding.tvDescription.setText(unit.description)
        binding.tvPrice.setText(unit.start_pirce)

        binding.cvLoft.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"loft")
            action?.let { Navigation.findNavController(binding.cvLoft).navigate(it, ) }

        }


    }
    private fun setVillaDetail(unit: SpaceType)
    {
        binding.tvSpace2.setText(unit.name)
        binding.tvAccom2.setText(unit.accomodation)
        binding.tvDescription2.setText(unit.description)
        binding.tvPrice2.setText(unit.start_pirce)

        binding.cvVilla.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"villa")
            action?.let { Navigation.findNavController(binding.cvVilla).navigate(it, ) }

        }

    }

    private fun setSuiteDetail(unit: SpaceType)
    {
        binding.tvSpace3.setText(unit.name)
        binding.tvAccom3.setText( unit.accomodation)
        binding.tvDescription3.setText(unit.description)
        binding.tvPrice3.setText(unit.start_pirce)

        binding.cvSuite.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"suite")
            action?.let { Navigation.findNavController(binding.cvSuite).navigate(it, ) }

        }


    }


}