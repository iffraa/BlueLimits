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
import androidx.navigation.Navigation
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
class VisitorInviteFragment : Fragment() {

    private lateinit var binding: FragmentVisitorInviteBinding
    private lateinit var viewModel: VisitorInviteViewModel
    private var prefsHelper = SharedPreferencesHelper()
    private lateinit var resortId: String
    private var totalVisitors = 0
    private var visitorPolicyPresent = false
    private lateinit var visitorListAdapter: AddVisitorsAdapter
    private lateinit var data: Data
    private var packages: HashMap<String, ServicePackage> = hashMapOf()
    private var fortCallback: FortCallBackManager? = null
    private var paymentHelper: PaymentHelper? = null

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

        if (fortCallback == null)
            fortCallback = FortCallBackManager.Factory.create()

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        data = gson.fromJson(data_string, Data::class.java)
        resortId = data.user?.resort_id.toString()

        data.user?.resort?.let { setLogo(it) }

        binding.etVisitorsTime.setOnClickListener(View.OnClickListener {
            showDateDialog(data)
        })

        navigateToListing();

        binding.btnSubmit.setOnClickListener {

            val date = binding.etVisitorsTime.text.toString()
            val visitors = binding.etVisitorsNum.text.toString()

            if (date.isNullOrEmpty() || visitors.isNullOrEmpty()) {
                showSuccessDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    getString(R.string.empty_fields)
                )
            } else if(this::visitorListAdapter.isInitialized){
                val idMsg = checkVisitorsID(visitorListAdapter.getData(), requireContext())
                if (idMsg.isEmpty()) {
                    if (willSenderPay(visitorListAdapter.getData())) {
                        val amount = getPayableAmount(visitorListAdapter.getData())
                        if (!amount.equals("0")) {
                            payForVisitor()
                        }
                        else
                        {
                            addServerVisitor()
                        }
                    } else {
                        addServerVisitor()
                    }
                } else {
                    showSuccessDialog(requireActivity(), getString(R.string.app_name), idMsg)
                }
            }

        }

        if (data.user?.user_type.equals(Constants.admin)) {
            binding.spResorts.visibility = View.VISIBLE
            binding.tvResorts.visibility = View.VISIBLE

            getResorts(data)
        }

        //   setVisitorList()
    }

    private fun addServerVisitor()
    {
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

        visitorListAdapter = context?.let { AddVisitorsAdapter(arrayListOf(), it, this) }!!

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
                        showSuccessDialog(
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
                                    null, "", "","","",Constants.UN_PAID
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
                    if (i < visitors.size) {
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
        var visitors: ArrayList<Visitor> = visitorListAdapter.getData()
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
            showSuccessDialog(
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
                    showSuccessDialog(
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
        Handler(Looper.getMainLooper()).postDelayed(
            {

                // This method will be executed once the timer is over
                observeTotalVisitorsVM()
            },
            2000 // value in milliseconds
        )

    }

    private fun observeTotalVisitorsVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.rlInclude.visibility = View.GONE
                if (it) {
                    showSuccessDialog(
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
                showSuccessDialog(
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
                    requireContext().getString(R.string.app_name),
                    msg
                )
            }

        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showSuccessDialog(
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
            .minutesStep(1).mustBeOnFuture()
            // .minDateRange(d).mustBeOnFuture()
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
                    showSuccessDialog(
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
                    showSuccessDialog(
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
        if (action != null &&
            Navigation.findNavController(binding.btnSubmit).currentDestination?.id == R.id.nav_invite
            && Navigation.findNavController(binding.btnSubmit).currentDestination?.id != action.actionId
        ) {
            (activity as DashboardActivity).navigateToVisitorsList(action)
        } else {
            Timer().schedule(2000) {
                (activity as DashboardActivity).navigateToVisitorsList(action)
            }
        }

    }

    private var builder: AlertDialog.Builder? = null

    fun showAlertDialog(title: String, msg: String) {
        if (builder == null) {
            builder = activity?.let {
                AlertDialog.Builder(it)
            }

            builder?.setMessage(msg)
                ?.setTitle(title)?.setPositiveButton(
                    R.string.ok
                ) { dialog, id ->
                    val action = VisitorInviteFragmentDirections.actionNavToList()
                    if (action != null &&
                        Navigation.findNavController(binding.btnSubmit).currentDestination?.id == R.id.nav_invite
                        && Navigation.findNavController(binding.btnSubmit).currentDestination?.id != action.actionId
                    ) {
                        Navigation.findNavController(binding.btnSubmit).navigate(action)
                    } else {
                        Timer().schedule(2000) {
                            Navigation.findNavController(binding.btnSubmit).navigate(action)
                        }
                    }

                }
            builder?.create()?.show()
        }
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

                if(sdkToken != null) {
                    val fortRequest = paymentHelper?.getFortRequest(
                        sdkToken,
                        getPayableAmount(visitorListAdapter.getData())
                    )
                    processPayment(fortRequest!!)
                }
                else
                {
                    showAlertDialog(getString(R.string.app_name),"Token not fetched")
                }
            }

            override fun onFailure(call: Call<PayFortData?>?, t: Throwable?) {
                t?.stackTraceToString()?.let { Log.e("onResponse: ", it) }

            }
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
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
                    fortCallback,
                    true,
                    object : OnTnxProcessed {
                        override fun onCancel(
                            requestParamsMap: Map<String, Any>,
                            responseMap: Map<String, Any>
                        ) {
                            println("onCancel==REQ=>> $responseMap")
                            Log.e("callPaymentSDK: ", responseMap.toString())
                            if (responseMap.size > 0) {
                                Log.e("callPaymentSDK: ", responseMap.toString())
                            }
                        }

                        override fun onSuccess(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            println("onSuccess==REQ=>> $requestParamsMap")
                            Log.e("callPaymentSDK: ", requestParamsMap.toString())

                            if (requestParamsMap.size > 0) {
                                Log.e("callPaymentSDK: ", requestParamsMap.toString())
                            }
                        }

                        override fun onFailure(
                            requestParamsMap: Map<String, Any>,
                            fortResponseMap: Map<String, Any>
                        ) {
                            println("onFailure==REQ=>> $requestParamsMap")
                            Log.e("callPaymentSDK: ", requestParamsMap.toString())
                            if (requestParamsMap.size > 0) {
                                Log.e("callPaymentSDK: ", requestParamsMap.toString())
                            }

                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}