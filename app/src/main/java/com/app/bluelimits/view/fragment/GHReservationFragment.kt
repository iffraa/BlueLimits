package com.app.bluelimits.view.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentGuestReservationBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.app.bluelimits.view.GuestListAdapter
import com.app.bluelimits.viewmodel.GHReservationViewModel
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import androidx.core.os.HandlerCompat.postDelayed

import android.view.MotionEvent

import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.core.os.HandlerCompat


/**
 * A simple [Fragment] subclass.
 * Use the [GHReservationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GHReservationFragment : Fragment() {

    private lateinit var viewModel: GHReservationViewModel
    private var _binding: FragmentGuestReservationBinding? = null

    private val guestListAdapter = GuestListAdapter()
    private val binding get() = _binding!!
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var resortId = ""
    private lateinit var obj: Data
    private lateinit var selectedUnit: AvailableUnit
    private var guestsLimit = 0
    private var discount = "0"
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGuestReservationBinding.inflate(inflater, container, false)
        val root: View = binding.root
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

        userType = obj.user?.user_type.toString()

        getResorts()
        setReservationDates()
        setGuestList()

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
            addGuests()
            observeReservationVM()
        })

    }

    fun addGuests() {
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
                     requireActivity(),
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
                    viewModel.getAvailableUnits(it, resortId, chk_in_date, chk_out_date, discount)
                }
                observeViewModel(true)
            }
        }
    }

    private fun populateSpinners(isUnits: Boolean) {

        var data: ArrayList<String>? = arrayListOf<String>()

        if (isUnits) {
            viewModel.availableUnits.value!!.forEachIndexed { index, e ->
                viewModel.availableUnits.value!!.get(index).unit?.let {
                    if (data != null) {
                        data.add(it)
                    }
                }
            }

        } else {
            viewModel.resorts.value!!.forEachIndexed { index, e ->
                viewModel.resorts.value!!.get(index).name?.let {
                    if (data != null) {
                        data.add(it)
                    }
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

                viewModel.availableUnits.value!!.forEachIndexed { index, e ->
                    if (viewModel.availableUnits.value!!.get(index).unit?.equals(selectedItem) == true) {
                        selectedUnit = viewModel.availableUnits.value!!.get(index)
                        setUnitDetails()
                    }

                }
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
    }

    fun observeViewModel(isUnits: Boolean) {

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

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        if (isUnits) {
            viewModel.availableUnits.observe(viewLifecycleOwner, Observer { availableUnits ->
                binding.progressBar.progressbar.visibility = View.GONE
                availableUnits?.let {
                    populateSpinners(true)
                }

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

        binding.etGuestsNo.textChanges()
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val enteredGuestTxt: String = binding.etGuestsNo.text.toString()
                if (!enteredGuestTxt.isEmpty()) {

                    activity?.let { hideKeyboard(it) }

                    val no_of_entered_guests: Int = enteredGuestTxt.toInt()
                    if (no_of_entered_guests > 0 && no_of_entered_guests < guestsLimit) {
                        val visitors = ArrayList<Guest>(no_of_entered_guests)
                        for (i in 1..no_of_entered_guests) {
                            val person = Guest("", "", "", "")
                            visitors.add(person)
                        }
                        binding.rvGuest.visibility = View.VISIBLE
                        binding.btnSubmit.visibility = View.VISIBLE
                        guestListAdapter?.setGuestList(
                            visitors, requireContext()
                        )

                    } else if (no_of_entered_guests > guestsLimit) {
                        showAlertDialog(
                            requireContext() as Activity,
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


            }

    }

    fun observeReservationVM() {

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.progressBar.progressbar.visibility = View.GONE
                    showAlertDialog(
                        context as Activity,
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
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    msg
                )
            }

        })

    }


}