package com.benoitthore.enamel.geometry.figures

import com.benoitthore.enamel.core.math.*
import com.benoitthore.enamel.geometry.Allocates
import com.benoitthore.enamel.geometry.Resetable
import com.benoitthore.enamel.geometry.allocateDebugMessage
import com.benoitthore.enamel.geometry.primitives.*


/*
TODO Make mutable/immutable version
TODO Make allocation free
 */
open class ELine(open val start: EPoint = EPoint.zero, open val end: EPoint = EPoint.zero) {

    private fun Float.opposite() = 1f - this

    init {
        allocateDebugMessage()
    }

    companion object {
        val unit = ELine(start = EPointMutable.zero, end = EPointMutable.unit)
        val zero = ELine(start = EPointMutable.zero, end = EPointMutable.zero)
    }

    val length
        get() = start.distanceTo(end).f

    fun angle(target: EAngleMutable) = angleRadians.radians(target = target)
    val x1
        get() = start.x
    val x2
        get() = end.x
    val y1
        get() = start.y
    val y2
        get() = end.y

    private val angleRadians get() = start._angleTo(end.x, end.y)

    @Allocates
    val linearFunction: ELinearFunction
        get() = run {
            val a = (end.y - start.y) / (end.x - start.x)
            // y = ax + b
            // So -> b  = y - ax
            val b = start.y - a * start.x
            ELinearFunction(a, b)
        }

    fun pointAt(at: Float, target: EPointMutable): EPointMutable =
        start.offsetTowards(end, length * at, target = target)

    @Allocates
    fun pointFrom(distance: Number, from: Float, target: EPointMutable): EPoint {
        val opposite = pointAt(from.opposite(), target = EPointMutable())
        return target.set(opposite.offsetFrom(pointAt(from, target = EPointMutable()), distance))
    }

    @Allocates
    fun pointTowards(distance: Number, towards: Float, target: EPointMutable) =
        target.set(
            pointAt(towards.opposite(), target = EPointMutable()).offsetTowards(
                pointAt(
                    towards,
                    target = EPointMutable()
                ), distance
            )
        )

    fun extrapolateFrom(distance: Number, from: Number, target: EPointMutable): EPoint {
        val from = from.toFloat()

        val fromPoint = pointAt(from.opposite(), target = target)

        val totalDistance = length + distance.f

        return fromPoint._offsetAngle(angleRadians, totalDistance, target = fromPoint)
    }

    fun isParallel(other: ELineMutable) = angleRadians == angleRadians

    fun center(target: EPointMutable) = pointAt(0.5f, target = target)

    fun rotate(
        offsetAngle: EAngleMutable,
        around: EPoint = center(EPointMutable()),
        target: ELineMutable
    ): ELineMutable {
        start.rotateAround(offsetAngle, around, target = target.start)
        end.rotateAround(offsetAngle, around, target = target.end)
        return target
    }

    fun expanded(distance: Number, from: Number = 0f, target: ELineMutable): ELineMutable {
        val from = from.toFloat()
        pointAt(from, target = target.start)
        extrapolateFrom(distance, from.opposite(), target = target.end)
        return target
    }

    fun toListOfPoints(number: Int): List<EPoint> {
        return when (number) {
            0 -> emptyList()
            1 -> listOf(start)
            2 -> listOf(start, end)
            else -> {
                val distance = length.i
                val step = (distance / (number - 1)).i
                (0..distance step step).map { currentDistance ->
                    start.offsetTowards(end, currentDistance)
                }
            }
        }
    }

    @Allocates
    fun perpendicularPointLeft(
        distanceFromLine: Number,
        distanceTowardsEndPoint: Number,
        towards: Float,
        target: EPointMutable
    ): EPoint {
        val x = pointTowards(distanceTowardsEndPoint, towards, target = EPointMutable())
        return target.set(x.offsetAngle(angle = angle(EAngleMutable()) - 90.degrees(), distance = distanceFromLine))
    }

    fun perpendicularPointRight(
        distanceFromLine: Number,
        distanceTowardsEndPoint: Number,
        towards: Float,
        target: EPointMutable
    ) =
        perpendicularPointLeft(
            -distanceFromLine.f,
            distanceTowardsEndPoint,
            towards,
            target = target
        )

    fun perpendicular(
        distance: Number,
        towards: Float,
        leftLength: Number,
        rightLength: Number,
        target: ELineMutable
    ): ELine {
        perpendicularPointLeft(
            distanceFromLine = leftLength,
            distanceTowardsEndPoint = distance,
            towards = towards,
            target = target.start
        )
        perpendicularPointRight(
            distanceFromLine = rightLength,
            distanceTowardsEndPoint = distance,
            towards = towards,
            target = target.end
        )

        return target
    }

    fun perpendicular(
        distance: Number,
        towards: Float,
        length: Number,
        target: ELineMutable
    ) =
        perpendicular(
            distance = distance,
            towards = towards,
            leftLength = length.f / 2f,
            rightLength = length.f / 2f,
            target = target
        )

