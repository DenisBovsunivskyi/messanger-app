package com.example.telegramdiplom.ui.screeens.base

import androidx.fragment.app.Fragment
import com.example.telegramdiplom.utilits.APP_ACTIVITY


open class BaseFragment( layout:Int) : Fragment(layout) {

    override fun onStart() {
        super.onStart()
        APP_ACTIVITY.mAppDrawer.disableDrawer()
    }

}