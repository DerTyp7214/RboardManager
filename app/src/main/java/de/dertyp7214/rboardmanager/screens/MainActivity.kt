package de.dertyp7214.rboardmanager.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import de.dertyp7214.rboardmanager.R
import de.dertyp7214.rboardmanager.core.appUpdateManager
import de.dertyp7214.rboardmanager.core.checkUpdate
import de.dertyp7214.rboardmanager.core.startUpdate
import de.dertyp7214.rboardmanager.screens.ui.theme.RboardManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val updateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                Log.d("UPDATE", "Update flow failed! Result code: ${result.resultCode}")
            }
        }

    private fun updateListener(
        manager: AppUpdateManager,
        onDownloading: AppUpdateManager.(progress: Float) -> Unit,
        onDownloadFinished: AppUpdateManager.() -> Unit,
        onInstalling: AppUpdateManager.() -> Unit,
        onInstallFinished: AppUpdateManager.() -> Unit,
        onFailed: AppUpdateManager.(errorCode: Int) -> Unit
    ) = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> onDownloadFinished(manager)
            InstallStatus.INSTALLED -> onInstallFinished(manager)
            InstallStatus.DOWNLOADING -> onDownloading(
                manager,
                state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
            )

            InstallStatus.INSTALLING -> onInstalling(manager)
            InstallStatus.FAILED -> onFailed(manager, state.installErrorCode())
            else -> {
            }
        }
    }

    private val hostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RboardManagerTheme {
                val scope = rememberCoroutineScope()

                checkUpdate { appUpdateInfo, type ->
                    val updateListener = updateListener(
                        this,
                        onDownloading = { progress ->
                            Log.d("UPDATE", "Downloading: $progress")
                        },
                        onDownloadFinished = {
                            scope.launch {
                                val result = hostState.showSnackbar(
                                    message = getString(R.string.update_downloaded),
                                    actionLabel = getString(R.string.update_install),
                                )

                                when (result) {
                                    SnackbarResult.ActionPerformed -> completeUpdate()
                                    SnackbarResult.Dismissed -> {
                                        Log.d("UPDATE", "Dismissed")
                                    }
                                }
                            }
                        },
                        onInstalling = {
                            Log.d("UPDATE", "Installing")
                        },
                        onInstallFinished = {
                            Log.d("UPDATE", "Install finished")
                        },
                        onFailed = { errorCode ->
                            Log.d("UPDATE", "Failed: $errorCode")
                        }
                    )

                    if (type == AppUpdateType.FLEXIBLE) registerListener(updateListener)
                    startUpdate(updateResultLauncher, this, appUpdateInfo, type)
                    if (type == AppUpdateType.FLEXIBLE) unregisterListener(updateListener)
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = hostState)
                    }
                ) { contentPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Greeting("Android")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                lifecycleScope.launch {
                    val result = hostState.showSnackbar(
                        message = getString(R.string.update_downloaded),
                        actionLabel = getString(R.string.update_install),
                    )

                    when (result) {
                        SnackbarResult.ActionPerformed -> appUpdateManager.completeUpdate()
                        SnackbarResult.Dismissed -> {
                            Log.d("UPDATE", "Dismissed")
                        }
                    }
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdate(updateResultLauncher, appUpdateManager, appUpdateInfo, IMMEDIATE)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RboardManagerTheme {
        Greeting("Android")
    }
}