package com.app.bluelimits.view.fragment

import android.app.Activity
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentResortInfoBinding
import com.app.bluelimits.model.Resort
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.view.activity.DashboardActivity
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView





/**
 * A simple [Fragment] subclass.
 * Use the [ResortInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResortInfoFragment : Fragment() {

    private var _binding: FragmentResortInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var type: Resort

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getParcelable<Resort>(Constants.TYPE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResortInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when{
            type.name?.contains(Constants.OIA) == true -> setLayout(R.drawable.oia_logo,R.drawable.boho_slide1,getString(R.string.oia_info))
            type.name?.contains(Constants.BOHO) == true -> setLayout(R.drawable.boho_logo,R.drawable.boho_slide1,getString(R.string.boho_info))
            type.name?.contains(Constants.MARINE) == true -> setLayout(R.drawable.marine_logo,R.drawable.boho_slide1,getString(R.string.marine_info))
        }

        type.name?.let { setSlider(it) }

        if(type.name?.contains(Constants.MARINE) == true)
        {
            setReqTripLayout()
        }
        else {
            binding.ivArrow.setOnClickListener(View.OnClickListener {
                val action =
                    ResortInfoFragmentDirections.actionResortInfoToResortFacilitiesFrag(type)
                Navigation.findNavController(it).navigate(action)
            })
        }

        setHomeNavigation(context as Activity, ResortInfoFragmentDirections.actionNavToHome())
    }

    private fun setReqTripLayout()
    {
        binding.ivArrow.visibility = View.GONE
        binding.rlTrip?.visibility = View.VISIBLE
        binding.rlTrip.setOnClickListener(View.OnClickListener {
            val action =
                ResortInfoFragmentDirections.actionResortInfoToMarineForm()
            Navigation.findNavController(it).navigate(action)

        })

    }

    private fun setSlider(type: String)
    {
        val slider: SliderLayout = binding.slider
        if(!type.equals(Constants.MARINE)) {
            for (i in 1..5) {
                val imgSliderView = DefaultSliderView(context)
                //val imageName = type + "_slide" + i
                val imageName =  "boho_slide" + i
                val resources: Resources = requireContext().resources
                val resourceId: Int = resources.getIdentifier(
                    imageName, "drawable",
                    requireContext().packageName
                )
                imgSliderView.image(resourceId)
                slider.addSlider(imgSliderView)
            }
        }
        else{
            val imgSliderView = DefaultSliderView(context)
            imgSliderView.image(R.drawable.boho_slide1)
            slider.addSlider(imgSliderView)

        }

    }

    private fun setLayout(logo_img: Int, slider_img: Int,info: String)
    {
        binding.ivLogo.setImageResource(logo_img)
      //  binding.ivImg.setImageResource(slider_img)
        binding.tvInfo.setText(info)

    }

}