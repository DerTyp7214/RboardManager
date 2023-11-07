package de.dertyp7214.rboardmanager.core

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

val Activity.appUpdateManager: AppUpdateManager
    get() = AppUpdateManagerFactory.create(this)

val Activity.appUpdateInfoTask: Task<AppUpdateInfo>
    get() = appUpdateManager.appUpdateInfo

fun Activity.checkUpdate(onUpdateAvailable: AppUpdateManager.(AppUpdateInfo, Int) -> Unit) {
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            when {
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> onUpdateAvailable(
                    appUpdateManager,
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE
                )

                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> onUpdateAvailable(
                    appUpdateManager,
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE
                )
            }
        }
    }
}

fun Activity.startUpdate(
    resultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    appUpdateManager: AppUpdateManager,
    appUpdateInfo: AppUpdateInfo,
    @AppUpdateType type: Int = AppUpdateType.FLEXIBLE
) {
    appUpdateManager.startUpdateFlowForResult(
        appUpdateInfo,
        resultLauncher,
        AppUpdateOptions.newBuilder(type).build()
    )
}