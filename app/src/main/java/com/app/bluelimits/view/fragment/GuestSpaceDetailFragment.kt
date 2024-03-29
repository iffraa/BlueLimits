package com.app.bluelimits.view.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.app.bluelimits.R
import com.app.bluelimits.databinding.FragmentSpaceDetailBinding
import com.app.bluelimits.util.setHomeNavigation
import com.app.bluelimits.view.ViewPagerAdapter

class GuestSpaceDetailFragment : Fragment() {

    private lateinit var binding: FragmentSpaceDetailBinding
    private var mViewPagerAdapter: ViewPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    private var space = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpaceDetailBinding.inflate(layoutInflater)
        return binding.root

    }

    private fun setIslandData() {
        binding.tvBth.setText("1 Bathroom")
        binding.tvPool.setText("Terrace")
        binding.tvDoublebed.setText("Bedside Tables")
        binding.tvSofa.setText("Sofa")

        binding.tvDiningtbl.visibility = View.VISIBLE
        binding.llIsland.visibility = View.VISIBLE

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomeNavigation(context as Activity, AboutUsFragmentDirections.actionNavToHome())

        val spaceTypeID: String? = arguments?.getString("spaceId")
        space = arguments?.get("space").toString()

        if(space!!.equals("island"))
            setIslandData()

        setSliderImgs()

        binding.btnRsrv.setOnClickListener {
            val action = GuestSpaceDetailFragmentDirections.actionDetailToReservation(spaceTypeID!!)
            action?.let {
                try {
                    Navigation.findNavController(view).navigate(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    }


    private fun setSliderImgs() {
        val loftImages = getImages()

        mViewPager = binding.viewPager

        // Initializing the ViewPagerAdapter
        mViewPagerAdapter = ViewPagerAdapter(requireContext(), loftImages)

        // Adding the Adapter to the ViewPager
        mViewPager!!.adapter = mViewPagerAdapter

        binding.ivLeft.setOnClickListener {
            mViewPager!!.setCurrentItem(mViewPager!!.getCurrentItem() - 1, true);

        }

        binding.ivRight.setOnClickListener {
            mViewPager!!.setCurrentItem(mViewPager!!.getCurrentItem() + 1, true);

        }
    }


    private fun getImages(): IntArray {

        if (space!!.equals("loft")) {
            return intArrayOf(
                R.drawable.loft1,
                R.drawable.loft2,
                R.drawable.loft3,
                R.drawable.loft4,
                R.drawable.loft5,
                R.drawable.loft6
            )
        } else if (space!!.equals("villa")) {
            return intArrayOf(
                R.drawable.villa1,
                R.drawable.villa2,
                R.drawable.villa3,
                R.drawable.villa4,
                R.drawable.villa5,
                R.drawable.villa6
            )
        }
        else if (space!!.equals("island")) {
            return intArrayOf(
                R.drawable.island_1,
                R.drawable.island_2,
                R.drawable.island_3,
                R.drawable.island_4
              )
        }
        else {
            return intArrayOf(
                R.drawable.suite1,
                R.drawable.suite2,
                R.drawable.suite3,
                R.drawable.suite4,
                R.drawable.suite5,
                R.drawable.suite6
            )
        }

    }
}