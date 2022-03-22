package com.app.bluelimits.viewmodel


import SingleLiveEvent
import android.app.Application
import com.app.bluelimits.model.APIResponse
import com.app.bluelimits.model.ResortApiService
import com.app.bluelimits.model.User
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class LoginViewModel(application: Application) : BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var userLiveEvent = SingleLiveEvent<User>()
    val loadError = SingleLiveEvent<Boolean>()
    val loading = SingleLiveEvent<Boolean>()

    fun loginUser(user_name: String, pwd: String, type: String) {
        loading.value = true
        resortService.login(user_name, pwd, type)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                val gson = Gson()
                                val user_data = gson.toJson(response.data)
                                prefsHelper.saveData(user_data, Constants.USER_DATA)
                                response.data.user?.let { it1 -> userRetrieved(it1) }
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                                e?.printStackTrace()
                            }

                        })
                )
            }

    }


    fun userRetrieved(user: User) {
        this.userLiveEvent.value = user
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }


}