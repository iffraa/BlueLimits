package com.app.bluelimits.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.databinding.ItemMemberBinding
import com.app.bluelimits.model.FamilyMember
import kotlin.collections.ArrayList

class FMemberListAdapter(val members: ArrayList<FamilyMember>) :
    RecyclerView.Adapter<FMemberListAdapter.FamilyViewHolder>() {

    private var _binding: ItemMemberBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context

    fun setFamilyList(mMembers: ArrayList<FamilyMember>) {
        members.clear()
        members.addAll(mMembers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemMemberBinding.inflate(inflater,parent,false)
        return FamilyViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        holder.view.familyMember = members.get(position)
    }

    override fun getItemCount() = members.size

    class FamilyViewHolder(val view: ItemMemberBinding) : RecyclerView.ViewHolder(view.root)

}

