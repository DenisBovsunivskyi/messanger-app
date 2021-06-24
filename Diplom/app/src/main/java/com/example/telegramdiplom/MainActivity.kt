package com.example.telegramdiplom

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.databinding.ActivityMainBinding
import com.example.telegramdiplom.ui.screeens.main_list.MainListFragment
import com.example.telegramdiplom.ui.screeens.register.EnterPhoneNumberFragment
import com.example.telegramdiplom.ui.objects.AppDrawer
import com.example.telegramdiplom.utilits.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.common.ConnectionResult
import com.google.firebase.messaging.FirebaseMessaging

const val TOPIC = "/topics/myTopic"
class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    lateinit var mAppDrawer: AppDrawer
    lateinit var mToolBar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        APP_ACTIVITY = this
        initFirebase()
        initUser { //после инициализации user все остальное приложение запустится
            CoroutineScope(Dispatchers.IO).launch {//корутина для асинхронного считывания контактов
                initContacts()
            }
            initFields()
            initFunc()
            initFirebaseToken() //создание и присвоение токена пользователю для получения уведомлений
        }
    }


    private fun initFunc() {
        //инициализация функциональности app
        setSupportActionBar(mToolBar)
        if(AUTH.currentUser != null){ //проверка авторизации
            mAppDrawer.create();
            replaceFragment(MainListFragment(),false)
        }else{ //если не прошла
            replaceFragment(EnterPhoneNumberFragment(),false)
        }


    }

    private fun initFields() {

        mToolBar = mBinding.mainToolbar
        mAppDrawer = AppDrawer()

    }
    private fun initFirebaseToken() {
        FireBaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            sendTokenToDatabase(it.token)
            FireBaseService.token = it.token
        }
    }



    override fun onStart() {
        super.onStart()
        AppStates.updateState(AppStates.ONLINE)

    }

    override fun onStop() {
        super.onStop()
        AppStates.updateState(AppStates.OFFLINE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(ContextCompat.checkSelfPermission(APP_ACTIVITY, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            initContacts()
        }
    }
}