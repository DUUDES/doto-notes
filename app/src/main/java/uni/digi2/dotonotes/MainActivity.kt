package uni.digi2.dotonotes

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import uni.digi2.dotonotes.ui.BottomNavigationApp
import uni.digi2.dotonotes.ui.Screen
import uni.digi2.dotonotes.ui.screens.authorization.FirebaseUIAuthScreen
import uni.digi2.dotonotes.ui.screens.splash.SplashViewModel
import uni.digi2.dotonotes.ui.theme.DoToTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val viewModel = SplashViewModel()

        installSplashScreen().setKeepOnScreenCondition { viewModel.isLoading.value }

        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        setContent {
            DoToTheme {
                val navController = rememberNavController()

                if (FirebaseAuth.getInstance().currentUser == null) {
                    FirebaseUIAuthScreen(
                        signInProviders = providers,
                        onSignInSuccess = {
                            setContent {
                                BottomNavigationApp(navController)
                            }
                        },
                        onSignInFailure = { error ->
                            Log.d("GAUTH ERROR", "error encountered on google auth ${error.message}")
                        })
                }
                else {
                    BottomNavigationApp(navController)
                }
            }
        }
    }
}


