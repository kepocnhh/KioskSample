package test.android.kiosk.provider

import kotlinx.coroutines.flow.StateFlow

internal interface Admins {
    val owners: StateFlow<Boolean>

    fun update(isDeviceOwner: Boolean)
}
