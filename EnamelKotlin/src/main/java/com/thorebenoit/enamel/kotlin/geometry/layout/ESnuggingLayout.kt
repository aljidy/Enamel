package com.thorebenoit.enamel.kotlin.geometry.layout

import com.fasterxml.jackson.annotation.JsonIgnore
import com.thorebenoit.enamel.kotlin.geometry.alignement.ELayoutAxis
import com.thorebenoit.enamel.kotlin.geometry.alignement.isVertical
import com.thorebenoit.enamel.kotlin.geometry.figures.ERectType
import com.thorebenoit.enamel.kotlin.geometry.figures.ESizeType

data class ESnuggingLayout(@get:JsonIgnore val child: ELayoutAlongAxis) : ELayout {
    override val childLayouts: List<ELayout> = listOf(child)

    override fun size(toFit: ESizeType): ESizeType {
        val sizes = child.childLayouts.map { it.size(toFit) }

        val maxChildExtent = sizes.map {
            if (child.layoutAxis.isVertical) {
                it.width
            } else {
                it.height
            }
        }.max() ?: 0f

        val unconstrainedSize = child.size(toFit)

        return if (child.layoutAxis.isVertical) {
            unconstrainedSize.copy(width = maxChildExtent)
        } else {
            unconstrainedSize.copy(height = maxChildExtent)
        }
    }

    override fun arrange(frame: ERectType) {
        child.arrange(frame)
    }
}