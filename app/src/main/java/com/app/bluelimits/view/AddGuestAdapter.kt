package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemGuestBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.hideKeyboard
import com.app.bluelimits.util.showAlertDialog
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class AddGuestAdapter() :
    RecyclerView.Adapter<AddGuestAdapter.GuestViewHolder>() {

    private var _binding: ItemGuestBinding? = null
    private val binding get() = _binding!!
    private val enteredData: ArrayList<Guest> = arrayListOf()
    private val guestList = ArrayList<Guest>()
    private lateinit var context: Context

    fun setGuestList(newFamList: ArrayList<Guest>, context: Context) {
        this.context = context
        enteredData.clear()
        guestList.clear()
        guestList.addAll(newFamList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemGuestBinding.inflate(inflater, parent, false)
        return GuestViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {

        val guest: Guest = guestList.get(position)

        var et_name: EditText = holder.view.etGuestName
        val et_id: EditText = holder.view.etGuestId
        val et_contact: EditText = holder.view.layoutMobile.etMobile

        val cb_male: CheckBox = holder.view.checkboxMale
        val cb_female: CheckBox = holder.view.checkboxFemale

        val btn_visitor: Button = holder.view.btnGuest
        btn_visitor.setText(context.getString(R.string.guest) + " " + (position + 1))

        et_name.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                guest.name = et_name.text.toString()
            }

        et_contact.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                guest.contact_no = context.getString(R.string.server_number_code) + et_contact.text.toString()
            }

        et_id.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                val id = et_id.text.toString()
                if (!id.isNullOrEmpty()) {
                    if(id.length < 10){
                        showAlertDialog(context as Activity, context.getString(R.string.app_name), context.getString(R.string.id_length_error))
                    }
                    else
                        guest.id_no = id
                }

            }
        getGender(cb_female, cb_male, guest)

     //   if(!isContain(guest))
            enteredData.add(guest)


    }

    private fun getGender(femaleChkBx: CheckBox, maleChkBx: CheckBox, guest: Guest): String {
        var gender = Constants.MALE

        maleChkBx.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val isChecked: Boolean = maleChkBx.isChecked()
                if (isChecked) {
                    selectMale(femaleChkBx,guest)
                } else {
                    //checkBox clicked and unchecked
                    femaleChkBx.setChecked(true)
                    selectFemale(maleChkBx,guest)
                }
                hideKeyboard(context as Activity)

            }
        })

        femaleChkBx.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val isChecked: Boolean = femaleChkBx.isChecked()
                if (isChecked) {
                    selectFemale(maleChkBx,guest)
                } else {
                    //checkBox clicked and unchecked
                    maleChkBx.setChecked(true)
                    selectMale(femaleChkBx,guest)
                }

                hideKeyboard(context as Activity)

            }
        })

        return gender
    }

    private fun selectFemale(maleChkBx: CheckBox, guest: Guest){
        maleChkBx.setChecked(false)
        guest.gender = Constants.FEMALE
      //  notifyDataSetChanged();
    }

    private fun selectMale(femaleChkBx: CheckBox, guest: Guest){
        femaleChkBx.setChecked(false)
        guest.gender = Constants.MALE
     //   notifyDataSetChanged();

    }

    private fun isContain(guest: Guest): Boolean {
        for (enteredGuest in enteredData) {
            val enteredName = enteredGuest.name
            val name = guest.name
            if (enteredName == name) {
                return true
            }
        }

        return false
    }


    fun getData(): ArrayList<Guest>
    {
        return enteredData
    }

    override fun getItemCount() = guestList.size

    class GuestViewHolder(val view: ItemGuestBinding) : RecyclerView.ViewHolder(view.root)


}

