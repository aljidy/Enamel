package com.thorebenoit.enamel.kotlin.geometry.layout.refs

import com.thorebenoit.enamel.kotlin.geometry.figures.ERect
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectType
import com.thorebenoit.enamel.kotlin.geometry.figures.ESizeType
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayout
import com.thorebenoit.enamel.kotlin.geometry.layout.ELayoutLeaf
import com.thorebenoit.enamel.kotlin.geometry.layout.serializer.ELayoutDeserializer
import com.thorebenoit.enamel.kotlin.geometry.layout.serializer.ELayoutSerializer

import java.util.*


class ELayoutRef<V : Any>(
    var ref: ELayoutRefObject<V>,
    private val sizeToFit: ELayoutRef<V>.(ESizeType) -> ESizeType,
    private val arrangeIn: ELayoutRef<V>.(ERectType) -> Unit
) : ELayout {

    override val childLayouts: List<ELayout> = listOf()

    var isInMeasureMode = false

    private var _frame: ERect = ERect()
    val frame: ERectType get() = _frame

    override fun size(toFit: ESizeType): ESizeType {
        return sizeToFit(toFit)
    }

    override fun arrange(frame: ERectType) {
        _frame.set(frame)
        if (!isInMeasureMode) {
            ref.addToParent()
            arrangeIn(frame)
        }
    }
}


//
// TODO Test
fun ELayout.getAllChildren(list: LinkedList<ELayout> = LinkedList()): List<ELayout> {
    list += this
    childLayouts.flatMap { it.getAllChildren(list) }
    return list
}

fun <T : Any> ELayout.getObjects(): List<T> = getRefs<T>().map { it.ref.viewRef }

fun ELayout.getLeafs(): List<ELayoutLeaf> {
    val list = mutableListOf<ELayoutLeaf>()

    if (this is ELayoutLeaf) {
        list.add(this)
    }
    list.addAll(childLayouts.flatMap { it.getLeafs() })

    return list
}

fun <T : Any> ELayout.getRefs(): List<ELayoutRef<T>> {
    val list = mutableListOf<ELayoutRef<T>>()

    (this as? ELayoutRef<T>)?.let {
        list.add(it)
    }
    list.addAll(childLayouts.flatMap { it.getRefs<T>() })

    return list
}
