package com.thorebenoit.enamel.kotlin.geometry.layout.transition

import com.thorebenoit.enamel.kotlin.core.print
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectType
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayout
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.ELayoutRef
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.getAllChildren
import com.thorebenoit.enamel.kotlin.geometry.layout.refs.getRefs
import com.thorebenoit.enamel.kotlin.threading.coroutine
import kotlinx.coroutines.CoroutineScope
import java.lang.Exception

/**TODO
 *  - getEnterAnimation and getExitAnimation should take old and new bounds
 *  - Create playground version for transition
 *  - Allow change of transition state in the middle of a transition
 *  - Improve performance
 */
class ETransition<V : Any>(
    val executeOnUiThread: (suspend CoroutineScope.() -> Unit) -> Unit,
    val doAnimation: suspend (Long, (Float) -> Unit) -> Unit,
    val getEnterAnimation: (ELayoutRef<V>) -> SingleElementAnimator<V>,
    val getExitAnimation: (ELayoutRef<V>) -> SingleElementAnimator<V>,
    val getUpdateAnimation: UpdateAnimator.Builder<V>,
    var bounds: ERectType? = null
) {

    private var layout: ELayout? = null

    private var isInTransition = false

    fun to(newLayout: ELayout, bounds: ERectType? = null) {
        if (isInTransition) {
            "Already in transition".print
            return
        }

        isInTransition = true
        val bounds = bounds ?: this.bounds ?: throw Exception("No bound provided, transition cannot proceed")
        val newRefs = newLayout.getRefs<V>()


        // If we don't have a layout yet, just lay views in without animation
        val oldLayout = this.layout ?: run {

            newLayout.arrange(bounds)
            newRefs.forEach {
                getEnterAnimation(it).animateTo(1f)
            }
            this.layout = newLayout
            isInTransition = false
            return
        }

        this.layout = newLayout

        ///////////////////////////////
        //  Getting Transition data  //
        ///////////////////////////////
        val updatingRefs = mutableMapOf<ELayoutRef<V>, ELayoutRef<V>>() // new -> old
        val goingOutRefs = mutableListOf<ELayoutRef<V>>()
        val goingInRefs = mutableListOf<ELayoutRef<V>>()

        val oldRefs = oldLayout.getRefs<V>()


        var found: Boolean
        // TODO Optimize double `for` loop
        oldRefs.forEach { old ->
            found = false
            newRefs.forEach { new ->
                // Check view equality
                if (new.ref.isSameView(old.ref.viewRef)) {
                    // Add mapping between new and old view
                    updatingRefs[new] = old
                    found = true
                }
            }

            // If you not found in new layout, view going out
            if (!found) {
                goingOutRefs += old
            }
        }

        // Going through refs in new layout
        newRefs.forEach { new ->
            // If the new ref isn't part of the ones being updated, it has to go in
            if (!updatingRefs.containsKey(new)) {
                goingInRefs += new
            }
        }
        /////////////////////////////////
        //  /Getting Transition data   //
        /////////////////////////////////


        // TODO Optimize getAllChildrenCall
        newLayout.getAllChildren().forEach {
            if (it as? ELayoutRef<V> != null) {
                it.isInMeasureMode = true
            }
        }

        // Measure layout without applying changes
        newLayout.arrange(bounds)

        ////////////////////////////
        //  Setting Up Animators  //
        ////////////////////////////


        val outAnimations = goingOutRefs.map(getExitAnimation)
        val inAnimations = goingInRefs.map(getEnterAnimation)
        val updateAnimations = mutableListOf<UpdateAnimator<V>>()

        // TODO use newRefs instead
        newLayout.getAllChildren().forEach {
            (it as? ELayoutRef<V>)?.let { new ->

                updatingRefs[new]?.let { old ->
                    updateAnimations += getUpdateAnimation.build(from = old, to = new)
                }
                new.isInMeasureMode = false
            }
        }

        ////////////////////////////
        //  /Setting Up Animators //
        ////////////////////////////


        // TODO Extract to separate function to increase readability ?
        /// Do animation
        executeOnUiThread {

            "Start: View OUT".print
            // OUT
            doAnimation(333L) { progress ->
                outAnimations.forEach { animator -> animator.animateTo(progress) }
            }
            oldRefs.forEach {
                it.ref.removeFromParent()
            }

            "Start: View UPDATE".print
            // UPDATE
            doAnimation(333L) { progress ->
                updateAnimations.forEach { animator -> animator.animateTo(progress) }
            }

            "Start: View IN".print
            // IN
            newRefs.forEach {
                it.ref.addToParent()
            }

            inAnimations.forEach { animator -> animator.animateTo(0f) }
            newLayout.arrange(bounds)
            doAnimation(333L) { progress ->
                inAnimations.forEach { animator -> animator.animateTo(progress) }
            }
            "Animation done".print

            isInTransition = false
        }


    }


}