package ua.polodarb.gmsflags

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import ua.polodarb.gmsflags.presentation.core.ui.overscroll.GmsOverscrollRubberBandCoefficient
import ua.polodarb.gmsflags.presentation.core.ui.overscroll.rememberCupertinoOverscrollFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import ua.polodarb.gmsflags.navigation.RootNavDisplay
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.GmsAdaptiveLayoutDefaults
import ua.polodarb.gmsflags.presentation.core.ui.adaptive.LocalGmsAdaptiveLayout
import ua.polodarb.gmsflags.presentation.core.ui.theme.GMSFlags20Theme
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.GmsSnackbarHost
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.LocalGmsSnackbarHostState
import ua.polodarb.gmsflags.presentation.core.ui.snackbar.rememberGmsSnackbarHostState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import ua.polodarb.gmsflags.startup.AppStartupState
import ua.polodarb.gmsflags.startup.AppStartupViewModel
import ua.polodarb.gmsflags.startup.ui.AppStartupGate
import ua.polodarb.gmsflags.update.AppUpdateGate

class HostActivity : ComponentActivity() {
    private var externalImportDocumentUri by mutableStateOf<String?>(null)
    private val startupViewModel: AppStartupViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.state.value == AppStartupState.Loading
        }
        super.onCreate(savedInstanceState)
        externalImportDocumentUri = intent.externalImportDocumentUri()
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val adaptiveLayout = remember(widthSizeClass) {
                GmsAdaptiveLayoutDefaults.from(widthSizeClass)
            }
            GMSFlags20Theme {
                val startupState by startupViewModel.state.collectAsStateWithLifecycle()
                val snackbarHostState = rememberGmsSnackbarHostState()
                CompositionLocalProvider(
                    LocalGmsAdaptiveLayout provides adaptiveLayout,
                    LocalGmsSnackbarHostState provides snackbarHostState,
                    LocalOverscrollFactory provides rememberCupertinoOverscrollFactory(
                        rubberBandCoefficient = GmsOverscrollRubberBandCoefficient,
                    ),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppStartupGate(
                            state = startupState,
                            onRetryRoot = startupViewModel::retryRootAccess,
                        ) {
                            RootNavDisplay(
                                externalImportDocumentUri = externalImportDocumentUri,
                                onExternalImportConsumed = { externalImportDocumentUri = null },
                            )
                        }
                        GmsSnackbarHost(hostState = snackbarHostState)
                        AppUpdateGate()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalImportDocumentUri = intent.externalImportDocumentUri()
    }

    private fun Intent.externalImportDocumentUri(): String? = data
        ?.toString()
        ?.takeIf { action == Intent.ACTION_VIEW }

}
