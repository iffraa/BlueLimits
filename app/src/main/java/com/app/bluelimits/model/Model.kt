package com.app.bluelimits.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class User(
    val user_type: String?,
    val email: String?,
    val name: String?,
    val id: Int?,
    val role: String?,
    val profile_image: String?,
    val contact_no: String?,
    val member_id: String?,
    val status: String?,
    val oia_resort_annual_members: String?,
    val oia_resort_locker_members: String?,
    val oia_resort_guest_visitors: String?,
    val oia_resort_guest_reservations: String?,

    val boho_resort_annual_members: String?,
    val boho_resort_locker_members: String?,
    val boho_resort_guest_visitors: String?,
    val boho_resort_guest_reservations: String?,

    val total_invitees: Int?,
    val total_guest_visitors: String?,

    val resort: String?,
    val service: String?,
    val service_type: String?,

    @SerializedName("package")
    val packagee: String?,
    val service_name: String?,
    val unit_no: String?,
    val role_id: Int?,
    val resort_id: Int?,
    val service_id: Int?,
    val package_id: Int?,
    val setup_unit_id: Int?,
    val id_iqama_copy: String?,
    val title: String?,
    val birth_date: String?,
    val gender: String?,
    val professional: String?,
    val home_city: String?,
    val home_country: String?,
    val office_city: String?,
    val office_country: String?,
    val user_name: String?,
    val qr_code: String?,
    val qr_code_download: String?,
    val contract_end_date: String?,
    val contract_remaining_days: String?,
    val invite_visitor_discount_percentage: String?,
    val guest_house_discount_percentage: String?,
    val loyalty_points: String?,
    val no_of_family_member: Int?,
    val no_of_extra_family_member: Int?,
    val invitees: ArrayList<Invitee>?,
    val permissions: ArrayList<Permission>,
    val members: ArrayList<FamilyMember>?

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        TODO("invitees"),
        TODO("permissions"),
        TODO("members")

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_type)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeValue(id)
        parcel.writeString(role)
        parcel.writeString(profile_image)
        parcel.writeString(contact_no)
        parcel.writeString(member_id)
        parcel.writeString(status)
        parcel.writeString(oia_resort_annual_members)
        parcel.writeString(oia_resort_locker_members)
        parcel.writeString(oia_resort_guest_visitors)
        parcel.writeString(oia_resort_guest_reservations)
        parcel.writeString(boho_resort_annual_members)
        parcel.writeString(boho_resort_locker_members)
        parcel.writeString(boho_resort_guest_visitors)
        parcel.writeString(boho_resort_guest_reservations)
        parcel.writeValue(total_invitees)
        parcel.writeString(total_guest_visitors)
        parcel.writeString(resort)
        parcel.writeString(service)
        parcel.writeString(service_type)
        parcel.writeString(packagee)
        parcel.writeString(service_name)
        parcel.writeString(unit_no)
        parcel.writeValue(role_id)
        parcel.writeValue(resort_id)
        parcel.writeValue(service_id)
        parcel.writeValue(package_id)
        parcel.writeValue(setup_unit_id)
        parcel.writeString(id_iqama_copy)
        parcel.writeString(title)
        parcel.writeString(birth_date)
        parcel.writeString(gender)
        parcel.writeString(professional)
        parcel.writeString(home_city)
        parcel.writeString(home_country)
        parcel.writeString(office_city)
        parcel.writeString(office_country)
        parcel.writeString(user_name)
        parcel.writeString(qr_code)
        parcel.writeString(qr_code_download)
        parcel.writeString(contract_end_date)
        parcel.writeString(contract_remaining_days)
        parcel.writeString(invite_visitor_discount_percentage)
        parcel.writeValue(guest_house_discount_percentage)
        parcel.writeString(loyalty_points)
        parcel.writeValue(no_of_family_member)
        parcel.writeValue(no_of_extra_family_member)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}


