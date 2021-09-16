package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemAddFamilyBinding
import com.app.bluelimits.model.FamilyMember
import com.app.bluelimits.model.FamilyMemberRequest
import com.app.bluelimits.model.User
import com.app.bluelimits.util.hideKeyboard
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class FamilyListAdapter(val famList: ArrayList<FamilyMemberRequest>) :
    RecyclerView.Adapter<FamilyListAdapter.FamilyViewHolder>() {

    private var _binding: ItemAddFamilyBinding? = null
    private val binding get() = _binding!!
    private val enteredData: ArrayList<FamilyMemberRequest> = arrayListOf()
    private lateinit var context: Context

    fun setFamilyList(newFamList: ArrayList<FamilyMemberRequest>, context: Context) {

        this.context = context

        enteredData.clear()
        famList.clear()
        famList.addAll(newFamList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemAddFamilyBinding.inflate(inflater)
        return FamilyViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {

        val person: FamilyMemberRequest = famList.get(position)

        var et_fName: EditText = holder.view.etFname
        val et_lName: EditText = holder.view.etLname
        val et_email: EditText = holder.view.etEmail
        val et_dob: EditText = holder.view.etDob
        val et_id: EditText = holder.view.etId
        val et_mobile: EditText = holder.view.etMobile

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale

        et_fName.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                person.first_name = et_fName.text.toString()
            }

        et_lName.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                person.last_name =  et_lName.text.toString()
            }

        et_mobile.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                person.contact_no = et_mobile.text.toString()
            }

        et_dob.setOnClickListener(View.OnClickListener {
            showDate(person,et_dob)
        })

        et_id.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                if(!et_id.text.toString().isEmpty())
                    person.member_id = et_id.text.toString().toInt()
            }

        person.gender = com.app.bluelimits.util.getGender(cb_female,cb_male)

        enteredData.add(person)
    }

    fun getData(): ArrayList<FamilyMemberRequest>
    {
        return enteredData
    }

    override fun getItemCount() = famList.size

    class FamilyViewHolder(val view: ItemAddFamilyBinding) : RecyclerView.ViewHolder(view.root)

    private fun showDate(member: FamilyMemberRequest, editText: EditText) {

        hideKeyboard(context as Activity)

        val d = Date()
        val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

        dateDialog.title(context.getString(R.string.select_date))
            .titleTextColor(context.getResources().getColor(R.color.white))
            .displayHours(false)
            .displayMinutes(false)
            .displayDays(false)
            .displayMonth(true)
            .displayYears(true)
            .displayDaysOfMonth(true)

            .backgroundColor(context.getResources().getColor(R.color.white))
            .mainColor(context.getResources().getColor(R.color.blue_text))
            .listener { date ->
                val DATE_FORMAT = "yyyy-MM-dd"
                var sdf = SimpleDateFormat(DATE_FORMAT)
                val sdate = sdf.format(date)

                member.birth_date = sdate
                editText.setText(sdate)
            }.display()

    }



}

