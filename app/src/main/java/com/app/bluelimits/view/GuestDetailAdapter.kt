package com.app.bluelimits.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemVisitorDetailBinding
import com.app.bluelimits.model.Guest
import com.app.bluelimits.util.loadImage
import kotlin.collections.ArrayList

class GuestDetailAdapter(val guests: ArrayList<Guest>, context: Context) :
    RecyclerView.Adapter<GuestDetailAdapter.GuestViewHolder>() {
    private var mContext = context
    private var _binding: ItemVisitorDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun setGuestDetail(newVistitorsList: List<Guest>) {
        guests.addAll(newVistitorsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorDetailBinding.inflate(inflater,parent,false)
        return GuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {

        val guestsData = guests.get(position)

        val name = holder.view.tvName
        val id = holder.view.tvId
        val contact = holder.view.tvMobile
        val qrImg = holder.view.ivQr
        val gender = holder.view.tvGender
        val status = holder.view.tvStatus
        holder.view.tvWho.visibility = View.GONE


        name.setText(mContext.getString(R.string.name) +": "+ guestsData.name)
        id.setText(mContext.getString(R.string.id_no) +": "+ guestsData.id_no)
        contact.setText(mContext.getString(R.string.contact) +": "+ guestsData.contact_no)
        gender.setText(mContext.getString(R.string.gender) +": "+ guestsData.gender)
        status.setText("Status: "+ guestsData.status)
        guestsData.qr_code?.let { loadImage(qrImg, it, mContext) }


    }

    override fun getItemCount() = guests.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    class GuestViewHolder(val view: ItemVisitorDetailBinding) : RecyclerView.ViewHolder(view.root)
    {

    }

}