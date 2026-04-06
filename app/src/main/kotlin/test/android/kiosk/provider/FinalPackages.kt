package test.android.kiosk.provider

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent

internal class FinalPackages(private val context: Context) : Packages {
    override fun launch(packageName: String, needsLockTask: Boolean) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: error("No intent for package: $packageName!")
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(flags)
        val options = ActivityOptions.makeBasic().setLockTaskEnabled(needsLockTask)
        context.startActivity(intent, options.toBundle())
    }
}
