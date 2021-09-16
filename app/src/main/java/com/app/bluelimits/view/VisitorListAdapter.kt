package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemVisitorBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.ServicePackage
import com.app.bluelimits.model.Visitor
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.fragment.VisitorInviteFragment
import com.app.bluelimits.viewmodel.VisitorInviteViewModel
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class VisitorListAdapter(
    val visitorList: ArrayList<Visitor>,
    mContext: Context,
    visitorInviteFragment: VisitorInviteFragment
) :
    RecyclerView.Adapter<VisitorListAdapter.VisitorViewHolder>() {

    private var _binding: ItemVisitorBinding? = null
    private val binding get() = _binding!!
    private val enteredData: ArrayList<Visitor> = arrayListOf()
    private val context: Context = mContext
    private val visitorFrag: VisitorInviteFragment = visitorInviteFragment
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var data: Data
    private lateinit var dateTime: String
    private lateinit var servicePackage: ServicePackage


    fun setVisitorList(newFamList: ArrayList<Visitor>) {
        prefsHelper = SharedPreferencesHelper(context)
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        data = gson.fromJson(data_string, Data::class.java)

        dateTime =  visitorFrag.getDateTime()
               //dateTime = "2021-08-02 04:01 PM"

        enteredData.clear()
        visitorList.clear()
        visitorList.addAll(newFamList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorBinding.inflate(inflater)
        return VisitorViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {

        val visitor: Visitor = visitorList.get(position)

        val et_name: EditText = holder.view.etVisitorsName
        val et_id: EditText = holder.view.etVisitorsId
        val et_mobile: EditText = holder.view.etVisitorsMobile

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale
        val cb_pay: CheckBox = holder.view.cbPay

        val btn_visitor: Button = holder.view.btnVisitor
        btn_visitor.setText(context.getString(R.string.visitor) + " " + (position + 1))

        et_name.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                visitor.name = et_name.text.toString()
            }

        et_mobile.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                visitor.contact_no = et_mobile.text.toString()
            }

        et_id.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                if(!et_id.text.toString().isEmpty())
                    visitor.id_no = et_id.text.toString().toInt().toString()
            }


        fetchGenderPackage(cb_female,cb_male,visitor)
        visitor.who_will_pay = getWhoWillPay(cb_pay)
        enteredData.add(visitor)
    }

    fun fetchGenderPackage(femaleChkBx: CheckBox, maleChkBx: CheckBox,visitor: Visitor): String {
        var gender = Constants.MALE
        val viewModel = ViewModelProvider(visitorFrag).get(VisitorInviteViewModel::class.java)
        var isChkBoxChkd = false

        femaleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                maleChkBx.setChecked(false)
                gender = Constants.FEMALE
                visitor.gender = gender
                data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                observeViewModel(viewModel,visitor)
            }

        })

        maleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                femaleChkBx.setChecked(false)
                gender = Constants.MALE
                visitor.gender = gender
                data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                observeViewModel(viewModel,visitor)
            }
        })

        if(!isChkBoxChkd) {
            visitor.gender = gender
            data.token?.let {
                viewModel.getPackages(
                    it,
                    gender,
                    dateTime,
                    data.user?.resort_id.toString()
                )
            }
            observeViewModel(viewModel, visitor)

        }

        return gender
    }


    fun getData(): ArrayList<Visitor>
    {
        return enteredData
    }

    fun observeViewModel(viewModel: VisitorInviteViewModel, visitor: Visitor) {

        viewModel.loadError.observe(visitorFrag, Observer { isError ->
            isError?.let {
                if (it) {
                    Constants.isLoggedIn = false
                    showAlertDialog(
                        context as Activity,
                        context.getString(R.string.app_name),
                        context.getString(R.string.loading_error)
                    )
                }
            }
        })

        viewModel.packages.observe(visitorFrag, Observer { sPackage ->
            sPackage?.let {
                visitor.package_id = sPackage.id.toString()
                servicePackage = sPackage

            }

        })

    }

    fun getWhoWillPay(cb_pay: CheckBox): String
    {
        var who_will_pay = "sender"

        cb_pay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                who_will_pay = "visitor"
            }
        })

        return who_will_pay
    }

    fun getPackage() = servicePackage

    override fun getItemCount() = visitorList.size

    class VisitorViewHolder(val view: ItemVisitorBinding) : RecyclerView.ViewHolder(view.root)


}

