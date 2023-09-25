package com.hualee.libs

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Gravity
import android.view.PixelCopy
import android.view.PixelCopy.OnPixelCopyFinishedListener
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.renderscript.Toolkit
import com.hualee.lame.LameDecode
import com.hualee.libs.ui.theme.LibsTheme
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CountDownLatch


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

private fun initRenderscriptFuncList(
    ctx: Context,
    composeView: View,
): List<LibFunc> {
    val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    var params: WindowManager.LayoutParams =
        WindowManager.LayoutParams().apply {
            gravity = Gravity.BOTTOM
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            //token = composeView.applicationWindowToken

            format = PixelFormat.TRANSLUCENT
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL // 触摸事件透传问题
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 返回事件透传问题


            width = WindowManager.LayoutParams.MATCH_PARENT
            height = 200
        }
    val activity = ctx as Activity

    return listOf(
        LibFunc("addWindow") {
            windowManager.addView(WindowComposeView(composeView).also { windowView ->
                windowView.setCustomContent {
                    val handlerThread = remember {
                        HandlerThread("blur_bitmap").apply {
                            start()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .onGloballyPositioned {
                                Log.d("LiJie", "boundsInWindow = ${it.boundsInWindow()}")
                                Log.d("LiJie", "boundsInParent = ${it.boundsInParent()}")
                                Log.d("LiJie", "boundsInRoot = ${it.boundsInRoot()}")
                                Log.d("LiJie", "left = ${windowView.left}")
                                Log.d("LiJie", "top = ${windowView.top}")
                                Log.d("LiJie", "right = ${windowView.right}")
                                Log.d("LiJie", "bottom = ${windowView.bottom}")

                            }
                            .clickable {
                                Toast
                                    .makeText(ctx, "sub window", LENGTH_SHORT)
                                    .show()
                            }
                            .drawWithContent {
                                val rect = Rect().apply {
                                    left = 0
                                    top = (activity.window.decorView.height - size.height).toInt()
                                    right = activity.window.decorView.width.toInt()
                                    bottom = activity.window.decorView.height.toInt()
                                }
                                val bitmap = Bitmap.createBitmap(
                                    size.width.toInt(),
                                    size.height.toInt(),
                                    Bitmap.Config.ARGB_8888
                                )
                                var blurredBitmap:Bitmap? = null
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val latch = CountDownLatch(1)
                                    PixelCopy.request(
                                        activity.window,
                                        rect,
                                        bitmap,
                                        object : OnPixelCopyFinishedListener {
                                            override fun onPixelCopyFinished(copyResult: Int) {
                                                if (copyResult == PixelCopy.SUCCESS) {
                                                    blurredBitmap = Toolkit.blur(bitmap, radius = 20)
                                                }
                                                latch.countDown()
                                            }
                                        },
                                        Handler(handlerThread.looper)
                                    )
                                    latch.await()
                                }
                                blurredBitmap?.let {
                                    drawImage(it.asImageBitmap())
                                }
                                bitmap.recycle()
                                drawContent()
                            }
                    ) {
                        Text(text = "hello", color = Color.Red)
                    }

                }
            }, params)
        },
        LibFunc("Toast 测试") {
            Toast.makeText(ctx, "-----", LENGTH_SHORT).show()
        },
    )
}


@Composable
private fun Content() {
    val ctx = LocalContext.current
    val width = 260.dp
    val view = LocalView.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
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
            LibsColumn(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(width),
                title = "renderscript Lib",
                list = initRenderscriptFuncList(ctx, composeView = view),
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