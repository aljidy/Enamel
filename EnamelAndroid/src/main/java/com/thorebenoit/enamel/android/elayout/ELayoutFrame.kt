package com.thorebenoit.enamel.android.elayout

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thorebenoit.enamel.android.dsl.views.backgroundColor
import com.thorebenoit.enamel.android.dsl.views.textColor
import com.thorebenoit.enamel.android.dsl.views.textView
import com.thorebenoit.enamel.android.dsl.withID
import com.thorebenoit.enamel.kotlin.core.backingfield.ExtraValueHolder
import com.thorebenoit.enamel.kotlin.core.color.randomColor
import com.thorebenoit.enamel.kotlin.core.color.*
import com.thorebenoit.enamel.kotlin.core.of
import com.thorebenoit.enamel.kotlin.geometry.alignement.EAlignment
import com.thorebenoit.enamel.kotlin.geometry.figures.ERect
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectSides
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectType
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayout
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayoutAlongAxis
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayoutLeaf
import com.thorebenoit.enamel.kotlin.geometry.layout.dsl.*
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutRef
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutRefObject


class ELayoutFrame : ViewGroup {

    private fun <T : View> List<T>.toLayoutRef(): List<ELayoutRef<T>> = map { it.toLayoutRef() }

    private fun <T : View> T.toLayoutRef(): ELayoutRef<T> {
        val view = this

        return ELayoutRef(
            ELayoutRefObject(
                viewRef = view,
                addToParent = {
                    if (parent == null) {
                        addView(view)
                    }
                },
                removeFromParent = {
                    (parent as? ViewGroup)?.removeView(view)
                }
            ),
            sizeToFIt = { size ->
                size
            },
            arrangeIn = { frame ->

                view.layout(
                    frame.left.toInt(),
                    frame.top.toInt(),
                    frame.right.toInt(),
                    frame.bottom.toInt()
                )
            },
            childLayouts = emptyList()
        )
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        backgroundColor = black
        Color.LTGRAY
        setWillNotDraw(false)
    }

    val tv1 = textView("Text1") {
        backgroundColor = red
        textSize = 20f
        textColor = white
    }
    val tv2 = textView("Text2") {
        backgroundColor = green
        textSize = 20f
        textColor = black
    }
    val tv3 = textView("Text3") {
        backgroundColor = blue
        textSize = 20f
        textColor = white
    }

    val layout: ELayout =

        listOf(tv1, tv2, tv3)
            .toLayoutRef()
            .mapIndexed { i, layout ->
                layout.sizedSquare((i + 1) * 100)
            }
            .stacked(EAlignment.rightTop, spacing = 10)
            .snugged()
            .arranged(EAlignment.middle)
//        .padded(20)

    val eframe: ERectType get() = _eframe
    private var _eframe: ERect = ERect()

    val paddedFrame: ERectType get() = _paddedFrame
    private var _paddedFrame: ERect = ERect()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = 0f
        val top = 0f
        val right = r.toFloat() - l
        val bottom = b.toFloat() - t


        _eframe.setSides(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )

        _eframe.padding(
            left = paddingLeft,
            right = paddingRight,
            top = paddingTop,
            bottom = paddingBottom,
            buffer = _paddedFrame
        )

        layout.arrange(_eframe)
    }


}

