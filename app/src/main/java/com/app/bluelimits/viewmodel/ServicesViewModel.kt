package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.ResortApiService
import com.app.bluelimits.model.ServiceResponse
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class ServicesViewModel(application: Application): BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var servicesResponse = MutableLiveData<ServiceResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getAllServices(token: String, per_page: String, page: String)
    {
        loading.value = true
        resortService.getAllServices(token, per_page, page)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                it
                    .subscribeWith(object : DisposableSingleObserver<ServiceResponse>() {
                        override fun onSuccess(response: ServiceResponse) {
                            servicesRetrieved(response)
                        }

                        override fun onError(e: Throwable) {
                            loadError.value = true
                            loading.value = false
                            e?.printStackTrace()
                        }

                    }))
            }

    }


    private fun servicesRetrieved(serviceResponse: ServiceResponse)
    {
        this.servicesResponse.value = serviceResponse
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}