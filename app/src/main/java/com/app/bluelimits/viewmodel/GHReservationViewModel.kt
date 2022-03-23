package com.app.bluelimits.viewmodel


import SingleLiveEvent
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

class GHReservationViewModel(application: Application) : BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var availableUnit = SingleLiveEvent<AvailableUnit>()
    var resorts = SingleLiveEvent<ArrayList<Resort>>()
    val loadError = SingleLiveEvent<Boolean>()
    val loading = SingleLiveEvent<Boolean>()
    val message = SingleLiveEvent<String>()
    var errorMsg = SingleLiveEvent<String>()

    fun getCustomerResorts(token: String) {
        loading.value = true
        resortService.getCustomerResorts("Bearer " + token)
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

                        })
                )
            }

    }

    fun getAvailableUnits(context: Context,
        token: String,
        resort_id: String,
        reservation_date: String,
        chk_out: String,
        discount: String,
        unitId: String
    ) {
        loading.value = true
        resortService.getAvailableUnits(
            "Bearer " + token,
            resort_id,
            reservation_date,
            chk_out,
            discount,
            unitId
        )
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<UnitsResponse>() {
                            override fun onSuccess(value: UnitsResponse) {
                                if(!value.data.isNullOrEmpty())
                                    unitsRetrieved(value.data[0])
                                else {
                                    loadError.value = true
                                    showAlertDialog(
                                        context as Activity,
                                        "Bluelimit",
                                        "Units not available for selected dates."
                                    )
                                }

                            }

                            override fun onError(e: Throwable) {
                                loading.value = false
                                loadError.value = true
                                e?.printStackTrace()
                            }

                        })
                )
            }

    }


    private fun resortsRetrieved(resorts: ArrayList<Resort>) {
        this.resorts.value = resorts
        loadError.value = false
        loading.value = false

    }

    private fun unitsRetrieved(availableUnits: AvailableUnit?) {
        this.availableUnit.value = availableUnits
        loadError.value = false
        loading.value = false

    }

    fun addGHReservation(
        token: String,
        guests: GHReservationRequest, context: Context
    ) {
        loading.value = true
            resortService.addGuestReservation("Bearer " + token, guests)
                ?.subscribeOn(Schedulers.newThread())
                ?.observeOn(AndroidSchedulers.mainThread())?.let {
                    disposable.add(
                        it
                            .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                                override fun onSuccess(response: APIResponse) {
                                    guestAdded(response)
                                }

                                override fun onError(e: Throwable) {
                                    if (e is HttpException) {
                                        val jObjError = JSONObject(e.response()?.errorBody()?.string())
                                        if (jObjError.has("errors")) {
                                            if (jObjError.has("errors")) {
                                                errorMsg.value =
                                                    jObjError.getJSONObject("errors").toString()
                                            }
                                        }
                                        else if(jObjError.has("message"))
                                        {
                                            showAlertDialog(
                                                context as Activity,
                                                context.getString(R.string.app_name), jObjError.getString("message")
                                            )
                                        }

                                    } else {
                                        e.message?.let { it1 ->
                                            showAlertDialog(
                                                context as Activity,
                                                context.getString(R.string.app_name), it1
                                            )
                                        }
                                    }
                                    loading.value = false
                                    loadError.value = true
                                    e?.printStackTrace()

                                }

                            })
                    )
                }

    }

    private fun guestAdded(response: APIResponse) {
        message.value = response.message
        loadError.value = false
        loading.value = false

    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}