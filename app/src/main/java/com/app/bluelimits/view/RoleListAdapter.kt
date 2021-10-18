package com.app.bluelimits.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ItemRoleBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.view.fragment.ResortFacilitiesFragmentDirections
import java.util.*
import kotlin.collections.ArrayList

class RoleListAdapter(val roleList: ArrayList<Resort>, context: Context) :
    RecyclerView.Adapter<RoleListAdapter.RoleViewHolder>() {
    private var mContext = context
    private var _binding: ItemRoleBinding? = null
    private lateinit var resort: Resort

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun setRoleList(newRoleList: List<Resort>, resort: Resort) {
        this.resort = resort
        roleList.clear()
        roleList.addAll(newRoleList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoleViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemRoleBinding.inflate(inflater)
        return RoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.view.role = roleList.get(position)

        val role = roleList.get(position).name

        if (role.equals(mContext.getString(R.string.guest_visitor))) {
            holder.view.btnRole.setOnClickListener(View.OnClickListener {
                val action =
                    ResortFacilitiesFragmentDirections.actionResortFacilitiesFragToGuestFragment(resort
                    )
                Navigation.findNavController(holder.view.root).navigate(action)
            })
        } else {
            holder.view.btnRole.setOnClickListener(View.OnClickListener {
                val action =
                    ResortFacilitiesFragmentDirections.actionResortFacilitiesFragToUnitFormFragment(
                        resort,
                        roleList.get(position)
                    )
                Navigation.findNavController(holder.view.root).navigate(action)
            })
        }

    }

    override fun getItemCount() = roleList.size

    class RoleViewHolder(val view: ItemRoleBinding) : RecyclerView.ViewHolder(view.root)

    /* override fun onResortClicked(v: View) {
         val resortName = v.res
         val action = HomeFragmentDirections.actionNavHomeToResortInfoFrag(resortName)
         Navigation.findNavController(v).navigate(action)
     }*/
}