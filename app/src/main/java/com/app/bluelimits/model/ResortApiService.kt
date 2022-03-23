package com.app.bluelimits.model

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ResortApiService {

    private val BASE_URL = "http://bluelimits.net/api/"
    //val client =  OkHttpClient().connectTimeoutMi;
   /* private var api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(
            ResortAPI::class.java
        )*/

    private val api = getAPI()

    private fun getAPI(): ResortAPI{
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ResortAPI::class.java)

    }

    fun login(user_name: String, password: String, user_type: String): Single<APIResponse?>? {
        return api.login(user_name, password, user_type)
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
        return api.getMemberPackages(resort_id, role_id)
    }

    fun addMember(
        data: RegisterMemberRequest
    ): Single<APIResponse?>? {
        return api.addMember(data)
    }

    fun getVisitorPackages(
        token: String,
        date_time: String,
        gender: String,
        resort_id: String
    ): Single<VisitorPackage> {
        return api.getVisitorPackages(token, date_time, gender, resort_id)
    }

    fun addVisitor(
        token: String,
        visitors: VisitorRequest?
    ): Single<APIResponse?>? {
        return api.addVisitor(token, visitors)
    }

    fun getCustomerResorts(token: String): Single<ResortResponse?>? {
        return api.getCustomerResorts(token)
    }

    fun getAvailableUnits(
        token: String,
        resort_id: String,
        reservation_date: String,
        chk_out: String,
        discount: String,
        unitId: String
    ): Single<UnitsResponse?>? {
        return api.getAvailableUnits(token, resort_id, reservation_date, chk_out, discount, unitId)
    }

    fun addGuestReservation(
        token: String,
        guests: GHReservationRequest
    ): Single<APIResponse?>? {
        return api.addGuestReservation(token, guests)
    }

    fun addMarineApplication(
        data: MarineServiceRequest
    ): Single<APIResponse?>? {
        return api.addMarineApplication(data)
    }

    fun checkBookingAvailability(
        service_id: String,
        resort_unit_id: String,
        reservation_date_time: String,
        hour: String
    ): Single<MarineBookingResponse?>? {
        return api.checkBookingAvailability(service_id, resort_unit_id, reservation_date_time, hour)
    }

    fun getUserRoles(): Single<ResortResponse?>? {
        return api.getUserRoles()
    }

    fun getNoOfVisitors(
        token: String,
        visiting_date_time: String,
        resort_id: String
    ): Single<TotalVisitorsResponse> {
        return api.getNoOfVisitors(token, visiting_date_time, resort_id)
    }

    fun getGuestUnits(resort_id: String): Single<GuestRegistrationResponse> {
        return api.getGuestUnits(resort_id)
    }

    fun updatePwd(token: String, req: PwdUpdateReq): Single<APIResponse> {
        return api.updatePwd(token, req)
    }

    fun getVisitors(token: String): Single<VisitorsResponse> {
        return api.getVisitors(token)
    }

    fun deleteVisitor(token: String, inviteeID: String): Single<APIResponse> {
        return api.deleteVisitor(token, inviteeID)
    }

    fun editVisitor(
        token: String,
        inviteeID: String,
        visitors: EditVisitorRequest
    ): Single<APIResponse> {
        return api.editVisitor(token, inviteeID, visitors)
    }

    fun getUnitTypes(token: String): Single<SpaceResponse> {
        return api.getUnitTypes(token)
    }

    fun getAllServices(
        token: String,
        per_page: String,
        page: String
    ): Single<ServiceResponse> {
        return api.getAllServices(token, per_page, page)
    }

    fun getGuests(token: String): Single<GuestsResponse> {
        return api.getGuests(token)
    }

    fun deleteGuest(
        token: String,
        reservID: String
    ): Single<APIResponse> {
        return api.deleteGuest(token, reservID)
    }

    fun editGuest(
        token: String,
        rsrvID: String,
        guest: GHReservationRequest
    ): Single<APIResponse> {
        return api.editGuest(token, rsrvID, guest)
    }


}