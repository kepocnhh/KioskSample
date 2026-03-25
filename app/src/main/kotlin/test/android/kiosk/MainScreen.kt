package test.android.kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun MainScreen() {
    val providers = remember { App.providers }
    val logger = remember { providers.loggers.create("[Main]") }
    val isDeviceOwner = providers.admins.owners.collectAsState().value
    val isLocked = providers.admins.locked.collectAsState().value
    val context = LocalContext.current
    val activity = LocalActivity.current ?: TODO()
    val packageName = "sp.sample.animations.debug" // todo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
                text = "owner: $isDeviceOwner",
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
                text = "locked: $isLocked",
            )
            if (isDeviceOwner) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            providers.admins.update(isDeviceOwner = false)
                        }
                        .wrapContentSize(),
                    text = "remove admin",
                )
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            if (isLocked) {
                                activity.stopLockTask()
                                val controller = WindowInsetsControllerCompat(
                                    activity.window,
                                    activity.window.decorView
                                )
                                controller.show(WindowInsetsCompat.Type.navigationBars())
                                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                            } else {
                                val dm = context.getSystemService(DevicePolicyManager::class.java)
                                val admin = ComponentName(context, MainDeviceAdminReceiver::class.java)
                                dm.setLockTaskPackages(admin, arrayOf(context.packageName))
                                dm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
                                activity.startLockTask()
                                val controller = WindowInsetsControllerCompat(
                                    activity.window,
                                    activity.window.decorView
                                )
                                controller.hide(WindowInsetsCompat.Type.navigationBars())
                                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        }
                        .wrapContentSize(),
                    text = if (isLocked) "unlock" else "lock",
                )
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        providers.packages.launch(packageName = packageName)
                    }
                    .wrapContentSize(),
                text = "launch: $packageName",
            )
        }
    }
}
