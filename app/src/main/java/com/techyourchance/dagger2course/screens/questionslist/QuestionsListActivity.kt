package com.techyourchance.dagger2course.screens.questionslist

import android.os.Bundle
import android.util.Log
import com.techyourchance.dagger2course.R
import com.techyourchance.dagger2course.screens.common.ScreensNavigator
import com.techyourchance.dagger2course.screens.common.activities.BaseActivity
import javax.inject.Inject

class QuestionsListActivity : BaseActivity() {

    @Inject
    lateinit var screensNavigator: ScreensNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
        Log.e("QuestionsListActivity", "$screensNavigator")
        setContentView(R.layout.layout_frame)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.frame_content, QuestionsListFragment())
                    .commit()
        }

    }


}