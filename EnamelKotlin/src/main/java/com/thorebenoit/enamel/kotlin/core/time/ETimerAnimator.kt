package com.thorebenoit.enamel.kotlin.core.time

import com.thorebenoit.enamel.kotlin.core.math.Scale
import com.thorebenoit.enamel.kotlin.core.math.f


class ETimerAnimator : ETimer() {
    enum class RepeatMode {
        REVERSE, RESTART, NONE
    }


    var repeatMode: RepeatMode = RepeatMode.NONE

    var duration = 1_000L
        set(value) {
            field = value
            start()
        }


    var progress: Float
        get() {
            val actualRatio = elapsed.f / duration.f
            if (actualRatio < 1f) {
                return actualRatio
            }

            // What to do if we've reached the end of the animation
            return when (repeatMode) {
                RepeatMode.REVERSE -> {
                    if (actualRatio > 2f) {
                        start()
                        return 0f
                    }
                    return Scale.map(actualRatio, 1, 2, 1, 0)
                }
                RepeatMode.RESTART -> {
                    start()
                    return 0f
                }
                RepeatMode.NONE -> elapsed.f / duration.f
            }


        }
        set(value) {
            val targetElapsed = (duration * value).toLong()
            startAt = System.currentTimeMillis() - targetElapsed
        }
}
