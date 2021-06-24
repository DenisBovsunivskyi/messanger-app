package com.example.telegram.ui.fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.telegramdiplom.R
import com.example.telegramdiplom.TOPIC
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.models.UserModel
import com.example.telegramdiplom.ui.screeens.base.BaseFragment
import com.example.telegramdiplom.ui.message_recycler_view.views.AppViewFactory
import com.example.telegramdiplom.ui.screeens.main_list.MainListFragment
import com.example.telegramdiplom.ui.screeens.single_chat.SingleChatAdapter
import com.example.telegramdiplom.utilits.*
import com.example.telegramdiplom.utilits.Notifications.NotificationsData
import com.example.telegramdiplom.utilits.Notifications.PushNotifications
import com.example.telegramdiplom.utilits.Notifications.RetrofitInstance
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DatabaseReference
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.choice_upload.*
import kotlinx.android.synthetic.main.fragment_single_chat.*
import kotlinx.android.synthetic.main.toolbar_info.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception



class SingleChatFragment(private val contact: CommonModel) :
    BaseFragment(R.layout.fragment_single_chat) {

    private lateinit var mListenerInfoToolbar: AppValueEventListener
    private lateinit var mReceivingUser: UserModel
    private lateinit var mToolbarInfo:View
    private lateinit var mRefUser: DatabaseReference
    private lateinit var mRefCurrentUser: UserModel
    private lateinit var mRefMessages:DatabaseReference
    private lateinit var mAdapter:SingleChatAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMessageListener:AppChildEventListener
    private var mCountMessages = 10
    private var mIsScrolling = false
    private var mSmoothScrollToPosition = true
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAppVoiceRecorder: AppVoiceRecorder
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    //val TAG = "SINGLE_CHAT"


        override fun onResume() {
        super.onResume()
        initFields()
        initToolbar()
        initRecycleView()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFields() {
        setHasOptionsMenu(true)
        mBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_choice)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mAppVoiceRecorder = AppVoiceRecorder()
        mSwipeRefreshLayout = chat_swipe_refresh
        mLayoutManager = LinearLayoutManager(this.context)
        chat_input_message.addTextChangedListener(AppTextWatcher{
            val string = chat_input_message.text.toString()
            if(string.isEmpty() || string == "Запись"){
                chat_btn_send_message.visibility = View.GONE
                chat_btn_attach.visibility = View.VISIBLE
                chat_btn_voice.visibility = View.VISIBLE
            }else{
                chat_btn_send_message.visibility = View.VISIBLE
                chat_btn_attach.visibility = View.GONE
                chat_btn_voice.visibility = View.GONE
            }
        })
        chat_btn_attach.setOnClickListener{ attach()}
        CoroutineScope(Dispatchers.IO).launch {
            chat_btn_voice.setOnTouchListener { v, event ->
                if (checkPermission(RECORD_AUDIO)) {
                    if(event.action == MotionEvent.ACTION_DOWN){
                        //TODO record
                        chat_input_message.setText("Запис")
                        chat_btn_voice.setColorFilter(ContextCompat.getColor(APP_ACTIVITY, R.color.primary))
                        val messageKey = getMessageKey(contact.id)
                        mAppVoiceRecorder.startRecord(messageKey)
                    }else if(event.action == MotionEvent.ACTION_UP){
                        //TODO stopRecotd
                        chat_input_message.setText("")
                        chat_btn_voice.colorFilter = null
                        mAppVoiceRecorder.stopRecord(){file,messageKey ->
                            uploadFileToStorage(Uri.fromFile(file),messageKey,contact.id, TYPE_MESSAGE_VOICE)
                            mSmoothScrollToPosition = true
                        }
                    }
                }
                true
            }
        }
    }

    private fun attach() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED //отображение меню отправки файлов
        btn_attach_file.setOnClickListener{attachFile()}
        btn_attach_image.setOnClickListener{attachImage()}

    }
    private fun attachFile(){
        val intent = Intent(Intent.ACTION_GET_CONTENT) //доступ к файлам
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }


    private fun attachImage() {
        CropImage.activity()
            .setAspectRatio(1,1)
            .setRequestedSize(600,600)
            .start(APP_ACTIVITY,this)
    }

    private fun initRecycleView() {
        mRecyclerView = chat_recycle_view
        mAdapter = SingleChatAdapter()
        mRefMessages = REF_DATABASE_ROOT
            .child(NODE_MESSAGES)
            .child(CURRENT_UID)
            .child(contact.id)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isNestedScrollingEnabled = false
        mRecyclerView.layoutManager = mLayoutManager
       mAdapter.getContact(contact)

        mMessageListener = AppChildEventListener{
            val message = it.getCommonModel()

            if(mSmoothScrollToPosition){
                mAdapter.addItemToBottom(AppViewFactory.getView(message)){
                    mRecyclerView.smoothScrollToPosition(mAdapter.itemCount)
                }
            }else{
                mAdapter.addItemToTop(AppViewFactory.getView(message)){
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }
        }


        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessageListener) //Listener изменений в NODE MESSAGES + загрузка последних десяти сообщений


        mRecyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(mIsScrolling && dy < 0 && mLayoutManager.findFirstVisibleItemPosition() <= 3){
                    updateData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING){
                    mIsScrolling = true
                }
            }
        })

        mSwipeRefreshLayout.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        mSmoothScrollToPosition = false
        mIsScrolling = false
        mCountMessages += 10;
        mRefMessages.removeEventListener(mMessageListener)
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessageListener) //добавления ещё 10 сообщений если есть движения по list
    }

    private fun initToolbar() {
        mToolbarInfo = APP_ACTIVITY.mToolBar.toolbar_info
        mToolbarInfo.visibility = View.VISIBLE
        mListenerInfoToolbar = AppValueEventListener {
            mReceivingUser = it.getUserModel()
            initInfoToolbar()
        }

        mRefUser = REF_DATABASE_ROOT.child(
            NODE_USERS
        ).child(contact.id)
        mRefUser.addValueEventListener(mListenerInfoToolbar)
        chat_btn_send_message.setOnClickListener {
            mSmoothScrollToPosition = true
            val message = chat_input_message.text.toString()
            if (message.isEmpty()) {
                showToast("Повідомлення не може бути пустим")
            } else sendMessage(
                message,
                contact.id,
                TYPE_TEXT
            ) {
                saveToMainList(contact.id, TYPE_CHAT) //сохранение данных для MainList
                if(contact.token.isNotEmpty() && mReceivingUser.state != "в сети"){
                    PushNotifications(
                        NotificationsData(USER.fullname, USER.fullname+":"+message),
                        contact.token
                    ).also { sendNotification(it) }
                }
                chat_input_message.setText("")
            }
        }
    }




    private fun initInfoToolbar() {

        if(mReceivingUser.fullname.isEmpty()){
            mToolbarInfo.toolbar_chat_fullname.text = contact.fullname
        }else  mToolbarInfo.toolbar_chat_fullname.text = mReceivingUser.fullname

        mToolbarInfo.toolbar_chat_image.downloadAndSetImage(mReceivingUser.photoUrl)
        mToolbarInfo.toolbar_chat_status.text = mReceivingUser.state
        mToolbarInfo.toolbar_chat_status.visibility = View.VISIBLE
        mToolbarInfo.group_counts_button.visibility= View.GONE

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Activity для получения картинки для фото пользователя
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            when(requestCode){
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val uri = CropImage.getActivityResult(data).uri
                    val messageKey = getMessageKey(contact.id)
                    uploadFileToStorage(uri,messageKey,contact.id, TYPE_MESSAGE_IMAGE)
                    mSmoothScrollToPosition = true
                }
                PICK_FILE_REQUEST_CODE ->{
                    val uri = data.data
                    val messageKey = getMessageKey(contact.id)
                    val filename = getFilenameFromUri(uri!!)
                    uploadFileToStorage(uri,messageKey,contact.id, TYPE_MESSAGE_FILE, filename)
                    mSmoothScrollToPosition = true
                }
        }
        }
    }


    override fun onPause() {
        super.onPause()
        APP_ACTIVITY.mToolBar.toolbar_info.visibility = View.GONE
        mRefUser.removeEventListener(mListenerInfoToolbar)
        mRefMessages.removeEventListener(mMessageListener)
        hideKeyboard();
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAppVoiceRecorder.releaseRecorder()
        mAdapter.onDestroy()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) { //Создание выпадающего меню
        activity?.menuInflater?.inflate(R.menu.single_chat_action_menu,menu) // ?-безопасный вызов
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Listener выбора пунктов выпадающего меню

        when(item.itemId){
        R.id.menu_clear_chat -> clearChat(contact.id){
            showToast("Чат очищен")
            replaceFragment(MainListFragment())
        }
        R.id.menu_delete_chat -> deleteChat(contact.id){
            showToast("Чат удален")
            replaceFragment(MainListFragment())
        }
        }
        return  true
    }
}