package test.android.kiosk.provider

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.sdkapi.api.SdkApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class FinalAdmins(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val default: CoroutineContext,
) : Admins {
    private val _owners: MutableStateFlow<Boolean>
    override val owners: StateFlow<Boolean>

    override val locked = object : StateFlow<Boolean> {
        override val value: Boolean
            get() {
                val am = context.getSystemService(ActivityManager::class.java)
                val isLocked = am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
                return isLocked
            }
        override val replayCache: List<Boolean> = emptyList()

        override suspend fun collect(collector: FlowCollector<Boolean>): Nothing {
            val am = context.getSystemService(ActivityManager::class.java)
            val lockedState = AtomicBoolean(am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE)
            collector.emit(lockedState.get())
            while (true) {
                val isLocked = am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
                if (lockedState.compareAndSet(!isLocked, isLocked)) {
                    collector.emit(isLocked)
                }
                delay(1.seconds)
            }
        }
    }

    private fun onDeviceOwner(isDeviceOwner: Boolean) {
        if (!isDeviceOwner) return
        coroutineScope.launch {
            withContext(default) {
                val dm = context.getSystemService(DevicePolicyManager::class.java)
                while (_owners.value) {
                    if (!dm.isDeviceOwnerApp(context.packageName)) {
                        _owners.value = false
                        break
                    }
                    delay(1.seconds)
                }
            }
        }
    }

    init {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        _owners = MutableStateFlow(dm.isDeviceOwnerApp(context.packageName))
        owners = _owners.asStateFlow()
        val receivers = object : BroadcastReceiver() {
            override fun onReceive(_context: Context?, intent: Intent?) {
                when (intent?.action) {
                    DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED -> {
                        _owners.value = dm.isDeviceOwnerApp(context.packageName)
                    }
                }
            }
        }
        val filters = IntentFilter()
        filters.addAction(DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receivers, filters, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receivers, filters)
        }
        coroutineScope.launch {
            withContext(default) {
                _owners.collect { isDeviceOwner ->
                    onDeviceOwner(isDeviceOwner = isDeviceOwner)
                }
            }
        }
        SdkApi.newInstance(context)
        coroutineScope.launch {
            withContext(default) {
                withTimeout(2.seconds) {
                    while (isActive) {
                        if (SdkApi.getInstance().serviceConnectionStatus) break
                        delay(250.milliseconds)
                    }
                }
            }
        }
    }

    override fun update(isDeviceOwner: Boolean) {
        if (_owners.value == isDeviceOwner) return
        if (isDeviceOwner) error("Set an app the device owner is not supported!")
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        dm.clearDeviceOwnerApp(context.packageName)
    }

    override var statusBarDisplay: Boolean
        get() {
            return SdkApi.getInstance().SystemCtrl().statusBarDisplay
        }
        set(value) {
            SdkApi.getInstance().SystemCtrl().statusBarDisplay = value
        }
}
