package com.app.bluelimits.model

import io.reactivex.Single
import retrofit2.http.*

import retrofit2.http.POST
import retrofit2.http.GET

interface ResortAPI {

    @FormUrlEncoded
    @POST("customer/login?")
    fun login(
        @Field("user_name") user_name: String?,
        @Field("password") password: String?,
        @Field("user_type") type: String?

    ): Single<APIResponse?>?


    @Headers("Accept: application/json")
    @POST("customer/me?")
    fun getProfile(@Header("Authorization") token: String): Single<APIResponse?>?

    @GET("guest/facilities?")
    fun getGuestResorts(
    ): Single<ResortResponse>

    @GET("guest/member/reg/facilities?")
    fun getMemberResorts(
    ): Single<ResortResponse>

    @GET("guest/services/{resort_id}")
    fun getServices(@Path("resort_id") id: String): Single<ResortResponse>

    @GET("guest/packages/{service_id}")
    fun getGuestPackages(@Path("service_id") id: String): Single<PackageResponse>

    @GET("guest/member/services/{service_id}/packages/{role_id}")
    fun getMemberPackages(
        @Path("service_id") service_id: String,
        @Path("role_id") role_id: String
    ): Single<PackageResponse>

    @Headers("Accept: application/json")
    @POST(" guest/member/register?")
    fun addMember(
        @Body request: RegisterMemberRequest,
    ): Single<APIResponse?>?


    @Headers("Accept: application/json")
    @GET("customer/visitor/packages?")
    fun getVisitorPackages(
        @Header("Authorization") token: String,
        @Query("visiting_date_time") date_time: String,
        @Query("visitor_gender") gender: String,
        @Query("resort_id") resort_id: String
    ): Single<VisitorPackage>


    @Headers("Accept: application/json")
    @POST("customer/invite/visitors?")
    fun addVisitor(
        @Header("Authorization") token: String,
        @Body visitor: VisitorRequest?,
    ): Single<APIResponse?>?


    @Headers("Accept: application/json")
    @GET("customer/facilities?")
    fun getCustomerResorts(@Header("Authorization") token: String): Single<ResortResponse?>?

    @Headers("Accept: application/json")
    @GET("customer/available/units/{resort_id}")
    fun getAvailableUnits(
        @Header("Authorization") token: String,
        @Path("resort_id") resort_id: String,
        @Query("reservation_date") reservation_date: String,
        @Query("check_out_date") check_out_date: String,
        @Query("custom_discount_percentage") discount: String,
        @Query("unit_type_id") unit_type_id: String,

        ): Single<UnitsResponse?>?

    @Headers("Accept: application/json")
    @GET("customer/available/units/{resort_id}")
    fun getAvailableUnitsEdit(
        @Header("Authorization") token: String,
        @Path("resort_id") resort_id: String,
        @Query("reservation_date") reservation_date: String,
        @Query("check_out_date") check_out_date: String,
        @Query("custom_discount_percentage") discount: String,
        @Query("unit_type_id") unit_type_id: String,
        @Query("reservation_id") reservation_id: String,
        ): Single<UnitsResponse?>?

    @Headers("Accept: application/json")
    @POST("customer/guest/reservation?")
    fun addGuestReservation(
        @Header("Authorization") token: String,
        @Body visitor: GHReservationRequest?,
    ): Single<APIResponse?>?

    @Headers("Accept: application/json")
    @POST("guest/visitors?")
    fun addMarineApplication(
        @Body request: MarineServiceRequest,
    ): Single<APIResponse?>?

    @GET("guest/visitor/check/booking?")
    fun checkBookingAvailability(
        @Query("service_id") service_id: String?,
        @Query("resort_unit_id") resort_unit_id: String?,
        @Query("reservation_date_time") reservation_date_time: String?,
        @Query("hour") hour: String?

    ): Single<MarineBookingResponse?>?

    @Headers("Accept: application/json")
    @GET("guest/member/reg/roles?")
    fun getUserRoles(): Single<ResortResponse?>?


    @Headers("Accept: application/json")
    @GET("customer/guest/invite/visitor?")
    fun getNoOfVisitors(
        @Header("Authorization") token: String,
        @Query("visiting_date_time") visiting_date_time: String,
        @Query("resort_id") resort_id: String,
    ): Single<TotalVisitorsResponse>

    @GET("guest/unit/pdfs/{resort_id}")
    fun getGuestUnits(@Path("resort_id") resortId: String): Single<GuestRegistrationResponse>

    @Headers("Accept: application/json")
    @POST("customer/update/password")
    fun updatePwd(
        @Header("Authorization") token: String,
        @Body request: PwdUpdateReq,
    ): Single<APIResponse>


    @Headers("Accept: application/json")
    @GET("customer/invite/visitors?")
    fun getVisitors(
        @Header("Authorization") token: String
    ): Single<VisitorsResponse>

    @Headers("Accept: application/json")
    @DELETE("customer/invite/visitors/{inviteeID}")
    fun deleteVisitor(
        @Header("Authorization") token: String,
        @Path("inviteeID") inviteeID: String
    ): Single<APIResponse>

    @Headers("Accept: application/json")
    @PUT("customer/invite/visitors/{inviteeID}")
    fun editVisitor(
        @Header("Authorization") token: String,
        @Path("inviteeID") inviteeID: String,
        @Body visitor: EditVisitorRequest,
    ): Single<APIResponse>

    @Headers("Accept: application/json")
    @GET("customer/unit/types")
    fun getUnitTypes(
        @Header("Authorization") token: String,
    ): Single<SpaceResponse>

    @Headers("Accept: application/json")
    @GET("customer/service/requests?page=1&per_page=100")
    fun getAllServices(
        @Header("Authorization") token: String,
        @Query("per_page") per_page: String,
        @Query("page") page: String

    ): Single<ServiceResponse>

    @Headers("Accept: application/json")
    @GET("customer/guest/reservation?")
    fun getGuests(
        @Header("Authorization") token: String
    ): Single<GuestsResponse>

    @Headers("Accept: application/json")
    @DELETE("customer/guest/reservation/{reservID}")
    fun deleteGuest(
        @Header("Authorization") token: String,
        @Path("reservID") reservID: String,
    ): Single<APIResponse>

    @Headers("Accept: application/json")
    @PUT("customer/guest/reservation/{reservID}")
    fun editGuest(
        @Header("Authorization") token: String,
        @Path("reservID") reservID: String,
        @Body guest: GHReservationRequest,
    ): Single<APIResponse>

}