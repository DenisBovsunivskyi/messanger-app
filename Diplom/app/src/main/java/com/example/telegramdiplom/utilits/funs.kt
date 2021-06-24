package com.example.telegramdiplom.utilits

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.telegramdiplom.MainActivity
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.CURRENT_UID
import com.example.telegramdiplom.database.NODE_MESSAGES
import com.example.telegramdiplom.database.REF_DATABASE_ROOT
import com.example.telegramdiplom.database.updatePhonesToDatabase
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.message_recycler_view.views.MessageView
import com.example.telegramdiplom.utilits.Notifications.PushNotifications
import com.example.telegramdiplom.utilits.Notifications.RetrofitInstance
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

//Общий файл для хранения функций которые использует приложение
fun showToast(message:String){
    //Отображение всплывающего сообщения
    Toast.makeText(APP_ACTIVITY, message,Toast.LENGTH_SHORT).show()
}

fun restartActivity(){
    //Функция для рассширения AppCompactActivity, для запуска новых activity
    val intent = Intent(APP_ACTIVITY,MainActivity::class.java)
    APP_ACTIVITY.startActivity(intent)
    APP_ACTIVITY.finish()
}

fun replaceFragment(fragment: Fragment,addStack: Boolean=true) {
    //Функция для запуска(установки) фрагментов
    if (addStack) {
       APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(
                R.id.data_container,
                fragment
            ).commit()
    }else{
        APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .replace(
                R.id.data_container,
                fragment
            ).commit()
    }
}


fun hideKeyboard(){
    val imm: InputMethodManager =  APP_ACTIVITY.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(APP_ACTIVITY.window.decorView.windowToken, 0)
}
fun initContacts() {
    //Инициализация контактов
    if (checkPermission(READ_CONTACTS)){
        var arrayContacts = arrayListOf<CommonModel>()
        val cursor = APP_ACTIVITY.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let{ //считывание курсора когда он != NULL
            while(it.moveToNext()){
                val fullName= it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phone = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val newModel = CommonModel()
                newModel.fullname = fullName
                newModel.phone = phone.replace(Regex("[\\s,-]"),"")
                arrayContacts.add(newModel)
            }
        }
        cursor?.close()
        updatePhonesToDatabase(arrayContacts)
    }
}

fun ImageView.downloadAndSetImage(url:String){
    Picasso.get()
        .load(url)
        .fit()
        .placeholder(R.drawable.default_photo)
        .into(this)
}


fun String.asTime(): String {
    val time = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(time)

}


fun getFilenameFromUri(uri: Uri): String { //Получить имя файла по его Uri
    var result = ""
    val cursor = APP_ACTIVITY.contentResolver.query(uri,null,null,null,null)
    try {
        if(cursor!=null && cursor.moveToFirst()){
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }catch (e:Exception){
        showToast(e.message.toString())
    }finally {
        cursor?.close()
        return result
    }
}
fun getPlurals(count:Int) = APP_ACTIVITY.resources.getQuantityString(
    R.plurals.count_members,count,count
)

fun sendNotification(notification: PushNotifications) = CoroutineScope(Dispatchers.IO).launch { //отправка уведомлений
    try {
        val response = RetrofitInstance.api.postNotification(notification)
        if(response.isSuccessful) {
            println("notification send")
        }
    } catch(e: Exception) {
       showToast(e.message.toString())
    }
}

