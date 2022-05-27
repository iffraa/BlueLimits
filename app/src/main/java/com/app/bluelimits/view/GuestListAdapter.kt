package com.app.bluelimits.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemViewGuestBinding
import com.app.bluelimits.model.*
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.fragment.GuestsFragmentDirections
import com.app.bluelimits.viewmodel.GuestsViewModel
import com.google.gson.Gson
import kotlin.collections.ArrayList

class GuestListAdapter(val guests: ArrayList<GuestData>, context: Context, frag: Fragment) :
    RecyclerView.Adapter<GuestListAdapter.GuestViewHolder>() {
    private val mContext = context
    private var _binding: ItemViewGuestBinding? = null
    private val fragment = frag

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun setGuestList(newGuestsList: List<GuestData>) {
        guests.addAll(newGuestsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemViewGuestBinding.inflate(inflater,parent,false)
        return GuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {

        val guestsData = guests.get(position)

        val tvFacilityLoc = holder.view.tvFacility
        val tvUnit = holder.view.tvUnit
        val tvFromDate = holder.view.tvFrom
        val tvToDate = holder.view.tvTo
        val tvDays = holder.view.tvDays
        val tvGuests = holder.view.tvGuests

        val btnDetail = holder.view.btnView
        val btnDelete = holder.view.btnDelete
        val btnEdit = holder.view.btnEdit

        tvFacilityLoc.setText(mContext.getString(R.string.faciliti_loc) +" "+ guestsData.facility_location)
        tvUnit.setText(mContext.getString(R.string.unit) + " " + guestsData.unit_no)
        tvFromDate.setText(mContext.getString(R.string.from) + " " +  guestsData.from)
        tvToDate.setText(mContext.getString(R.string.to) + " " + guestsData.to)
        tvDays.setText(mContext.getString(R.string.no_of_days) + " " + guestsData.no_of_day)

        var guests = guestsData.no_of_guest
        if(guests.isNullOrEmpty())
            guests = "0"

        tvGuests.setText(mContext.getString(R.string.no_of_guests) + " " + guests)

        btnDetail.setOnClickListener{
            val action = GuestsFragmentDirections.actionViewDetail(guestsData)
            Navigation.findNavController(holder.view.root).navigate(action)
        }

        val paymentStatus = guestsData.payment_status

        if(paymentStatus == Constants.UN_PAID) {
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            btnDelete.setOnClickListener {
                showDeleteDialog(
                    mContext.getString(R.string.app_name),
                    mContext.getString(R.string.delete_msg_guest),
                    guestsData
                )
            }

            btnEdit.setOnClickListener {
                val action = GuestsFragmentDirections.actionEditGuests(guestsData)
                Navigation.findNavController(holder.view.root).navigate(action)
            }
        }
        else
        {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }

    }

    override fun getItemCount() = guests.size

    class GuestViewHolder(val view: ItemViewGuestBinding) : RecyclerView.ViewHolder(view.root)

    private fun showDeleteDialog(title: String, msg: String, guest: GuestData) {
        val builder: AlertDialog.Builder? = mContext?.let {
            AlertDialog.Builder(it)
        }

        builder?.setMessage(msg)
            ?.setTitle(title)?.setPositiveButton(R.string.yes
            ) { dialog, id ->

                val prefsHelper = mContext?.let { SharedPreferencesHelper(it) }!!
                val data_string = prefsHelper.getData(Constants.USER_DATA)
                val gson = Gson()
                val data: Data = gson.fromJson(data_string, Data::class.java)

                val viewModel = ViewModelProvider(fragment).get(GuestsViewModel::class.java)
                data.token?.let {
                    binding.rlInclude.visibility = View.VISIBLE
                    viewModel.deleteGuest(it, guest.id!!)
                    observeViewModel(viewModel, guest)
                }

            }
            ?.setNegativeButton(R.string.no
            ) { dialog, id ->
                dialog.dismiss()
            }
        builder?.create()?.show()
    }


    fun observeViewModel(viewModel: GuestsViewModel, guest: GuestData) {
        viewModel.delResponse.observe(fragment.viewLifecycleOwner) { data ->
            data?.let {
                binding.rlInclude.visibility = View.GONE
                showAlertDialog(
                    mContext as Activity,
                    mContext.getString(R.string.app_name),
                    data.message
                )

                guests.remove(guest)
                notifyDataSetChanged()
            }

        }

        viewModel.loadError.observe(fragment.viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(
                        mContext as Activity,
                        mContext.getString(R.string.app_name),
                        mContext.getString(R.string.delete_error_guest)
                    )
                }
            }
        })

    }

}