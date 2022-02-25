package com.app.bluelimits.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentHomeBinding
import com.app.bluelimits.databinding.FragmentUserDashboardBinding
import com.app.bluelimits.model.User
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.loadImage
import com.app.bluelimits.view.activity.DashboardActivity as DashboardActivity
import android.R.string
import android.app.Activity
import android.text.TextUtils.split
import com.app.bluelimits.model.Data
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.setHomeNavigation
import com.google.gson.Gson


/**
 * A simple [Fragment] subclass.
 * Use the [UserDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserDashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var user_data: User? = null
    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user_data = it.getParcelable("user_data")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         (activity as DashboardActivity).changeMenuIcon()
        setHomeNavigation(context as Activity, UserDashboardFragmentDirections.actionNavToHome())

        setUserData()
        setProgressBar()
    }

    private fun setProgressBar() {
        var remainingDays = user_data?.contract_remaining_days
        if (!remainingDays.isNullOrEmpty()) {
            if(remainingDays.equals("today"))
            {
                remainingDays = "0"
            }
            val remaining_days: Int? = remainingDays?.toInt()
            binding.pbDays.max = Constants.CONTRACT_TOTAL_DAYS
            binding.pbDays.progress = remaining_days!!
        }
    }


    private fun setUserData() {
        if(user_data == null)
        {
            val prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

            val data_string = prefsHelper.getData(Constants.USER_DATA)
            val gson = Gson()
            val data = gson.fromJson(data_string, Data::class.java)
            user_data = data.user

        }

        binding.tvName.setText(user_data!!.name)
        user_data?.profile_image?.let {
            context?.let { it1 ->
                loadImage(
                    binding.ivProfile,
                    it,
                    it1
                )
            }
        }

        binding.tvRole.setText(user_data?.resort + " - " + user_data?.role)
        user_data?.total_invitees?.let { binding.tvInviteNo.setText(it.toString()) }

        if (user_data!!.user_type.equals(Constants.admin)) {
            binding.rlProgress.visibility = View.GONE
            binding.rlContract.visibility = View.GONE
            binding.rlPoints.visibility = View.GONE
        } else {
            var rDays = user_data?.contract_remaining_days
            if (!rDays.isNullOrEmpty()) {
                if(rDays.equals("today"))
                    rDays ="0"
                binding.tvProgress.setText(rDays + "\nDAYS" )
            }
            binding.tvTotalPoints.setText(user_data?.loyalty_points + " " + getString(R.string.points))

            val date = user_data?.contract_end_date // "2021-07-12"
            if (!date.isNullOrEmpty()) {
                var strs = date.split("-").toTypedArray()

                if (date?.contains("/") == true) {
                    strs = date.split("/").toTypedArray()
                }
                else if (date?.contains(" ") == true) {
                    strs = date.split(" ").toTypedArray()
                }

                binding.tvExpiryDate.setText(strs[0])
                binding.tvExpiryMnth.setText(strs[1])
                binding.tvExpiryYr.setText(strs[2])

            }
        }

    }

}

