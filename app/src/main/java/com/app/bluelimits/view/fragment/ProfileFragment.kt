package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentProfileBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.FamilyMember
import com.app.bluelimits.model.User
import com.app.bluelimits.util.*
import com.app.bluelimits.view.FMemberListAdapter
import com.app.bluelimits.viewmodel.ProfileViewModel
import com.google.gson.Gson

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private var prefsHelper = SharedPreferencesHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomeNavigation(context as Activity, ProfileFragmentDirections.actionNavToHome())

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val obj: Data = gson.fromJson(data_string, Data::class.java)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        obj.token?.let { viewModel.getProfile(it) }
        observeViewModel()

    }

    fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                setProfileDetails(user)
            }

        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.progressbar.visibility = View.GONE
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.login_error)
                    )
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })


    }

    private fun setProfileDetails(user: User)
    {
        context?.let {
            user.profile_image?.let { it1 -> loadImage(binding.ivProfile, it1, it) }
            user.qr_code?.let { it1 -> loadImage(binding.ivBarcode, it1, it) }
        }

        binding.tvName.setText(user.name)
        binding.tvRole.setText(user?.resort + " - " + user?.role)
        binding.tvId.setText(getString(R.string.id_no) + " " + user.id.toString())
        binding.tvMobile.setText(getString(R.string.mobile) + " " + user.contact_no)
        binding.tvEmail.setText(getString(R.string.email_profile) + " " + user.email)

        if(user.user_type.equals(Constants.admin)) {
            binding.llFamily.visibility = View.GONE
            binding.llExtra.visibility = View.GONE
            binding.llUnit.visibility = View.GONE
            binding.ivBarcode.visibility = View.GONE

        }
        else
        {
            binding.tvExtra.setText(getString(R.string.extra_mems) + " " + user.no_of_extra_family_member.toString())
            binding.tvFamily.setText(getString(R.string.total_mems) + " " + user.no_of_family_member.toString())
            binding.tvUnit.setText(getString(R.string.unit) + " " + user.unit_no)
            context?.let { user.qr_code?.let { it1 -> loadImage(binding.ivBarcode, it1, it) } }

        }

        user.members?.let { it1 -> setFamilyList(it1) }

        setExtraFamilyList(user.extra_members)
    }

    private fun setExtraFamilyList(familyMembers: ArrayList<FamilyMember>) {
        binding.rvExtraFam.visibility = View.VISIBLE
        binding.btnExtraFamily.visibility = View.VISIBLE

        binding.rvExtraFam.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvExtraFam.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            val famListAdapter = FMemberListAdapter(arrayListOf())
            adapter = famListAdapter
            famListAdapter.setFamilyList(familyMembers)
        }

    }

    private fun setFamilyList(familyMembers: ArrayList<FamilyMember>) {
        binding.rvFam.visibility = View.VISIBLE
        binding.btnFamily.visibility = View.VISIBLE

        binding.rvFam.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvFam.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            val famListAdapter = FMemberListAdapter(arrayListOf())
            adapter = famListAdapter
            famListAdapter.setFamilyList(familyMembers)
        }

    }
}