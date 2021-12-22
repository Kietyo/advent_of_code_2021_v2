import kotlin.math.max

data class Grid(
    var defaultGridValue: Boolean,
    val algo: BooleanArray,
    // Only contains "active" points
    val activePoints: MutableSet<Pair<Long, Long>> = mutableSetOf(),

    ) {
    init {
        require(algo.size == 512)
    }

    val minAffectedX: Long
        get() = activePoints.minOf { it.first }

    val maxAffectedX: Long
        get() = activePoints.maxOf { it.first }

    val minAffectedY: Long
        get() = activePoints.minOf { it.second }

    val maxAffectedY: Long
        get() = activePoints.maxOf { it.second }

    val isTransitioningGrid = algo[0] && !algo[511]

    fun set(x: Long, y: Long, b: Boolean) {
        val newPoint = Pair(x, y)
        if (defaultGridValue == b) {
            activePoints.remove(newPoint)
        } else {
            activePoints.add(newPoint)
        }
    }

    fun get(x: Long, y: Long): Boolean {
        val pair = Pair(x, y)
        if (activePoints.contains(pair)) {
            return !defaultGridValue
        }
        return defaultGridValue
    }

    private fun copy(): Grid {
        return Grid(
            defaultGridValue,
            algo,
            activePoints.toMutableSet(),
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
        if (isTransitioningGrid) {
            defaultGridValue = !defaultGridValue
        }
        activePoints.clear()
        for (affectedPoint in originalCopy.activePoints) {
            for (yOffset in -1..1) {
                for (xOffset in -1..1) {
                    val currX = affectedPoint.first + xOffset
                    val currY = affectedPoint.second + yOffset
                    //                    println("\t($currX, $currY)")
                    val updateValue = originalCopy.getUpdateValue(currX, currY)
                    set(currX, currY, updateValue)
                }
            }
        }
    }

    val numLit: Int
        get() = run {
            if (isTransitioningGrid && defaultGridValue) {
                return Int.MAX_VALUE
            }
            activePoints.size
        }

    fun boardString(): String {
        val sb = StringBuilder()
        val yGridLength = max(minAffectedY.toString().length, maxAffectedY.toString().length)
        for (y in (minAffectedY - 5)..(maxAffectedY + 5)) {
            val gridNumberStr = y.toString().padStart(yGridLength)
            sb.append("$gridNumberStr: ")
            for (x in (minAffectedX - 5)..(maxAffectedX + 5)) {
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


        println(
            """
            minAffectedX: $minAffectedX, maxX: $maxAffectedX
            minAffectedY: $minAffectedY, maxY: $maxAffectedY
            isTransitioningGrid: $isTransitioningGrid
            defaultGridValue: $defaultGridValue
            numLit: $numLit
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

        val grid = Grid(false, algo)

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

        repeat(50) {
            grid.update()
            grid.print()
        }

        //        File("day20_out.txt").printWriter().use {
        //            it.println(boardString)
        //        }
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day20_test")
    val mainInput = readInput("day20")

    part1(testInput)
    //        part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}