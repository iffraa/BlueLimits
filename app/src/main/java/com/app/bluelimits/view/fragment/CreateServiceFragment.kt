package com.app.bluelimits.view.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.BuildConfig
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentCreateServiceBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.ServicePackage
import com.app.bluelimits.util.*
import com.app.bluelimits.view.PackageListAdapter
import java.io.File

import java.util.*
import okhttp3.RequestBody

import com.google.gson.Gson

import okhttp3.MultipartBody


/**
 * A simple [Fragment] subclass.
 * Use the [AboutUsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateServiceFragment : Fragment() {

    private lateinit var binding: FragmentCreateServiceBinding
   // private lateinit var viewModel: CreateServiceViewModel
    private lateinit var packageListAdapter: PackageListAdapter
    private lateinit var serviceId: String
    private var packageId = "0"
    private lateinit var userData: Data
    private var prefsHelper = SharedPreferencesHelper()
    private var imgUri: Uri? = null
    private var cameraImgFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateServiceBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        getServices()
        selectPicture()

        binding.ivCross.setOnClickListener(View.OnClickListener {
            removeImg()
        })

        binding.etDate.setOnClickListener(View.OnClickListener {
            showDateTime(requireContext(), binding.etDate)
        })

        binding.btnListing.setOnClickListener(View.OnClickListener {
            viewServices(it)
        })

        binding.btnSubmit.setOnClickListener(View.OnClickListener {
            val date = binding.etDate.text.toString()
            binding.rlPb.visibility = View.VISIBLE

            if (!packageId.equals("0") && !date.isNullOrEmpty()) {

                val token = userData.token
                var imgFile: File? = null
                if (!binding.tvBrowse.text.equals(getString(R.string.browse))) {
                    if (cameraImgFile != null) {
                        imgFile = cameraImgFile
                    } else {
                    //    val path = imgUri?.let { it1 -> getGalleryImgPath(it1, requireContext()) }
                     //   imgFile = File(path)
                    }
                }
                var request = getRequest(imgFile)

             //   token?.let { it1 -> viewModel.addService(it1, request) }
                setServiceCreationObserver(it)
            } else {
                showSuccessDialog(
                    context as Activity,
                    getString(R.string.app_name),
                    getString(R.string.empty_fields)
                )

            }

        })

     //   val action = CreateServiceFragmentDirections.actionServiceToTour()
      //  onHomeIconClick(binding.rlFooter.ivHome, action)
    }

    private fun selectPicture() {
        binding.tvBrowse.setOnClickListener(View.OnClickListener {
            // pickImages.launch("image/*")
            /// val imgHelper = ImageHelper(requireActivity(), this, binding.tvBrowse,takePicture, pickImages)
            showImgChooserDialog()

        })
    }


    private fun getServices() {
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        userData = gson.fromJson(data_string, Data::class.java)

      //  userData.token?.let { viewModel.getServices(it) }
        setServicesObserver()
    }

    private fun getPackages(serviceId: String) {
        //userData.token?.let { viewModel.getPackages(it, serviceId) }
        setPckgsObserver()
    }

    private fun setServicesObserver() {
           }

    private fun setPckgsObserver() {

    }

    private fun populateSpinners() {

        var data: ArrayList<String>? = arrayListOf<String>()

      /*  val services = viewModel.getServices().value?.api_data?.resort
        services?.forEachIndexed { index, e ->
            services.get(index).name?.let {
                if (data != null) {
                    data.add(it)
                }
            }
        }*/

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
                    binding.spServices.adapter = adapter
                    onServiceSelection()

                }
        }


    }

    private fun onServiceSelection() {
        binding.spServices?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

            /*    val services = viewModel.getServices().value?.api_data?.resort
                services?.forEachIndexed { index, e ->
                    if (services.get(index).name?.equals(selectedItem) == true) {
                        serviceId = services.get(index).id.toString()
                        getPackages(serviceId)
                    }

                }*/
            }

        }

    }

    fun showSuccessDialog(view: View) {
        val builder: androidx.appcompat.app.AlertDialog.Builder? = activity?.let {
            androidx.appcompat.app.AlertDialog.Builder(it)
        }

        builder?.setMessage(getString(R.string.service_req_success))
            ?.setTitle(getString(R.string.app_name))?.setPositiveButton(R.string.ok,
                { dialog, id ->
                    viewServices(view)
                })
        builder?.create()?.show()
    }


    private fun setServiceCreationObserver(view: View) {
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
                    packageId = item?.id.toString()
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

        }


    }

    private fun setupViewModel() {

    }


//    --------chosing image dialog-------------

    fun showImgChooserDialog() {
        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        myAlertDialog.setTitle("Upload Pictures Option")
        myAlertDialog.setMessage("How do you want to set your picture?")
        myAlertDialog.setPositiveButton("Gallery",
            DialogInterface.OnClickListener { arg0, arg1 ->
                pickImages.launch("image/*")
                cameraImgFile = null

            })
        myAlertDialog.setNegativeButton("Camera",
            DialogInterface.OnClickListener { arg0, arg1 ->
                takePicture()
            })
        myAlertDialog.show()
    }

    fun takePicture() {
        val root =
            File(
                requireContext().getExternalFilesDir(null),
                BuildConfig.APPLICATION_ID + File.separator
            )

        root.mkdirs()
        val fname = "img_" + System.currentTimeMillis() + ".jpg"
        cameraImgFile = File(root, fname)
        imgUri = FileProvider.getUriForFile(
            Objects.requireNonNull(requireContext()),
            BuildConfig.APPLICATION_ID + ".provider", cameraImgFile!!
        );
        takePicture.launch(imgUri)
    }

    val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            val path = imgUri?.path
            binding.tvBrowse.setText(path)
            binding.ivCross.visibility = View.VISIBLE
        }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                imgUri = uri
                binding.tvBrowse.setText(it.path)
                binding.ivCross.visibility = View.VISIBLE
            }
        }


    private fun getRequest(featured_image: File?): RequestBody {
        val builder: okhttp3.MultipartBody.Builder =
            okhttp3.MultipartBody.Builder().setType(MultipartBody.FORM)

        builder.addFormDataPart("service_id", serviceId)
            .addFormDataPart("package_id", packageId)
            .addFormDataPart("details", binding.etComments.text.toString())
            .addFormDataPart("date_time", binding.etDate.text.toString())

        if (featured_image != null) {
            if (featured_image.exists()) {

                builder.addFormDataPart(
                    "image",
                    featured_image.getName(),
                    RequestBody.create(MultipartBody.FORM, featured_image)
                );
            }
        }


        val requestBody: RequestBody = builder.build()

        return requestBody
    }

    fun removeImg() {
        binding.tvBrowse.setText(getString(R.string.browse))
        binding.ivCross.visibility = View.GONE
    }

    fun viewServices(view: View) {
      //  val action = CreateServiceFragmentDirections.actionServiceFormToListing()
       // action?.let { Navigation.findNavController(view).navigate(it) }

    }
}


