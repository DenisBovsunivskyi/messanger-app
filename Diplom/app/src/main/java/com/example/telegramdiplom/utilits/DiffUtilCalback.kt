package com.example.telegramdiplom.utilits

import androidx.recyclerview.widget.DiffUtil
import com.example.telegramdiplom.models.CommonModel

class DiffUtilCalback(
    private val oldList: List<CommonModel>,
    private val newList: List<CommonModel>
):DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = //проверка списков по идентификатору(id одинаковые или нет)
        oldList[oldItemPosition].timeStamp == newList[newItemPosition].timeStamp

    override fun getOldListSize(): Int = oldList.size //размер старого списка


    override fun getNewListSize(): Int = newList.size //размер нового списка

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = //проверка если индентификаторы одинаковые, всех полей сообщения
        oldList[oldItemPosition]== newList[newItemPosition]
}