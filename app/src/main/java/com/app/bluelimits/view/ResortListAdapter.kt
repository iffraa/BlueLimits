package com.app.bluelimits.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.databinding.ResortItemBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.util.loadImage
import com.app.bluelimits.view.fragment.HomeFragmentDirections
import java.util.*
import kotlin.collections.ArrayList

class ResortListAdapter(val resortList: ArrayList<Resort>) :
    RecyclerView.Adapter<ResortListAdapter.ResortViewHolder>() {

    private var _binding: ResortItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun updateResortList(newResortList: List<Resort>) {
      //  if(!newResortList.isNullOrEmpty() && newResortList.size > 1)
        ///    Collections.swap(newResortList,0,1)
        resortList.clear()
        resortList.addAll(newResortList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResortViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ResortItemBinding.inflate(inflater)
        return ResortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResortViewHolder, position: Int) {
        holder.view.resort = resortList.get(position)

        holder.view.ivResort.setOnClickListener(View.OnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToResortInfoFrag(resortList.get(position))
            val navC =  Navigation.findNavController(holder.view.root)
           navC.navigate(action)

        })

    }

    override fun getItemCount() = resortList.size

    class ResortViewHolder(val view: ResortItemBinding) : RecyclerView.ViewHolder(view.root)

   /* override fun onResortClicked(v: View) {
        val resortName = v.res
        val action = HomeFragmentDirections.actionNavHomeToResortInfoFrag(resortName)
        Navigation.findNavController(v).navigate(action)
    }*/
}