package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.app.bluelimits.R

import com.app.bluelimits.databinding.FragmentUpdatePwdBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.PwdUpdateReq
import com.app.bluelimits.util.*
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.UpdatePwdViewModel
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdatePwdFragment : Fragment() {

    private lateinit var binding: FragmentUpdatePwdBinding
    private lateinit var viewModel: UpdatePwdViewModel
    private var user_type = Constants.customer;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUpdatePwdBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomeNavigation(context as Activity, AboutUsFragmentDirections.actionNavToHome())

        viewModel = ViewModelProvider(this).get(UpdatePwdViewModel::class.java)

        binding.etConfirm.removeUnderline()
        binding.etPwd.removeUnderline()

        binding.btnUpdate.setOnClickListener {

            if (context?.let { it1 -> ConnectivityUtils.isConnected(it1) } == true) {

                val pwd = binding.etPwd.text.toString()
                val confirmPwd = binding.etConfirm.text.toString()


                if (!pwd.isNullOrEmpty() && !confirmPwd.isNullOrEmpty()) {

                    if (pwd == confirmPwd) {

                        hideKeyboard(requireActivity())
                        binding.rlInclude.visibility = View.VISIBLE


                        val data = getRequest(pwd ,confirmPwd)

                        viewModel.updatePwd(data.first!!, data.second)
                        observeViewModel(it)
                    }
                    else
                    {
                        activity?.let { it1 ->
                            showSuccessDialog(
                                it1,
                                getString(R.string.app_name),
                                getString(R.string.pwd_match_error)
                            )
                        }
                    }
                  } else {
                      activity?.let { it1 ->
                          showSuccessDialog(
                              it1,
                              getString(R.string.app_name),
                              getString(R.string.empty_fields)
                          )
                      }
                  }
            } else {
                activity?.let { it1 ->
                    showSuccessDialog(
                        it1,
                        getString(R.string.app_name),
                        getString(R.string.connectivity_error)
                    )
                }
            }
        }

    }

    private fun getRequest(pwd: String, confirmPwd: String): Pair<String?, PwdUpdateReq>
    {
        val prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val obj: Data = gson.fromJson(data_string, Data::class.java)

        val token = obj.token
        val requestBody = PwdUpdateReq(pwd,confirmPwd)

        return Pair(token, requestBody)
    }


    fun observeViewModel(view: View) {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.pwd_change_error)
                    )
                }
            }
        })

        viewModel.response.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
               // navigateToLogin(view)
                binding.rlInclude.visibility = View.GONE

                showSuccessDialog(
                    context as Activity,
                    getString(R.string.app_name), it.message
                )
            }

        })

    }

    private fun navigateToLogin(view: View) {

        activity?.let { hideKeyboard(it) }
        (activity as DashboardActivity).makeUserDashboardStart()

        val action = UpdatePwdFragmentDirections.actionNavToLogin()
        action?.let { Navigation.findNavController(view).navigate(it) }
    }


}