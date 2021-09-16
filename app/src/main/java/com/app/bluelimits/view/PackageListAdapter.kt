package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.PackageItemBinding
import com.app.bluelimits.model.ServicePackage
import android.content.ClipData.Item
import android.widget.CheckedTextView
import androidx.annotation.NonNull
import com.app.bluelimits.view.PackageListAdapter.OnItemCheckListener


class PackageListAdapter(val pckgList: ArrayList<ServicePackage>, val onItemCheckListener: OnItemCheckListener) :
    RecyclerView.Adapter<PackageListAdapter.PackageViewHolder>() {

    private var _binding: PackageItemBinding? = null
    private val binding get() = _binding!!
    private var package_id: String =""
    private var packagee: ServicePackage? =null
    private var selectedPosition = -1 // no selection by default

    interface OnItemCheckListener {
        fun onItemCheck(item: ServicePackage?)
        fun onItemUncheck(item: ServicePackage?)
    }

    @NonNull
    private var onItemClick: OnItemCheckListener? = null

    fun updatePckgList(newPckgList: ArrayList<ServicePackage>) {
        pckgList.clear()
        pckgList.addAll(newPckgList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = PackageItemBinding.inflate(inflater)
        return PackageViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.view.servicePackage = pckgList.get(position)

        holder.view.ctvPckgs.setChecked(selectedPosition == position);

        onItemClick = onItemCheckListener
        holder.view.ctvPckgs.setOnClickListener(View.OnClickListener {
            holder.view.ctvPckgs.toggle()
            selectedPosition = holder.getAdapterPosition();

            if(selectedPosition == position){
                holder.view.ctvPckgs.setChecked(true);
            }
            else{
                holder.view.ctvPckgs.setChecked(false);
            }

            notifyDataSetChanged()

            if(holder.view.ctvPckgs.isChecked) {
                onItemClick!!.onItemCheck(holder.view.servicePackage);
            }
            else
            {
                onItemClick!!.onItemUncheck(holder.view.servicePackage);
            }

        })

    }

    override fun getItemCount() = pckgList.size

    class PackageViewHolder(val view: PackageItemBinding) : RecyclerView.ViewHolder(view.root)

}