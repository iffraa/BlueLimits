package com.app.bluelimits.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentVisitorInviteBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.app.bluelimits.view.AddVisitorsAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.VisitorInviteViewModel
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import com.payfort.fortpaymentsdk.FortSdk
import com.payfort.fortpaymentsdk.callbacks.FortCallBackManager
import com.payfort.fortpaymentsdk.callbacks.FortCallback
import com.payfort.fortpaymentsdk.callbacks.FortInterfaces.OnTnxProcessed
import com.payfort.fortpaymentsdk.domain.model.FortRequest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
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
    private var builder: AlertDialog.Builder? = null
    private var mAlertDialog: AlertDialog? = null
    private var prefsHelper = SharedPreferencesHelper()
    private lateinit var resortId: String
    private var totalVisitors = 0
    private var visitorPolicyPresent = false
    private var visitorListAdapter: AddVisitorsAdapter? = null
    private lateinit var data: Data
    private var packages: HashMap<String, ServicePackage> = hashMapOf()

    // private var fortCallback: FortCallBackManager? = null
    private var paymentHelper: PaymentHelper? = null
    private var navController: NavController? = null
    private var fortCallBackManager: FortCallBackManager? = null

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

        navController = (activity as DashboardActivity).getNavController()

        if (fortCallBackManager == null)
            fortCallBackManager = FortCallBackManager.Factory.create()

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        data = gson.fromJson(data_string, Data::class.java)
        resortId = data.user?.resort_id.toString()

        data.user?.resort?.let { setLogo(it) }

        binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
            showDateDialog(data)
        })

        visitorListAdapter = context?.let { AddVisitorsAdapter(arrayListOf(), it, this) }!!

        navigateToListing();

        binding.btnSubmit.setOnClickListener {

            val date = binding.etVisitorsTime.text.toString()
            val visitors = binding.etVisitorsNum.text.toString()

            if (date.isNullOrEmpty() || visitors.isNullOrEmpty()) {
                showAlertDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    getString(R.string.empty_fields)
                )
            } else if (!isFemalePckgAvailable) {
                showAlertDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    getString(R.string.pckg_unavialable)
                )
                binding.etVisitorsNum.setText("")
            } else if (!visitorListAdapter!!.getData().isNullOrEmpty()) {
                val idMsg = checkVisitorsID(visitorListAdapter!!.getData(), requireContext())
                if (idMsg.isEmpty()) {
                    if (willSenderPay(visitorListAdapter!!.getData())) {
                        val amount = getPayableAmount(visitorListAdapter!!.getData())
                        if (!amount.equals("0")) {
                            payForVisitor()
                        } else {
                            addServerVisitor()
                        }
                    } else {
                        addServerVisitor()
                    }

                } else {
                    showAlertDialog(requireActivity(), getString(R.string.app_name), idMsg)
                }
            }

        }

      /*  if (data.user?.user_type.equals(Constants.admin)) {
            binding.spResorts.visibility = View.VISIBLE
            binding.tvResorts.visibility = View.VISIBLE

            getResorts(data)
        }*/

    }


    private fun addServerVisitor() {
        binding.rlInclude.visibility = View.VISIBLE
        addVisitor()
        observeViewModel()
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
        data.token?.let { viewModel.getCustomerResorts(it, requireContext()) }
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
                            totalVisitors.toString() + " " + getString(R.string.max_visitors)
                        )
                    } else {
                        val no_of_visitors: Int = noOfVisitors.toInt()
                        if (no_of_visitors > 0) {
                            val visitors = ArrayList<Visitor>(no_of_visitors)
                            for (i in 1..no_of_visitors) {
                                val person = Visitor(
                                    "0", "", "", "", "",
                                    null, "", "", "", "",
                                    Constants.UN_PAID
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

    private fun updateList() {
        if (!visitorListAdapter!!.getData().isNullOrEmpty()) {
            val visitors: ArrayList<Visitor> = visitorListAdapter!!.getData()
            val num = binding.etVisitorsNum.text.toString()
            if (visitors.size > 0 && !num.isNullOrEmpty()) {
                for (i in 0 until num.toInt()) {
                    if (i < visitors.size) {
                        val visitor = visitors.get(i)
                        val gender = visitor.gender
                        val servicePackage = packages.get(gender)
                        val price = servicePackage?.price

                        //update list
                        visitor.servicePackage = servicePackage
                        visitor.price = price!!

                        visitorListAdapter!!.notifyItemChanged(i)
                    }
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

    private fun removeDuplicateElemets(visitors: ArrayList<Visitor>): ArrayList<Visitor> {
        val noVisitors = Integer.parseInt(binding.etVisitorsNum.text.toString())
        if (visitors.size > noVisitors)
            return visitors?.distinct() as ArrayList<Visitor>
        else
            return visitors
    }

    private fun addVisitor() {
        var visitors: ArrayList<Visitor> = visitorListAdapter!!.getData()
        //  visitors = removeDuplicateElemets(visitors)
        var (total, subTotal, discount) = getPriceInfo(visitors)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val customDiscount = data.user?.invite_visitor_discount_percentage
        val token = data.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val resortId = data.user?.resort_id.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()

        val errorMsg = visitors?.let { getErrorMsg(it) }
        if (errorMsg.isNullOrEmpty()) {

            binding.scrollView.fullScroll(ScrollView.FOCUS_UP);

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
                errorMsg
            )
        }

    }


    fun observeResortVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showAlertDialog(
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

    private fun observeTotalVisitorsVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.rlInclude.visibility = View.GONE
                if (it) {
                    showAlertDialog(
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
                    val perDay = response.total_allow

                    binding.tvPerDay.visibility = View.VISIBLE
                    binding.tvPerDay.setText("Per Day " + totalVisitors + " visitors are allowed")

                    // adjustLayout()

                    binding.tvTotalVisitors.visibility = View.VISIBLE
                    binding.tvTotalVisitors.setText(
                        "Only " + totalMale.toString() + " " + Constants.MALE + " & " +
                                totalFemale + " " + Constants.FEMALE + " are allowed"
                    )
                }

                viewModel.totalVisitors.removeObservers(viewLifecycleOwner)
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


    fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                binding.rlInclude.visibility = View.GONE
                if (mAlertDialog?.isShowing == true)
                    mAlertDialog?.dismiss()
                showSuccessDialog()
            }

        })

        viewModel.inviteErrorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (!it.isNullOrEmpty()) {
                    binding.rlInclude.visibility = View.GONE
                    val errorMsg =
                        getServerErrors(it, requireContext())
                    showAlertDialog(errorMsg)
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.rlInclude.visibility = if (it) View.VISIBLE else View.GONE

            }
        })
    }

    private fun showDateDialog(data: Data) {

        hideKeyboard(context as Activity)

        val d = Date()
        val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

        dateDialog.title(getString(R.string.select_date))
            .titleTextColor(getResources().getColor(R.color.white))
            .minutesStep(1).mustBeOnFuture()
            // .minDateRange(d).mustBeOnFuture()
            .backgroundColor(getResources().getColor(R.color.white))
            .mainColor(getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_TIME_FORMAT = "yyyy-MM-dd   hh:mm aa"
                val sdf = SimpleDateFormat(DATE_TIME_FORMAT)
                val sdate = sdf.format(date)
                binding.etVisitorsTime.setText(sdate)
                binding.etVisitorsNum.setText("")

                getPackages()


            }.display()

    }

    var isFemalePckgAvailable = true

    private fun getPackages() {

        val visitingDate = binding.etVisitorsTime.text.toString()

        data.token?.let {

            viewModel.getFemalePackage(
                it,
                visitingDate,
                data.user?.resort_id.toString(), requireContext()
            )
            observeFemalePckgVM(it)

            /*  viewModel.getMalePackage(
                  it, visitingDate,
                  data.user?.resort_id.toString(), requireContext()
              )
              observeMalePckgVM()*/


        }
    }


    private fun observeMalePckgVM() {

        viewModel.errorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (!it.isNullOrEmpty() && isFemalePckgAvailable) {
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(errorMsg + ". Cannot add visitor for the selected time.")
                    // Toast.makeText(requireContext(), it + ". Please select another time.", Toast.LENGTH_LONG).show()

                }
            }
        })

        viewModel.malePackage.observe(viewLifecycleOwner, Observer { sPackage ->
            sPackage?.let {

                packages.put(Constants.MALE, sPackage)

                data.token?.let {
                    getTotalVisitors(it, binding.etVisitorsTime.text.toString())

                }

            }

        })

    }

    private fun observeFemalePckgVM(token: String) {

        viewModel.errorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (!it.isNullOrEmpty()) {
                    isFemalePckgAvailable = false
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(errorMsg + ". Cannot add visitor for the selected time.")
                    //Toast.makeText(requireContext(), it + ". Please select another time.", Toast.LENGTH_LONG).show()

                }
            }
        })


        viewModel.femalePackage.observe(viewLifecycleOwner, Observer { sPackage ->
            sPackage?.let {

                viewModel.getMalePackage(
                    token, binding.etVisitorsTime.text.toString(),
                    data.user?.resort_id.toString(), requireContext()
                )
                observeMalePckgVM()


                isFemalePckgAvailable = true
                packages.put(Constants.FEMALE, sPackage)
                Log.i("Fprice", sPackage.price)
                val num = binding.etVisitorsNum.text.toString()

                if (num.isNullOrEmpty())
                    setVisitorList()
                else
                    updateList()

            }

        })

    }

    private fun navigateToListing() {
        val action = VisitorInviteFragmentDirections.actionNavToList()
        action?.let {
            (activity as DashboardActivity).navigateToVisitorsList(action)
        }
    }

    private fun showSuccessDialog() {

        val action = VisitorInviteFragmentDirections.actionNavToList()
        try {
            navController?.navigate(action)
        } catch (e: IllegalArgumentException) {
            // User tried tapping 2 links at once!
        }

        /* if (builder == null) {
             builder = activity?.let {
                 AlertDialog.Builder(it)
             }
         }
         builder?.setMessage(msg)
             ?.setTitle(title)?.setPositiveButton(
                 R.string.ok
             ) { dialog, id ->

             }
         builder?.create()?.show()*/

    }

    private fun adjustLayout() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 200
        params.width = 300

        binding.tvVisitorsLbl.layoutParams = params
        binding.tvVisitorsLbl.textSize = 20F

        val typeface = ResourcesCompat.getFont(requireContext(), R.font.jura_demi_bold)
        binding.tvVisitorsLbl.typeface = typeface
    }


    private fun payForVisitor() {
        val url = "https://sbpaymentservices.payfort.com/FortAPI/"
        //   val url = "https://paymentservices.payfort.com/FortAPI/"

        paymentHelper = PaymentHelper()
        val request = paymentHelper?.getTokenRequest(requireContext())

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PaymentAPI::class.java)

        val repos: Call<PayFortData>? = request?.let {
            service.postRequest(
                it
            )
        }

        repos?.enqueue(object : Callback<PayFortData?> {
            override fun onResponse(call: Call<PayFortData?>?, response: Response<PayFortData?>) {
                val sdkToken = response.body()?.sdk_token

                if (sdkToken != null) {
                    val fortRequest = paymentHelper?.getFortRequest(
                        sdkToken,
                        getPayableAmount(visitorListAdapter!!.getData())
                    )
                    processPayment(fortRequest!!)
                } else {
                    showAlertDialog(
                        "Token not fetched"
                    )
                }
            }

            override fun onFailure(call: Call<PayFortData?>?, t: Throwable?) {
                t?.stackTraceToString()?.let { Log.e("onResponse: ", it) }

            }
        })

    }


    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        fortCallback: FortCallBackManager
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            fortCallBackManager = fortCallback
            fortCallback!!.onActivityResult(requestCode, resultCode, data)
            Log.i("fort", "onActivityResult")
        } catch (e: Exception) {
            Log.i("fort", e.message!!)
        }

    }

    private fun processPayment(fortRequest: FortRequest) {
        try {
            FortSdk.getInstance()
                .registerCallback(requireActivity(),
                    fortRequest,
                    FortSdk.ENVIRONMENT.TEST,
                    paymentHelper?.getRandomNumber()!!,
                    fortCallBackManager,
                    true,
                    object : OnTnxProcessed {
                        override fun onCancel(
                            requestParamsMap: Map<String, Any>,
                            responseMap: Map<String, Any>
                        ) {
                            Log.e("onCancel: ", responseMap.toString())
                            updatePaymentStatus(false)
                            addServerVisitor()

                        }

                        override fun onSuccess(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            Log.e("onSuccess: ", requestParamsMap.toString())
                            if (requestParamsMap.size > 0) {
                                updatePaymentStatus(true)
                                addServerVisitor()

                            }
                        }

                        override fun onFailure(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            Log.e("onFailure: ", requestParamsMap.toString())
                            updatePaymentStatus(false)
                            addServerVisitor()


                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePaymentStatus(isSuccess: Boolean) {
        for (visitor in visitorListAdapter!!.getData()) {
            val payment = visitor.who_will_pay
            if (payment == "sender" && isSuccess) {
                visitor.payment_status = Constants.PAID
            }
        }
        Log.i("visitor", visitorListAdapter!!.getData().toString())
    }


    fun showAlertDialog(msg: String) {
        if (builder == null) {
            builder = activity?.let {
                AlertDialog.Builder(it)
            }
        }
        builder?.setMessage(msg)
            ?.setTitle(getString(R.string.app_name))?.setPositiveButton(
                R.string.ok
            ) { _, _ ->

            }
        mAlertDialog = builder?.create()
        mAlertDialog?.show()
    }


}