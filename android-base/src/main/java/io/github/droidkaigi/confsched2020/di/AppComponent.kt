package io.github.droidkaigi.confsched2020.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.github.droidkaigi.confsched2020.App
import io.github.droidkaigi.confsched2020.MainActivityModule
import io.github.droidkaigi.confsched2020.data.repository.StaffRepository
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        MainActivityModule.MainActivityBuilder::class,
        DbComponentModule::class,
        RepositoryComponentModule::class,
        FirestoreComponentModule::class,
        ApiComponentModule::class,
        DeviceComponentModule::class
    ]
)
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    interface Builder {
        @BindsInstance fun application(application: Application): Builder

        fun build(): AppComponent
    }

    override fun inject(app: App)

    fun staffRepository(): StaffRepository
}

fun Application.createAppComponent() = DaggerAppComponent.builder()
    .application(this)
    .build()