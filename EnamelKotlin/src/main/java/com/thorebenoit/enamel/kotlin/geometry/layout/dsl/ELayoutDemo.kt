package com.thorebenoit.enamel.kotlin.geometry.layout.dsl

import com.thorebenoit.enamel.kotlin.core.color.*
import com.thorebenoit.enamel.kotlin.core.of
import com.thorebenoit.enamel.kotlin.geometry.alignement.EAlignment
import com.thorebenoit.enamel.kotlin.geometry.alignement.ERectEdge
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectType
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayout
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayoutLeaf

typealias ELayoutExample = (ERectType) -> List<ELayout>

infix fun ELayoutExample.arrangedIn(frame: ERectType) = this(frame)

object ELayoutDemo {
    val _2: ELayoutExample = { frame ->

        3.of { ELayoutLeaf() }
            .mapIndexed { i, layout ->
                layout.sizedSquare((i + 1) * 100)
            }
            .stacked(EAlignment.rightTop, spacing = 10)
            .snugged()
            .arranged(EAlignment.topLeft)
            .padded(20)
            .apply {
                arrange(frame)
            }
            .asList()
    }
    val _1: ELayoutExample = { frame ->
        val redLayout = ELayoutLeaf(red)
        val blueLayout = ELayoutLeaf(blue)

        val sliceLayout = redLayout.sizedSquare(100)
            .aligned(ERectEdge.top)

        val combinedLayout = sliceLayout.aligned(ERectEdge.left, of = blueLayout, spacing = 5)

        combinedLayout
            .scaled(0.5, 0.25)
            .padded(10)
            .arranged(EAlignment.leftCenter)
            .arrange(frame)

        listOf(redLayout, blueLayout)
    }

    private fun <T : Any> T.asList(): List<T> = listOf(this)
}
/*


 */


/*

                    .arrangeAndDraw()


 */