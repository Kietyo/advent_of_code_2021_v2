import kotlin.math.absoluteValue
import kotlin.math.sign

data class Point(val x: Int, val y: Int)

data class LineSegment(val p1: Point, val p2: Point) {
    fun slope(): Int {
        return (p2.y - p1.y) / (p2.x - p1.x)
    }

    fun isVerticalLine(): Boolean {
        return p1.y == p2.y
    }

    fun isHorizontalLine(): Boolean {
        return p1.x == p2.x
    }

    fun is45DegreeDiagonalLine(): Boolean {
        return slope().absoluteValue == 1
    }

    fun getPoints(): List<Point> {
        if (isVerticalLine()) {
            val xDiff = p2.x - p1.x
            val xDelta = xDiff / xDiff.absoluteValue
            return (0..xDiff.absoluteValue).map {
                Point(p1.x + it * xDelta, p1.y)
            }
        } else if (isHorizontalLine()) {
            val yDiff = p2.y - p1.y
            val yDelta = yDiff / yDiff.absoluteValue
            return (0..yDiff.absoluteValue).map {
                Point(p1.x, p1.y + it * yDelta)
            }
        } else if (is45DegreeDiagonalLine()) {
            val slope = slope()
            val diff = p2.x - p1.x
            val sign = diff.sign
            return (0..diff.absoluteValue).map {
                Point(p1.x + it * sign, p1.y + it * slope * sign)
            }
        }
        TODO()
    }
}

fun main() {

    fun part1(inputs: List<String>) {
        val lineSegments = inputs.map {
            println(it)

            val split = it.split(" -> ")
            val p1 = split.first().toPoint()
            val p2 = split.last().toPoint()
            println("p1: $p1, p2: $p2")
            LineSegment(p1, p2)
        }

        val onlyVerticalAndHorizontalLines = lineSegments.filter {
            it.isHorizontalLine() || it.isVerticalLine()
        }

        val points = onlyVerticalAndHorizontalLines.flatMap {
            it.getPoints()
        }

        val maxX = points.maxOf {
            it.x
        } + 1
        val maxY = points.maxOf {
            it.y
        } + 1

        println("maxX: $maxX, maxY: $maxY")

        val counter = Array<IntArray>(maxY) { IntArray(maxX) }
        points.forEach {
            counter[it.y][it.x]++
        }

        for (y in 0 until maxY) {
            for (x in 0 until maxX) {
                val num = counter[y][x]
                print(
                    if (num == 0) '.' else num
                )
            }
            println()
        }

        var numOverlapping = 0
        for (y in 0 until maxY) {
            for (x in 0 until maxX) {
                val num = counter[y][x]
                if (num > 1) {
                    numOverlapping++
                }
            }
        }

        println("numOverlapping: $numOverlapping")
    }

    fun part2(inputs: List<String>) {
        val lineSegments = inputs.map {
            println(it)

            val split = it.split(" -> ")
            val p1 = split.first().toPoint()
            val p2 = split.last().toPoint()
            println("p1: $p1, p2: $p2")
            LineSegment(p1, p2)
        }

        val candidateLines = lineSegments.filter {
            it.isHorizontalLine() || it.isVerticalLine() || it.is45DegreeDiagonalLine()
        }

        println("candidate lines:")
        candidateLines.forEach {
            println(it)
            println(it.getPoints())
        }

        val points = candidateLines.flatMap {
            it.getPoints()
        }

        val maxX = points.maxOf {
            it.x
        } + 1
        val maxY = points.maxOf {
            it.y
        } + 1

        println("maxX: $maxX, maxY: $maxY")

        val counter = Array<IntArray>(maxY) { IntArray(maxX) }
        points.forEach {
            counter[it.y][it.x]++
        }

        for (y in 0 until maxY) {
            for (x in 0 until maxX) {
                val num = counter[y][x]
                print(
                    if (num == 0) '.' else num
                )
            }
            println()
        }

        var numOverlapping = 0
        for (y in 0 until maxY) {
            for (x in 0 until maxX) {
                val num = counter[y][x]
                if (num > 1) {
                    numOverlapping++
                }
            }
        }

        println("numOverlapping: $numOverlapping")
    }

    val mainInput = readInput("day5")
    val testInput = readInput("day5_test")

    //    part1(testInput)
    //    part1(mainInput)

    //        part2(testInput)
    part2(mainInput)
}

fun String.toPoint(): Point {
    val split = this.split(",")
    val x = split.first().toInt()
    val y = split.last().toInt()
    return Point(x, y)
}