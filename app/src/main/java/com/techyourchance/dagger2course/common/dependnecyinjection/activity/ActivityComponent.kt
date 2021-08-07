package com.techyourchance.dagger2course.common.dependnecyinjection.activity

import com.techyourchance.dagger2course.common.dependnecyinjection.presentation.PresentationComponent
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    //You don't need to pass `presentationModule` when it doesn't have any bootstrapping dependency
    fun newPresentationComponent(): PresentationComponent
}

