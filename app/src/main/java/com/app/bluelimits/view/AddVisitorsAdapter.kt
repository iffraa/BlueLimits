package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.app.bluelimits.util.*
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemVisitorBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.Guest
import com.app.bluelimits.model.ServicePackage
import com.app.bluelimits.model.Visitor
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.view.fragment.VisitorInviteFragment
import com.google.gson.Gson
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class AddVisitorsAdapter(
    val visitorList: ArrayList<Visitor>,
    mContext: Context,
    visitorInviteFragment: VisitorInviteFragment
) :
    RecyclerView.Adapter<AddVisitorsAdapter.VisitorViewHolder>() {

    private var isVisitorClicked = false
    private var isCBClicked = false
    private var _binding: ItemVisitorBinding? = null
    private val binding get() = _binding!!
    private val enteredData: ArrayList<Visitor> = arrayListOf()
    private val context: Context = mContext
    private val visitorFrag: VisitorInviteFragment = visitorInviteFragment
    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var data: Data
    private lateinit var dateTime: String
    private var packages: HashMap<String, ServicePackage> = hashMapOf()


    fun setVisitorList(newList: ArrayList<Visitor>, vPackages: HashMap<String, ServicePackage>) {
        packages = vPackages
        enteredData.clear()
        visitorList.clear()
        visitorList.addAll(newList)

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
        val et_mobile: EditText = holder.view.layoutMobile.etMobile
        val price: EditText = holder.view.etPay

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale
        //  val cb_sender_pay: CheckBox = holder.view.cbSenderPay
        //val cb_visitor_pay: CheckBox = holder.view.cbVisitorPay
        val payRg: RadioGroup = holder.view.radioGroup
        val rbSender: RadioButton = holder.view.cbSenderPay
        val rbVisitor: RadioButton = holder.view.cbVisitorPay

        val btn_visitor: Button = holder.view.btnVisitor
        btn_visitor.setText(context.getString(R.string.visitor) + " " + (position + 1))

        price.setText(visitor.price)

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
                visitor.contact_no =
                    context.getString(R.string.server_number_code) + et_mobile.text.toString()
            }

        et_id.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val id = et_id.text.toString()
                if (!id.isNullOrEmpty()) {
                    if (id.length < 10) {
                        showAlertDialog(
                            context as Activity,
                            context.getString(R.string.app_name),
                            context.getString(R.string.id_length_error)
                        )
                    } else
                        visitor.id_no = id
                }
            }

        fetchGenderPackage(cb_female, cb_male, visitor, price)
        if (!isContain(enteredData, visitor))
            enteredData.add(visitor)
        getWhoWillPay(rbSender, rbVisitor, visitor)

        price.setText(visitor.price)
    }

    private fun isContain(enteredList: ArrayList<Visitor>, visitor: Visitor): Boolean {
        for (enteredVisitor in enteredList) {
            val enteredName = enteredVisitor.name
            val name = visitor.name
            if (enteredName == name) {
                return true
            }
        }

        return false
    }

    private fun getWhoWillPay(senderRB: RadioButton, visitorRB: RadioButton, visitor: Visitor) {
        var who_will_pay = "visitor"
        /*   senderRB.setOnCheckedChangeListener { buttonView, isChecked ->
               hideKeyboard(context as Activity)
               isCBClicked = true
               if (isChecked) {
                   who_will_pay = "sender"
                   visitor.who_will_pay = who_will_pay
                   notifyDataSetChanged();
               }

           }

           visitorRB.setOnCheckedChangeListener { buttonView, isChecked ->
               isCBClicked = true
               hideKeyboard(context as Activity)
               if (isChecked) {
                   who_will_pay = "visitor"
                   visitor.who_will_pay = who_will_pay
                   notifyDataSetChanged();
               }
           }*/

        senderRB.setOnClickListener {
            hideKeyboard(context as Activity)
            isCBClicked = true
            who_will_pay = "sender"
            visitor.who_will_pay = who_will_pay
            notifyDataSetChanged();


        }

        visitorRB.setOnClickListener {
            isCBClicked = true
            hideKeyboard(context as Activity)
            who_will_pay = "visitor"
            visitor.who_will_pay = who_will_pay
            notifyDataSetChanged();

        }
    }


    private fun fetchGenderPackage(
        femaleChkBx: CheckBox,
        maleChkBx: CheckBox,
        visitor: Visitor,
        price: EditText
    ): String {
        var gender = Constants.FEMALE
        var isChkBoxChkd = false

        femaleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                maleChkBx.setChecked(false)
                gender = Constants.FEMALE
                visitor.gender = gender

                setVisitorPackage(gender, visitor, price)

                //  data.token?.let { viewModel.getPackages(it,gender,dateTime, data.user?.resort_id.toString()) }
                //  observeViewModel(viewModel,visitor,price, position, holder)
            }

        }

        maleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                femaleChkBx.setChecked(false)
                gender = Constants.MALE
                visitor.gender = gender

                setVisitorPackage(gender, visitor, price)
            }
        }

        if (!isChkBoxChkd) {

            if (visitor.gender.isNullOrEmpty()) {
                visitor.gender = gender
            } else if (visitor.gender.equals(Constants.MALE)) {
                gender = Constants.MALE
                femaleChkBx.setChecked(false)
                maleChkBx.setChecked(true)

            } else if (visitor.gender.equals(Constants.FEMALE)) {
                gender = Constants.FEMALE
                femaleChkBx.setChecked(true)
                maleChkBx.setChecked(false)

            }

            setVisitorPackage(gender, visitor, price)

        }

        return gender
    }

    private fun setVisitorPackage(gender: String, visitor: Visitor, etPrice: EditText) {
        val visitorPckg = packages.get(gender)
        if (visitorPckg != null) {
            visitor.package_id = visitorPckg.id.toString()
            visitor.servicePackage = visitorPckg
            visitor.price = visitorPckg.price

            etPrice.setText(visitor.price)

            val pInfo = getDiscountInfo(visitor.price)
            visitor.discount = pInfo.first
            visitor.total = pInfo.second
        }
    }

    private fun getDiscountInfo(priceTxt: String): Pair<String, String> {
        var discount = 0
        var total = 0

        prefsHelper = context?.let { SharedPreferencesHelper(it) }!!
        val data_string = prefsHelper.getData(Constants.USER_DATA)
        val gson = Gson()
        data = gson.fromJson(data_string, Data::class.java)

        val serverDiscount = data.user?.invite_visitor_discount_percentage
        var customDiscount: Int = serverDiscount?.toInt()!!

        if (customDiscount != 0 && !priceTxt.isNullOrEmpty()) {
            discount = (customDiscount / 100) * priceTxt.toInt()
        }
        total = priceTxt.toInt() - discount

        return Pair(discount.toString(), total.toString())
    }

    fun getData(): ArrayList<Visitor> {
        return enteredData
    }


    fun clear() {

        enteredData.clear()
        val size: Int = visitorList.size
        if (size > 0) {
            for (i in 0 until size) {
                visitorList.removeAt(i)
            }

            notifyItemRangeRemoved(0, size)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = visitorList.size

    class VisitorViewHolder(val view: ItemVisitorBinding) : RecyclerView.ViewHolder(view.root)


}

