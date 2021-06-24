package com.example.telegramdiplom.ui.message_recycler_view.views

import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.utilits.TYPE_MESSAGE_FILE
import com.example.telegramdiplom.utilits.TYPE_MESSAGE_IMAGE
import com.example.telegramdiplom.utilits.TYPE_MESSAGE_VOICE

//Factory для создания и передачи View в SingleChatAdapter
class AppViewFactory {
    companion object{
        fun getView(message:CommonModel):MessageView{
            return when(message.type){
                TYPE_MESSAGE_IMAGE -> ViewImageMessage(
                    message.id,
                    message.from,
                    message.timeStamp.toString(),
                    message.fileUrl
                )
                TYPE_MESSAGE_VOICE -> ViewVoiceMessage(
                    message.id,
                    message.from,
                    message.timeStamp.toString(),
                    message.fileUrl
                )
                TYPE_MESSAGE_FILE -> ViewFileMessage(
                    message.id,
                    message.from,
                    message.timeStamp.toString(),
                    message.fileUrl,
                    message.text
                )
                else -> ViewTextMessage(
                    message.id,
                    message.from,
                    message.timeStamp.toString(),
                    message.fileUrl,
                    message.text
                )
            }
        }
    }
}