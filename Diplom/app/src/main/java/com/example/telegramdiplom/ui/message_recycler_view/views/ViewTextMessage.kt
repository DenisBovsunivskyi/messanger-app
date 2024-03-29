package com.example.telegramdiplom.ui.message_recycler_view.views

class ViewTextMessage(
    override val id: String,
    override val from: String,
    override val timeStamp: String,
    override val fileUrl: String = "",
    override var text: String
) :MessageView{
    override fun getTypeView(): Int {
        return MessageView.MESSAGE_TEXT
    }

    override fun equals(other: Any?): Boolean {
        return (other as MessageView).id == id

    }
}