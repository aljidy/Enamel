package com.thorebenoit.enamel.android.elayout

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.core.animation.addListener
import com.thorebenoit.enamel.android.threading.mainThreadCoroutine
import com.thorebenoit.enamel.kotlin.animations.EasingInterpolators
import com.thorebenoit.enamel.kotlin.core.color.blue
import com.thorebenoit.enamel.kotlin.core.color.green
import com.thorebenoit.enamel.kotlin.core.color.withAlpha
import com.thorebenoit.enamel.kotlin.geometry.alignement.EAlignment
import com.thorebenoit.enamel.kotlin.geometry.figures.ESize
import com.thorebenoit.enamel.kotlin.geometry.layout.androidlike.dsl.stacked
import com.thorebenoit.enamel.kotlin.geometry.layout.dsl.arranged
import com.thorebenoit.enamel.kotlin.geometry.layout.dsl.layoutTag
import com.thorebenoit.enamel.kotlin.geometry.layout.dsl.leaf
import com.thorebenoit.enamel.kotlin.geometry.layout.dsl.padded
import com.thorebenoit.enamel.kotlin.geometry.layout.playground.PlaygroundServer
import com.thorebenoit.enamel.kotlin.geometry.layout.playground.sendToPlayground
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutRef
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutRefObject
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutTag
import com.thorebenoit.enamel.kotlin.geometry.layout.serializer.ELayoutDeserializer
import com.thorebenoit.enamel.kotlin.geometry.layout.transition.ETransition
import com.thorebenoit.enamel.kotlin.geometry.layout.transition.SingleElementAnimator
import com.thorebenoit.enamel.kotlin.threading.CoroutineLock


// Run in terminal: adb forward tcp:9327 tcp:9327
// Run this in a KTS file once app is running
private val Number.dp get() = this.toDouble() * 3.0

fun main() {


    // val Number.dp get() = this.toDouble() * 3.0
    val layouts = ('A'..'Z').map { it.toString().layoutTag }

    layouts
        .shuffled()
        .subList(0, 5)
//    .map { it.sized(100, 200) }
        .map { it.padded(16.dp).leaf() }
        .stacked(EAlignment.bottomCenter, spacing = 1.dp, gravity = EAlignment.topRight)
        .leaf(green.withAlpha(0.25))
        .padded(64.dp)
        .leaf(blue.withAlpha(0.25))
        .sendToPlayground()
}

private fun Throwable.log(tag: String = "ERROR") = Log.e(tag, message, this)

fun EDroidLayout.startServer() {
    val viewGroup = this

    val deserializer = ELayoutDeserializer()
    deserializer.addDeserializer(ELayoutTag::class.java) { jsonObject ->
        val tag = jsonObject.getString("tag")
        viewGroup.viewList.first { it.tag == tag }.laidIn(viewGroup)
    }
    PlaygroundServer().start(
        deserializer = deserializer,
        onError = { e -> e.log(); System.exit(0) }) { newLayout ->
        mainThreadCoroutine {
            goToLayout(newLayout)
        }
    }
}

private typealias Interpolator = (Float) -> Float

val Interpolator.android
    get() = object : android.view.animation.Interpolator {
        override fun getInterpolation(input: Float): Float = invoke(input)
    }

private suspend fun doAnimationPlug(duration: Long, block: (Float) -> Unit) {
    ValueAnimator.ofFloat(0f, 1f).apply {
        this.duration = duration
        val lock = CoroutineLock()

        interpolator = EasingInterpolators.cubicInOut.android

        addUpdateListener {
            block(it.animatedFraction)
        }

        addListener(onEnd = { lock.unlock() }, onCancel = { lock.unlock() })
        start()
        lock.wait()
    }
}


fun androidDefaultTransition() = ETransition<View>(
    executeOnUiThread = { mainThreadCoroutine(it) },
    doAnimation = { duration, animation -> doAnimationPlug(duration, animation) },
    getExitAnimation = { ref, rect ->
        object : SingleElementAnimator<View>(ref, rect) {
            override fun animateTo(progress: Float) {
                ref.view.alpha = 1f - progress
            }

        }
    },
    getEnterAnimation = { ref, rect ->
        object : SingleElementAnimator<View>(ref, rect) {
            override fun animateTo(progress: Float) {
                ref.view.alpha = progress
            }

        }
    }
)


private fun <T : View> T.createLayoutRef(viewGroup: EDroidLayout): ELayoutRefObject<T> {
    return ELayoutRefObject(
        viewRef = this,
        addToParent = {
            if (parent != viewGroup) {
                (parent as? ViewGroup)?.removeView(this)
            }
            if (parent == null) {
                viewGroup.addView(this)
            }
        },
        removeFromParent = {
            (parent as? ViewGroup)?.removeView(this)
        },
        isSameView = { this.tag == it.tag }
    )
}

fun <T : View> List<T>.laidIn(frame: EDroidLayout): List<ELayoutRef<T>> = map { it.laidIn(frame) }

private inline val <T : View> ELayoutRef<T>.view get() = ref.viewRef
fun <T : View> T.laidIn(viewGroup: EDroidLayout): ELayoutRef<T> {
    val sizeBuffer = ESize()

    return ELayoutRef(
        createLayoutRef(viewGroup),
        sizeToFit = { size ->
            view.measure(
                View.MeasureSpec.makeMeasureSpec(size.width.toInt(), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(size.height.toInt(), View.MeasureSpec.AT_MOST)
            )
            sizeBuffer.set(view.measuredWidth, view.measuredHeight)
        },
        arrangeIn = { frame ->
            view.layout(
                frame.left.toInt(),
                frame.top.toInt(),
                frame.right.toInt(),
                frame.bottom.toInt()
            )
        }
    )
}

