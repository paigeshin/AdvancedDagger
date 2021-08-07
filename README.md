# Advanced Dagger Tutorial

### Relevant Repository

[https://github.com/paigeshin/PureDependencyInjection](https://github.com/paigeshin/PureDependencyInjection)

[https://github.com/techyourchance/course-android-dependency-injection-with-dagger-2](https://github.com/techyourchance/course-android-dependency-injection-with-dagger-2)

Recommended Structure ⇒ [https://github.com/paigeshin/DependencyInjectionWithDagger/tree/1da15c90fefbeb1cfaf85c3e4aaa57c7f7c69c30](https://github.com/paigeshin/DependencyInjectionWithDagger/tree/1da15c90fefbeb1cfaf85c3e4aaa57c7f7c69c30)

[https://github.com/paigeshin/DependencyInjectionWithDagger](https://github.com/paigeshin/DependencyInjectionWithDagger)

# v.0.0.1 - Add Service

- Mostly handles dialog.

```kotlin
@Subcomponent(modules = [ServiceModule::class])
interface ServiceComponent {

}
```

```kotlin
@Module
class ServiceModule(
        val service: Service
) {

		//All service requires basically context
    @Provides
    fun context(): Context = service

}
```

```kotlin
abstract class BaseService: Service() {

    private val appComponent get() = (application as MyApplication).appComponent

    val serviceComponent by lazy {
        appComponent.newServiceComponent(ServiceModule(this))
    }

}
```

```kotlin
open class BaseDialog: DialogFragment() {
    private val presentationComponent by lazy {
        (requireActivity() as BaseActivity).activityComponent.newPresentationComponent()
    }
    protected val injector get() = presentationComponent
}
```

⇒ Dialog are controllers. It can be a very complex object.

⇒ Recommended to be used with `DialogFragment()`

# v.0.0.2 - optimization

⇒ Actually, the optimization is not so obvious.

⇒ Not recommended to implement this concept

### Make static..

```kotlin
@Module    //argument `activity` is called bootstrapping dependency, which you can only get when running application
class ActivityModule(
        val activity: AppCompatActivity
) {

    @Provides
    fun activity() = activity

    companion object {

        @Provides
        @ActivityScope
        fun screensNavigator(activity: AppCompatActivity) = ScreensNavigator(activity)

        @Provides
        fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

    }

}
```

### More advanced concept - how to handle bootstrapped argument?

- Bootstrapped argument means, some dependencies must be injected outside of the module

- ActivityModule.kt

```kotlin
@Module    //argument `activity` is called bootstrapping dependency, which you can only get when running application
class ActivityModule(
        val activity: AppCompatActivity
) {

    @Provides
    fun activity() = activity

    companion object {

        @Provides
        @ActivityScope
        fun screensNavigator(activity: AppCompatActivity) = ScreensNavigator(activity)

        @Provides
        fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

    }

}
```

⇒ ActivityModule has bootstrapped value

⇒ What can we do to remove this?

- ActivityComponent.kt

```kotlin
@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun newPresentationComponent(): PresentationComponent
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun activity(activity: AppCompatActivity): Builder
        fun activityModule(activityModule: ActivityModule): Builder
        fun build(): ActivityComponent
    }
}
```

⇒ Define Builder with `@Subcomponent` and `@BindsInstance`

- Refactor ActivityModule.kt to `object`

```kotlin
@Module    //argument `activity` is called bootstrapping dependency, which you can only get when running application
class ActivityModule(
        val activity: AppCompatActivity
) {

    @Provides
    fun activity() = activity

    companion object {

        @Provides
        @ActivityScope
        fun screensNavigator(activity: AppCompatActivity) = ScreensNavigator(activity)

        @Provides
        fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

    }

}
```

```kotlin
@Module    //argument `activity` is called bootstrapping dependency, which you can only get when running application
object ActivityModule {

//    @Provides
//    fun activity() = activity

    @Provides
    @ActivityScope
    fun screensNavigator(activity: AppCompatActivity) = ScreensNavigator(activity)

    @Provides
    fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

    @Provides
    fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

}
```

- BaseActivity.kt

```kotlin
open class BaseActivity : AppCompatActivity() {

    private val appComponent get() = (application as MyApplication).appComponent

    val activityComponent by lazy {
        appComponent.newActivityComponentBuilder()
                .activity(this) //Inject activity here
                .activityModule(ActivityModule)
                .build()
    }

    private val presentationComponent: PresentationComponent by lazy {
        activityComponent.newPresentationComponent()
    }

    protected val injector get() = presentationComponent

}
```

⇒ Instead of passing activity in `ActivityModule`, you can pass activity directly.

### Dagger Conventions (8):

- Dagger generates more performant code for static providers in Modules (use companion object or top-level object in Kotlin)
- @Component.Builder(or @Subcomponent.Builder) designates builder interface for Component
- @BindsInstance allows for injection of "bootstrapping dependencies" directly into Component Builders

# v.0.0.3 - Type Bindings

- Interface <=> Impl

### ScreensNavigator Split into two files

- **Original File**

```kotlin
class ScreensNavigator (private val activity: AppCompatActivity)   {

    fun navigateBack() {
        activity.onBackPressed()
    }

    fun toQuestionDetails(questionId: String) {
        QuestionDetailsActivity.start(activity, questionId)
    }
}
```

- Split File - ScreensNavigator

```kotlin
interface ScreensNavigator  {
    fun navigateBack()
    fun toQuestionDetails(questionId: String)
}
```

- Split File - ScreensNavigatorImpl

```kotlin
class ScreensNavigatorImpl (private val activity: AppCompatActivity): ScreensNavigator   {

    override fun navigateBack() {
        activity.onBackPressed()
    }

    override fun toQuestionDetails(questionId: String) {
        QuestionDetailsActivity.start(activity, questionId)
    }
}
```

⇒ Will dagger know the difference?

⇒ No

### ActivityModule.kt

- before refactoring

```kotlin
@Module
object ActivityModule {

    @Provides
    @ActivityScope
    fun screensNavigator(activity: AppCompatActivity) = ScreensNavigator(activity)

    @Provides
    fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

    @Provides
    fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

}
```

- after refactoring

```kotlin
@Module
object ActivityModule {

    @Provides
    @ActivityScope
    fun screensNavigator(activity: AppCompatActivity): ScreensNavigator = ScreensNavigatorImpl(activity)

    @Provides
    fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

    @Provides
    fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager

}
```

⇒ screensNavigator returns interface `ScreensNavigator`

### Second option using automatic detecting dependency injection

- ScreensNavigatorImpl.kt

  ⇒ Give two more notations `@ActivityScope` and `@Inject`

```kotlin
@ActivityScope
class ScreensNavigatorImpl @Inject constructor(private val activity: AppCompatActivity): ScreensNavigator   {

    override fun navigateBack() {
        activity.onBackPressed()
    }

    override fun toQuestionDetails(questionId: String) {
        QuestionDetailsActivity.start(activity, questionId)
    }
}
```

- ActivityModule.kt

```kotlin
@Module
abstract class ActivityModule {

//    @Provides
//    @ActivityScope
//    fun screensNavigator(activity: AppCompatActivity): ScreensNavigator = ScreensNavigatorImpl(activity)

    @Binds
    abstract fun screensNavigator(screensNavigatorImpl: ScreensNavigatorImpl): ScreensNavigator

    companion object {
        @Provides
        fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager
    }

}
```

⇒ delete screensNavigator.kt

- ActivityComponent.kt

```kotlin
@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent {
    fun newPresentationComponent(): PresentationComponent
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun activity(activity: AppCompatActivity): Builder
//        fun activityModule(activityModule: ActivityModule): Builder
        fun build(): ActivityComponent
    }
}
```

⇒ Delete `fun activityModule` , activityModule is abstract class so it can never be instantiated.

- Initialize ActivityComponent.kt

```kotlin
val activityComponent by lazy {
        appComponent.newActivityComponentBuilder()
                .activity(this)
                .build()
    }
```

### Dagger Conventions (9):

- @Binds allows to map specific provided type to another provided type (e.g. provide implementation of an interface)
- Custom bindings using @Binds must be defined as abstract functions in abstract modules
- Abstract @Binds functions can't coexist with non-static provider methods in the same module
