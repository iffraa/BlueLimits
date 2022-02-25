package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.databinding.ItemServiceBinding
import com.app.bluelimits.model.ServiceEntry
import com.app.bluelimits.R
import kotlin.collections.ArrayList

class ServiceListAdapter(val services: ArrayList<ServiceEntry>) :
    RecyclerView.Adapter<ServiceListAdapter.ServiceViewHolder>() {

    private var _binding: ItemServiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context

    fun setServicesList(newServicesList: ArrayList<ServiceEntry>, context: Context) {

        mContext = context
        services.clear()
        services.addAll(newServicesList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemServiceBinding.inflate(inflater,parent,false)
        return ServiceViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.view.service = services.get(position)

        val status = services.get(position).status
        setStatusButton(status,holder)

    }

    private fun setStatusButton(status: String, holder: ServiceViewHolder)
    {
        when(status)
        {
            mContext.getString(R.string.completed) ->
            {
                holder.view.btnService.setBackgroundColor(mContext.getColor(R.color.green))
            }
            mContext.getString(R.string.pending) ->
            {
                holder.view.btnService.setBackgroundColor(mContext.getColor(R.color.pink))
            }
            mContext.getString(R.string.rejected) ->
            {
                holder.view.btnService.setBackgroundColor(mContext.getColor(android.R.color.holo_red_dark))
            }
            mContext.getString(R.string.in_progress) ->
            {
                holder.view.btnService.setBackgroundColor(mContext.getColor(R.color.yellow))
            }


        }
    }

    override fun getItemCount() = services.size

    class ServiceViewHolder(val view: ItemServiceBinding) : RecyclerView.ViewHolder(view.root)

}

