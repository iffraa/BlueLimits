package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentResortFacilitiesBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.model.ServicePackage
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.PackageListAdapter
import com.app.bluelimits.view.RoleListAdapter
import com.app.bluelimits.viewmodel.HomeViewModel
import com.app.bluelimits.viewmodel.ResortFacilityViewModel
import android.content.Intent
import android.view.MotionEvent
import android.widget.AdapterView

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResortFacilitiesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResortFacilitiesFragment : Fragment() {

    private var _binding: FragmentResortFacilitiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var type: Resort
    private lateinit var roleListAdapter: RoleListAdapter
    private lateinit var viewModel: ResortFacilityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            type = it.getParcelable<Resort>(Constants.TYPE)!!
        }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResortFacilitiesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLogo()

        viewModel = ViewModelProvider(this).get(ResortFacilityViewModel::class.java)
        viewModel.getUserRoles()
        observeViewModel()

        /*  binding.btnGhouse.setOnClickListener(View.OnClickListener {
              val action = ResortFacilitiesFragmentDirections.actionResortFacilitiesFragToUnitFormFragment(type,getString(R.string.guest_house))
              Navigation.findNavController(it).navigate(action)
          })*/

    }

    fun observeViewModel() {
        viewModel.roles.observe(viewLifecycleOwner, Observer { roles ->
            binding.progressBar.progressbar.visibility = View.GONE
            roles?.let {
                viewModel.roles.value?.let { it1 -> setRolesList(roles)
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

    private fun setRolesList(roles: ArrayList<Resort>) {

        roleListAdapter = RoleListAdapter(arrayListOf(),requireContext())

        binding.rvRoles.visibility = View.VISIBLE
        binding.rvRoles.apply {
            layoutManager = LinearLayoutManager(context)

            adapter = roleListAdapter
            roleListAdapter.setRoleList(roles,type)


        }


    }


    private fun setLogo()
    {
        when{
            type.name?.contains(Constants.OIA) == true ->  setLayout(R.drawable.oia_logo)
            type.name?.contains(Constants.BOHO) == true -> setLayout(R.drawable.boho_logo)
            type.name?.contains(Constants.MARINE) == true -> setLayout(R.drawable.marine_logo)
        }
    }


    private fun setLayout(logo_img: Int)
    {
        binding.ivLogo.setImageResource(logo_img)

    }
}