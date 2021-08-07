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
							//.activityModule(ActivityModule())
                .build()
    }
```

### Dagger Conventions (9):

- @Binds allows to map specific provided type to another provided type (e.g. provide implementation of an interface)
- Custom bindings using @Binds must be defined as abstract functions in abstract modules
- Abstract @Binds functions can't coexist with non-static provider methods in the same module

# v.0.0.4, Qualifiers

### Situation # 1, You need multiple retrofit instances

```kotlin
package com.techyourchance.dagger2course.common.dependnecyinjection

import javax.inject.Qualifier

@Qualifier
annotation class Retrofit1 {
}
```

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Retrofit1
    fun retrofit1(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    fun retrofit2(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

### Situation #2, just provide specific retrofit

```kotlin
@Qualifier
annotation class Retrofit1 {
}
```

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Retrofit1
    fun retrofit1(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Retrofit1 retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

### Give specific retrofit instance

```kotlin
@Qualifier
annotation class Retrofit1 {
}
@Qualifier
annotation class Retrofit2 {
}
```

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Retrofit1
    fun retrofit1(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    @Retrofit2
    fun retrofit2(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://blabla.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Retrofit2 retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

### using `Named()`

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Named("Retrofit1")
    fun retrofit1(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    @Named("Retrofit2")
    fun retrofit2(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://blabla.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Named("retrofit2") retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

### Named() extremely used

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Named("Retrofit1")
    fun retrofit1(@Named("base_url") baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    @Named("Retrofit2")
    fun retrofit2(@Named("other_base_url") baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @Named("base_url")
    fun baseUrl() = Constants.BASE_URL

    @Provides
    @Named("other_base_url")
    fun otherBaseUrl() = "https://blabla.com/"

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Named("Retrofit2") retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

⇒ Bad examples

⇒ Dependency Injection only uses `behavior` but `baseUrl` and `otherBaseUrl` are not behavior but `data structure`

### Correct way to avoid dependency injection with data structures

- Create behavior

```kotlin

class UrlProvider {
    fun getBaseUrl1(): String = Constants.BASE_URL
    fun getBaseUrl2(): String = "https://www.helloworld.com"
}
```

- Refactor AppModule

```kotlin
@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Named("Retrofit1")
    fun retrofit1(urlProvider: UrlProvider): Retrofit {
        return Retrofit.Builder()
                .baseUrl(urlProvider.getBaseUrl1())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    @Named("Retrofit2")
    fun retrofit2(urlProvider: UrlProvider): Retrofit {
        return Retrofit.Builder()
                .baseUrl(urlProvider.getBaseUrl2())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

//    @Provides
//    @Named("base_url")
//    fun baseUrl() = Constants.BASE_URL

//    @Provides
//    @Named("other_base_url")
//    fun otherBaseUrl() = "https://blabla.com/"

    @AppScope
    @Provides
    fun urlProvider() = UrlProvider()

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Named("Retrofit2") retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}
```

### Dagger Conventions (10)

- Qualifiers are annotation classes annotated with @Qualifier
- From Dagger's standpoint, qualifiers are part of the type (e.g. @Q1 Service and @Q2 Service are different types)

# v.0.0.5 - Providers

```kotlin
@PresentationScope
@Subcomponent()
interface PresentationComponent {
    fun imageLoader(): ImageLoader
    fun inject(fragment: QuestionsListFragment)
    fun inject(activity: QuestionDetailsActivity)
    fun inject(questionsListActivity: QuestionsListActivity)
}
```

```kotlin
@ActivityScope
class ImageLoader @Inject constructor(private val activity: AppCompatActivity) {

    private val requestOptions = RequestOptions().centerCrop()

    fun loadImage(imageUrl: String, target: ImageView) {
        Glide.with(activity).load(imageUrl).apply(requestOptions).into(target)
    }

}
```

```kotlin
class ViewMvcFactory @Inject constructor(private val layoutInflater: LayoutInflater, private val imageLoaderProvider: Provider<ImageLoader>) {

    fun newQuestionsListViewMvc(parent: ViewGroup?): QuestionsListViewMvc {
        return QuestionsListViewMvc(layoutInflater, parent)
    }

    fun newQuestionDetailsViewMvc(parent: ViewGroup?): QuestionDetailsViewMvc {
        return QuestionDetailsViewMvc(layoutInflater, imageLoaderProvider.get(), parent)
    }
}
```

⇒ By giving `Provider<ImageLoader>` , ImageLoader keeps all the characteristics of Dagger Injection

```kotlin
class ViewMvcFactory @Inject constructor(private val layoutInflaterProvider: Provider<LayoutInflater>, private val imageLoaderProvider: Provider<ImageLoader>) {

    fun newQuestionsListViewMvc(parent: ViewGroup?): QuestionsListViewMvc {
        return QuestionsListViewMvc(layoutInflaterProvider.get(), parent)
    }

    fun newQuestionDetailsViewMvc(parent: ViewGroup?): QuestionDetailsViewMvc {
        return QuestionDetailsViewMvc(layoutInflaterProvider.get(), imageLoaderProvider.get(), parent)
    }
}
```

⇒ You can apply it to any properties which are provided by dagger

### Dagger Conventions (11):

- Provider<Type> wrappers are "windows" into Dagger's object graph and allow you to retrieve a single type of services
- Providers are basically "extensions" of composition roots
- You use Providers when you need to perform "late injection" (allowing same instance)
