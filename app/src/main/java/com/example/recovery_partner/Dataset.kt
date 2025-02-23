package com.example.recovery_partner
import android.provider.ContactsContract.CommonDataKinds.Email
import java.net.URL

data class Dataset(
    var signup: Signup,
    var login: Login,
    var friendrequest:FriendRequest,
    val friendaccept:Accept,
    var streaks: Streaks,
    var blogs: Blogs,
    val sound: Sound,
    val craft: Craft,
    val breathing: Breathing,
    val chekins: Chekins
)
data class Signup(
    var email: Email,
    var password : String,
    var isTherapistFriend: Boolean


)
data class  Login(
    var email: Email,
    var password : String,
    )
data class FriendRequest(
    val senderId: String,
    val receiverId:String
)
data class Accept(
    val requestId: String

)
data class  Streaks(
    val userId:String,
    var activityType: String


)
data class Blogs(
    var title: String,
    var content: String,
    val authorId:String
)
data class Sound(
    val title:String,
    val audioUrl: URL
)
data class Craft(
    val title: String,
    val vedioUrl: URL
)
data class Breathing(
    val title: String,
    val steps:Array<String>
)
data class Chekins(
    val userId: String,
    val therapistFriendId:String
)