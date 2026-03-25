package test.android.kiosk.provider

import android.content.Context

internal class FinalPackages(private val context: Context) : Packages {
    override fun launch(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: error("No intent for package: $packageName!")
        context.startActivity(intent)
    }
}
