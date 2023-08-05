package com.hualee.libs

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.hualee.lame.LameDecode
import com.hualee.libs.ui.theme.LibsTheme
import kotlinx.coroutines.launch
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
                    Content()
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

private fun initLameFuncList(ctx: Context): List<LibFunc> {
    return listOf(
        LibFunc("MP3->PCM") {
            val path = ctx.externalCacheDir?.absolutePath
            Log.d("LiJie", "mp3 path:$path")
            File("$path/audio_demo.mp3").also {
                Log.d("LiJie", "mp3 ${it.absolutePath}:${it.exists()}")
            }
            LameDecode.mp3ToPcm("$path/audio_demo.mp3", "$path/audio_demo.pcm")
        },
        LibFunc("PCM->MP3") {

        },
    )
}

private fun initXMLParseFuncList(ctx: Context): List<LibFunc> {
    return listOf(
        LibFunc("Java->XML") {

        },
        LibFunc("XML->Java") {

        },
    )
}

@Composable
private fun Content() {
    val ctx = LocalContext.current
    val width = 260.dp
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            LibsColumn(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(width),
                title = "Lame Lib",
                list = initLameFuncList(ctx),
                itemWidth = width,
            )
            LibsColumn(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(width),
                title = "xml-parse Lib",
                list = initXMLParseFuncList(ctx),
                itemWidth = width,
            )
        }
    }
}

data class LibFunc(val name: String, val func: Runnable)

@Composable
private fun LibsColumn(
    modifier: Modifier,
    title: String,
    list: List<LibFunc>,
    itemWidth: Dp = 260.dp,
    itemHeight: Dp = 60.dp,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(itemWidth)
                .height(itemHeight)
                .align(Alignment.TopCenter)
                .background(Color.LightGray)
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Divider()
        LazyColumn(
            modifier = Modifier
                .width(itemWidth)
                .padding(top = itemHeight)
                .align(Alignment.TopCenter)
        ) {
            itemsIndexed(list) { _, item ->
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .height(itemHeight)
                        .border(width = 1.dp, color = Color.LightGray)
                        .clickable { scope.launch { item.func.run() } },
                ) {
                    Text(
                        text = item.name,
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun GreetingPreview() {
    LibsTheme {
        Content()
    }
}