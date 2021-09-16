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
import com.app.bluelimits.databinding.FragmentVisitorInviteBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.ServicePackage
import com.app.bluelimits.model.Visitor
import com.app.bluelimits.model.VisitorRequest
import com.app.bluelimits.util.*
import com.app.bluelimits.view.VisitorListAdapter
import com.app.bluelimits.viewmodel.VisitorInviteViewModel
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VisitorInviteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorInviteFragment : Fragment() {

    private lateinit var binding: FragmentVisitorInviteBinding
    private lateinit var viewModel: VisitorInviteViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private lateinit var resort_id: String
    private lateinit var visitorListAdapter: VisitorListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVisitorInviteBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(VisitorInviteViewModel::class.java)
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data: Data = gson.fromJson(data_string, Data::class.java)
        resort_id = data.user?.resort_id.toString()

        data.user?.resort?.let { setLogo(it) }

        binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
            showDateTime(requireContext(),binding.etVisitorsTime)
        })

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            binding.rlInclude.visibility = View.VISIBLE
            addVisitor()
            observeViewModel()
        })

        setVisitorList()
    }

    private fun setLogo(resort_name: String) {
        when {
            resort_name?.contains(Constants.OIA) == true -> binding.ivLogo.setImageResource(R.drawable.oia_logo)
            resort_name?.contains(Constants.BOHO) == true -> binding.ivLogo.setImageResource(R.drawable.boho_logo)
            resort_name?.contains(Constants.MARINE) == true -> binding.ivLogo.setImageResource(R.drawable.marine_logo)
        }

    }

    private fun setVisitorList() {

        visitorListAdapter = context?.let { VisitorListAdapter(arrayListOf(), it, this) }!!

        binding.rvVisitor.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorListAdapter
        }

        binding.rvVisitor.addItemDecoration(
            DividerItemDecoration(
                binding.rvVisitor.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etVisitorsNum.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val noOfVisitors: String = binding.etVisitorsNum.text.toString()
                if (!noOfVisitors.isEmpty()) {

                        activity?.let { hideKeyboard(it) }

                        val no_of_visitors: Int = noOfVisitors.toInt()
                        if (no_of_visitors > 0) {
                            val visitors = ArrayList<Visitor>(no_of_visitors)
                            for (i in 1..no_of_visitors) {
                                val person = Visitor("0", "", "", "", "", "")
                                visitors.add(person)
                            }
                            binding.rvVisitor.visibility = View.VISIBLE
                            binding.btnSubmit.visibility = View.VISIBLE
                            visitorListAdapter?.setVisitorList(
                                visitors
                            )

                        } else {
                            binding.rvVisitor.visibility = View.GONE
                            binding.btnSubmit.visibility = View.GONE

                        }
                    }


            }

    }

    fun addVisitor()
    {
        val visitors: ArrayList<Visitor>? = visitorListAdapter.getData()
        val servicePackage: ServicePackage = visitorListAdapter.getPackage()

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        val data: Data = gson.fromJson(data_string, Data::class.java)

        val token = data.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val resort_id = data.user?.resort_id.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()
        val sub_total = ""
        val total_price = servicePackage.price
        val discount_percentage = servicePackage.discount_percentage
        val visitorReq = visitors?.let {
            VisitorRequest(no_of_visitors,resort_id,visiting_date_time,discount_percentage,sub_total,"",total_price,
                it
            )
        }

        token?.let { viewModel.addVisitor(it,visitorReq, requireContext()) }
    }




    fun getDateTime(): String
    {
        val dateTime = binding.etVisitorsTime.text.toString()

        if (dateTime.isNullOrEmpty()) {
            activity?.let { showAlertDialog(it,getString(R.string.app_name),getString(R.string.missing_date)) }
        }
        else
        {
            return dateTime
        }

        return ""
    }

    fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                binding.rlInclude.visibility = View.GONE
                showAlertDialog(context as Activity, requireContext().getString(R.string.app_name), msg)
            }

        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.add_visitor_error)
                    )
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
               binding.rlInclude.visibility = if (it) View.VISIBLE else View.GONE

            }
        })


    }


}