data class Data(var token: String?, var user: User?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(User::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Data> {
        override fun createFromParcel(parcel: Parcel): Data {
            return Data(parcel)
        }

        override fun newArray(size: Int): Array<Data?> {
            return arrayOfNulls(size)
        }
    }
}

data class APIResponse(val status: Boolean, var message: String, var data: Data)

data class VisitorRequest(
    var no_of_visitor: String,
    var resort_id: String,
    var visiting_date_time: String,
    var custom_discount_percentage: String?,
    var sub_total: String,
    var discount: String,
    var total_price: String,
    var visitor: ArrayList<Visitor>
                          )

data class Permission(val id: String, val name: String)

data class Invitee(
    val id: Int,
    val no_of_visitor: String,
    val visiting_date_time: String,
    val who_will_pay: String,
    val total_payment: Int,
    val sub_total: Int,
    val discount: Int,
    val total_price: String,
    val custom_discount_percentage: Int,
    val visitors: ArrayList<Visitor>
)

data class Visitor(
  //  var id: String,
    var name: String,
    var contact_no: String,
    var id_no: String,
    var gender: String,
   // val status: String,
    var package_id: String,

    @SerializedName("package")
    var servicePackage: ServicePackage?,
    var who_will_pay: String,
  //  val qr_code: String,
    var price: String

)

data class ServicePackage(
    val id: Int,
    val service_name: String,
    val price: String,
    val discount_percentage: Int,
    val hour: String,
)

data class VisitorPackage(
    val status: Boolean,
    val message: String,

    val data : ServicePackage
)

data class PackageResponse(
    val status: Boolean,
    val message: String,

    val data : ArrayList<ServicePackage>
)

data class FamilyMember(
    var id: String,
    var name: String,
    var email: String,
    var contact_no: String,
    val profile_image: String,
    var birth_date: String,
    var gender: String,

    )

data class FamilyMemberRequest(
    var member_id: Int,
    var first_name: String,
    var last_name: String,
    var contact_no: String,
    var birth_date: String,
    var gender: String,

    )

data class UnitsResponse(var status: Boolean, var message: String, var data: ArrayList<AvailableUnit>)

data class AvailableUnit(
    var id: Int,
    var price: String,
    var unit: String,
    var service_name: String,
    var package_id: String,
    var no_of_guest: String,
    var no_of_day: String,
    var sub_total: String,
    var discount: String,
    var total_price: String,
    var discount_percentage: String,

    )


data class ResortResponse(
    val status: Boolean,
    val message: String,

    @SerializedName("data")
    val resort : ArrayList<Resort>
    )

data class Resort(
    val id: Int,
    val name: String?,
    val image: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resort> {
        override fun createFromParcel(parcel: Parcel): Resort {
            return Resort(parcel)
        }

        override fun newArray(size: Int): Array<Resort?> {
            return arrayOfNulls(size)
        }
    }

}

data class RegisterMemberRequest(var role_id: String,
                                 var resort_id: String,
                                 var service_id: String,
                                 var package_id: String,
                                 var first_name: String,
                                 var last_name: String,
                                 var member_id: String,
                                 var email: String,
                                 var birth_date: String,
                                 var gender: String,
                                 var contact_no: String,
                                 var home_city: String,
                                 var home_country: String,
                                 var office_city: String,
                                 var office_country: String,
                                 var membership_no: String,
                                 var no_of_family_member: String,
                                 var member: ArrayList<FamilyMemberRequest>
)

data class Guest(var name: String, var gender: String,var id_no: String, var contact_no: String)

data class GHReservationRequest(var setup_unit_id: String,
                                 var discount: String,
                                 var no_of_guest: String,
                                 var package_id: String,
                                 var resort_id: String,
                                var sub_total: String,
                                var total_price: String,
                                var reservation_date: String,
                                var check_out_date: String,
                                var custom_discount_percentage: String,
                                var guest: ArrayList<Guest>
)

data class MarineServiceRequest(var service_id: String,
                                var package_id: String,
                                var resort_id: String,
                                var name: String,
                                var contact_no: String,
                                var id_no: String,
                                var email: String,
                                var reservation_date_time: String,
                                var resort_unit_id: String,
                                var hour: String,
                                var total_price: String

                                )

data class MarineBookingResponse(var is_booked: Boolean, var total_price: String)

data class TotalVisitorsResponse(
    val status: Boolean,
    val message: String,
    val data : TotalVisitors
)

data class TotalVisitors(
    val visitor_policy_exist: Boolean,
    val available: Int,
    val total_allow: Int,
    val each_time_limit: Int,
    val male: Int,
    val female: Int,
)


data class GuestRegistrationResponse(
    val status: Boolean,
    val message: String,
    val data : ArrayList<GuestUnit>
)

data class GuestUnit(
    val id: Int,
    val units: String,
    val description: String,
    val pdf: String,
)
