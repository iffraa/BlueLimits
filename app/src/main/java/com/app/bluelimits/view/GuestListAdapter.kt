package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemGuestBinding
import com.app.bluelimits.model.*
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class GuestListAdapter() :
    RecyclerView.Adapter<GuestListAdapter.GuestViewHolder>() {

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

        _binding = ItemGuestBinding.inflate(inflater)
        return GuestViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {

        val guest: Guest = guestList.get(position)

        var et_name: EditText = holder.view.etGuestName
        val et_id: EditText = holder.view.etGuestId
        val et_contact: EditText = holder.view.etGuestMobile

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
                guest.contact_no = et_contact.text.toString()
            }

        et_id.textChanges()
            .debounce(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { textChanged ->
                if(!et_id.text.toString().isEmpty())
                    guest.id_no = et_id.text.toString()
            }

        guest.gender = com.app.bluelimits.util.getGender(cb_female,cb_male)

        enteredData.add(guest)
    }

    fun getData(): ArrayList<Guest>
    {
        return enteredData
    }

    override fun getItemCount() = guestList.size

    class GuestViewHolder(val view: ItemGuestBinding) : RecyclerView.ViewHolder(view.root)


}

