package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentMarineFormBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import com.app.bluelimits.view.PackageListAdapter
import com.app.bluelimits.viewmodel.MarineFormViewModel
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MarineFormFragment : Fragment() {

    private var _binding: FragmentMarineFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var resortType: Resort
    private lateinit var viewModel: MarineFormViewModel
    private var isBookingAvailable: Boolean = false

    private lateinit var packageListAdapter: PackageListAdapter
    private var prefsHelper = SharedPreferencesHelper()
    private var resort_id: String = ""
    private var service_id: String = ""
    private var package_id: String = ""
    private var total_price: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarineFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        if (!data_string.isNullOrEmpty()) {
            val gson = Gson()
            val obj: Data = gson.fromJson(data_string, Data::class.java)
        }

        viewModel = ViewModelProvider(this).get(MarineFormViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE

        binding.etDate.setOnClickListener(View.OnClickListener {
            selectReservDate()
        })

        getResorts()


        binding.etHrs.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!binding.etDate.text.toString().isNullOrEmpty()) {
                    checkAvailability()
                }
            }


        binding.btnSubmit.setOnClickListener(View.OnClickListener {

            if (!package_id.isEmpty()) {
                if (isBookingAvailable) {
                    binding.progressBar.progressbar.visibility = View.VISIBLE
                    hideKeyboard(context as Activity)
                    submitApplication()
                } else {
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.booking_unavailable)
                    )
                }
            } else {
                showSuccessDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.no_pckg_error)
                )
            }
        })


    }


    private fun getResorts() {
        viewModel.getGuestResorts()
        observeResortsVM()
    }

    private fun getServices() {
        viewModel.getServices(resort_id)
        observeViewModel(true)
    }

    fun observeViewModel(isServices: Boolean) {

        if (isServices) {
            viewModel.services.observe(viewLifecycleOwner, Observer { services ->
                services?.let {
                    populateSpinners(false)
                }
                binding.progressBar.progressbar.visibility = View.GONE

            })
        } else {
            viewModel.packages.observe(viewLifecycleOwner, Observer { packages ->
                packages?.let {
                    setPackagesList(packages)
                }
                binding.progressBar.progressbar.visibility = View.GONE

            })
        }
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })


        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })
    }

    fun observeResortsVM() {

        viewModel.resorts.observe(viewLifecycleOwner, Observer { resorts ->
            resorts?.let {
                populateSpinners(true)
            }
            binding.progressBar.progressbar.visibility = View.GONE

        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })


        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (it) {
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })
    }

    private fun populateSpinners(isResorts: Boolean) {

        var data: ArrayList<String>? = arrayListOf<String>()

        if (isResorts) {
            viewModel.resorts.value!!.forEachIndexed { index, e ->
                viewModel.resorts.value!!.get(index).name?.let {
                    if (data != null) {
                        data.add(it)
                    }
                }
            }
        } else {
            viewModel.services.value!!.forEachIndexed { index, e ->
                viewModel.services.value!!.get(index).name?.let {
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
                R.layout.item_spinner,
                data as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    if (isResorts) {
                        binding.spFacility.adapter = adapter
                        onResortSelected()
                    } else {
                        binding.spService.adapter = adapter
                        onServiceSelected()
                    }
                }
        }


    }

    private fun onServiceSelected() {

        binding.spService?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                viewModel.services.value!!.forEachIndexed { index, e ->
                    if (viewModel.services.value!!.get(index).name?.equals(selectedItem) == true) {
                        service_id = viewModel.services.value!!.get(index).id.toString()

                        getPackages()
                    }

                }
            }


        }
    }

    private fun onResortSelected() {

        binding.spFacility?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                        resort_id = viewModel.resorts.value!!.get(index).id.toString()
                        getServices()

                    }

                }
            }


        }
    }

    private fun getPackages() {
        if (!service_id?.isEmpty())
            viewModel.getGuestPackages(service_id)

        observeViewModel(false)
    }


    private fun setPackagesList(servicePackages: ArrayList<ServicePackage>) {
        binding.rvPckgs.visibility = View.VISIBLE
        binding.rvPckgs.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvPckgs.getContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            packageListAdapter = PackageListAdapter(arrayListOf(), object :
                PackageListAdapter.OnItemCheckListener {

                override fun onItemCheck(item: ServicePackage?) {
                    package_id = item?.id.toString()
                    Log.e(
                        "check package",
                        item?.service_name + "  " + item?.id
                    )
                    binding.etHrs.setText(item?.hour)
                }

                override fun onItemUncheck(item: ServicePackage?) {
                    Log.e(
                        "uncheck package",
                        item?.service_name + "  " + item?.id
                    )
                }
            })
            adapter = packageListAdapter
            packageListAdapter.updatePckgList(servicePackages)

        }

    }

    private fun submitApplication() {
        val name: String = binding.etName.text.toString()
        val email: String = binding.etEmail.text.toString()
        val contact: String =
            binding.layoutMobile.etMobile.text.toString().replace("\\s".toRegex(), "")
        val id: String = binding.etId.text.toString()
        val reserv_date: String = binding.etDate.text.toString()
        val hrs: String = binding.etHrs.text.toString()
        val gender: String = getGender(binding.cbFemale, binding.cbMale)

        if (!name.isNullOrEmpty() && !id.isNullOrEmpty()) {
            if (id.length < 10) {
                showSuccessDialog(
                    requireActivity(),
                    getString(R.string.app_name),
                    getString(R.string.id_length_error)
                )
            } else {
                val contact = binding.layoutMobile.etMobile.text
                if (contact.isNullOrEmpty() || contact.length < 8) {
                    showSuccessDialog(
                        requireActivity(),
                        getString(R.string.app_name),
                        getString(R.string.contact_error)
                    )
                } else {
                    val request = MarineServiceRequest(
                        service_id,
                        package_id,
                        resort_id,
                        name,
                        getString(R.string.number_code) + contact,
                        id,
                        email,
                        reserv_date,
                        package_id,
                        hrs,
                        total_price.toString(),
                        gender
                    )

                    viewModel.addApplication(request, requireContext())
                    observeAddApplicationVM()
                }
            }
        } else {
            showSuccessDialog(
                requireActivity(),
                getString(R.string.app_name),
                getString(R.string.empty_fields)
            )
        }

    }

    private fun selectReservDate() {

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
                binding.etDate.setText(sdate)

                checkAvailability()
            }.display()

    }


    private fun checkAvailability() {
        if (!package_id.isEmpty()) {

            binding.progressBar.progressbar.visibility = View.VISIBLE

            viewModel.checkBookingAviability(
                service_id,
                package_id,
                binding.etDate.text.toString(),
                binding.etHrs.text.toString()
            )
            observeBookingVM()

        } else {
            showSuccessDialog(
                context as Activity,
                getString(R.string.app_name),
                getString(R.string.no_pckg_error)
            )
        }
    }


    fun observeBookingVM() {
        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.progressBar.progressbar.visibility = View.GONE
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.booking_unavailable)
                    )
                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.progressBar.progressbar.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        viewModel.bookingResponse.observe(viewLifecycleOwner, Observer { bookingResponse ->
            bookingResponse?.let {
                binding.progressBar.progressbar.visibility = View.GONE
                if (!bookingResponse.is_booked && !bookingResponse.total_price.isNullOrEmpty()) {
                    total_price = Integer.parseInt(bookingResponse.total_price);
                    isBookingAvailable = true

                    binding.etTPrice.setText(total_price.toString())
                } else {
                    isBookingAvailable = false
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name), getString(R.string.booking_unavailable)
                    )
                }
            }

        })


    }


    fun observeAddApplicationVM() {
        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.progressBar.progressbar.visibility = View.GONE
                    showSuccessDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.adding_member_error)
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
                binding.progressBar.progressbar.visibility = View.GONE
                showAlertDialog(
                    getString(R.string.app_name), "Form submitted successfully."
                )
            }

        })

    }

    fun showAlertDialog(title: String, msg: String) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }

        builder?.setMessage(msg)
            ?.setTitle(title)?.setPositiveButton(
                R.string.ok
            ) { dialog, id ->
                val action = MarineFormFragmentDirections.actionNavToHome()
                Navigation.findNavController(binding.btnSubmit).navigate(action)

            }
        builder?.create()?.show()
    }

}