package com.app.bluelimits.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.showAlertDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException


class VisitorEditViewModel(application: Application): BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()
    var resorts = MutableLiveData<ArrayList<Resort>>()
    var totalVisitors = MutableLiveData<TotalVisitorsResponse>()

    var message = MutableLiveData<String>()
    var malePackage = MutableLiveData<ServicePackage>()
    var femalePackage = MutableLiveData<ServicePackage>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getCustomerResorts(token: String)
    {
        loading.value = true
        resortService.getCustomerResorts("Bearer " + token)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<ResortResponse>() {
                            override fun onSuccess(value: ResortResponse) {
                                resortsRetrieved(value.resort)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    private fun resortsRetrieved(resorts: ArrayList<Resort>)
    {
        this.resorts.value = resorts
        loadError.value = false
        loading.value = false

    }

    fun getMalePackage(token: String, datetime: String, resort_id: String)
    {
        loading.value = true
        resortService.getVisitorPackages("Bearer " + token,datetime,Constants.MALE,resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<VisitorPackage>() {
                            override fun onSuccess(response: VisitorPackage) {
                                packagesRetrieved(response.data, Constants.MALE)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    fun getFemalePackage(token: String, datetime: String, resort_id: String)
    {
        loading.value = true
        resortService.getVisitorPackages("Bearer " + token,datetime,Constants.FEMALE,resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<VisitorPackage>() {
                            override fun onSuccess(response: VisitorPackage) {
                                packagesRetrieved(response.data, Constants.FEMALE)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    private fun packagesRetrieved(servicePackages: ServicePackage, gender: String)
    {
        if(gender.equals(Constants.MALE))
             this.malePackage.value = servicePackages
        else
            this.femalePackage.value = servicePackages

        loadError.value = false
        loading.value = false

    }

    fun updateVisitor(token: String,
                   visitors: EditVisitorRequest,context: Context, inviteeID: String )
    {
        loading.value = true
        resortService.editVisitor("Bearer " + token,inviteeID, visitors)
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
                                if (e is HttpException) {
                                    val jObjError = JSONObject(e.response()?.errorBody()?.string())

                                    if (jObjError.has("errors")) {

                                        if (jObjError.has("errors")) {
                                            val errors  =
                                                jObjError.getJSONObject("errors").toString()
                                            showAlertDialog(context as Activity,
                                                context.getString(R.string.app_name),
                                                errors)

                                        }
                                    }
                                }
                               // showSuccessDialog(context as Activity, context.getString(R.string.app_name), context.getString(R.string.add_visitor_error))
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


    fun getTotalVisitors(token: String, date_time: String, resort_id: String)
    {
        loading.value = true
        resortService.getNoOfVisitors("Bearer " + token, date_time,resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<TotalVisitorsResponse>() {
                            override fun onSuccess(value: TotalVisitorsResponse) {
                                totalVisitorsRetrieved(value)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    private fun totalVisitorsRetrieved(totalVisitors: TotalVisitorsResponse)
    {
        this.totalVisitors.value = totalVisitors
        loadError.value = false
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}