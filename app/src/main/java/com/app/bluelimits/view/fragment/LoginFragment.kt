package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentLoginBinding
import com.app.bluelimits.viewmodel.LoginViewModel

import androidx.navigation.NavOptions
import com.app.bluelimits.model.User
import com.app.bluelimits.util.*
import com.app.bluelimits.view.activity.DashboardActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var user_type = Constants.customer;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.ivBg?.let { context?.let { it1 -> loadGif(it, R.raw.white_bg, it1) } }

        binding.etEmail.removeUnderline()
        binding.etPwd.removeUnderline()

        setUserType()

        binding.btnLogin.setOnClickListener(View.OnClickListener {

            if (context?.let { it1 -> ConnectivityUtils.isConnected(it1) } == true) {
                binding.etEmail.setText("warsi@gmail.com")
             //    binding.etEmail.setText("test@gmail.com")
                binding.etPwd.setText("12345678")

                if (!binding.etEmail.text.isEmpty() && !binding.etPwd.text.isEmpty()) {
                    binding.rlInclude.visibility = View.VISIBLE
                    binding.progressBar.progressbar.visibility = View.VISIBLE

                    viewModel.loginUser(
                        binding.etEmail.text.toString(),
                        binding.etPwd.text.toString(),
                        user_type
                    )
                    observeViewModel(it)
                } else {
                    activity?.let { it1 ->
                        showAlertDialog(
                            it1,
                            getString(R.string.app_name),
                            getString(R.string.empty_fields)
                        )
                    }
                }
            } else {
                activity?.let { it1 ->
                    showAlertDialog(
                        it1,
                        getString(R.string.app_name),
                        getString(R.string.connectivity_error)
                    )
                }
            }
        })

    }

    private fun setUserType(): String {

        binding.cbAdmin.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbCustomer.setChecked(false)
                user_type = Constants.admin
            }

        })

        binding.cbCustomer.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.cbAdmin.setChecked(false)
                user_type = Constants.customer
            }
        })
        return user_type
    }

    fun observeViewModel(view: View) {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    Constants.isLoggedIn = false
                    binding.rlInclude.visibility = View.GONE
                    binding.progressBar.progressbar.visibility = View.GONE
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
                binding.rlInclude.visibility = if (it) View.VISIBLE else View.GONE
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                Constants.isLoggedIn = true

                setPermissions(user)
                openUserDashboard(view)
            }

        })

    }

    private fun openUserDashboard(view: View) {

        activity?.let { hideKeyboard(it) }
        (activity as DashboardActivity).makeUserDashboardStart()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_login, true)
            .build()

        val action = viewModel.user.value?.let {
            LoginFragmentDirections.actionNavLoginFragToUserDashboardFragment(
                it
            )
        }
        action?.let { Navigation.findNavController(view).navigate(it, navOptions) }
    }

    fun EditText.removeUnderline() {
        val paddingBottom = this.paddingBottom
        val paddingStart = ViewCompat.getPaddingStart(this)
        val paddingEnd = ViewCompat.getPaddingEnd(this)
        val paddingTop = this.paddingTop
        ViewCompat.setBackground(this, null)
        ViewCompat.setPaddingRelative(this, paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    private fun setPermissions(user: User)
    {
        val permissions = user.permissions
        var guestCheck  = false
        var visitorCheck = false

        for(value in permissions)
        {
            val permission = value.name
            if(!guestCheck)
                guestCheck = Constants.PERMISSION_GUEST in permission

            if(!visitorCheck)
                visitorCheck = Constants.PERMISSION_VISITOR in permission
        }

        (activity as DashboardActivity).changeLoginDisplay(true)

        (activity as DashboardActivity).setPermissions(guestCheck, true)
        (activity as DashboardActivity).setPermissions(visitorCheck, false)
    }

}