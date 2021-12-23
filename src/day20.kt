import kotlin.math.max
import kotlin.math.min

data class GridOld(
    val algo: BooleanArray,
    // Only contains "active" points.
    // Note that active != lit.
    // For example if the "inactive grid value" was true, then the "active" value would be false.
    val activePoints: MutableSet<Pair<Long, Long>> = mutableSetOf(),
) {
    init {
        require(algo.size == 512)
    }

    val minAffectedX: Long
        get() = activePoints.minOfOrNull { it.first } ?: 0

    val maxAffectedX: Long
        get() = activePoints.maxOfOrNull { it.first } ?: 0

    val minAffectedY: Long
        get() = activePoints.minOfOrNull { it.second } ?: 0

    val maxAffectedY: Long
        get() = activePoints.maxOfOrNull { it.second } ?: 0

    fun set(x: Long, y: Long, b: Boolean) {
        val newPoint = Pair(x, y)
        if (b) {
            activePoints.add(newPoint)
        } else {
            activePoints.remove(newPoint)
        }
    }

    fun get(x: Long, y: Long): Boolean {
        val pair = Pair(x, y)
        return activePoints.contains(pair)
    }

    private fun copy(): GridOld {
        return GridOld(
            algo,
            mutableSetOf<Pair<Long, Long>>().apply {
                addAll(activePoints)
            },
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
        val str = sb.toString()
        require(str.length == 9)
        return str
    }

    private fun getWindowInt(x: Long, y: Long): Int {
        return getWindowString(x, y).toInt(2)
    }

    fun getUpdateValue(x: Long, y: Long): Boolean {
        val windowInt = getWindowInt(x, y)
        return algo[windowInt]
    }

    fun update() {
        val originalCopy = this.copy()
        for (y in (minAffectedY - 1)..(maxAffectedY + 1)) {
            for (x in (minAffectedX - 1)..(maxAffectedX + 1)) {
                val updateValue = originalCopy.getUpdateValue(x, y)
                set(x, y, updateValue)
            }
        }
    }

    val numLit: Int
        get() = run {
            activePoints.size
        }

    fun boardString(): String {
        val sb = StringBuilder()
        val yGridLength = max(minAffectedY.toString().length, maxAffectedY.toString().length)
        for (y in (minAffectedY - 3)..(maxAffectedY + 3)) {
            val gridNumberStr = y.toString().padStart(yGridLength)
            sb.append("$gridNumberStr: ")
            for (x in (minAffectedX - 3)..(maxAffectedX + 3)) {
                sb.append(if (get(x, y)) '#' else '.')
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun print() {
        println(
            """
            minAffectedX: $minAffectedX, maxX: $maxAffectedX
            minAffectedY: $minAffectedY, maxY: $maxAffectedY
            numLit: $numLit
        """.trimIndent()
        )
        //        println(boardString())
    }
}

data class Grid(
    var inactiveGridValue: Boolean,
    val algo: BooleanArray,
    // Only contains "active" points.
    // Note that active != lit.
    // For example if the "inactive grid value" was true, then the "active" value would be false.
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
        if (inactiveGridValue == b) {
            require(!activePoints.contains(newPoint))
            return
        } else {
            activePoints.add(newPoint)
        }
    }

    fun get(x: Long, y: Long): Boolean {
        val pair = Pair(x, y)
        if (activePoints.contains(pair)) {
            return !inactiveGridValue
        }
        return inactiveGridValue
    }

    private fun copy(): Grid {
        return Grid(
            inactiveGridValue,
            algo,
            mutableSetOf<Pair<Long, Long>>().apply {
                addAll(activePoints)
            },
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
        val str = sb.toString()
        require(str.length == 9)
        return str
    }

    private fun getWindowInt(x: Long, y: Long): Int {
        return getWindowString(x, y).toInt(2)
    }

    fun getUpdateValue(x: Long, y: Long): Boolean {
        val windowInt = getWindowInt(x, y)
        return algo[windowInt]
    }

    fun update() {
        val originalCopy = this.copy()
        if (isTransitioningGrid) {
            inactiveGridValue = !inactiveGridValue
        }
        activePoints.clear()
        for (affectedPoint in originalCopy.activePoints) {
            for (yOffset in -2..2) {
                for (xOffset in -2..2) {
                    val currX = affectedPoint.first + xOffset
                    val currY = affectedPoint.second + yOffset
                    val updateValue = originalCopy.getUpdateValue(currX, currY)
                    set(currX, currY, updateValue)
                }
            }
        }
    }

    val numLit: Int
        get() = run {
            if (isTransitioningGrid && inactiveGridValue) {
                return Int.MAX_VALUE
            }
            activePoints.size
        }

    fun boardString(): String {
        val sb = StringBuilder()
        val yGridLength = max(minAffectedY.toString().length, maxAffectedY.toString().length)
        for (y in (minAffectedY - 3)..(maxAffectedY + 3)) {
            val gridNumberStr = y.toString().padStart(yGridLength)
            sb.append("$gridNumberStr: ")
            for (x in (minAffectedX - 3)..(maxAffectedX + 3)) {
                sb.append(if (get(x, y)) '#' else '.')
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun print() {
        println(
            """
            minAffectedX: $minAffectedX, maxX: $maxAffectedX
            minAffectedY: $minAffectedY, maxY: $maxAffectedY
            isTransitioningGrid: $isTransitioningGrid
            defaultGridValue: $inactiveGridValue
            numLit: $numLit
        """.trimIndent()
        )
        //        println(boardString())
    }
}

fun main() {
    fun part1(inputs: List<String>) {
        val algo = inputs.first().map { it == '#' }.toBooleanArray()

        val itr = inputs.iterator()
        itr.next()
        itr.next()

        val grid = Grid(false, algo)
        val oldGrid = GridOld(algo)

        var y = 0L
        while (itr.hasNext()) {
            val currLine = itr.next()
            currLine.forEachIndexed { index, c ->
                grid.set(index.toLong(), y, c == '#')
                oldGrid.set(index.toLong(), y, c == '#')
            }
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
            println("New Grid")
            grid.update()
            grid.print()

            println("Old grid")
            oldGrid.update()
            oldGrid.print()
        }

        //        File("day20_out.txt").printWriter().use {
        //            it.println(boardString)
        //        }
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day20_test")
    val mainInput = readInput("day20")

    //    println("000100010".toInt(2))

    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}