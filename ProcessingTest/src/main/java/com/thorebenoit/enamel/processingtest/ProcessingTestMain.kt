package com.thorebenoit.enamel.processingtest

import com.thorebenoit.enamel.core.threading.coroutine
import com.thorebenoit.enamel.geometry.alignement.EAlignment
import com.thorebenoit.enamel.geometry.layout.dsl.arranged
import com.thorebenoit.enamel.geometry.layout.dsl.layoutTag
import com.thorebenoit.enamel.geometry.layout.playground.sendToPlayground
import com.thorebenoit.enamel.processingtest.kotlinapplet.applet.PlaygroundApplet

////
import com.thorebenoit.enamel.geometry.layout.dsl.*
import com.thorebenoit.enamel.geometry.alignement.*
import com.thorebenoit.enamel.geometry.alignement.EAlignment.*
import com.thorebenoit.enamel.geometry.alignement.ERectEdge.*
import com.thorebenoit.enamel.geometry.layout.EEmptyLayout
import com.thorebenoit.enamel.geometry.layout.ELayout
import com.thorebenoit.enamel.geometry.layout.ELayoutLeaf
import com.thorebenoit.enamel.geometry.layout.playground.PlaygroundServer
import com.thorebenoit.enamel.geometry.layout.playground.sendToPlayground
import com.thorebenoit.enamel.processingtest.kotlinapplet.colorHSL
import com.thorebenoit.enamel.processingtest.kotlinapplet.randomColor

//
object ProcessingTestMain {


// NOT WORKING LIKE IN EXAMPLE
//    val big = ELayoutLeaf(red)
//    val small = ELayoutLeaf(blue)
//
//    val smallLayout = small
//        .sizedSquare(200)
//        .padded(50)
//        .arranged(center, snugged = true)
//        .scaled(y = 2)
//        .arranged(topRight)
//
//    val layout = big
//        .tracked(smallLayout)
//        .height(100)
//        .padded(50)
//        .aligned(top)


    fun <T> T.asList(): List<T> = listOf(this)
    @JvmStatic
    fun main(args: Array<String>) {


        PlaygroundApplet.start(800, 800) {


            val big = ELayoutLeaf(colorHSL(.25))
//            val small = ELayoutLeaf(blue)
//
//            val smallLayout = small
//                .sizedSquare(200)
//                .padded(50)
//                .arranged(center, snugged = true)
//                .scaled(y = 2)
//                .arranged(topRight)
//
//            val layout = big
//                .tracked(smallLayout)
//                .height(100)
//                .padded(50)
//                .aligned(top)

//            var layout : ELayout = EEmptyLayout
            var layout: ELayout = ELayoutLeaf(colorHSL(.25))
                .sizedSquare(100)
                .scaled(0.5, 1)
                .padded(50)
                .arranged(topLeft)

            coroutine {
                PlaygroundServer.start {
                    layout = it
                }
            }

            onDraw {

                background(0)
                fill(255f, 0f, 0f)



                layout.arrange(eframe)
                layout.draw()

            }
        }

    }

}

