package eu.deysouvik.projectkaro.Models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class User(
    val id:String="",
    val name:String="",
    val email:String="",
    val password:String="",
    val photo:String="",
    val number:Long=0,
    val fmctoken:String=""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!
    )


    override fun writeToParcel(parcel: Parcel, flags: Int)=with(parcel) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(password)
        writeString(photo)
        writeLong(number)
        writeString(fmctoken)
    }

    override fun describeContents()=0


    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}
