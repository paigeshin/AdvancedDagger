package com.techyourchance.dagger2course.screens.common.dialogs

import androidx.fragment.app.FragmentManager
import com.techyourchance.dagger2course.common.dependnecyinjection.activity.ActivityScope
import javax.inject.Inject

@ActivityScope
class DialogsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

    fun showServerErrorDialog() {
        fragmentManager.beginTransaction()
                .add(ServerErrorDialogFragment.newInstance(), null)
                .commitAllowingStateLoss()
    }
}