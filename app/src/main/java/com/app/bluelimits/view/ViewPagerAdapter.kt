package com.app.bluelimits.view

import android.content.Context

import android.view.ViewGroup


import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.getSystemService

import androidx.viewpager.widget.PagerAdapter
import com.app.bluelimits.R
import java.util.*
import android.widget.LinearLayout





class ViewPagerAdapter(context: Context, images: IntArray) :
    PagerAdapter() {
    // Context object
    var context: Context

    // Array of images
    var images: IntArray

    // Layout Inflater
    var mLayoutInflater: LayoutInflater
    override fun getCount(): Int {
        // return the number of images
        return images.size
    }

    override fun isViewFromObject(@NonNull view: View, @NonNull `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    @NonNull
    override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
        // inflating the item.xml
        val itemView: View = mLayoutInflater.inflate(R.layout.item_slider, container, false)

      /*  val params = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 200
        )*/
        // referencing the image view from the item.xml file
        val imageView: ImageView = itemView.findViewById(R.id.iv_item) as ImageView
        // setting the image in the imageView
        imageView.setImageResource(images[position])
     //   imageView.layoutParams = params

        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    // Viewpager Constructor
    init {
        this.context = context
        this.images = images
        mLayoutInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?)!!
    }
}