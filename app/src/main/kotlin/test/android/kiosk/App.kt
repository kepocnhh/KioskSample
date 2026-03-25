package test.android.kiosk

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import test.android.kiosk.provider.Admins
import test.android.kiosk.provider.Contexts
import test.android.kiosk.provider.FinalAdmins
import test.android.kiosk.provider.FinalLoggers
import test.android.kiosk.provider.FinalPackages
import test.android.kiosk.provider.Loggers
import test.android.kiosk.provider.Packages
import test.android.kiosk.provider.Providers

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        val job = SupervisorJob()
        val coroutineScope = CoroutineScope(contexts.main + job)
        val context: Context = this
        val admins: Admins = FinalAdmins(
            context = context,
            coroutineScope = coroutineScope,
            default = contexts.default,
        )
        val loggers: Loggers = FinalLoggers
        val packages: Packages = FinalPackages(context = context)
        _providers = Providers(
            contexts = contexts,
            admins = admins,
            loggers = loggers,
            packages = packages,
        )
    }

    companion object {
        private var _providers: Providers? = null
        val providers: Providers get() = checkNotNull(_providers) { "No providers!" }
    }
}
