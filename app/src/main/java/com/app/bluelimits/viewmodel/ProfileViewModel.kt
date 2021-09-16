package com.app.bluelimits.viewmodel


import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.model.APIResponse
import com.app.bluelimits.model.Data
import com.app.bluelimits.model.ResortApiService
import com.app.bluelimits.model.User
import com.app.bluelimits.util.Constants
import com.app.bluelimits.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import com.google.gson.Gson




class ProfileViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var user = MutableLiveData<User>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getProfile(token: String)
    {
        loading.value = true
        token?.let {
            resortService.getProfile("Bearer " + token)
                ?.subscribeOn(Schedulers.newThread())
                ?.observeOn(AndroidSchedulers.mainThread())?.let {
                    disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                response.data.user?.let { it1 -> userRetrieved(it1) }
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                                e?.printStackTrace()
                            }

                        }))
                }
        }

    }



    private fun userRetrieved(user: User)
    {
        this.user.value = user
        loadError.value = false
        loading.value = false

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}