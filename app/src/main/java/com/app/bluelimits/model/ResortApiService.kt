package com.app.bluelimits.model

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class ResortApiService {

    private val BASE_URL = "http://saudiaweb.com/bluelimits/api/"

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(
            ResortAPI::class.java)

    fun login(user_name: String,password: String, user_type: String): Single<APIResponse?>? {
        return api.login(user_name,password,user_type)
    }

    fun getGuestResorts(): Single<ResortResponse> {
        return api.getGuestResorts()
    }

    fun getMemberResorts(): Single<ResortResponse> {
        return api.getMemberResorts()
    }

    fun getProfile(token: String): Single<APIResponse?>? {
      return api.getProfile(token)
  }

    fun getServices(resort_id: String): Single<ResortResponse> {
        return api.getServices(resort_id)
    }

    fun getGuestPackages(service_id: String): Single<PackageResponse> {
        return api.getGuestPackages(service_id)
    }

    fun getMemberPackages(resort_id: String, role_id: String): Single<PackageResponse> {
        return api.getMemberPackages(resort_id,role_id)
    }

    fun addMember(data: RegisterMemberRequest
    ): Single<APIResponse?>? {
        return api.addMember(data)
    }

    fun getVisitorPackages(token: String,date_time: String,gender: String,resort_id: String): Single<VisitorPackage> {
        return api.getVisitorPackages(token,date_time,gender,resort_id)
    }

    fun addVisitor(
        token:String,
        visitors: VisitorRequest?
    ): Single<APIResponse?>?
    {
        return api.addVisitor(token,visitors)
    }

    fun getCustomerResorts(token: String): Single<ResortResponse?>? {
        return api.getCustomerResorts(token)
    }

    fun getAvailableUnits(token: String, resort_id: String, reservation_date: String,chk_out: String): Single<UnitsResponse?>? {
        return api.getAvailableUnits(token,resort_id,reservation_date,chk_out)
    }

    fun addGuestReservation(
        token:String,
        guests: GHReservationRequest
    ): Single<APIResponse?>?
    {
        return api.addGuestReservation(token,guests)
    }

    fun addMarineApplication(data: MarineServiceRequest
    ): Single<APIResponse?>? {
        return api.addMarineApplication(data)
    }

    fun checkBookingAvailability(service_id: String,resort_unit_id: String, reservation_date_time: String, hour: String): Single<MarineBookingResponse?>? {
        return api.checkBookingAvailability(service_id,resort_unit_id,reservation_date_time,hour)
    }

    fun getUserRoles(): Single<ResortResponse?>?
    {
        return api.getUserRoles()
    }


}