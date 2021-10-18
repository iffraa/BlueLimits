package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.GuestRegistrationResponse
import com.app.bluelimits.model.ResortApiService
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class GuestUnitsViewModel(application: Application): BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var units = MutableLiveData<GuestRegistrationResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getGuestUnits(resortId: String)
    {
        loading.value = true
        resortService.getGuestUnits(resortId)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                it
                    .subscribeWith(object : DisposableSingleObserver<GuestRegistrationResponse>() {
                        override fun onSuccess(response: GuestRegistrationResponse) {
                            unitsRetrieved(response)
                        }

                        override fun onError(e: Throwable) {
                            loadError.value = true
                            loading.value = false
                            e?.printStackTrace()
                        }

                    }))
            }

    }



    private fun unitsRetrieved(data: GuestRegistrationResponse)
    {
        this.units.value = data
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}