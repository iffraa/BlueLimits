package com.app.bluelimits.view.fragment

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog

import android.view.View.OnTouchListener
import android.widget.*

import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.app.bluelimits.databinding.FragmentEditGuestBinding
import com.app.bluelimits.view.EditGuestAdapter
import com.app.bluelimits.viewmodel.GuestEditViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [EditGuestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditGuestFragment : Fragment() {

    private lateinit var viewModel: GuestEditViewModel
    private var _binding: FragmentEditGuestBinding? = null
    private var builder: AlertDialog.Builder? = null
    private var mAlertDialog: AlertDialog? = null
    private val guestListAdapter = EditGuestAdapter()
    private val binding get() = _binding!!

    private lateinit var prefsHelper: SharedPreferencesHelper
    private var resortId = ""
    private lateinit var obj: Data
    private lateinit var selectedUnit: AvailableUnit
    private var guestsLimit = 0
    private var discount = "0"
    private lateinit var userType: String
    var root: View? = null
    private lateinit var unitId: String
    private lateinit var guestData: GuestData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditGuestBinding.inflate(inflater, container, false)
        root = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GuestEditViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE
        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!

        arguments?.let {
            guestData = it.getParcelable("gDetails")!!
        }

        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        obj = gson.fromJson(data_string, Data::class.java)

        // spaceId = arguments?.getString("spaceId").toString().uppercase()

        userType = obj.user?.user_type.toString()

        getResorts()
        setReservationDates()
        setReservationData()

        if (userType.equals(Constants.admin)) {
            showDiscountView()

            binding.etDiscount.setOnTouchListener(OnTouchListener { v, event ->
                binding.etDiscount.textChanges()
                    .debounce(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { textChanged ->
                        if (!binding.etChkIn.text.toString()
                                .isNullOrEmpty() && !binding.etChkOut.text.toString()
                                .isNullOrEmpty()
                        ) {
                            val discount = getDiscount()
                            getAvailableUnits(discount)

                        }
                    }

                false
            })
        }

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            val noOfGuests = binding.etGuestsNo.text.toString()
            if (!noOfGuests.isNullOrEmpty() && noOfGuests != "0") {
                val idMsg = checkGuestsID(guestListAdapter.getData(), requireContext())
                if (idMsg.isNotEmpty()) {
                    showSuccessDialog(requireActivity(), getString(R.string.app_name), idMsg)
                } else {
                    addGuests()
                    observeReservationVM()
                }
            } else {
                showAlertDialog(
                    getString(R.string.app_name),
                    "Please add any guest."
                )
            }
        })

    }

    private fun setReservationData() {
        binding.etChkIn.setText(guestData.from)
        binding.etChkOut.setText(guestData.to)
        binding.etDiscount.setText(guestData.discount)
        binding.etGuestsNo.setText(guestData.no_of_guest)

        resortId = guestData.resort_id!!
        unitId = guestData.unit_type_id!!

        val discount = getDiscount()
        getAvailableUnits(discount)

    }

    private fun addGuests() {
        val guests: ArrayList<Guest>? = guestListAdapter.getData()

        val no_of_guests = binding.etGuestsNo.text.toString()
        val setup_unit_id = selectedUnit.id
        val discount = selectedUnit.discount
        val package_id = selectedUnit.package_id
        val sub_total = selectedUnit.sub_total
        val total_price = selectedUnit.total_price
        val custom_discount_percentage = selectedUnit.discount_percentage
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
                custom_discount_percentage,
                it
            )
        }

        binding.scrollView.fullScroll(ScrollView.FOCUS_UP);
        hideKeyboard(requireActivity())

        obj.token?.let {
            reservationReq?.let { request ->
                viewModel.updateGuests(
                    guestData.id!!,
                    it,
                    request, requireContext()
                )
            }
        }
    }

    private fun showDiscountView() {
        binding.etDiscount.visibility = View.VISIBLE
        binding.tvDiscountLbl.visibility = View.VISIBLE

        binding.etDiscount.setText(obj.user?.guest_house_discount_percentage)
    }

    private fun getAdminDiscount(): Int {
        val discount = binding.etDiscount.text.toString()
        if (!discount.isNullOrEmpty())
            return discount.toInt()
        return 0

    }

    private fun getDiscount(): String {
        val serverDiscount = obj.user?.guest_house_discount_percentage?.toInt()
        var discount = ""

        if (obj.user?.user_type.equals(Constants.admin)) {
            val adminDiscount = getAdminDiscount()
            if (adminDiscount > serverDiscount!!) {
                hideKeyboard(requireActivity())

                showAlertDialog(
                    getString(R.string.app_name),
                    getString(R.string.discount_msg) + " " + serverDiscount + "%"
                )
                /*  Toast.makeText(
                      requireContext(),
                      getString(R.string.discount_msg) + " " + serverDiscount + "%",
                      Toast.LENGTH_LONG
                  ).show()*/

            } else
                discount = adminDiscount.toString()
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
                    val discount = getDiscount()
                    getAvailableUnits(discount)

                }
            }.display()


    }

    private fun getResorts() {
        obj.token?.let { viewModel.getCustomerResorts(it) }

        observeViewModel(false)

    }

    private fun getAvailableUnits(discount: String) {
        val chk_in_date = binding.etChkIn.text.toString()
        val chk_out_date = binding.etChkOut.text.toString()

        if (!chk_in_date.isNullOrEmpty() && !chk_out_date.isNullOrEmpty()) {

            if (!discount.isNullOrEmpty()) {
                binding.progressBar.progressbar.visibility = View.VISIBLE
                obj.token?.let {
                    viewModel.getAvailableUnits(
                        it,
                        resortId,
                        chk_in_date,
                        chk_out_date,
                        discount,
                        unitId
                    )
                }
                observeViewModel(true)
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

    private fun setUnitDetails() {

        binding.tvPrice.setText(selectedUnit.price)
        binding.tvMaxGuests.setText(
            getString(R.string.max) + " " + selectedUnit.no_of_guest + " " + getString(
                R.string.max_guests
            )
        )
        guestsLimit = selectedUnit.no_of_guest.toInt()
        binding.tvPayment.setText(getString(R.string.payable) + " " + selectedUnit.total_price)

        chkGuestLimit()
    }

    private fun chkGuestLimit() {
        if (guestData.no_of_guest?.toInt()!! > guestsLimit) {
            showAlertDialog(
                getString(R.string.app_name),
                guestsLimit.toString() + " " + getString(R.string.max_guests) + " on selected dates."
            )
        }
    }

    fun observeViewModel(isUnits: Boolean) {

        viewModel.errorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (!it.isNullOrEmpty()) {
                    binding.progressBar.progressbar.visibility = View.GONE
                    val msg = getServerErrors(viewModel.errorMsg.value.toString(), requireContext())
                    showAlertDialog(getString(R.string.app_name), msg)
                }
            }
        })


        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        if (isUnits) {
            viewModel.availableUnit.observe(viewLifecycleOwner, Observer {
                binding.progressBar.progressbar.visibility = View.GONE
                selectedUnit = viewModel.availableUnit.value!!
                /* populateSpinners(true)
                 for(availableUnit in it) {
                     val unitName = availableUnit.unit
                     Log.i("unitName", unitName)
                     if (unitName.contains(spaceId) && !unitName.equals("G 00"))
                     {
                         selectedUnit = availableUnit
                     }
                 }*/

                setUnitDetails()
                setGuestList()
            })

        } else {
            viewModel.resorts.observe(viewLifecycleOwner, Observer { resorts ->
                binding.progressBar.progressbar.visibility = View.GONE
                resorts?.let {
                    populateSpinners(false)
                }

            })
        }


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

        /* binding.etGuestsNo.textChanges()
             .debounce(1, TimeUnit.SECONDS)
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe { textChanged ->
                 val enteredGuestTxt: String = binding.etGuestsNo.text.toString()
                 if (!enteredGuestTxt.isEmpty()) {

                     activity?.let { hideKeyboard(it) }
 */
        val no_of_entered_guests: Int = guestData.no_of_guest!!.toInt()
        if (no_of_entered_guests > 0 && no_of_entered_guests <= guestsLimit) {
            /*  val visitors = ArrayList<Guest>(no_of_entered_guests)
              for (i in 1..no_of_entered_guests) {
                  val person = Guest("", "", "", "","","")
                  visitors.add(person)
              }*/

            binding.rvGuest.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.VISIBLE
            guestListAdapter?.setGuestList(
                guestData.guests!!, requireContext()
            )

        } else if (no_of_entered_guests > guestsLimit) {
            showAlertDialog(
                getString(R.string.app_name),
                getString(R.string.guests_exceeded)
            )
            binding.rvGuest.visibility = View.GONE
            binding.btnSubmit.visibility = View.GONE

        } else {
            binding.rvGuest.visibility = View.GONE
            binding.btnSubmit.visibility = View.GONE

        }
    }


    private fun adjustLayout() {
        val config: Configuration = resources.configuration
        val height = config.screenHeightDp

        if (height >= 900) {

            //linear layout
            val attributLayoutParams = RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            attributLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            attributLayoutParams.addRule(RelativeLayout.BELOW, R.id.tv_title);

            binding.llMain.layoutParams = attributLayoutParams

            val params = RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = 40
            params.leftMargin = 125
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.addRule(RelativeLayout.BELOW, R.id.ll_main);

            binding.tvPayment.layoutParams = params
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

                navigateToListing()

            }

        })

    }

    private fun navigateToListing() {
        val action = EditGuestFragmentDirections.actionNavToList()

        if (action != null &&
            Navigation.findNavController(binding.btnSubmit).currentDestination?.id == R.id.nav_guest_edit
            && Navigation.findNavController(binding.btnSubmit).currentDestination?.id != action.actionId
        ) {
            action?.let { Navigation.findNavController(binding.btnSubmit).navigate(it) }
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
                ) { _, _ ->

                }
            mAlertDialog = builder?.create()
            mAlertDialog?.show()
        }
    }


}