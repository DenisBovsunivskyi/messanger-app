package com.example.telegramdiplom.ui.screeens.main_list

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.telegram.ui.screens.main_list.MainListAdapter
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.utilits.*
import kotlinx.android.synthetic.main.fragment_main_list.*


class MainListFragment : Fragment(R.layout.fragment_main_list) { //Главный фрагмент, в котором отображаются все чаты пользователя с которыми он взаимодействовал

    private lateinit var  mRecyclerView:RecyclerView
    private lateinit var mAdapter: MainListAdapter
    private val mRefMainList = REF_DATABASE_ROOT.child(NODE_MAIN_LIST).child(CURRENT_UID)
    private val mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    private val mRefMessages = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID)
    private var mListItems = listOf<CommonModel>()


    override fun onResume() { //жизненый цикл фрагмента
        super.onResume()
        APP_ACTIVITY.title = "TelegraphDiplom"
        APP_ACTIVITY.mAppDrawer.enableDrawer()
        hideKeyboard()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mRecyclerView = main_list_recycle_view
        mAdapter = MainListAdapter()
        val mMsgList = mutableListOf<CommonModel>()

        // 1 запрос
        mRefMainList.addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot ->   //получение List
            mListItems = dataSnapshot.children.map{it.getCommonModel()} //получение map каждого пользователя в список из NODE_MAIN_LIST и преобразование их в CommonModel
            mListItems.forEach{model ->     //при проходе по model
                when(model.type){
                    TYPE_CHAT -> showChat(model,mMsgList)
                    TYPE_GROUP -> showGroup(model,mMsgList)
                }

            }

        })

        mRecyclerView.adapter = mAdapter
    }

    private fun showGroup(model: CommonModel,mMsgList: MutableList<CommonModel>) {
        REF_DATABASE_ROOT.child(NODE_GROUPS).child(model.id).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot1 -> //получение пользователя в NODE_USERS
            val newModel = dataSnapshot1.getCommonModel() //запись данных пользователя в newModel

            // 3 запрос
            REF_DATABASE_ROOT.child(NODE_GROUPS).child(model.id).child(NODE_MESSAGES).limitToLast(1)
                .addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot2 -> //получение последнего елемента из списка для последнего сообщения пользователя
                val tmpList = dataSnapshot2.children.map { it.getCommonModel() }
                if(tmpList.isEmpty()) {
                    newModel.lastMessage = "Чат очищен"
                    newModel.timeStamp = ""
                }else{
                    newModel.lastMessage = tmpList[0].text
                    newModel.timeStamp = tmpList[0].timeStamp
                }
                    newModel.type = TYPE_GROUP
                mMsgList.add(newModel)
                if (mMsgList.size == mListItems.size) {
                    mMsgList.sortByDescending { it.timeStamp.toString() }
                    mAdapter.updateListItemsNew(mMsgList)
                }
            })

        })

    }

    private fun showChat(model: CommonModel,mMsgList: MutableList<CommonModel>) {
        //2 запрос
        mRefUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot1 -> //получение пользователя в NODE_USERS
            val newModel = dataSnapshot1.getCommonModel() //запись данных пользователя в newModel

            // 3 запрос
            mRefMessages.child(model.id).limitToLast(1).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot2 -> //получение последнего елемента из списка для последнего сообщения пользователя
                val tmpList = dataSnapshot2.children.map { it.getCommonModel() }
                if(tmpList.isEmpty()) {
                    newModel.lastMessage = "Чат очищен"
                    newModel.timeStamp = ""
                }else{
                    newModel.lastMessage = tmpList[0].text
                    newModel.timeStamp = tmpList[0].timeStamp
                }
                if(newModel.fullname.isEmpty()){
                    newModel.fullname= newModel.phone
                }
                newModel.type = TYPE_CHAT
                mMsgList.add(newModel)
                if (mMsgList.size == mListItems.size) {
                    mMsgList.sortByDescending { it.timeStamp.toString() }
                    mAdapter.updateListItemsNew(mMsgList)
                }
            })

        })

    }
}