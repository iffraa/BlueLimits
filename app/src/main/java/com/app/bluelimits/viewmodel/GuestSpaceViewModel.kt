package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.*
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class GuestSpaceViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var unitTypes = MutableLiveData<ArrayList<SpaceType>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getUnitTypes(token: String)
    {
        loading.value = true
        resortService.getUnitTypes("bearer " + token)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                it
                    .subscribeWith(object : DisposableSingleObserver<SpaceResponse>() {
                        override fun onSuccess(response: SpaceResponse) {
                            unitRetrieved(response.data)
                        }

                        override fun onError(e: Throwable) {
                            loadError.value = true
                            loading.value = false
                            e?.printStackTrace()
                        }

                    }))
            }

    }


    private fun unitRetrieved(unitTypes: ArrayList<SpaceType>)
    {
        this.unitTypes.value = unitTypes
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}