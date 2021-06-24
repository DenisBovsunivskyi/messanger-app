package com.example.telegramdiplom.ui.message_recycler_view.view_holders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.message_recycler_view.views.MessageView
import com.example.telegramdiplom.utilits.AppValueEventListener
import com.example.telegramdiplom.utilits.asTime
import com.example.telegramdiplom.utilits.downloadAndSetImage
import kotlinx.android.synthetic.main.message_item_text.view.*

//holder для Text
class HolderTextMessage(view: View) : RecyclerView.ViewHolder(view), MessageHolder {
    private val blocUserMessage: ConstraintLayout = view.bloc_user_message
    private val chatUserMessage: TextView = view.chat_user_message
    private val chatUserMessageTime: TextView = view.chat_user_message_time
    private val blocReceivedMessage: ConstraintLayout = view.bloc_received_message
    private val chatReceivedMessage: TextView = view.chat_received_message
    private val chatReceivedMessageTime: TextView = view.chat_received_message_time
    private val chatReceivedMessageName: TextView = view.chat_received_message_name
    val mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS)
    override fun onAttach(view: MessageView) {
    }

    override fun onDetach() {

    }

    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {
            blocUserMessage.visibility = View.VISIBLE
            blocReceivedMessage.visibility = View.GONE
            chatUserMessage.text = view.text
            chatUserMessageTime.text =
                view.timeStamp.asTime()
        } else {
            blocUserMessage.visibility = View.GONE
            blocReceivedMessage.visibility = View.VISIBLE
            chatReceivedMessage.text = view.text
            chatReceivedMessageTime.text =
                view.timeStamp.asTime()
            mRefUsers.child(view.from).addListenerForSingleValueEvent(AppValueEventListener{ dataSnapshot1 ->
                var newModel:CommonModel
                newModel = dataSnapshot1.getCommonModel()
                chatReceivedMessageName.text = newModel.fullname
                println(chatReceivedMessageName.text)
            })
        }
    }
}







