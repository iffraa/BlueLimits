package com.app.bluelimits.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentVisitorEditBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.app.bluelimits.view.EditVisitorAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.VisitorEditViewModel
import com.app.bluelimits.viewmodel.VisitorInviteViewModel
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import com.payfort.fortpaymentsdk.FortSdk
import com.payfort.fortpaymentsdk.callbacks.FortCallBackManager
import com.payfort.fortpaymentsdk.callbacks.FortInterfaces
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
import kotlin.concurrent.schedule

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VisitorInviteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorEditFragment : Fragment() {

    private lateinit var binding: FragmentVisitorEditBinding
    private lateinit var viewModel: VisitorInviteViewModel
    private lateinit var updateVM: VisitorEditViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private var builder: AlertDialog.Builder? = null
    private var mAlertDialog: AlertDialog? = null
    private lateinit var resortId: String
    private var totalVisitors = 0
    private var visitorPolicyPresent = false
    private lateinit var visitorListAdapter: EditVisitorAdapter
    private lateinit var data: Data
    private var packages: HashMap<String, ServicePackage> = hashMapOf()
    private lateinit var visitorsData: VisitorResult
    private var isNewVisitor: Boolean = false
    private var paymentHelper: PaymentHelper? = null
    private var navController: NavController? = null
    private var fortCallBackManager: FortCallBackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            visitorsData = it.getParcelable("vDetails")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVisitorEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(VisitorInviteViewModel::class.java)
        updateVM = ViewModelProvider(this).get(VisitorEditViewModel::class.java)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        data = gson.fromJson(data_string, Data::class.java)
        resortId = data.user?.resort_id.toString()

        data.user?.resort?.let { setLogo(it) }

            binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
                showDateDialog(data)
            })

        if (fortCallBackManager == null)
            fortCallBackManager = FortCallBackManager.Factory.create()

        navigateToListing();

        binding.btnSubmit.setOnClickListener(View.OnClickListener {

            val date = binding.etVisitorsTime.text.toString()
            val visitors = binding.etVisitorsNum.text.toString()

            if (date.isNullOrEmpty() || visitors.isNullOrEmpty()) {
                showAlertDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    getString(R.string.empty_fields)
                )
            } else {
                val idMsg = checkVisitorsID(visitorListAdapter.getData(), requireContext())
                if (idMsg.isEmpty()) {

                    if (willSenderPay(visitorListAdapter!!.getData())) {
                        val amount =getPayableAmount(visitorListAdapter!!.getData())
                        if (!amount.equals("0")) {
                            payForVisitor()
                        } else {
                            updateServerVisitor()
                        }
                    } else {
                        updateServerVisitor()
                    }
                }
                else
                {
                    showAlertDialog(requireActivity(), getString(R.string.app_name), idMsg)
                }

            }

        })

        populateDetails()
        setHomeNavigation(context as Activity, VisitorEditFragmentDirections.actionNavToHome())
    }

    fun willSenderPay(visitors: ArrayList<VisitorDetail>): Boolean {
        for (visitor in visitors) {
            val payment = visitor.who_will_pay
            if (payment == "sender") {
                return true
            }
        }

        return false
    }


    private fun updateServerVisitor()
    {
        binding.rlInclude.visibility = View.VISIBLE
        updateVisitors()
        observeViewModel()
    }

    private fun checkVisitorsID(visitors: ArrayList<VisitorDetail>, context: Context): String
    {
        for(visitor in visitors)
        {
            val id = visitor.id_no
            if(!isValidID(id!!))
                return context.getString(R.string.id_length_error)
        }

        return ""
    }


    private fun populateDetails() {
        val date = visitorsData.visiting_date_time
        val noOfVisitors = visitorsData.no_of_visitor

        binding.etVisitorsTime.setText(date)
        binding.etVisitorsNum.setText(noOfVisitors)
        binding.btnSubmit.setText("EDIT")

        if(resortId.isNullOrEmpty() || resortId == "null")
            resortId = visitorsData.resort_id!!

        setVisitorList()

        data.token?.let { date?.let { it1 -> getTotalVisitors(it, it1) } }

    }

    private fun getErrorMsg(visitors: ArrayList<VisitorDetail>): String {
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
        data.token?.let { viewModel.getCustomerResorts(it,requireContext()) }
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

        visitorListAdapter = context?.let { EditVisitorAdapter(arrayListOf(), it, this) }!!

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

                    /*  if (visitorPolicyPresent && noOfVisitors.toInt() > totalVisitors) {
                          showAlertDialog(requireActivity(),
                              getString(R.string.app_name),
                              totalVisitors.toString() + " " + getString(R.string.max_visitors)
                          )
                      } else {*/
                    val no_of_visitors: Int = noOfVisitors.toInt()
                    if (no_of_visitors > 0) {

                        binding.rvVisitor.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        visitorsData.visitors?.let {
                            visitorListAdapter?.setVisitorList(
                                it, packages
                            )
                        }

                        /*  } else {
                              binding.rvVisitor.visibility = View.GONE
                              binding.btnSubmit.visibility = View.GONE

                          }*/
                    }
                }

            }

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

                getPackages()


            }.display()

    }


    private fun updateList() {
        if (visitorListAdapter != null) {
            val visitors: ArrayList<VisitorDetail> = visitorListAdapter.getData()
            //  val visitors = visitorsData.visitors
            val num = visitors.size//binding.etVisitorsNum.text.toString()
            if (visitors?.size!! > 0) {
                for (i in 0 until num.toInt()) {
                    val visitor = visitors.get(i)
                    val gender = visitor.gender
                    val servicePackage = packages.get(gender)
                    val price = servicePackage?.price

                    //update list
                    if(visitor != null) {
                        visitor.servicePackage = servicePackage
                        visitor.price = price!!

                        visitorListAdapter.notifyItemChanged(i)
                    }
                }

            }
        }

    }

    private fun getPriceInfo(visitors: ArrayList<VisitorDetail>): Triple<String, String, String> {
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

    private fun setWhoWillPay()
    {
        val visitors: ArrayList<VisitorDetail> = visitorListAdapter.getData()
        for(visitor in visitors)
        {
            val whoWillPay = visitor.who_will_pay
            if(whoWillPay == "I will pay")
            {
                visitor.who_will_pay = "sender"
            }
            else if(whoWillPay == "Visitor will pay")
            {
                visitor.who_will_pay = "visitor"
            }
        }

    }


    fun updateVisitors() {
        var visitors: ArrayList<VisitorDetail> = visitorListAdapter.getData()
        setWhoWillPay()
        var (total, subTotal, discount) = getPriceInfo(visitors)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val customDiscount = data.user?.invite_visitor_discount_percentage
        val token = data.token
        val no_of_visitors = binding.etVisitorsNum.text.toString()
        val visiting_date_time = binding.etVisitorsTime.text.toString()

        val errorMsg = visitors?.let { getErrorMsg(it) }
        if (errorMsg.isNullOrEmpty()) {

            binding.scrollView.fullScroll(ScrollView.FOCUS_UP);

            hideKeyboard(requireActivity())

            val visitorReq = visitors?.let {
                EditVisitorRequest(
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

            token?.let {
                updateVM.updateVisitor(
                    it,
                    visitorReq,
                    requireContext(),
                    visitorsData.id.toString()
                )
            }

        } else {
            showAlertDialog(
                requireActivity(),
                getString(R.string.app_name), errorMsg
            )
        }

    }


    private fun observeResortVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showAlertDialog(
                        requireActivity(),
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
                        requireActivity(),
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
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


    fun showAlertDialog(title: String, msg: String) {
        if (builder == null) {
            builder = activity?.let {
                AlertDialog.Builder(it)
            }

            builder?.setMessage(msg)
                ?.setTitle(title)?.setPositiveButton(
                    R.string.ok
                ) { dialog, id ->

                }
            mAlertDialog = builder?.create()
            mAlertDialog?.show()
        }
    }


    fun observeViewModel() {
        updateVM.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                binding.rlInclude.visibility = View.GONE
                try {
                    val action = VisitorEditFragmentDirections.actionNavToList()
                    Navigation.findNavController(binding.btnSubmit).navigate(action)
                }
                catch (e: Exception){}

            }

        })

        updateVM.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(
                        requireActivity(),
                        getString(R.string.app_name),
                        "Sorry. Cannot update."
                    )
                }
            }
        })

        updateVM.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.rlInclude.visibility = if (it) View.VISIBLE else View.GONE

            }
        })
    }

    private fun getPackages() {

        val visitingDate = binding.etVisitorsTime.text.toString()

        data.token?.let {
            viewModel.getMalePackage(
                it, visitingDate,
                data.user?.resort_id.toString(), requireContext()
            )
            observeMalePckgVM()


            viewModel.getFemalePackage(
                it,
                visitingDate,
                data.user?.resort_id.toString(),requireContext()
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

              /*  val num = binding.etVisitorsNum.text.toString()

                if (num.isNullOrEmpty())
                    setVisitorList()
                else
                    updateList()*/

            }

        })

    }

    private fun navigateToListing() {
        if (mAlertDialog?.isShowing == true)
            mAlertDialog!!.dismiss()

        val action = VisitorEditFragmentDirections.actionNavToList()

        if (action != null &&
            Navigation.findNavController(binding.btnSubmit).currentDestination?.id == R.id.nav_edit
            && Navigation.findNavController(binding.btnSubmit).currentDestination?.id != action.actionId
        ) {
            (activity as DashboardActivity).navigateToVisitorsList(action)
        } else {
            Timer().schedule(2000) {
                (activity as DashboardActivity).navigateToVisitorsList(action)
            }
        }



    }
    //-----------------payment methods------------------

    private fun payForVisitor() {
           val url = "https://sbpaymentservices.payfort.com/FortAPI/"
      //  val url = "https://paymentservices.payfort.com/FortAPI/"

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

    fun showAlertDialog(msg: String) {
        if (builder == null) {
            builder = activity?.let {
                AlertDialog.Builder(it)
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
                    object : FortInterfaces.OnTnxProcessed {
                        override fun onCancel(
                            requestParamsMap: Map<String, Any>,
                            responseMap: Map<String, Any>
                        ) {
                            Log.e("onCancel: ", responseMap.toString())
                            updatePaymentStatus(false)
                            updateVisitors()

                        }

                        override fun onSuccess(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            Log.e("onSuccess: ", requestParamsMap.toString())
                            if (requestParamsMap.size > 0) {
                                updatePaymentStatus(true)
                                updateVisitors()

                            }
                        }

                        override fun onFailure(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            Log.e("onFailure: ", requestParamsMap.toString())
                            updatePaymentStatus(false)
                            updateServerVisitor()


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


    fun getPayableAmount(visitors: ArrayList<VisitorDetail>): String {
        var price = 0
        for (visitor in visitors) {
            if (!visitor.price.isNullOrEmpty()) {
                val payment = visitor.who_will_pay
                if (payment == "sender") {
                    val amount = visitor.price!!.toInt()
                    price += amount
                }
            }
        }
        return price.toString()
    }


}