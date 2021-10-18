package com.app.bluelimits.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.databinding.ItemGuestRegistrationBinding
import com.app.bluelimits.model.GuestUnit
import com.app.bluelimits.view.fragment.HomeFragmentDirections
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.core.content.ContextCompat
import android.webkit.WebView
import androidx.databinding.DataBindingUtil.setContentView
import com.app.bluelimits.R
import androidx.core.content.ContextCompat.startActivity

import com.app.bluelimits.view.activity.Webview
import com.app.bluelimits.view.fragment.GuestRegistrationFragmentDirections


class UnitListAdapter(val unitList: ArrayList<GuestUnit>, val context: Context) :
    RecyclerView.Adapter<UnitListAdapter.UnitViewHolder>() {

    private var _binding: ItemGuestRegistrationBinding? = null
    private var mContext = context
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun updateUnitList(newUnitList: List<GuestUnit>) {
        unitList.clear()
        unitList.addAll(newUnitList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemGuestRegistrationBinding.inflate(inflater)
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.view.data = unitList.get(position)

        val pdf = unitList.get(position).pdf

        holder.view.btnBrochure.setOnClickListener(View.OnClickListener {

           /* val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.setDataAndType(Uri.parse(pdf), "application/pdf")

            val chooser = Intent.createChooser(browserIntent, "Choose")
            chooser.flags = FLAG_ACTIVITY_NEW_TASK // optional

            context.startActivity(chooser)*/

           /* val intent = Intent(context,
                Webview::class.java
            )
            intent.putExtra("url", pdf)
            context.startActivity(intent)*/

            val action = GuestRegistrationFragmentDirections.actionNavToBrochure(pdf)
            Navigation.findNavController(holder.view.root).navigate(action)


        })

    }

    override fun getItemCount() = unitList.size

    class UnitViewHolder(val view: ItemGuestRegistrationBinding) : RecyclerView.ViewHolder(view.root)

}