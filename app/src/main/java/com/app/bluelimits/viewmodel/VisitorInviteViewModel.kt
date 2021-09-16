package com.app.bluelimits.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.SharedPreferencesHelper
import com.app.bluelimits.util.showAlertDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers


class VisitorInviteViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var message = MutableLiveData<String>()
    var packages = MutableLiveData<ServicePackage>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getPackages(token: String, gender: String, datetime: String,resort_id: String)
    {
        loading.value = true
        resortService.getVisitorPackages("Bearer " + token,datetime,gender,resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<VisitorPackage>() {
                            override fun onSuccess(response: VisitorPackage) {
                                packagesRetrieved(response.data)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    private fun packagesRetrieved(servicePackages: ServicePackage)
    {
        this.packages.value = servicePackages
        loadError.value = false
        loading.value = false

    }

    fun addVisitor(token: String,
                   visitors: VisitorRequest?,context: Context)
    {
        loading.value = true
        resortService.addVisitor("Bearer " + token,visitors)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                visitorAdded(response)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                                showAlertDialog(context as Activity, context.getString(R.string.app_name), context.getString(R.string.add_visitor_error))
                            }

                        }))
            }

    }

    private fun visitorAdded(response: APIResponse)
    {
        message.value = response.message
        loadError.value = false
        loading.value = false

    }



    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}