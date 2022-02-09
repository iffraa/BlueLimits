package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.*
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class VisitorsViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var delResponse = MutableLiveData<APIResponse>()
    var visitors = MutableLiveData<VisitorsData>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getVisitors(token: String)
    {
        loading.value = true
        resortService.getVisitors("Bearer $token")
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                it
                    .subscribeWith(object : DisposableSingleObserver<VisitorsResponse>() {
                        override fun onSuccess(value: VisitorsResponse) {
                            visitorsRetrieved(value)
                        }
                        override fun onError(e: Throwable) {
                            loading.value = false
                            e?.printStackTrace()
                        }

                    }))
            }

    }


    fun deleteVisitor(token: String, inviteeID: String )
    {
        loading.value = true
        resortService.deleteVisitor("Bearer $token", inviteeID)
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


    private fun visitorsRetrieved(visitor: VisitorsResponse)
    {
        this.visitors.value = visitor.data
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}