package com.example.telegramdiplom.ui.screeens.groups.settings

import AddContactsAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramdiplom.R
import com.example.telegramdiplom.models.CommonModel
import com.example.telegramdiplom.ui.screeens.base.BaseFragment
import com.example.telegramdiplom.utilits.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_group.*
import kotlinx.android.synthetic.main.fragment_create_group.create_group_recycle_view
import kotlinx.android.synthetic.main.fragment_show_members.*

class ShowMembersFragment(private var listContacts:List<CommonModel>,var group:CommonModel):BaseFragment(R.layout.fragment_show_members) {
    private lateinit var  mRecyclerView: RecyclerView
    private lateinit var mAdapter: AddContactsAdapter
    private var mUri = Uri.EMPTY

    override fun onResume() { //жизненый цикл фрагмента
        super.onResume()
        APP_ACTIVITY.title = getString(R.string.members_group_list)
        hideKeyboard()
        initRecyclerView()
        members_group_photo.downloadAndSetImage(group.photoUrl)
        show_members_group_name.text = group.fullname
        show_members_group_counts.text = getPlurals(listContacts.size)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Activity для получения картинки для фото пользователя
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            mUri = CropImage.getActivityResult(data).uri
            create_group_photo.setImageURI(mUri)
        }
    }

    private fun initRecyclerView() {
        mRecyclerView = create_group_recycle_view
        mAdapter = AddContactsAdapter()
        mRecyclerView.adapter = mAdapter
        listContacts.forEach{mAdapter.updateListItems(it)}
        }
    private fun addPhoto() {
        CropImage.activity()
            .setAspectRatio(1,1)
            .setRequestedSize(250,250)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(APP_ACTIVITY,this)
    }
}