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
import androidx.navigation.Navigation
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestSpaceBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.SpaceType
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.viewmodel.GuestSpaceViewModel
import com.google.gson.Gson

class GuestSpaceFragment : Fragment() {

    private lateinit var binding: FragmentGuestSpaceBinding
    private lateinit var viewModel: GuestSpaceViewModel
    private lateinit var spaceList: ArrayList<SpaceType>
    private var prefsHelper = SharedPreferencesHelper()

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
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

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
                               "ISLAND SPACE" -> setIslandDetail(space)
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

    private fun setIslandDetail(unit: SpaceType)
    {
        binding.tvSpace4.setText(unit.name)
        binding.tvAccom4.setText("Accomodation for " + unit.accomodation)
        binding.tvDescription4.setText(unit.description)
        binding.tvPrice4.setText(unit.start_pirce)
        saveUnitData(unit, Constants.ISLAND_SPACE)

        binding.btnIsland.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"island")
            action?.let { Navigation.findNavController(binding.btnIsland).navigate(it) }
        }


    }


    private fun setLoftDetail(unit: SpaceType)
    {
        binding.tvSpace.setText(unit.name)
        binding.tvAccom.setText("Accomodation for " + unit.accomodation)
        binding.tvDescription.setText(unit.description)
        binding.tvPrice.setText(unit.start_pirce)
        saveUnitData(unit, Constants.LOFT_SPACE)

        binding.btnLoft.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"loft")
            action?.let { Navigation.findNavController(binding.btnLoft).navigate(it) }
        }


    }
    private fun setVillaDetail(unit: SpaceType)
    {
        binding.tvSpace2.setText(unit.name)
        binding.tvAccom2.setText("Accomodation for " + unit.accomodation)
        binding.tvDescription2.setText(unit.description)
        binding.tvPrice2.setText(unit.start_pirce)
        saveUnitData(unit, Constants.VILLA_SPACE)

        binding.btnVilla.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"villa")
            action?.let { Navigation.findNavController(binding.btnVilla).navigate(it) }

        }

    }

    private fun setSuiteDetail(unit: SpaceType)
    {
        binding.tvSpace3.setText(unit.name)
        binding.tvAccom3.setText("Accomodation for " + unit.accomodation)
        binding.tvDescription3.setText(unit.description)
        binding.tvPrice3.setText(unit.start_pirce)
        saveUnitData(unit, Constants.SUITE_SPACE)

        binding.btnSuite.setOnClickListener{
            val action = GuestSpaceFragmentDirections.actionSpaceToDetail(String.format(unit.id),"suite")
            action?.let { Navigation.findNavController(binding.btnSuite).navigate(it) }

        }


    }

    private fun saveUnitData(unit: SpaceType, key: String)
    {
        val gson = Gson()
        val unitJson = gson.toJson(unit)
        prefsHelper.saveData(unitJson,key)

    }


}