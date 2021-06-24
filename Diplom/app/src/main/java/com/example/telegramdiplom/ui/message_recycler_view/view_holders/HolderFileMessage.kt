package com.example.telegramdiplom.ui.message_recycler_view.view_holders

import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramdiplom.database.CURRENT_UID
import com.example.telegramdiplom.database.getFileFromStorage
import com.example.telegramdiplom.ui.message_recycler_view.views.MessageView
import com.example.telegramdiplom.utilits.WRITE_FILES
import com.example.telegramdiplom.utilits.asTime
import com.example.telegramdiplom.utilits.checkPermission
import com.example.telegramdiplom.utilits.showToast
import kotlinx.android.synthetic.main.message_item_file.view.*
import java.io.File
import java.lang.Exception


//holder для file
class HolderFileMessage(view: View) : RecyclerView.ViewHolder(view), MessageHolder {

    private val blocReceivedFileMessage: ConstraintLayout = view.bloc_received_file_message
    private val blocUserFileMessage: ConstraintLayout = view.bloc_user_file_message
    private val chatUserFileMessageTime: TextView = view.chat_user_file_message_time
    private val chatReceivedFileMessageTime: TextView = view.chat_received_file_message_time

    private val chatUserFilename:TextView = view.chat_user_filename
    private val chatUserBtnDownload:ImageView = view.chat_user_btn_download
    private val chatUserProgressBar:ProgressBar = view.chat_user_progrssBar

    private val chatReceivedFilename:TextView = view.chat_received_filename
    private val chatReceivedBtnDownload:ImageView = view.chat_received_btn_download
    private val chatReceivedProgressBar:ProgressBar = view.chat_received_progrssBar


    override fun onAttach(view: MessageView) {
        //Подключение слушателей на элементы которые находятся на экране
        if(view.from == CURRENT_UID) chatUserBtnDownload.setOnClickListener{ clickToBtnFile(view)}
        else chatReceivedBtnDownload.setOnClickListener { clickToBtnFile(view) }


    }

    private fun clickToBtnFile(view: MessageView) {
        if(view.from == CURRENT_UID){
            chatUserBtnDownload.visibility = View.INVISIBLE
            chatUserProgressBar.visibility = View.VISIBLE
        }else{
            chatReceivedBtnDownload.visibility = View.INVISIBLE
            chatReceivedProgressBar.visibility = View.VISIBLE
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), //создание папки для хранения файлов
            view.text
        )
        try {
            if(checkPermission(WRITE_FILES)){
                file.createNewFile()
                getFileFromStorage(file,view.fileUrl){
                    if(view.from == CURRENT_UID){
                        chatUserBtnDownload.visibility = View.VISIBLE
                        chatUserProgressBar.visibility = View.INVISIBLE
                    }else{
                        chatReceivedBtnDownload.visibility = View.VISIBLE
                        chatReceivedProgressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }catch (e:Exception){
            showToast(e.message.toString())
        }
    }


    override fun onDetach() {
        //Отключение слушателей
        chatUserBtnDownload.setOnClickListener(null)
        chatReceivedBtnDownload.setOnClickListener(null)

    }

    override fun drawMessage(view: MessageView) {
        if (view.from == CURRENT_UID) {
            blocReceivedFileMessage.visibility = View.GONE
            blocUserFileMessage.visibility = View.VISIBLE
            chatUserFileMessageTime.text = view.timeStamp.asTime()
            chatUserFilename.text = view.text
        } else {
            blocReceivedFileMessage.visibility = View.VISIBLE
            blocUserFileMessage.visibility = View.GONE
            chatReceivedFileMessageTime.text =
                view.timeStamp.asTime()
            chatReceivedFilename.text = view.text
        }
    }
}