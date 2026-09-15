package me.elhizazi.Castix
 
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine
import me.elhizazi.Castix.privilege.ui.MainViewModel
import me.elhizazi.Castix.privilege.ui.PrivilegeHomeScreen
import me.elhizazi.Castix.ui.theme.MyApplicationTheme
import rikka.shizuku.Shizuku
 
class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels {
    MainViewModel.Factory(application)
  }

  private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
    viewModel.refreshEngineStatus()
  }

  private val binderDeadListener = Shizuku.OnBinderDeadListener {
    viewModel.refreshEngineStatus()
  }

  private val requestPermissionResultListener =
    Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
      if (requestCode == ShizukuEngine.SHIZUKU_REQUEST_CODE) {
        viewModel.onShizukuPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED)
      }
    }
 
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Register Shizuku lifecycle listeners safely
    runCatching {
      Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
      Shizuku.addBinderDeadListener(binderDeadListener)
      Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
    }

    setContent {
      MyApplicationTheme {
        PrivilegeHomeScreen(viewModel = viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Refresh status when returning from other apps (e.g. LSPosed Manager or Shizuku)
    viewModel.refreshEngineStatus()
  }

  override fun onDestroy() {
    super.onDestroy()
    runCatching {
      Shizuku.removeBinderReceivedListener(binderReceivedListener)
      Shizuku.removeBinderDeadListener(binderDeadListener)
      Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
  }
}
