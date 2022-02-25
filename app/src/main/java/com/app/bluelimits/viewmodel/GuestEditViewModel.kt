package com.app.bluelimits.viewmodel


import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.app.bluelimits.R
import com.app.bluelimits.model.*
import com.app.bluelimits.util.showSuccessDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class GuestEditViewModel(application: Application) : BaseViewModel(application) {

    private val resortService = ResortApiService()
    private val disposable = CompositeDisposable()

    var availableUnit = MutableLiveData<AvailableUnit>()
    var resorts = MutableLiveData<ArrayList<Resort>>()
    val loadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()
    val message = MutableLiveData<String>()
    var errorMsg = MutableLiveData<String>()

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

    fun getAvailableUnits(
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
                                unitsRetrieved(value.data[0])
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

    private fun unitsRetrieved(availableUnits: AvailableUnit) {
        this.availableUnit.value = availableUnits
        loadError.value = false
        loading.value = false

    }

    fun updateGuests(
        rsrvID: String,
        token: String,
        guests: GHReservationRequest, context: Context
    ) {
        loading.value = true
        resortService.editGuest("Bearer " + token,rsrvID, guests)
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())?.let {
                disposable.add(
                    it
                        .subscribeWith(object : DisposableSingleObserver<APIResponse>() {
                            override fun onSuccess(response: APIResponse) {
                                guestListUpdated(response)
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
                                } else {
                                    e.message?.let { it1 ->
                                        showSuccessDialog(
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

    private fun guestListUpdated(response: APIResponse) {
        message.value = response.message
        loadError.value = false
        loading.value = false

    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}