package com.benoitthore.enamel.geometry.layout.dsl

import com.benoitthore.enamel.core.withAlpha
import com.benoitthore.enamel.geometry.layout.ELayout
import com.benoitthore.enamel.geometry.layout.ELayoutLeaf
import com.benoitthore.enamel.geometry.layout.ETrackingLayout
import com.benoitthore.enamel.geometry.layout.refs.ELayoutTag

inline val String.layoutTag get() = ELayoutLeaf(0x00_00_00_00, ELayoutTag(this))
inline fun String.layoutTag(color: Int) = ELayoutLeaf(color, ELayoutTag(this))
inline val List<String>.layoutTag get() = map { it.layoutTag }


fun ELayout.tracked(dst: ELayout) = ETrackingLayout(src = this, dst = dst)

fun ELayout.leaf(color: Int) = ELayoutLeaf(color = color, child = this)
fun ELayout.leafTransparent(color: Int) = ELayoutLeaf(color = color.withAlpha(0.5), child = this)