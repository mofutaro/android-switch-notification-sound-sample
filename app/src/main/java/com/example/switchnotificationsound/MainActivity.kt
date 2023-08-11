package com.example.switchnotificationsound

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
        makeChannel(this)
        setContent {
            val context = LocalContext.current
            val currentSelectedSound = remember {
                mutableStateOf<UltimateRingtonePicker.RingtoneEntry?>(null)
            }
            val ringtoneLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    // The id of the channel.


                    currentSelectedSound.value = RingtonePickerActivity.getPickerResult(it.data).firstOrNull()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (currentSelectedSound.value != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.deleteNotificationChannel(CHANNEL_ID)
                        makeChannel(context, currentSelectedSound.value!!.uri)
                    }
                }
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

            }
            val permissions = remember {
                mutableStateListOf<String>()
            }

            LaunchedEffect(Unit) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && !checkIfPostNotificationsGranted(context)
                ) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                for (p in permissions) {
                    permissionLauncher.launch(p)
                }
            }
            SwitchNotificationSoundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {

                        Button(onClick = { ringtoneLauncher.launch(Intent(context, SoundActivity::class.java)) }) {
                            Text("通知音を選択する")
                        }

                        if (currentSelectedSound.value != null) {
                            Text(currentSelectedSound.value!!.name)
                        }
                        Button(
                            onClick = {
                                val builder = getNotificationBuilder(context, "通知のテスト", currentSelectedSound.value?.uri)
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    Log.d("TEST", "NOT_GRANTED")
                                } else {
                                    Log.d("TEST", "GRANTED")
                                    NotificationManagerCompat.from(context).notify(0, builder.build())
                                }

                             }
                        ) {
                            Text("通知のテスト")
                        }

                    }


                }
            }
        }
    }
}

private const val CHANNEL_ID = "my_channel_1"
private const val CHANNEL_NAME = "通知チャンネルです"

private fun makeChannel(context: Context, sound: Uri? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .build()

        //val uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.clock_alarm}")
        val name = CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        //channel.setSound(uri, audioAttributes)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        if (sound != null) {
            channel.setSound(sound, audioAttributes)
        }


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
        NotificationCompat.Builder(context, CHANNEL_ID)
    }
}

fun getNotificationBuilder(context: Context, message: String, sound: Uri? = null): NotificationCompat.Builder {
    //val uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.clock_alarm}")
    return NotificationCompat.Builder(context, CHANNEL_ID)
        //.setContentIntent(getContentIntent(context))
        .setSmallIcon(R.drawable.ic_stat_name)
        .setContentTitle("通知テスト")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVibrate(longArrayOf(0, 500))
        .apply {
            if (sound != null) {
                // ignored!!
                setSound(sound)
            }
        }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkIfPostNotificationsGranted(context: Context): Boolean {
    val permissions = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    )
    return permissions == PackageManager.PERMISSION_GRANTED
}