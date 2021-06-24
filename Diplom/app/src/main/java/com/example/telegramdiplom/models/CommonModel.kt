package com.example.telegramdiplom.models
//Общая модель для всего приложения
data class CommonModel(
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var fullname: String = "",
    var phone:String = "",
    var state: String = "",
    var photoUrl: String = "empty",
    var token: String = "",

    var text:String= "",
    var type:String = "",
    var from:String ="",
    var timeStamp: Any = "",
    var fileUrl:String = "empty",


    var lastMessage:String = "",
    var choice:Boolean = false,
    var userId:String = ""

) {
    override fun equals(other: Any?): Boolean {
        return (other as CommonModel).id == id
    }
}