    fun perpendicular(
        at: Number, leftLength: Number, rightLength: Number,
        target: ELineMutable
    ): ELine {
        val offset = length * at.toFloat()
        return perpendicular(
            distance = offset,
            towards = 1f,
            leftLength = leftLength,
            rightLength = rightLength,
            target = target
        )
    }

    fun perpendicular(at: Number, length: Number, target: ELineMutable) = perpendicular(
        at = at,
        leftLength = length.f / 2f,
        rightLength = length.f / 2f,
        target = target
    )

    fun parallel(distance: Number, target: ELineMutable): ELine {

        TODO()

        return target
    }

    override fun toString(): String {
        return "($start, $end)"
    }


}

class ELineMutable(
    override val start: EPointMutable = EPointMutable.zero,
    override val end: EPointMutable = EPointMutable.zero
) : ELine(start, end), Resetable {

    fun set(start: EPoint = this.start, end: EPoint = this.end) =
        set(start.x, start.y, end.x, end.y)

    fun set(
        x1: Number = start.x,
        y1: Number = start.y,
        x2: Number = end.x,
        y2: Number = end.y
    ) = apply {
        start.x = x1.f
        start.y = y1.f

        end.x = x2.f
        end.y = y2.f
    }

    fun selfOffset(xOff: Number, yOff: Number) = apply {
        start.selfOffset(xOff, yOff)
        end.selfOffset(xOff, yOff)
    }

    fun selfOffset(p: EPoint) = selfOffset(p.x, p.y)
    fun selfScale(width: Number, height: Number): ELineMutable {
        start.x *= width.f
        end.x *= width.f

        start.y *= height.f
        end.y *= height.f
        return this
    }

    override fun reset() {
        start.reset()
        end.reset()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ELineMutable) return false


        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }


}

infix fun EPoint.line(end: EPoint) = ELine(start = this, end = end)
infix fun EPointMutable.line(end: EPointMutable) = ELineMutable(start = this, end = end)

/// CONVERT
fun List<EPoint>.toListOfLines(): List<ELine> {
    val ret = mutableListOf<ELine>()
    forEachIndexed { i, curr ->
        if (i > 1) {
            val prev = get(i - 1)
            ret.add(prev line curr)
        }
    }
    return ret
}

//
// TODO

fun EPoint.closetPointOnSegment(line: ELine) = line.closetPointOnSegment(this)
fun ELine.closetPointOnSegment(point: EPoint) = getClosestPointOnSegment(
    start.x, start.y,
    end.x, end.y,
    point.x, point.y
)

/**
 * Returns closest point on segment to point
 * @param sx1 - segment x coord 1
 * @param sy1 - segment y coord 1
 * @param sx2 - segment x coord 2
 * @param sy2 - segment y coord 2
 * @param px - point x coord
 * @param py - point y coord
 * @return closets point on segment to point
 */
private fun getClosestPointOnSegment(
    sx1: Float,
    sy1: Float,
    sx2: Float,
    sy2: Float,
    px: Float,
    py: Float
): EPoint {
    val xDelta = sx2 - sx1
    val yDelta = sy2 - sy1

    if (xDelta == 0.0f && yDelta == 0.0f) {
        throw IllegalArgumentException("Segment start equals segment end")
    }

    val u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta)

    return when {
        u < 0 -> EPoint(sx1, sy1)
        u > 1 -> EPoint(sx2, sy2)
        else -> EPoint(Math.round(sx1 + u * xDelta), Math.round(sy1 + u * yDelta))
    }
}

//
fun EPoint.distanceTo(line: ELine) = line.distanceTo(this)

fun ELine.distanceTo(point: EPoint): Float {
    // A - the standalone point (x, y)
    // B - start point of the line segment (x1, y1)
    // C - end point of the line segment (x2, y2)
    // D - the crossing point between line from A to BC
    val A = point
    val B = start
    val C = end


    val AB = A.distanceTo(B)
    val BC = B.distanceTo(C)
    val AC = A.distanceTo(C)

    // Heron's formula
    val s = (AB + BC + AC) / 2
    val area = Math.sqrt((s * (s - AB) * (s - BC) * (s - AC)).toDouble()).toFloat()

    // but also area == (BC * AD) / 2
    // BC * AD == 2 * area
    // AD == (2 * area) / BC
    // TODO: check if BC == 0
    return 2 * area / BC
}


val List<ELine>.length: Float get() = sumByDouble { it.length.toDouble() }.toFloat()


operator fun ELine.component1() = start
operator fun ELine.component2() = end

fun List<EPoint>.pointAtFraction(fraction: Number, target: EPointMutable = EPointMutable()) =
    pointAtDistance(fraction.toFloat() * length, target)

fun List<EPoint>.pointAtDistance(
    distance: Number,
    target: EPointMutable = EPointMutable()
): EPointMutable {
    var last: EPoint? = null
    val distance = distance.toFloat()
    var remainingDistance = distance

    forEach { p ->
        last?.let { last ->
            val distanceToNext = last.distanceTo(p)
            if (remainingDistance - distanceToNext < 0) {
                return last.offsetTowards(p, remainingDistance, target)
            }
            remainingDistance -= distanceToNext
        }
        last = p
    }

    return last().toMutable(target)
}



