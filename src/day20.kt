import java.io.File
import kotlin.math.max
import kotlin.math.min

data class Grid(
    val algo: BooleanArray,
    val data: MutableMap<Long, MutableMap<Long, Boolean>> = mutableMapOf(),
    var minAffectedX: Long = Long.MAX_VALUE,
    var maxAffectedX: Long = Long.MIN_VALUE,
    var minAffectedY: Long = Long.MAX_VALUE,
    var maxAffectedY: Long = Long.MIN_VALUE,
) {
    init {
        require(algo.size == 512)
    }

    val isTransitioningGrid = algo[0] && !algo[511]
    var defaultGridValue = false

    fun set(x: Long, y: Long, b: Boolean) {
        minAffectedX = min(minAffectedX, x)
        maxAffectedX = max(maxAffectedX, x)
        minAffectedY = min(minAffectedY, y)
        maxAffectedY = max(maxAffectedY, y)
        data.getOrPut(y) { mutableMapOf() }[x] = b
    }

    fun get(x: Long, y: Long): Boolean {
        if (x !in minAffectedX..maxAffectedX) {
            return defaultGridValue
        }
        if (y !in minAffectedY..maxAffectedY) {
            return defaultGridValue
        }
        return data[y]?.get(x) ?: defaultGridValue
    }

    private fun copy(): Grid {
        val copiedData = mutableMapOf<Long, MutableMap<Long, Boolean>>()
        for (e1 in data.entries) {
            val xMap = copiedData.getOrPut(e1.key) { mutableMapOf() }
            xMap.putAll(e1.value)
        }
        return Grid(
            algo,
            copiedData,
            minAffectedX,
            maxAffectedX,
            minAffectedY,
            maxAffectedY
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

    private fun getWindowDecimal(x: Long, y: Long): Int {
        return getWindowString(x, y).toInt(2)
    }

    fun getUpdateValue(x: Long, y: Long): Boolean {
        val windowInt = getWindowDecimal(x, y)
        return algo[windowInt]
    }

    fun update() {
        val originalCopy = this.copy()
        for (y in (minAffectedY - 1)..(maxAffectedY + 1)) {
            for (x in (minAffectedX - 1)..(maxAffectedX + 1)) {
                val updatedValue = originalCopy.getUpdateValue(x, y)
                set(x, y, updatedValue)
            }
        }

        defaultGridValue = !defaultGridValue
    }

    val numLit: Int
        get() = run {
            if (isTransitioningGrid && defaultGridValue) {
                return Int.MAX_VALUE
            }
            var sum = 0
            for (y in minAffectedY..maxAffectedY) {
                for (x in minAffectedX..maxAffectedX) {
                    if (get(x, y)) {
                        sum++
                    }
                }
            }
            return sum
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
        val yGridLength = max(minAffectedY.toString().length, maxAffectedY.toString().length)
        for (y in minAffectedY..maxAffectedY) {
            val gridNumberStr = y.toString().padStart(yGridLength)
            sb.append("$gridNumberStr: ")
            for (x in minAffectedX..maxAffectedX) {
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
            minAffectedX: $minAffectedX, maxX: $maxAffectedX
            minAffectedY: $minAffectedY, maxY: $maxAffectedY
            isTransitioningGrid: $isTransitioningGrid
            defaultGridValue: $defaultGridValue
            numLit: $numLit
            range: $range
            range1: $range1
            range2: $range2
        """.trimIndent()
        )
        println(boardString())
    }
}

fun main() {
    fun part1(inputs: List<String>) {
        val algo = inputs.first().map { it == '#' }.toBooleanArray()

        val itr = inputs.iterator()
        itr.next()
        itr.next()

        val grid = Grid(algo)

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

        grid.update()
        grid.print()


        grid.update()
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

    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}