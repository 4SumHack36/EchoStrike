package com.foursum.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.foursum.helper.ui.screenmodel.SensorScreenModel
import com.foursum.helper.ui.screens.SensorScreen
import com.foursum.helper.ui.theme.HelperTheme
import com.foursum.helper.util.SharedPrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var n by mutableIntStateOf(1)

class MainActivity : ComponentActivity() {

    private val manageExternalStoragePermissionLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                // Permission granted, create the directory and perform the task
                createFilesSavedDirectory()
                handleIntent(intent)
            } else {
                // Permission denied, show a message to the user
                // You can use a Toast or Snackbar to inform the user
            }
        }

    private lateinit var sensorScreenModel: SensorScreenModel
    private lateinit var sharedPrefsManager: SharedPrefsManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // get the value of n from shared preferences if it doesn't exist set it to 0

        val sharedPref = getSharedPreferences("com.kanhaji.savefile", MODE_PRIVATE)
        n = sharedPref.getInt("n", 1)

        var currentPrefix by mutableStateOf(sharedPref.getString("fileName", "<null>")!!)

        var nextFileName by mutableStateOf("${currentPrefix}_$n")
        handleIntent(intent)

        sharedPrefsManager = SharedPrefsManager(this@MainActivity) // Initialize SharedPrefsManager
        sensorScreenModel =
            SensorScreenModel(sharedPrefsManager, this@MainActivity) // Initialize SensorScreenModel

        setContent {
            HelperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Spacer(modifier = Modifier.padding(innerPadding))
                        Navigator(
                            SensorScreen(
                                this@MainActivity,
                                sharedPrefsManager
                            )
                        ) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                }
            }
        }
        if (!Environment.isExternalStorageManager()) {
            requestManageExternalStoragePermission()
        } else {
            createFilesSavedDirectory()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    CoroutineScope(Dispatchers.Main).launch {
                        copyFileToSdCard(this@MainActivity, uri)
                    }
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                    CoroutineScope(Dispatchers.Main).launch {
                        copyFilesToSdCard(this@MainActivity, uris)
                    }
                }
            }
        }
    }

    private fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = "package:$packageName".toUri()
        }
        manageExternalStoragePermissionLauncher.launch(intent)
    }

}