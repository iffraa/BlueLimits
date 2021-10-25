package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private lateinit var resortId: String
    private var totalVisitors = 0
    private var visitorPolicyPresent = false
    private lateinit var visitorListAdapter: VisitorListAdapter
    private lateinit var data: Data
    private var packages: HashMap<String, ServicePackage> = hashMapOf()

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
        data = gson.fromJson(data_string, Data::class.java)
        resortId = data.user?.resort_id.toString()

        data.user?.resort?.let { setLogo(it) }

        binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
            showDateDialog(data)
        })

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            binding.rlInclude.visibility = View.VISIBLE
            addVisitor()
            observeViewModel()

        })

        if (data.user?.user_type.equals(Constants.admin)) {
            binding.spResorts.visibility = View.VISIBLE
            binding.tvResorts.visibility = View.VISIBLE

            getResorts(data)
        }

        //   setVisitorList()
    }

    private fun getErrorMsg(visitors: ArrayList<Visitor>): String {
        var msg = ""

        for (visitor in visitors) {
            val name = visitor.name
            if (name.isNullOrEmpty()) {
                msg = getString(R.string.empty_name)
            }
        }

        return msg
    }

    private fun getResorts(data: Data) {
        data.token?.let { viewModel.getCustomerResorts(it) }
        observeResortVM()
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

        binding.rvVisitor.addItemDecoration(
            DividerItemDecoration(
                binding.rvVisitor.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.rvVisitor.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorListAdapter

        }

        binding.etVisitorsNum.textChanges()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val noOfVisitors: String = binding.etVisitorsNum.text.toString()
                if (!noOfVisitors.isEmpty()) {
                    activity?.let { hideKeyboard(it) }

                    if (visitorPolicyPresent && noOfVisitors.toInt() > totalVisitors) {
                        showAlertDialog(
                            context as Activity,
                            getString(R.string.app_name),
                            totalVisitors.toString() + " " + getString(R.string.max_visitors)
                        )
                    } else {
                        val no_of_visitors: Int = noOfVisitors.toInt()
                        if (no_of_visitors > 0) {
                            val visitors = ArrayList<Visitor>(no_of_visitors)
                            for (i in 1..no_of_visitors) {
                                val person = Visitor(
                                    "0", "", "", "", "",
                                    null, "", ""
                                )
                                visitors.add(person)
                            }
                            binding.rvVisitor.visibility = View.VISIBLE
                            binding.btnSubmit.visibility = View.VISIBLE
                            visitorListAdapter?.setVisitorList(
                                visitors, packages
                            )

                        } else {
                            binding.rvVisitor.visibility = View.GONE
                            binding.btnSubmit.visibility = View.GONE

                        }
                    }
                }

            }

    }

   /* private fun updateList() {
        visitorListAdapter.clear()

        val num = binding.etVisitorsNum.text.toString()
        if (!num.isNullOrEmpty()) {
            val no_of_visitors = num.toInt()
            if (no_of_visitors > 0) {
                val visitors = ArrayList<Visitor>(no_of_visitors)
                for (i in 1..no_of_visitors) {
                    val person = Visitor(
                        "0", "", "", "", "",
                        null, "", ""
                    )
                    visitors.add(person)
                }
                visitorListAdapter?.setVisitorList(
                    visitors, packages
                )

            } else {
                binding.rvVisitor.visibility = View.GONE
                binding.btnSubmit.visibility = View.GONE

            }
        }
    }*/

    private fun updateList() {
        if (visitorListAdapter != null) {
            val visitors: ArrayList<Visitor> = visitorListAdapter.getData()
            val num = binding.etVisitorsNum.text.toString()
            if (visitors.size > 0 && !num.isNullOrEmpty()) {
                for (i in 0 until num.toInt()) {
                    val visitor = visitors.get(i)
                    val gender = visitor.gender
                    val servicePackage = packages.get(gender)
                    val price = servicePackage?.price

                    //update list
                    visitor.servicePackage = servicePackage
                    visitor.price = price!!

                    visitorListAdapter.notifyItemChanged(i)
                }

            }
        }

    }

    private fun getPriceInfo(visitors: ArrayList<Visitor>): Triple<String, String, String> {
        var discount = 0
        var total = 0
        var subTotal = 0
        val serverDiscount = data.user?.invite_visitor_discount_percentage
        var customDiscount: Int = serverDiscount?.toInt()!!

        for (visitor in visitors) {
            val servicePackage = visitor.servicePackage
            val price = servicePackage?.price
            subTotal += price?.toInt() ?: 0
        }

        if (customDiscount != 0) {
            discount = (customDiscount / 100) * subTotal
        }
        total = subTotal - discount
        return Triple(total.toString(), subTotal.toString(), discount.toString())
    }

    fun addVisitor() {
        val visitors: ArrayList<Visitor> = visitorListAdapter.getData()
        var (total, subTotal, discount) = getPriceInfo(visitors)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val customDiscount = data.user?.invite_visitor_discount_percentage
        val token = data.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val resortId = data.user?.resort_id.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()

        val errorMsg = visitors?.let { getErrorMsg(it) }
        if (errorMsg.isNullOrEmpty()) {

            hideKeyboard(requireActivity())

            val visitorReq = visitors?.let {
                VisitorRequest(
                    no_of_visitors,
                    resortId,
                    visiting_date_time,
                    customDiscount,
                    subTotal,
                    discount,
                    total,
                    it
                )
            }

            token?.let { viewModel.addVisitor(it, visitorReq, requireContext()) }

        } else {
            showAlertDialog(
                context as Activity,
                getString(R.string.app_name), errorMsg
            )
        }

    }


    fun observeResortVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.units_loading_error)
                    )
                }
            }
        })

        viewModel.resorts.observe(viewLifecycleOwner, Observer { resorts ->
            binding.progressBar.progressbar.visibility = View.GONE
            resorts?.let {
                populateSpinner()
            }

        })
    }

    private fun getTotalVisitors(token: String, date: String) {
        binding.rlInclude.visibility = View.VISIBLE
        viewModel.getTotalVisitors(token, date, resortId)
        observeTotalVisitorsVM()
    }

    fun observeTotalVisitorsVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.rlInclude.visibility = View.GONE
                if (it) {
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.units_loading_error)
                    )
                }
            }
        })

        viewModel.totalVisitors.observe(viewLifecycleOwner, Observer { total ->
            binding.rlInclude.visibility = View.GONE
            total?.let {
                val response = it.data
                visitorPolicyPresent = response.visitor_policy_exist
                if (visitorPolicyPresent) {
                    totalVisitors = response.each_time_limit
                    val totalMale = response.male
                    val totalFemale = response.female

                    binding.tvTotalVisitors.visibility = View.VISIBLE
                    binding.tvTotalVisitors.setText(
                        totalVisitors.toString() + " " + getString(R.string.max_visitors)
                                + ": " + totalMale + " " + Constants.MALE + " & " + totalFemale + " " + Constants.FEMALE
                    )
                }

                viewModel.totalVisitors.removeObservers(viewLifecycleOwner)
                getPackages()
            }

        })
    }

    private fun populateSpinner() {

        var data: java.util.ArrayList<String>? = arrayListOf<String>()

        viewModel.resorts.value!!.forEachIndexed { index, e ->
            viewModel.resorts.value!!.get(index).name?.let {
                if (data != null) {
                    data.add(it)
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item,
                data as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spResorts.adapter = adapter
                    onResortSelection()

                }
        }


    }

    private fun onResortSelection() {
        binding.spResorts?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                viewModel.resorts.value!!.forEachIndexed { index, e ->
                    if (viewModel.resorts.value!!.get(index).name?.equals(selectedItem) == true) {
                        resortId = viewModel.resorts.value!!.get(index).id.toString()
                    }

                }
            }


        }

    }


    fun getDateTime(): String {
        val dateTime = binding.etVisitorsTime.text.toString()

        if (dateTime.isNullOrEmpty()) {
            activity?.let {
                showAlertDialog(
                    it,
                    getString(R.string.app_name),
                    getString(R.string.missing_date)
                )
            }
        } else {
            return dateTime
        }

        return ""
    }

    fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                binding.rlInclude.visibility = View.GONE
                showAlertDialog(
                    context as Activity,
                    requireContext().getString(R.string.app_name),
                    msg
                )
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

    fun showDateDialog(data: Data) {

        hideKeyboard(context as Activity)

        val d = Date()
        val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

        dateDialog.title(getString(R.string.select_date))
            .titleTextColor(getResources().getColor(R.color.white))
            .minutesStep(1)
            .minDateRange(d)
            .backgroundColor(getResources().getColor(R.color.white))
            .mainColor(getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm aa"
                val sdf = SimpleDateFormat(DATE_TIME_FORMAT)
                val sdate = sdf.format(date)
                binding.etVisitorsTime.setText(sdate)

                data.token?.let {
                    getTotalVisitors(it, sdate)

                }
            }.display()

    }

    private fun getPackages() {

        val visitingDate = binding.etVisitorsTime.text.toString()

        data.token?.let {
            viewModel.getMalePackage(
                it, visitingDate,
                data.user?.resort_id.toString()
            )
            observeMalePckgVM()


            viewModel.getFemalePackage(
                it,
                visitingDate,
                data.user?.resort_id.toString()
            )
            observeFemalePckgVM()

        }
    }


    private fun observeMalePckgVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    showAlertDialog(
                        requireActivity(),
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })

        viewModel.malePackage.observe(viewLifecycleOwner, Observer { sPackage ->
            sPackage?.let {

                packages.put(Constants.MALE, sPackage)

           //     viewModel.malePackage.removeObservers(viewLifecycleOwner)

            }

        })

    }

    private fun observeFemalePckgVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    showAlertDialog(
                        requireActivity(),
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })

        viewModel.femalePackage.observe(viewLifecycleOwner, Observer { sPackage ->
            sPackage?.let {

                packages.put(Constants.FEMALE, sPackage)

                val num = binding.etVisitorsNum.text.toString()

                 if(num.isNullOrEmpty())
                     setVisitorList()
                 else
                     updateList()

            }

        })

    }

}