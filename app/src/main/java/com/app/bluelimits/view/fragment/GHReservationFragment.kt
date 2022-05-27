package com.app.bluelimits.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestReservationBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.app.bluelimits.view.AddGuestAdapter
import com.app.bluelimits.viewmodel.GHReservationViewModel
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog

import android.view.View.OnTouchListener
import android.widget.*

import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.app.bluelimits.view.activity.DashboardActivity


/**
 * A simple [Fragment] subclass.
 * Use the [GHReservationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GHReservationFragment : Fragment() {

    private lateinit var viewModel: GHReservationViewModel
    private var _binding: FragmentGuestReservationBinding? = null
    private var builder: AlertDialog.Builder? = null
    private var mAlertDialog: AlertDialog? = null
    private val guestListAdapter = AddGuestAdapter()
    private val binding get() = _binding!!

    private var isDiscountAvailable = false
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var resortId = ""
    private lateinit var obj: Data
    private lateinit var selectedUnit: AvailableUnit
    private var guestsLimit = 0
    private var discount = "0"
    private lateinit var userType: String
    var root: View? = null
    private lateinit var spaceId: String
    private var isDateValid = false
    private var isUnitAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGuestReservationBinding.inflate(inflater, container, false)
        root = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GHReservationViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        obj = gson.fromJson(data_string, Data::class.java)

        spaceId = arguments?.getString("spaceId").toString().uppercase()

        userType = obj.user?.user_type.toString()

        navigateToListing()

        getResorts()
        setReservationDates()
        setGuestList()

        if (userType.equals(Constants.admin)) {
            showDiscountView(
                obj.user?.guest_house_discount_percentage_self!!
            )

            binding.etDiscount.setOnTouchListener(OnTouchListener { v, event ->
                binding.etDiscount.textChanges()
                    .debounce(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { textChanged ->
                        //if (binding.etGuestsNo.text.toString().isNullOrEmpty())
                        if (!binding.etChkIn.text.toString()
                                .isNullOrEmpty() && !binding.etChkOut.text.toString()
                                .isNullOrEmpty()
                        ) {
                            var discount = ""
                            if (binding.etGuestsNo.text.toString().isNullOrEmpty())
                                discount =
                                    getDiscount(obj.user?.guest_house_discount_percentage_self?.toInt()!!)
                            else
                                discount =
                                    getDiscount(obj.user?.guest_house_discount_percentage?.toInt()!!)

                            getAvailableUnits(discount)

                            //   }
                        }
                    }

                false
            })
        }
        else
            obj.user?.guest_house_discount_percentage_self?.let { setCustomerDiscount(it) }

        binding.btnSubmit.setOnClickListener {

            if (!isDateValid) {
                showAlertDialog(getString(R.string.app_name), "Invalid Checkin date.")
                return@setOnClickListener
            } else if (!isDiscountAvailable && obj.user?.user_type.equals(Constants.admin)) {
                showAlertDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    "Entered discount not available."
                )
                return@setOnClickListener
            } else if (!isUnitAvailable) {
                showAlertDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    "Units not available for selected dates."
                )
                return@setOnClickListener

            }


            val noOGuests = binding.etGuestsNo.text.toString()
            if (!noOGuests.isNullOrEmpty() && noOGuests != "0") {
                val idMsg = checkGuestsID(guestListAdapter.getData(), requireContext())
                if (idMsg.isNotEmpty()) {
                    showAlertDialog(requireActivity(), getString(R.string.app_name), idMsg)
                } else {
                    addGuests()
                    observeReservationVM()
                }
            } else {
                if (this::selectedUnit.isInitialized && selectedUnit != null) {
                    addGuests()
                    observeReservationVM()
                } else {
                    showAlertDialog(
                        requireActivity(),
                        getString(R.string.app_name),
                        "Units not available for selected dates."
                    )
                }
            }

        }

    }

    private fun setCustomerDiscount(discount: String)
    {
        binding.etDiscount.setText(discount
        )

    }

    private fun navigateToListing() {
        val action = GHReservationFragmentDirections.actionNavToList()
        try {
            (activity as DashboardActivity).navigateToVisitorsList(action)
        } catch (e: IllegalArgumentException) {
            // User tried tapping 2 links at once!
            Log.i("nav error", "Can't open 2 links at once!")
        }

    }

    private fun getGHDiscount(): String? {
        val guests = binding.etGuestsNo.text.toString()
        if (guests.isNullOrEmpty() || guests == "0") {
            //return obj.user?.guest_house_discount_percentage_self
            return getDiscount(obj.user?.guest_house_discount_percentage_self?.toInt()!!)
        } else {
            return getDiscount(obj.user?.guest_house_discount_percentage?.toInt()!!)

        }

    }


    //if no of guests are more than 0 so, call separate avialable units api
    private fun addGuests() {
        val guests: ArrayList<Guest>? = guestListAdapter.getData()

        val no_of_guests = binding.etGuestsNo.text.toString()
        val setup_unit_id = selectedUnit.id
        val discount = selectedUnit.discount
        val package_id = selectedUnit.package_id
        val sub_total = selectedUnit.sub_total
        val total_price = selectedUnit.total_price
        val custom_discount_percentage = getGHDiscount()

        val reservation_date = binding.etChkIn.text.toString()
        val check_out_date = binding.etChkOut.text.toString()

        val reservationReq = guests?.let {
            GHReservationRequest(
                setup_unit_id.toString(),
                discount,
                no_of_guests,
                package_id,
                resortId,
                sub_total,
                total_price,
                reservation_date,
                check_out_date,
                custom_discount_percentage!!,
                it
            )
        }

        binding.scrollView.fullScroll(ScrollView.FOCUS_UP);
        hideKeyboard(requireActivity())

        obj.token?.let {
            reservationReq?.let { it1 ->
                viewModel.addGHReservation(
                    it,
                    it1, requireContext()
                )
            }
        }
    }


    private fun showDiscountView(serverDiscount: String) {
        binding.etDiscount.isEnabled = true
        binding.etDiscount.setText(serverDiscount)
        getAvailableUnits(serverDiscount)
    }

    private fun getAdminEnteredDiscount(): Int {
        val discount = binding.etDiscount.text.toString().replace(" ","")
        if (!discount.isNullOrEmpty())
            return discount.toInt()
        return 0

    }

    private fun getDiscount(serverDiscount: Int): String {
        //  val serverDiscount = obj.user?.guest_house_discount_percentage?.toInt()
        var discount = ""

        if (obj.user?.user_type.equals(Constants.admin)) {
            val adminDiscount = getAdminEnteredDiscount()
            if (adminDiscount > serverDiscount!!) {
                hideKeyboard(requireActivity())
                isDiscountAvailable = false
                showAlertDialog(
                    getString(R.string.app_name),
                    getString(R.string.discount_msg) + " " + serverDiscount + "%"
                )

            } else {
                discount = adminDiscount.toString()
                isDiscountAvailable = true
            }
        } else {
            discount = serverDiscount.toString()
        }

        return discount
    }

    private fun setReservationDates() {
        binding.etChkIn.setOnClickListener(View.OnClickListener {
            showDateTime(binding.etChkIn, Constants.CHKIN_TIME)
        })

        binding.etChkOut.setOnClickListener(View.OnClickListener {
            showDateTime(binding.etChkOut, Constants.CHKOUT_TIME)
        })

    }

    fun showDateTime(editText: EditText, showTime: String) {

        hideKeyboard(context as Activity)

        val d = Date()
        SingleDateAndTimePickerDialog.Builder(context as Activity)
            .title(getString(R.string.select_date))
            .titleTextColor(getResources().getColor(R.color.white))
            .displayMinutes(false)
            .displayHours(false)
            .minDateRange(d)
            .backgroundColor(getResources().getColor(R.color.white))
            .mainColor(getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_FORMAT = "yyyy-MM-dd"
                var sdf = SimpleDateFormat(DATE_FORMAT)

                val sdate = sdf.format(date)
                editText.setText(sdate + " " + showTime)

                if (!binding.etChkIn.text.toString()
                        .isNullOrEmpty() && !binding.etChkOut.text.toString().isNullOrEmpty()
                ) {
                    if (isChkInDateGreater(
                            binding.etChkIn.text.toString(),
                            binding.etChkOut.text.toString()
                        )
                    ) {
                        isDateValid = false
                        showAlertDialog(getString(R.string.app_name), "Invalid Checkin date.")
                    } else {
                        isDateValid = true
                        var discount = ""
                        if (binding.etGuestsNo.text.toString().isNullOrEmpty())
                            discount =
                                getDiscount(obj.user?.guest_house_discount_percentage_self?.toInt()!!)
                        else
                            discount =
                                getDiscount(obj.user?.guest_house_discount_percentage?.toInt()!!)


                        getAvailableUnits(discount)
                    }
                }
            }.display()


    }

    private fun getResorts() {
        obj.token?.let { viewModel.getCustomerResorts(it) }

        observeResortsVM()

    }

    //TO DO : pass a flag to show if no of guests are empty or not. if not empty, then obsserve vm will call add guest user asell
    // or do a separate call to a seapcarate getAvailableUnits() on clicking submit button
    private fun getAvailableUnits(discount: String) {
        val chk_in_date = binding.etChkIn.text.toString()
        val chk_out_date = binding.etChkOut.text.toString()

        if (!chk_in_date.isNullOrEmpty() && !chk_out_date.isNullOrEmpty()) {

            if (!discount.isNullOrEmpty()) {
                binding.progressBar.progressbar.visibility = View.VISIBLE
                obj.token?.let {
                    viewModel.getAvailableUnits(
                        requireContext(),
                        it,
                        resortId,
                        chk_in_date,
                        chk_out_date,
                        discount,
                        spaceId
                    )
                }
                observeUnitVM()
            }
        }
    }

    private fun populateSpinners(isUnits: Boolean) {

        var data: ArrayList<String>? = arrayListOf<String>()

        if (isUnits) {
            selectedUnit = viewModel.availableUnit.value!!
            /* viewModel.availableUnit.value!!.forEachIndexed { index, e ->
                 viewModel.availableUnit.value!!.get(index).unit?.let {
                     if (data != null) {
                         data.add(it)
                     }
                 }
             }*/

        } else {
            viewModel.resorts.value!!.forEachIndexed { index, e ->
                val resort = viewModel.resorts.value!!.get(index).name
                resort?.let {
                    if (data != null) {
                        if (!resort.contains(Constants.BOHO))
                            data.add(it)
                    }
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                R.layout.item_spinner,
                data as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    if (isUnits) {
                        binding.spUnits.adapter = adapter
                        onUnitsSelection()
                    } else {
                        binding.spResorts.adapter = adapter
                        onResortSelection()
                    }
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

    private fun onUnitsSelection() {
        binding.spUnits?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                /*   viewModel.availableUnit.value!!.forEachIndexed { index, e ->
                       if (viewModel.availableUnit.value!!.get(index).unit?.equals(selectedItem) == true) {
                           selectedUnit = viewModel.availableUnit.value!!.get(index)
                           setUnitDetails()
                       }

                   }*/
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUnitDetails() {

        binding.tvPrice.setText(selectedUnit.price)
        binding.tvMaxGuests.setText(
            getString(R.string.max) + " " + selectedUnit.no_of_guest + " " + getString(
                R.string.max_guests
            )
        )
        guestsLimit = selectedUnit.no_of_guest.toInt()
        binding.tvPayment.setText(getString(R.string.payable) + " " + selectedUnit.total_price)
    }


    fun observeResortsVM() {
        viewModel.loadError.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = View.GONE

            }
        })

        viewModel.resorts.observe(viewLifecycleOwner, Observer { resorts ->
            binding.progressBar.progressbar.visibility = View.GONE
            resorts?.let {
                populateSpinners(false)
            }

        })

    }

    fun observeUnitVM() {

        viewModel.errorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (!it.isNullOrEmpty()) {
                    isDiscountAvailable = false
                    isUnitAvailable = false
                    showAlertDialog(getString(R.string.title), it)


                }
            }
        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = View.GONE
                /*  isUnitAvailable = false

                  showAlertDialog(
                      context as Activity,
                      "Bluelimit",
                      "Units not available for selected dates."
                  )*/
            }
        })


        viewModel.availableUnit.observe(viewLifecycleOwner, Observer {
            binding.progressBar.progressbar.visibility = View.GONE
            if (it != null) {
                isDiscountAvailable = true
                isUnitAvailable = true
                selectedUnit = viewModel.availableUnit.value!!
                setUnitDetails()
            }


        })


    }

    private fun setGuestList() {

        binding.rvGuest.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guestListAdapter
        }

        binding.rvGuest.addItemDecoration(
            DividerItemDecoration(
                binding.rvGuest.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etGuestsNo.textChanges()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val enteredGuestTxt: String = binding.etGuestsNo.text.toString().replace(" ","")
                if (!enteredGuestTxt.isEmpty()) {

                    activity?.let { hideKeyboard(it) }

                    val no_of_entered_guests: Int = enteredGuestTxt.toInt()
                    if (no_of_entered_guests > 0 && no_of_entered_guests <= guestsLimit) {

                        if (userType.equals(Constants.admin)) {
                            showDiscountView(obj.user?.guest_house_discount_percentage!!)
                        }
                        else
                            obj.user?.guest_house_discount_percentage?.let { setCustomerDiscount(it) }


                        val visitors = ArrayList<Guest>(no_of_entered_guests)
                        for (i in 1..no_of_entered_guests) {
                            val person = Guest("", "", "", "", "", "", "")
                            visitors.add(person)
                        }

                        binding.rvGuest.visibility = View.VISIBLE
                        guestListAdapter?.setGuestList(
                            visitors, requireContext()
                        )

                    } else if (no_of_entered_guests == 0) {
                        if (userType.equals(Constants.admin)) {
                            showDiscountView(obj.user?.guest_house_discount_percentage_self!!)
                        }
                        else
                            obj.user?.guest_house_discount_percentage_self?.let { setCustomerDiscount(it) }

                        binding.rvGuest.visibility = View.GONE
                        guestListAdapter?.clearData()


                    } else if (no_of_entered_guests > guestsLimit) {
                        showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.guests_exceeded)
                        )
                        binding.rvGuest.visibility = View.GONE
                        guestListAdapter?.clearData()


                    } else {
                        binding.rvGuest.visibility = View.GONE
                        guestListAdapter?.clearData()

                    }
                }
                else {
                    guestListAdapter?.clearData()
                    binding.rvGuest.visibility = View.GONE
                    if (userType.equals(Constants.admin)) {
                        showDiscountView(obj.user?.guest_house_discount_percentage_self!!)
                    }
                    else
                        obj.user?.guest_house_discount_percentage_self?.let { setCustomerDiscount(it) }


                }


            }

    }

    private fun observeReservationVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.progressBar.progressbar.visibility = View.GONE
                    showAlertDialog(
                        getString(R.string.app_name),
                        getString(R.string.adding_guest_error)
                    )
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        viewModel.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                if (mAlertDialog?.isShowing == true)
                    mAlertDialog?.dismiss()

                showSuccessMsg()

            }

        })

    }

    private fun showSuccessMsg() {
        val action = GHReservationFragmentDirections.actionNavToMsg();

        try {
            action?.let { Navigation.findNavController(binding.btnSubmit).navigate(it) }
        } catch (e: IllegalArgumentException) {
            // User tried tapping 2 links at once!
            Log.i("nav error", "Can't open 2 links at once!")
        }

    }

    fun showAlertDialog(title: String, msg: String) {
        if (builder == null) {
            builder = activity?.let {
                AlertDialog.Builder(it)
            }
        }
        builder?.setMessage(msg)
            ?.setTitle(title)?.setPositiveButton(
                R.string.ok
            ) { _, _ ->

            }
        mAlertDialog = builder?.create()
        mAlertDialog?.show()

    }


}