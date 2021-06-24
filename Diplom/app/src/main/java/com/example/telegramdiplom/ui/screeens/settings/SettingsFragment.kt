package com.example.telegramdiplom.ui.screeens.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.*
import com.example.telegramdiplom.ui.screeens.base.BaseFragment
import com.example.telegramdiplom.utilits.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : BaseFragment(R.layout.fragment_settings) {


    override fun onResume() { //жизненый цикл фрагмента
        super.onResume()
        setHasOptionsMenu(true)
        initFields()
    }

    private fun initFields() {
        settings_bio.text = USER.bio
        settings_full_name.text = USER.fullname
        settings_phone_number.text = USER.phone
        settings_status.text = USER.state
        settings_username.text = USER.username
        settings_btn_change_username.setOnClickListener{
            replaceFragment(ChangeUsernameFragment())
        }
        settings_btn_change_bio.setOnClickListener{
            replaceFragment(ChangeBioFragment())
        }
        settings_change_photo.setOnClickListener{changePhotoUser()}
        settings_user_photo.downloadAndSetImage(USER.photoUrl)
    }

    private fun changePhotoUser() {
        CropImage.activity()
            .setAspectRatio(1,1)
            .setRequestedSize(250,250)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(APP_ACTIVITY,this)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) { //Создание выпадающего меню
        activity?.menuInflater?.inflate(R.menu.settings_action_menu,menu) // ?-безопасный вызов
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Listener выбора пунктов выпадающего меню

        when(item.itemId){
            R.id.settings_menu_exit -> {
                AppStates.updateState(AppStates.OFFLINE)
                AUTH.signOut()
                restartActivity()
            }
            R.id.settings_menu_change_name ->replaceFragment(ChangeNameFragment())
        }
        return  true
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Activity для получения картинки для фото пользователя
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            val uri = CropImage.getActivityResult(data).uri
            val path = REF_STORAGE_ROOT.child(
                FOLDER_PROFILE_IMAGE
            ) //путь для image
                .child(CURRENT_UID)

            putFileToStorage(uri, path) {
                getUrlFromStorage(path) {
                    putUrlToDatabase(it) {
                        settings_user_photo.downloadAndSetImage(it)
                        showToast(getString(R.string.toast_data_update))
                        USER.photoUrl = it
                        APP_ACTIVITY.mAppDrawer.updateHeader()
                    }
                }
            }
            }
        }

}

/* path.putFile(uri).addOnCompleteListener{task1->
          if(task1.isSuccessful){
              path.downloadUrl.addOnCompleteListener{task2 -> //сыллка на картинку в бд
                  if(task2.isSuccessful){
                      val photoUrl = task2.result.toString()
                      REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID)
                          .child(CHILD_PHOTO_URL).setValue(photoUrl)
                          .addOnCompleteListener{
                              if(it.isSuccessful){
                                  settings_user_photo.donwloadAndSetImage(photoUrl)
                                  showToast(getString(R.string.toast_data_update))
                                  USER.photoUrl = photoUrl
                              }
                          }
                  }
              }
          }*/