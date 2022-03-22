package com.app.bluelimits.util

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.app.bluelimits.R
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import java.text.SimpleDateFormat
import java.util.*

import android.text.TextUtils

import androidx.core.view.ViewCompat
import androidx.navigation.NavDirections
import com.app.bluelimits.model.Guest
import com.app.bluelimits.model.Visitor
import com.app.bluelimits.view.activity.DashboardActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import org.json.JSONException
import org.json.JSONObject

fun isValidID(id: String): Boolean {
    if (id.length < 10)
        return false
    return true
}

fun checkGuestsID(guests: ArrayList<Guest>, context: Context): String {
    for (guest in guests) {
        val id = guest.id_no
        if (!id?.let { isValidID(it) }!!)
            return context.getString(R.string.id_length_error)
    }

    return ""
}

fun willSenderPay(visitors: ArrayList<Visitor>): Boolean {
    for (visitor in visitors) {
        val payment = visitor.who_will_pay
        if (payment == "sender") {
            return true
        }
    }

    return false
}

fun getPayableAmount(visitors: ArrayList<Visitor>): String {
    var price = 0
    for (visitor in visitors) {

        if (!visitor.price.isNullOrEmpty()) {
            val payment = visitor.who_will_pay
            if (payment == "sender") {
                val amount = visitor.price.toInt()
                price += amount
            }
        }
    }
    return price.toString()
}

fun checkVisitorsID(visitors: ArrayList<Visitor>, context: Context): String {
    for (guest in visitors) {
        val id = guest.id_no
        if (!isValidID(id))
            return context.getString(R.string.id_length_error)
    }

    return ""
}

fun loadGif(view: ImageView, resId: Int, context: Context) {
    Glide.with(context).asGif()
        .load(resId)
        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
        .skipMemoryCache(true)
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
                (resource as GifDrawable).setLoopCount(1)

                return false
            }
        })
        .into(view)

}

fun loadImage(view: ImageView, url: String, context: Context) {
    Glide.with(context)
        .load(url)
        .into(view)
    view.clipToOutline = true

}

fun showAlertDialog(activity: Activity, title: String, msg: String) {
    val builder: AlertDialog.Builder? = activity?.let {
        AlertDialog.Builder(it)
    }

    builder?.setMessage(msg)
        ?.setTitle(title)?.setPositiveButton(
            R.string.ok
        ) { _, _ ->
        }
    builder?.create()?.show()
}

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()


fun ImageView.loadImage(uri: String?) {
    val options = RequestOptions()
        .error(R.mipmap.ic_launcher)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

@BindingAdapter("android:imageUrl")
fun loadViewImage(view: ImageView, url: String?) {
    view.loadImage(url)
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}


fun selectDOB(context: Context, dobField: EditText) {
    hideKeyboard(context as Activity)

    val newCalendar: Calendar = Calendar.getInstance()
    var mDatePickerDialog: DatePickerDialog
    mDatePickerDialog = context?.let {
        DatePickerDialog(
            it,
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                val sd = SimpleDateFormat("dd-MM-yyyy")

                val startDate: Date = newDate.getTime()
                val fdate: String = sd.format(startDate)
                dobField.setText(fdate)
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)


        )
    }!!

    mDatePickerDialog?.getDatePicker()//?.setMaxDate(System.currentTimeMillis())
    mDatePickerDialog.show()
}

fun getGender(femaleChkBx: CheckBox, maleChkBx: CheckBox): String {
    var gender = ""

    femaleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            maleChkBx.setChecked(false)
            gender = Constants.FEMALE
        }

    })

    maleChkBx.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            femaleChkBx.setChecked(false)
            gender = Constants.MALE
        }
    })
    return gender
}

fun setHomeNavigation(activity: Activity, action: NavDirections) {
    (activity as DashboardActivity).onCustomTBIconClick(action)

}

fun getServerErrors(errorJson: String, context: Context): String {
    var message = ""
    try {
        val json = JSONObject(errorJson)
        var errors: ArrayList<String> = arrayListOf()
        val iter: Iterator<String> = json.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            try {
                var value = json.get(key)

                val filtered = "[]\""
                value = value.toString().filterNot { filtered.indexOf(it) > -1 }

                errors.add(value)
            } catch (e: JSONException) {
                // Something went wrong!
            }
        }

        for (error in errors) {
            message += error + "\n"
        }
    } catch (ex: JSONException) {
        message = ex.toString()
    }

    return message
}

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun showDateTime(context: Context, editText: EditText) {

    hideKeyboard(context as Activity)

    val d = Date()
    val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

    dateDialog.title(context.getString(R.string.select_date))
        .titleTextColor(context.getResources().getColor(R.color.white))
        .minutesStep(1)
        .minDateRange(d)
        .backgroundColor(context.getResources().getColor(R.color.white))
        .mainColor(context.getResources().getColor(R.color.blue_text))
        .listener { date ->
            val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm aa"
            val sdf = SimpleDateFormat(DATE_TIME_FORMAT)
            val sdate = sdf.format(date)
            editText.setText(sdate)
        }.display()

}

fun showBirthdayDialog(context: Context, editText: EditText) {

    hideKeyboard(context as Activity)

    val d = Date()
    val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

    dateDialog.title(context.getString(R.string.select_date))
        .titleTextColor(context.getResources().getColor(R.color.white))
        .displayHours(false)
        .displayMinutes(false)
        .maxDateRange(d)

        .displayDays(false)
        .displayMonth(true)
        .displayYears(true)
        .displayDaysOfMonth(true)
        .backgroundColor(context.getResources().getColor(R.color.white))
        .mainColor(context.getResources().getColor(R.color.blue_text))
        .listener { date ->
            val DATE_FORMAT = "yyyy-MM-dd"
            var sdf = SimpleDateFormat(DATE_FORMAT)
            val sdate = sdf.format(date)
            editText.setText(sdate)
        }.display()

}

fun EditText.removeUnderline() {
    val paddingBottom = this.paddingBottom
    val paddingStart = ViewCompat.getPaddingStart(this)
    val paddingEnd = ViewCompat.getPaddingEnd(this)
    val paddingTop = this.paddingTop
    ViewCompat.setBackground(this, null)
    ViewCompat.setPaddingRelative(this, paddingStart, paddingTop, paddingEnd, paddingBottom)
}


