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
