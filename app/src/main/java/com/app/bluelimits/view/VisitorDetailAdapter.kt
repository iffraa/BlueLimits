package com.app.bluelimits.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemVisitorDetailBinding
import com.app.bluelimits.databinding.ItemVisitorsBinding
import com.app.bluelimits.model.VisitorDetail
import com.app.bluelimits.model.VisitorRequest
import com.app.bluelimits.model.VisitorResult
import com.app.bluelimits.util.loadImage
import kotlin.collections.ArrayList

class VisitorDetailAdapter(val visitors: ArrayList<VisitorDetail>, context: Context) :
    RecyclerView.Adapter<VisitorDetailAdapter.VisitorViewHolder>() {
    private var mContext = context
    private var _binding: ItemVisitorDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun setVisitorDetail(newVistitorsList: List<VisitorDetail>) {
        visitors.addAll(newVistitorsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorDetailBinding.inflate(inflater,parent,false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {

        val visitorsData = visitors.get(position)

        val name = holder.view.tvName
        val id = holder.view.tvId
        val contact = holder.view.tvMobile
        val qrImg = holder.view.ivQr
        val gender = holder.view.tvGender
        val whoPays = holder.view.tvWho
        val status = holder.view.tvStatus


        name.setText(mContext.getString(R.string.name_visi) +": "+ visitorsData.name)
        id.setText(mContext.getString(R.string.id_no) +": "+ visitorsData.id_no)
        contact.setText(mContext.getString(R.string.mobile) +": "+ visitorsData.contact_no)
        gender.setText(mContext.getString(R.string.gender) +": "+ visitorsData.gender)
        status.setText("Status: "+ visitorsData.status)
        whoPays.setText("Who will Pay: "+ visitorsData.who_will_pay)
        visitorsData.qr_code?.let { loadImage(qrImg, it, mContext) }


    }

    override fun getItemCount() = visitors.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    class VisitorViewHolder(val view: ItemVisitorDetailBinding) : RecyclerView.ViewHolder(view.root)
    {

    }

}