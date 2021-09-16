package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.*
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class HomeViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var resorts = MutableLiveData<ArrayList<Resort>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

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

    fun getMemberResorts()
    {
        loading.value = true
        resortService.getMemberResorts()
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

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}