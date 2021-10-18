package com.app.bluelimits.view.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentUnitFormBinding

import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.view.PackageListAdapter
import com.app.bluelimits.viewmodel.UnitFormViewModel
import java.util.*
import kotlin.collections.ArrayList
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.app.bluelimits.view.FamilyListAdapter
import com.hbb20.CountryCodePicker
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.bluelimits.model.*
import com.app.bluelimits.util.*
import android.content.DialogInterface
import android.widget.AdapterView
import android.widget.ArrayAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [UnitFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UnitFormFragment : Fragment() {

    private var _binding: FragmentUnitFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var resortType: Resort
    private lateinit var viewModel: UnitFormViewModel
    private val familyListAdapter = FamilyListAdapter(arrayListOf())

    //private val packageListAdapter = PackageListAdapter(arrayListOf())
    private lateinit var packageListAdapter: PackageListAdapter
    private var prefsHelper = SharedPreferencesHelper()
    private var role_id: String = ""
    private var service_id: String = ""
    private var package_id: String = ""
    private var role: Resort? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            role = it.getParcelable<Resort>("facility")!!
            resortType = it.getParcelable<Resort>(Constants.TYPE)!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnitFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        /*val data_string = prefsHelper.getData(Constants.USER_DATA)
        if (!data_string.isNullOrEmpty()) {
            val gson = Gson()
            val obj: Data = gson?.fromJson(data_string, Data::class.java)
            role_id = obj?.user?.role_id.toString()
        }*/
        viewModel = ViewModelProvider(this).get(UnitFormViewModel::class.java)
        binding.progressBar.progressbar.visibility = View.VISIBLE

        setRoleUI()
        selectPicture()
        getPackages()
        getServices()

        binding.etDob.setOnClickListener(View.OnClickListener {
            showDate(requireContext(), binding.etDob)
        })

        getGender(binding.cbFemale, binding.cbMale)
        setFamilyList()
        applyForMembership()
    }

    private fun setRoleUI() {
        setLogo()

        role_id = role?.id.toString()
        binding.tvService.setText(role?.name)

        if (role?.name?.equals(getString(R.string.locker_member)) == true) {
            binding.tvMembership.setText(getString(R.string.locker_membership))
        }

    }

    private fun getServices() {
        viewModel.getServices(resortType.id.toString())
        observeDataVM(true)
    }

    fun observeDataVM(isServices: Boolean) {

        if (isServices) {
            viewModel.services.observe(viewLifecycleOwner, Observer { services ->
                services?.let {
                    populateServices()
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
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.loading_error)
                    )
                }
            }
        })
    }

    private fun populateServices() {

        var services: ArrayList<String>? = arrayListOf<String>()

        viewModel.services.value!!.forEachIndexed { index, e ->
            viewModel.services.value!!.get(index).name?.let {
                if (services != null) {
                    services.add(it)
                }
            }
        }

        // Creating adapter for spinner
        activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item,
                services as MutableList<String>
            )

                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    binding.spService.adapter = adapter
                }
        }

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
                    }

                }
            }


        }

    }

    private fun getPackages() {
        /*if (!role_id?.isEmpty())
            viewModel.getMemberPackages(resortType.id.toString(), role_id)
        else
            viewModel.getGuestPackages(service_id)*/

        viewModel.getMemberPackages(resortType.id.toString(), role_id)
        observeDataVM(false)

    }

    private fun setFamilyList() {
        binding.rvFamily.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = familyListAdapter
        }

        binding.rvFamily.addItemDecoration(
            DividerItemDecoration(
                binding.rvFamily.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        binding.etFamily.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val noOfFam: String = binding.etFamily.text.toString()
                if (!noOfFam.isEmpty()) {
                    activity?.let { hideKeyboard(it) }

                    val no_of_fam_members: Int = noOfFam.toInt()
                    val members = ArrayList<FamilyMemberRequest>(no_of_fam_members)
                    for (i in 1..no_of_fam_members) {
                        val person = FamilyMemberRequest(0, "", "", "", "", "")
                        members.add(person)
                    }
                    binding.rvFamily.visibility = View.VISIBLE
                    familyListAdapter.setFamilyList(members, requireContext())

                }
            }

    }


    private fun setPackagesList(servicePackages: ArrayList<ServicePackage>) {
        binding.rvPckgs.visibility = View.VISIBLE
        binding.rvPckgs.apply {
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    binding.rvFamily.getContext(),
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

            /*    addOnItemTouchListener(RecyclerItemClickListenr(context, binding.rvPckgs, object : RecyclerItemClickListenr.OnItemClickListener {

                    override fun onItemClick(view: View, position: Int) {
                        val chkBox: CheckedTextView = view.findViewById(R.id.ctv_pckgs)
                        if(chkBox.isChecked){
                            val selected_package: ServicePackage = servicePackages.get(position)
                            Log.e(
                            "sel package",
                            selected_package?.service_name + "  " + selected_package?.id
                        )
                    }}
                }))*/
        }


    }

    private fun setLogo() {
        when {
            resortType.name?.contains(Constants.OIA) == true -> setLayout(R.drawable.oia_logo)
            resortType.name?.contains(Constants.BOHO) == true -> {
                setLayout(R.drawable.boho_logo)
                binding.tvFormTitle.setText(getString(R.string.boho_form_text))
            }
            resortType.name?.contains(Constants.MARINE) == true -> setLayout(R.drawable.marine_logo)
        }
    }

    private fun setLayout(logo_img: Int) {
        binding.ivLogo.setImageResource(logo_img)

    }

    private fun selectPicture() {
        binding.tvBrowse.setOnClickListener(View.OnClickListener {
            pickImages.launch("image/*")
        })

    }


    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
                binding.tvBrowse.setText(it.path)
            }
        }


    private fun getSelectedCountry(cpp: CountryCodePicker): String {
        var selectedCountry = cpp.defaultCountryName
        Log.i("country", cpp.selectedCountryName)
        cpp.setOnCountryChangeListener { //Alert.showMessage(RegistrationActivity.this, ccp.getSelectedCountryCodeWithPlus());
            Log.i("country", cpp.selectedCountryName)
            selectedCountry = cpp.selectedCountryName
        }
        return selectedCountry
    }

    fun applyForMembership() {
        binding.btnApply.setOnClickListener(View.OnClickListener {
            val firstName: String = binding.etFname.text.toString()
            val lastName: String = binding.etLname.text.toString()
            val email: String = binding.etEmail.text.toString()
            val contact: String = binding.layoutMobile.etMobile.text.toString()
            val no_of_fam_member: String = binding.etFamily.text.toString()
            val city_offc: String = binding.etOffCity.text.toString()
            val city_home: String = binding.etHCity.text.toString()
            val country_offc = getSelectedCountry(binding.ccpOffice)
            val country_home = getSelectedCountry(binding.ccpHome)
            val gender: String = getGender(binding.cbFemale, binding.cbMale)
            val member_id: String = binding.etId.text.toString()
            val dob: String = binding.etDob.text.toString()

            val family_data: ArrayList<FamilyMemberRequest> = familyListAdapter.getData()
            Log.i("family_data", family_data.toString())
            binding.rlPb.visibility = View.VISIBLE

            if (role_id.isNullOrEmpty())
                role_id = role?.id.toString()

            val errorMsg = getEmptyFieldsMsg()
            if (errorMsg.isNullOrEmpty()) {

                val request = RegisterMemberRequest(
                    role_id,
                    resortType.id.toString(),
                    service_id,
                    package_id,
                    firstName,
                    lastName,
                    member_id,
                    email,
                    dob,
                    gender,
                    getString(R.string.number_code) + contact,
                    city_home,
                    country_home,
                    city_offc,
                    country_offc,
                    "",
                    no_of_fam_member,
                    family_data
                )

                if (!package_id.isEmpty()) {

                    hideKeyboard(requireActivity())
                    viewModel.addMember(request, requireContext())
                    observeAddMemberViewModel()

                } else {
                    showAlertDialog(
                        context as Activity,
                        getString(R.string.app_name),
                        getString(R.string.no_pckg_error)
                    )
                }
            } else {
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name), errorMsg
                )
            }
        })


    }


    fun observeAddMemberViewModel() {

        viewModel.errorMsg.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (!it.isNullOrEmpty()) {
                    binding.rlPb.visibility = View.GONE
                    displayServerErrors(viewModel.errorMsg.value.toString(), requireContext())

                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading.let {
                binding.rlPb.visibility = if (it) View.VISIBLE else View.GONE

            }
        })

        viewModel.message.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                binding.rlPb.visibility = View.GONE
                showAlertDialog(
                    context as Activity,
                    getString(R.string.app_name),msg
                )
            }

        })

    }


    class DatePickerFragment(editText: EditText) : DialogFragment(),
        android.app.DatePickerDialog.OnDateSetListener {

        val dateText = editText
        lateinit var dpDialog: DatePickerDialog
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            dpDialog = android.app.DatePickerDialog(
                context as Activity,
                R.style.DialogTheme,
                this,
                year,
                month,
                day
            )



            dpDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, "Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.dismiss()
                        Log.d("Button_value", "$which ----  negatif")
                    }
                })

            dpDialog.setButton(
                DialogInterface.BUTTON_POSITIVE, "Set",
                DialogInterface.OnClickListener { dialog, which ->
                    dateText.setText(day.toString() + " " + month.toString())

                    Log.d(
                        "Button_value",
                        "$which ----  Pozitif"
                    )
                }
            )


            // Create a new instance of DatePickerDialog and return it
            return dpDialog
        }


        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            // Do something with the date chosen by the user
            val c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);


            dateText.setText(day.toString() + " " + month.toString())

        }


    }

    private fun getEmptyFieldsMsg(): String {
        var errorMsg = ""
        val firstName = binding.etFname.text.toString()
        val lastName = binding.etLname.text.toString()
        val member_id: String = binding.etId.text.toString()
        val email = binding.etEmail.text.toString()
        val mobile = "9665" + binding.layoutMobile.etMobile.text.toString()

        //employee required fields
        if (mobile.isNullOrEmpty() || mobile.length < 8) {
            errorMsg = getString(R.string.contact_error)
        } else if (email.isNullOrEmpty() || !email.isEmailValid()) {
            errorMsg = getString(R.string.email_error)
        } else if (lastName.isNullOrEmpty()) {
            errorMsg = getString(R.string.lname_error)
        } else if (firstName.isNullOrEmpty()) {
            errorMsg = getString(R.string.fname_error)
        } else if (member_id.isNullOrEmpty()) {
            errorMsg = getString(R.string.id_error)
        }

        return errorMsg
    }


}