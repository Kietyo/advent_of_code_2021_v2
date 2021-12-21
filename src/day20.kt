import java.io.File
import kotlin.math.max
import kotlin.math.min

data class Grid(
    val data: MutableMap<Long, MutableMap<Long, Boolean>> = mutableMapOf(),
    var cachedMinX: Long = Long.MAX_VALUE,
    var cachedMaxX: Long = Long.MIN_VALUE,
    var cachedMinY: Long = Long.MAX_VALUE,
    var cachedMaxY: Long = Long.MIN_VALUE

) {
    val minY: Long
        get() = cachedMinY - 10

    val maxY: Long
        get() = cachedMaxY + 10

    val minX: Long
        get() = cachedMinX - 10

    val maxX: Long
        get() = cachedMaxX + 10

    fun set(x: Long, y: Long, b: Boolean) {
        cachedMinY = min(cachedMinY, y)
        cachedMaxY = max(cachedMaxY, y)
        cachedMinX = min(cachedMinX, x)
        cachedMaxX = max(cachedMaxX, x)
        data.getOrPut(y) { mutableMapOf() }[x] = b
    }

    fun get(x: Long, y: Long): Boolean {
        return data[y]?.get(x) ?: false
    }

    private fun copy(): Grid {
        val copiedData = mutableMapOf<Long, MutableMap<Long, Boolean>>()
        for (e1 in data.entries) {
            val xMap = copiedData.getOrPut(e1.key) { mutableMapOf() }
            xMap.putAll(e1.value)
        }
        return Grid(
            copiedData,
            cachedMinX,
            cachedMaxX,
            cachedMinY,
            cachedMaxY
        )
    }

    fun getWindowString(x: Long, y: Long): String {
        val sb = StringBuilder()
        for (yOffset in -1..1) {
            for (xOffset in -1..1) {
                val b = get(x + xOffset, y + yOffset)
                sb.append(if (b) '1' else '0')
            }
        }
        return sb.toString()
    }

    fun getWindowDecimal(x: Long, y: Long): Int {
        return getWindowString(x, y).toInt(2)
    }

    fun getUpdateValue(algo: BooleanArray, x: Long, y: Long): Boolean {
        val windowInt = getWindowDecimal(x, y)
        return algo[windowInt]
    }

    fun getUpdateValue(algo: BooleanArray, x: Long, y: Long, nTimes: Int): Boolean {
        val gridCopy = this.copy()
        repeat(nTimes) {
            gridCopy.update(algo)
        }
        return gridCopy.get(x, y)
    }

    fun update(algo: BooleanArray) {
        val originalCopy = this.copy()
        for (y in (minY)..(maxY)) {
            for (x in (minX)..(maxX)) {
                val updatedValue = originalCopy.getUpdateValue(algo, x, y)
                set(x, y, updatedValue)
            }
        }
    }

    fun update(algo: BooleanArray, nTimes: Int) {
        val copy = this.copy()
        for (y in (minY)..(maxY)) {
            for (x in (minX)..(maxX)) {
                val updateValue = copy.getUpdateValue(algo, x, y, nTimes)
                set(x, y, updateValue)
            }
        }
    }

    val numLit: Int
        get() = run {
            var sum = 0
            for (y in minY..maxY) {
                for (x in minY..maxX) {
                    if (get(x, y)) {
                        sum++
                    }
                }
            }
            sum
        }

    fun getNumLitWithinRange(xRange: IntRange, yRange: IntRange): Int {
        var sum = 0
        for (y in yRange) {
            for (x in xRange) {
                if (get(x.toLong(), y.toLong())) {
                    sum++
                }
            }
        }
        return sum
    }

    fun boardString(): String {
        val sb = StringBuilder()
        for (y in minY..maxY) {
            for (x in minY..maxX) {
                sb.append(if (get(x, y)) '#' else '.')
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun print() {

        //        numLit: 19949
        //        range1: 6135
        //        range2: 7309

        //        val range1 = getNumLitWithinRange(-5..105, -5..105)
        //        val range2 = getNumLitWithinRange(-10..110, -10..110)

        val range = numLit
        val range1 = null
        val range2 = null

        println(
            """
            minX: $minX, maxX: $maxX
            minY: $minY, maxY: $maxY
            numLit: $numLit
            range: $range
            range1: $range1
            range2: $range2
        """.trimIndent()
        )
    }
}

fun main() {
    fun part1(inputs: List<String>) {
        val algo = inputs.first().map { it == '#' }.toBooleanArray()

        val itr = inputs.iterator()
        itr.next()
        itr.next()

        val grid = Grid()

        var y = 0L
        while (itr.hasNext()) {
            val currLine = itr.next()
            currLine.forEachIndexed { index, c -> grid.set(index.toLong(), y, c == '#') }
            println("$y: $currLine")
            y++
        }

        val algoString = algo.joinToString("") {
            if (it) "#" else "."
        }

        println(
            """
            algo: ${algoString}
            algo: ${algo.size}
        """.trimIndent()
        )

        println("original grid")
        grid.print()

        grid.update(algo, 2)

        grid.print()

        val boardString = grid.boardString()

        println(boardString)

        //        File("day20_out.txt").printWriter().use {
        //            it.println(boardString)
        //        }
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day20_test")
    val mainInput = readInput("day20")

    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}