package com.app.bluelimits.view

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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
import com.app.bluelimits.databinding.ItemVisitorsBinding
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.VisitorResult
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.showAlertDialog
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.view.fragment.VisitorsFragmentDirections
import com.app.bluelimits.viewmodel.VisitorsViewModel
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class VisitorsListAdapter(val visitors: ArrayList<VisitorResult>, context: Context, frag: Fragment) :
    RecyclerView.Adapter<VisitorsListAdapter.VisitorViewHolder>() {
    private val mContext = context
    private var _binding: ItemVisitorsBinding? = null
    private val fragment = frag

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun setVisitorList(newVistitorsList: List<VisitorResult>) {
        visitors.addAll(newVistitorsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorsBinding.inflate(inflater,parent,false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {

        val visitorsData = visitors.get(position)

        val etNoOfVisitors = holder.view.tvNoOfVisitors
        val etDate = holder.view.tvDate
        val btnDetail = holder.view.btnView
        val btnDelete = holder.view.btnDelete
        val btnEdit = holder.view.btnEdit

        etNoOfVisitors.setText(mContext.getString(R.string.no_of_visitors) +": "+ visitorsData.no_of_visitor)
        etDate.setText(mContext.getString(R.string.date_time) +": "+ visitorsData.visiting_date_time)

        btnDetail.setOnClickListener{
            val action = VisitorsFragmentDirections.actionViewDetail(visitorsData)
            Navigation.findNavController(holder.view.root).navigate(action)
        }

        btnDelete.setOnClickListener{
            showDeleteDialog(mContext.getString(R.string.app_name), mContext.getString(R.string.delete_msg),
                visitorsData)
        }

        btnEdit.setOnClickListener{
            val action = VisitorsFragmentDirections.actionEditVisitors(visitorsData)
            val navController = (mContext as DashboardActivity).getNavController()

            if (action != null &&
                navController.currentDestination?.id == R.id.nav_visitors
                && navController.currentDestination?.id != action.actionId
            ) {
                action?.let { navController.navigate(it) }
            } else {
                Timer().schedule(2000) {
                    action?.let { navController.navigate(it) }
                }
            }

        }

    }

    override fun getItemCount() = visitors.size

    class VisitorViewHolder(val view: ItemVisitorsBinding) : RecyclerView.ViewHolder(view.root)

    private fun showDeleteDialog(title: String, msg: String, visitor: VisitorResult) {
        val builder: AlertDialog.Builder? = mContext?.let {
            AlertDialog.Builder(it)
        }

        builder?.setMessage(msg)
            ?.setTitle(title)?.setPositiveButton(R.string.yes,
                DialogInterface.OnClickListener { dialog, id ->

                    val prefsHelper = mContext?.let { SharedPreferencesHelper(it) }!!
                    val data_string = prefsHelper.getData(Constants.USER_DATA)
                    val gson = Gson()
                    val data: Data = gson.fromJson(data_string, Data::class.java)

                    val viewModel = ViewModelProvider(fragment).get(VisitorsViewModel::class.java)
                    data.token?.let { viewModel.deleteVisitor(it, visitor.id.toString())
                    observeViewModel(viewModel,visitor )}

                })
            ?.setNegativeButton(R.string.no,
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })
        builder?.create()?.show()
    }


    fun observeViewModel(viewModel: VisitorsViewModel, visitor: VisitorResult) {
        viewModel.delResponse.observe(fragment.viewLifecycleOwner, Observer { data ->
            data?.let {
                binding.rlInclude.visibility = View.GONE
                showAlertDialog(
                    mContext as Activity,
                    mContext.getString(R.string.app_name),
                    data.message
                )

                visitors.remove(visitor)
                notifyDataSetChanged()
            }

        })

        viewModel.loadError.observe(fragment.viewLifecycleOwner, Observer { isError ->
            isError?.let {
                if (it) {
                    binding.rlInclude.visibility = View.GONE
                    showAlertDialog(
                        mContext as Activity,
                        mContext.getString(R.string.app_name),
                        mContext.getString(R.string.delete_error)
                    )
                }
            }
        })

    }


}