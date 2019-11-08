package com.benoitthore.enamel.android

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextPaint
import androidx.appcompat.app.AppCompatActivity
import com.benoitthore.enamel.geometry.AllocationTracker
import com.benoitthore.enamel.layout.android.dp
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.benoitthore.enamel.R
import com.benoitthore.enamel.geometry.alignement.EAlignment.*
import com.benoitthore.enamel.geometry.alignement.EAlignment
import com.benoitthore.enamel.geometry.layout.ELayout
import com.benoitthore.enamel.geometry.layout.dsl.arranged
import com.benoitthore.enamel.geometry.layout.dsl.sizedSquare
import com.benoitthore.enamel.geometry.layout.dsl.stackedBottomCenter
import com.benoitthore.enamel.layout.android.extract.CanvasLayoutView
import com.benoitthore.enamel.layout.android.extract.GeometryPool
import com.benoitthore.enamel.layout.android.extract.layout.*
import com.benoitthore.enamel.layout.android.extract.randomColor


val Context.isLandscape get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val loremIpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"

fun Context.toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

fun generateText(numberOfWords: Int = 10): String = with(loremIpsum.split(" ").shuffled()) {
    (0 until numberOfWords).joinToString(separator = " ") { random() }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AllocationTracker.debugAllocations = false

        val textPaint = TextPaint().apply {
            textSize = 60f
            color = WHITE
        }

        val view = CanvasLayoutView(this)
        view.setBackgroundColor(DKGRAY)

        val image1 = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)!!
            .imageLayout()
            .changeTintOnClick(view)
            .sizedSquare(64.dp)

        val image2 = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)!!
            .imageLayout()
            .changeTintOnClick(view)
            .sizedSquare(128.dp)

        val textview = generateText(10).wordLayout(textPaint)

        view.layout = listOf(image1, textview, image2).stackedBottomCenter(16.dp).arranged(center)
        setContentView(view)
    }

}

private fun EImageLayout.changeTintOnClick(view: View): ELayout =
    let { layout ->
        layout.onClick {
            paint.colorFilter =
                PorterDuffColorFilter(randomColor(), PorterDuff.Mode.MULTIPLY)
            view.invalidate()
        }
    }

/////
/////
/////
/////
/////

// KEEP HERE - Picasso shouldn't be put into the geometry module
suspend inline fun String.imageLayout(
    paint: Paint = Paint(), crossinline block: RequestCreator.() -> Unit = {}
) = EImageLayout(downloadImage(block), paint)

suspend inline fun String.downloadImage(crossinline block: RequestCreator.() -> Unit = {}): Bitmap =
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Bitmap> { cont ->

            var done = false
            Picasso.get()
                .load(this@downloadImage)
                .apply(block)
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        done = true
                        cont.cancel(e)
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                        done = true
                        cont.resume(bitmap)
                    }
                })


            GlobalScope.launch {
                delay(5000L)
                if (!done) {
                    cont.cancel(Exception("TIMEOUT"))
                }
            }
        }
    }


