package com.app.bluelimits.util

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
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
import android.widget.TimePicker

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher

import android.widget.DatePicker
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavDirections
import com.app.bluelimits.view.activity.DashboardActivity
import com.app.bluelimits.view.fragment.AboutUsFragment.Companion.newInstance
import java.lang.reflect.Array.newInstance
import com.app.bluelimits.view.activity.MainActivity
import com.app.bluelimits.view.fragment.ResortInfoFragmentDirections
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import java.text.ParseException


fun loadGif(view: ImageView, resId: Int, context: Context) {
    Glide.with(context).asGif()
        .load(resId)
        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
        .skipMemoryCache(true)
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
        ?.setTitle(title)?.setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { dialog, id ->
            })
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
    var gender = Constants.MALE

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

fun showDate(context: Context, editText: EditText) {

    hideKeyboard(context as Activity)

    val d = Date()
    val dateDialog = SingleDateAndTimePickerDialog.Builder(context)

    dateDialog.title(context.getString(R.string.select_date))
        .titleTextColor(context.getResources().getColor(R.color.white))
        .displayHours(false)
        .displayMinutes(false)
       // .minDateRange(d)

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

