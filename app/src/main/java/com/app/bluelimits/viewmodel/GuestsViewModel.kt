package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.*
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class GuestsViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var delResponse = MutableLiveData<APIResponse>()
    var guests = MutableLiveData<ArrayList<GuestData>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()



    fun deleteGuest(token: String, reservID: String )
    {
        loading.value = true
        resortService.deleteGuest("Bearer $token", reservID)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(value: APIResponse) {
                                delResponse.value = value
                            }
                            override fun onError(e: Throwable) {
                                loadError.value = true
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    fun getGuests(token: String)
    {
        loading.value = true
        resortService.getGuests("bearer $token")
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<GuestsResponse>() {
                            override fun onSuccess(value: GuestsResponse) {
                                val data = value.data
                                guests.value = data.data
                                guestsRetrieved(guests.value!!)
                            }
                            override fun onError(e: Throwable) {
                                loadError.value = true
                                e?.printStackTrace()
                            }

                        }))
            }

    }

    private fun guestsRetrieved(response: ArrayList<GuestData>)
    {
        this.guests.value = response
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}