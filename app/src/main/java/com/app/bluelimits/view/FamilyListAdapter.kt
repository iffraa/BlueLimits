package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemAddFamilyBinding
import com.app.bluelimits.model.FamilyMember
import com.app.bluelimits.model.FamilyMemberRequest
import com.app.bluelimits.model.User
import com.app.bluelimits.model.Visitor
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.hideKeyboard
import com.app.bluelimits.util.isEmailValid
import com.app.bluelimits.util.showAlertDialog
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

    fun clear()
    {
        enteredData.clear()
        famList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemAddFamilyBinding.inflate(inflater, parent, false)
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
        val et_mobile: EditText = holder.view.layoutMobile.etMobile

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale

        et_email.textChanges()
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val email = et_email.text.toString()
              //  person.email = email

                 if (!email.isNullOrEmpty()) {
                     if (!email.isEmailValid()) {
                         showAlertDialog(
                             context as Activity, context.getString(R.string.app_name),
                             context.getString(R.string.email_error)
                         )
                     } else
                         person.email = email
                 }
            }


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
                person.last_name = et_lName.text.toString()
            }

        et_mobile.textChanges()
            .debounce(4, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val mobile = et_mobile.text.toString()
                if (!mobile.isNullOrEmpty()) {
                    if (mobile.length < 8) {
                        showAlertDialog(
                            context as Activity,
                            context.getString(R.string.app_name),
                            context.getString(R.string.contact_error)
                        )

                    } else
                        person.contact_no = mobile
                }

            }

        et_dob.setOnClickListener(View.OnClickListener {
            showDate(person, et_dob)
        })

        et_id.textChanges()
            .debounce(4, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val id = et_id.text.toString()

                  if (!id.isNullOrEmpty()) {
                      if (id.length < 10) {
                          showAlertDialog(
                              context as Activity, context.getString(R.string.app_name),
                              context.getString(R.string.id_length_error)
                          )

                      } else
                          person.member_id = id
                }
            }

        fetchGender(cb_female, cb_male,person)

        enteredData.add(person)
    }


    private fun fetchGender(
        femaleChkBx: CheckBox,
        maleChkBx: CheckBox,
        person: FamilyMemberRequest,
    ): String {
        var gender = Constants.FEMALE
        var isChkBoxChkd = false

        femaleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                maleChkBx.setChecked(false)
                gender = Constants.FEMALE
                person.gender = gender
            }

        }

        maleChkBx.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                femaleChkBx.setChecked(false)
                gender = Constants.MALE
                person.gender = gender

            }
        }

        if (!isChkBoxChkd) {

            if (person.gender.isNullOrEmpty()) {
                person.gender = gender
            } else
                if (person.gender.equals(Constants.MALE)) {
                    gender = Constants.MALE
                    femaleChkBx.setChecked(false)
                    maleChkBx.setChecked(true)

                } else if (person.gender.equals(Constants.FEMALE)) {
                    gender = Constants.FEMALE
                    femaleChkBx.setChecked(true)
                    maleChkBx.setChecked(false)

                }


        }

        return gender
    }
    fun getData(): ArrayList<FamilyMemberRequest> {
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
            .maxDateRange(d)

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

