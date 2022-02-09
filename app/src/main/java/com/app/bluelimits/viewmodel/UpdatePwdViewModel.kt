package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.APIResponse
import com.app.bluelimits.model.PwdUpdateReq
import com.app.bluelimits.model.ResortApiService
import com.app.bluelimits.model.User
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class UpdatePwdViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var response = MutableLiveData<APIResponse>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun updatePwd(token: String, req: PwdUpdateReq)
    {
        loading.value = true
        resortService.updatePwd("Bearer " + token, req)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(result: APIResponse) {
                                setResponse(result)
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
            }

    }



    private fun setResponse(response: APIResponse)
    {
        this.response.value = response
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}