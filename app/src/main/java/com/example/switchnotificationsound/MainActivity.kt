package com.example.switchnotificationsound

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.switchnotificationsound.ui.theme.SwitchNotificationSoundTheme
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

class MainActivity : ComponentActivity() {

    //private var currentSelectedSound: UltimateRingtonePicker.RingtoneEntry? = null

    /*private val ringtoneLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                currentSelectedSound = RingtonePickerActivity.getPickerResult(it.data).firstOrNull()
            }
        }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentSelectedSound = remember {
                mutableStateOf<UltimateRingtonePicker.RingtoneEntry?>(null)
            }
            val ringtoneLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    currentSelectedSound.value = RingtonePickerActivity.getPickerResult(it.data).firstOrNull()
                }
            }
            SwitchNotificationSoundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        if (currentSelectedSound.value != null) {
                            Text(currentSelectedSound.value!!.name)
                        }
                        val context = LocalContext.current
                        Button(onClick = { ringtoneLauncher.launch(Intent(context, SoundActivity::class.java)) }) {
                            Text("通知音を選択する")
                        }
                    }


                }
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
    SwitchNotificationSoundTheme {
        Greeting("Android")
    }
}