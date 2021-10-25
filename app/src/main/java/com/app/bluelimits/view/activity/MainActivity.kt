package com.app.bluelimits.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.app.bluelimits.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions

import android.media.MediaPlayer
import androidx.fragment.app.FragmentManager
import com.app.bluelimits.databinding.ActivityMainBinding
import com.app.bluelimits.util.Constants


class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }

    private fun loadGif() {
        Glide.with(this).asGif()
            .load(R.raw.waves)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            //.skipMemoryCache(true)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    p0: GlideException?,
                    p1: Any?,
                    p2: Target<GifDrawable>?,
                    p3: Boolean
                ): Boolean {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    p1: Any?,
                    p2: Target<GifDrawable>?,
                    p3: DataSource?,
                    p4: Boolean
                ): Boolean {
                    playAudio()
                    (resource as GifDrawable).setLoopCount(1)
                    resource.registerAnimationCallback(object :
                        Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable) {
                            binding.llBtns.visibility = View.VISIBLE
                        }
                    })
                    return false
                }
            })
            .into(binding.ivWaves)
    }

    fun onLoginClick(v: View) {
        startDashboardActivity(true)
    }

    fun onApplyClick(v: View) {
        startDashboardActivity(false)
    }

    fun startDashboardActivity(isLogin: Boolean) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra(Constants.IS_LOGIN, isLogin)
        startActivity(intent)
    }

    fun playAudio() {
        mediaPlayer = MediaPlayer.create(this, com.app.bluelimits.R.raw.waves_audio)
        mediaPlayer.start()
    }

    override fun onResume() {
        super.onResume()

        loadGif()
       // binding.llBtns.visibility = View.VISIBLE

    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        binding.llBtns.visibility = View.GONE

    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity();
    }
}