package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentProfileBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.User
import com.app.bluelimits.util.*
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
                    showAlertDialog(
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
        binding.tvRole.setText(user.role)
        binding.tvId.setText(getString(R.string.id_no) + " " + user.id.toString())
        binding.tvMobile.setText(getString(R.string.mobile) + " " + user.contact_no)
        binding.tvEmail.setText(getString(R.string.email_profile) + " " + user.email)

        if(user.user_type.equals(Constants.admin)) {
            binding.llFamily.visibility = View.GONE
            binding.llUnit.visibility = View.GONE
            binding.ivBarcode.visibility = View.GONE

        }
        else
        {
            binding.tvFamily.setText(user.no_of_family_member.toString())
            binding.tvUnit.setText(user.unit_no)
            context?.let { user.qr_code?.let { it1 -> loadImage(binding.ivBarcode, it1, it) } }

        }

    }
}