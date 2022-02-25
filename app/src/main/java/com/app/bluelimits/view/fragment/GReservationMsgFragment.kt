package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentHomeBinding
import com.app.bluelimits.databinding.FragmentReservMsgBinding
import com.app.bluelimits.databinding.FragmentWebviewBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.util.*
import com.app.bluelimits.view.ResortListAdapter
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.viewmodel.HomeViewModel
import java.util.*

class GReservationMsgFragment : Fragment()  {

    private var pdf = ""
    private var _binding: FragmentReservMsgBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReservMsgBinding.inflate(inflater, container, false)
        val root: View = binding.root
     return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            pdf = it.getString("url").toString()

        }

       }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}