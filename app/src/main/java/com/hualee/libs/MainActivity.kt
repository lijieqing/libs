package com.hualee.libs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.hualee.lame.LameControl
import com.hualee.libs.ui.theme.LibsTheme
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(
                "android.permission.WRITE_EXTERNAL_STORAGE",
            ),
            0
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val path = ctx.externalCacheDir?.absolutePath
    Text(
        text = "Hello $name!",
        modifier = modifier.clickable {
            Thread {
                Log.d("LiJie", "mp3 path:$path")
                File("$path/audio_demo.mp3").also {
                    Log.d("LiJie", "mp3 ${it.absolutePath}:${it.exists()}")
                }
                LameControl.mp3ToPCM("$path/audio_demo.mp3", "$path/audio_demo.pcm")
            }.start()
        },
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LibsTheme {
        Greeting("Android")
    }
}