package com.benoitthore.enamel.geometry.layout.dsl

import com.benoitthore.enamel.geometry.alignement.EAlignment
import com.benoitthore.enamel.geometry.alignement.ERectEdge
import com.benoitthore.enamel.geometry.layout.EBarLayout
import com.benoitthore.enamel.geometry.layout.EBoxLayout
import com.benoitthore.enamel.geometry.layout.EDivideLayout
import com.benoitthore.enamel.geometry.layout.ELayout


fun ELayout.aligned(side: ERectEdge) = EBarLayout(this, side)


fun distance(n: Number) = EDivideLayout.Division.Distance(n)
fun fraction(n: Number) = EDivideLayout.Division.Fraction(n)
inline fun slice() = EDivideLayout.Division.Slice

fun ELayout.aligned(
    side: ERectEdge,
    of: ELayout,
    sizedBy: EDivideLayout.Division = EDivideLayout.Division.Slice,
    spacing: Number = 0,
    snugged: Boolean = true
) = EDivideLayout(
    slice = this,
    edge = side,
    remainder = of,
    by = sizedBy,
    spacing = spacing,
    snugged = snugged
)


fun ELayout.arranged(alignement: EAlignment, snugged: Boolean = true) = EBoxLayout(this, alignement, snugged)

fun ELayout.spreadBetween(start: ELayout, end: ELayout, axis: ERectEdge = ERectEdge.right) =
    end.aligned(axis, start.aligned(axis.opposite, this))