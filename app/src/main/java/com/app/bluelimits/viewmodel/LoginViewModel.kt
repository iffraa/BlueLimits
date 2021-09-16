package com.app.bluelimits.viewmodel


import android.app.Application
import androidx.lifecycle.MutableLiveData
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

class LoginViewModel(application: Application): BaseViewModel(application) {

    private var prefsHelper = SharedPreferencesHelper(getApplication())
    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var user = MutableLiveData<User>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun loginUser(user_name: String, pwd: String, type: String)
    {
        loading.value = true
        resortService.login(user_name,pwd,type)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                it
                    .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                        override fun onSuccess(response: APIResponse) {
                            val gson = Gson()
                            val user_data = gson.toJson(response.data)
                            prefsHelper.saveData(user_data,Constants.USER_DATA)

                          //  prefsHelper.saveData(response.data.token,Constants.token)
                            //val id = response.data.user.role_id
                            //prefsHelper.saveData(response.data.user.role_id.toString(),Constants.ROLE_ID)

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