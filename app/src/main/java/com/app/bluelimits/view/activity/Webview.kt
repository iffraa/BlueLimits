package com.app.bluelimits.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.bluelimits.R

import android.webkit.WebView

import android.view.View
import com.app.bluelimits.view.fragment.GuestRegistrationFragment


class Webview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_webview)

        val intent = intent

        val pdf = intent.getStringExtra("url")
        val webview = findViewById<View>(com.app.bluelimits.R.id.web_view) as WebView
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdf")
      /*  val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.setDataAndType(Uri.parse(pdf), "application/pdf")

        val chooser = Intent.createChooser(browserIntent, "Choose")
        chooser.flags = FLAG_ACTIVITY_NEW_TASK // optional

        startActivity(chooser)*/
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val fragment = GuestRegistrationFragment()
        supportFragmentManager.beginTransaction().replace(com.app.bluelimits.R.id.container, fragment).commit()
    }
}