package com.example.telegramdiplom.ui.screeens.groups.settings

import AddContactsAdapter
import AddMembersAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.screeens.base.BaseFragment
import com.example.telegramdiplom.ui.screeens.groups.GroupChatFragment
import com.example.telegramdiplom.utilits.*
import kotlinx.android.synthetic.main.fragment_add_contatcs.*


class AddMembersFragment(val group: CommonModel, listMembers: MutableList<CommonModel>) : BaseFragment(R.layout.fragment_add_contatcs) { //Главный фрагмент, в котором отображаются все чаты пользователя с которыми он взаимодействовал

    private lateinit var  mRecyclerView:RecyclerView
    private lateinit var mAdapter: AddMembersAdapter
    private val mRefContactsList = REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS).child(CURRENT_UID)
    private val mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    private val mRefMessages = REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID)
    private var mListItems = listOf<CommonModel>()
    private  var  listOfMembers = listMembers

    override fun onResume() { //жизненый цикл фрагмента
        listMembersToDb.clear()
        super.onResume()
        APP_ACTIVITY.title = "Додайте учасників групи"
        hideKeyboard()
        initRecyclerView()

        add_contacts_btn_next.setOnClickListener{
            if(listMembersToDb.isEmpty()) showToast("Додайте учасників")
            else{
                addMembersToGroup(group, listMembersToDb){
                    replaceFragment(GroupChatFragment(group))
                }
            }
        }
    }

    private fun addMembersToGroup(group: CommonModel, listContacts: MutableList<CommonModel>, function: () -> Unit) {
        val path = REF_DATABASE_ROOT.child(NODE_GROUPS).child(group.id).child(NODE_MEMBERS)
        val mapMembers = hashMapOf<String,Any>()
        val mapData = hashMapOf<String,Any>()
        mapData[CHILD_ID] = group.id
        listContacts.forEach{
            mapMembers[it.id]= USER_MEMBER
        }
        path.updateChildren(mapMembers).addOnSuccessListener {
            function()
            addGroupsToMainList(mapData,listContacts){
                function()
            }
        }
            .addOnFailureListener { showToast(it.message.toString()) }
    }

    private fun initRecyclerView() {
        mRecyclerView = add_contacts_recycle_view
        mAdapter = AddMembersAdapter(listOfMembers)

        // 1 запрос
        mRefContactsList.addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot ->   //получение List
            mListItems = dataSnapshot.children.map{it.getCommonModel()} //получение map каждого пользователя в список из NODE_MAIN_LIST и преобразование их в CommonModel
            mListItems.forEach{model ->     //при проходе по model
                //2 запрос
                mRefUsers.child(model.id).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot1 -> //получение пользователя в NODE_USERS
                    val newModel = dataSnapshot1.getCommonModel() //запись данных пользователя в newModel
                        // 3 запрос
                    mRefMessages.child(model.id).limitToLast(1).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot2 -> //получение последнего елемента из списка для последнего сообщения пользователя
                        val tmpList = dataSnapshot2.children.map { it.getCommonModel() }
                        if(tmpList.isEmpty()) {
                            newModel.lastMessage = "Чат очищено"
                            newModel.timeStamp = ""
                        }else{
                        newModel.lastMessage = tmpList[0].text
                            newModel.timeStamp = tmpList[0].timeStamp
                        }
                        if(newModel.fullname.isEmpty()){
                            newModel.fullname= newModel.phone
                        }

                        mAdapter.updateListItems(newModel)
                    })

                })

            }

        })

        mRecyclerView.adapter = mAdapter
    }
    companion object{
        val listMembersToDb = mutableListOf<CommonModel>()
    }
}