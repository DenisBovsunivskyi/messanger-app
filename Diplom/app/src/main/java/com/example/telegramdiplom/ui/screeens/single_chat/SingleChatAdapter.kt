package com.example.telegramdiplom.ui.screeens.single_chat

import android.annotation.SuppressLint
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.CURRENT_UID
import com.example.telegramdiplom.database.NODE_MESSAGES
import com.example.telegramdiplom.database.REF_DATABASE_ROOT
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.message_recycler_view.view_holders.AppHolderFactory
import com.example.telegramdiplom.ui.message_recycler_view.view_holders.MessageHolder
import com.example.telegramdiplom.ui.message_recycler_view.views.MessageView
import com.example.telegramdiplom.utilits.showToast


class SingleChatAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mListMessagesCache = mutableListOf<MessageView>()

    // private lateinit var mDiffResult: DiffUtil.DiffResult
    private var mListHolders = mutableListOf<MessageHolder>()
    private lateinit var contact:CommonModel

    fun getContact(contactId:CommonModel){
        contact = contactId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppHolderFactory.getHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return mListMessagesCache[position].getTypeView()

    }

    override fun getItemCount(): Int = mListMessagesCache.size //size of listMessageCache

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as MessageHolder).onAttach(mListMessagesCache[holder.adapterPosition])
        mListHolders.add(holder as MessageHolder)
        println(mListHolders)
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as MessageHolder).onDetach()
        mListHolders.remove(holder as MessageHolder)
        super.onViewDetachedFromWindow(holder)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) { //принимает holder, передает position, функция drawMessage отрисовует сообщения

        (holder as MessageHolder).drawMessage(mListMessagesCache[position])
        holder.itemView.setOnClickListener {
            showPopUpMenu(holder.itemView,position,mListMessagesCache,contact)
        }
    }




    fun setList(list: List<MessageView>) {

        //notifyDataSetChanged()
    }

    fun addItemToBottom(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!mListMessagesCache.contains(item)) {
            mListMessagesCache.add(item)
            notifyItemInserted(mListMessagesCache.size)
        }
        onSuccess()
    }

    fun addItemToTop(
        item: MessageView,
        onSuccess: () -> Unit
    ) {
        if (!mListMessagesCache.contains(item)) {
            mListMessagesCache.add(item)
            mListMessagesCache.sortBy { it.timeStamp.toString() }
            notifyItemInserted(0)
        }
        onSuccess()
    }


    fun onDestroy() {
        mListHolders.forEach {
            it.onDetach()
        }
    }

 private fun deleteSingleMessageFromChat(
        position: Int,
        mListMessagesCache: MutableList<MessageView>,
        contact: CommonModel
    ) {
        REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID).child(contact.id).child(mListMessagesCache[position].id)
            .removeValue()
            .addOnFailureListener{ showToast(it.message.toString())}
            .addOnSuccessListener {
                REF_DATABASE_ROOT.child(NODE_MESSAGES).child(contact.id).child(CURRENT_UID).child(mListMessagesCache[position].id  )
                    .removeValue()
                    .addOnFailureListener{ showToast(it.message.toString())}
                    .addOnSuccessListener {
                        mListMessagesCache[position].text = "Повідомлення видалено"
                        notifyItemChanged(position)
                    }
            }
              .addOnFailureListener { showToast(it.message.toString()) }

    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun showPopUpMenu(
        itemView: View,
        position: Int,
        mListMessagesCache: MutableList<MessageView>,
        contact:CommonModel){
        val wrapper = ContextThemeWrapper(itemView.context,R.style.BasePopupMenu)
        val popUp = PopupMenu(wrapper,itemView)
        popUp.inflate(R.menu.popup_menu)
        popUp.setOnMenuItemClickListener {item->
            when(item.itemId)
            {
                R.id.delete_single_chat_message->{
                    deleteSingleMessageFromChat(position,mListMessagesCache,contact)
                    }
                R.id.cancel_single_chat->{ }

            }
            true
        }
        popUp.show()
        true
    }


}







