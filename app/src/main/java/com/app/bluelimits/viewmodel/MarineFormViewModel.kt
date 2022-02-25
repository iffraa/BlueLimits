package com.app.bluelimits.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.showSuccessDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MarineFormViewModel(application: Application): BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var bookingResponse = MutableLiveData<MarineBookingResponse>()
    var message = MutableLiveData<String>()
    var packages = MutableLiveData<ArrayList<ServicePackage>>()
    var services = MutableLiveData<ArrayList<Resort>>()
    var resorts = MutableLiveData<ArrayList<Resort>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getServices(resort_id: String)
    {
        loading.value = true
        resortService.getServices(resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<ResortResponse>() {
                            override fun onSuccess(value: ResortResponse) {
                                servicesRetrieved(value.resort)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    fun checkBookingAviability(service_id: String, resort_unit_id: String, reserv_date: String, hour: String)
    {
        loading.value = true
        resortService.checkBookingAvailability(service_id,resort_unit_id,reserv_date, hour)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<MarineBookingResponse>() {
                            override fun onSuccess(value: MarineBookingResponse) {
                                bookingResponse.value = value
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    fun addApplication(request: MarineServiceRequest,context: Context) {
        loading.value = true
        resortService.addMarineApplication(request
           )
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                memberAdded(response)
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                //message.value = e.message
                                showSuccessDialog(context as Activity, context.getString(R.string.app_name), context.getString(
                                    R.string.add_visitor_error))

                                e?.printStackTrace()
                            }

                        })
                )
            }

    }

    private fun memberAdded(response: APIResponse)
    {
        message.value = response.message
        loadError.value = false
        loading.value = false

    }

    private fun servicesRetrieved(resorts: ArrayList<Resort>)
    {
        this.services.value = resorts
        loadError.value = false
        loading.value = false

    }

    fun getGuestPackages(service_id: String)
    {
        loading.value = true
        resortService.getGuestPackages(service_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<PackageResponse>() {
                            override fun onSuccess(value: PackageResponse) {
                                packagesRetrieved(value.data)
                            }
                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    fun getGuestResorts()
    {
        loading.value = true
        resortService.getGuestResorts()
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

    private fun packagesRetrieved(servicePackages: ArrayList<ServicePackage>)
    {
        this.packages.value = servicePackages
        loadError.value = false
        loading.value = false

    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}