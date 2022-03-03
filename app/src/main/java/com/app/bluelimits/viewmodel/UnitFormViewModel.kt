package com.app.bluelimits.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.showAlertDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class UnitFormViewModel(application: Application) : BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var errorMsg = MutableLiveData<String>()
    var message = MutableLiveData<String>()
    var packages = MutableLiveData<ArrayList<ServicePackage>>()
    var services = MutableLiveData<ArrayList<Resort>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getServices(resort_id: String) {
        loading.value = true
        resortService.getServices(resort_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<ResortResponse>() {
                            override fun onSuccess(value: ResortResponse) {
                                servicesRetrieved(value.resort)
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        })
                )
            }

    }

    fun addMember(request: RegisterMemberRequest, context: Context) {
        loading.value = true
        resortService.addMember(
            request
        )
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                memberAdded(response)
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                //message.value = e.message
                                if (e is HttpException) {
                                    val jObjError = JSONObject(e.response()?.errorBody()?.string())

                                    if (jObjError.has("errors")) {

                                        if (jObjError.has("errors")) {
                                            errorMsg.value =
                                                jObjError.getJSONObject("errors").toString()
                                        }
                                    }
                                } else {
                                    e.message?.let { it1 ->
                                        showAlertDialog(
                                            context as Activity,
                                            context.getString(R.string.app_name), it1
                                        )
                                    }
                                }
                            }

                        })
                )
            }

    }

    private fun memberAdded(response: APIResponse) {
        message.value = response.message
        loadError.value = false
        loading.value = false

    }

    private fun servicesRetrieved(resorts: ArrayList<Resort>) {
        this.services.value = resorts
        loadError.value = false
        loading.value = false

    }

    fun getGuestPackages(service_id: String) {
        loading.value = true
        resortService.getGuestPackages(service_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<PackageResponse>() {
                            override fun onSuccess(value: PackageResponse) {
                                packagesRetrieved(value.data)
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        })
                )
            }

    }

    fun getMemberPackages(resort_id: String, role_id: String) {
        loading.value = true
        resortService.getMemberPackages(resort_id, role_id)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<PackageResponse>() {
                            override fun onSuccess(value: PackageResponse) {
                                packagesRetrieved(value.data)
                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                e?.printStackTrace()
                            }

                        })
                )
            }

    }


    private fun packagesRetrieved(servicePackages: ArrayList<ServicePackage>) {
        this.packages.value = servicePackages
        loadError.value = false
        loading.value = false

    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}