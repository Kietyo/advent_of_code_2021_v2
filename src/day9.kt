fun main() {
    data class Point(val x: Int, val y: Int, val num: Int)
    data class PuzzleInput(
        val data: List<List<Int>>
    ) {
        val height = data.size
        val width = data.first().size
        fun print2dArray() {
            println(data.joinToString("\n"))
        }

        fun getOrElseMax(x: Int, y: Int): Int {
            return data.getOrElse(y) { emptyList() }.getOrElse(x) { Int.MAX_VALUE }
        }

        fun getPointOrNull(x: Int, y: Int): Point? {
            if (x in 0 until width && y in 0 until height) {
                return Point(x, y, data[y][x])
            }
            return null
        }

        fun getLowPoints(): List<Point> {
            println(
                """
                height: $height,
                width: $width
            """.trimIndent()
            )


            val lowPoints = mutableListOf<Point>()

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val curr = data[y][x]
                    val top = getOrElseMax(x, y - 1)
                    val bot = getOrElseMax(x, y + 1)
                    val left = getOrElseMax(x - 1, y)
                    val right = getOrElseMax(x + 1, y)
                    println(
                        """
                        y: $y, x: $x, curr: $curr, top: $top, bot: $bot, left: $left, right: $right
                    """.trimIndent()
                    )
                    if (curr < top && curr < bot && curr < left && curr < right) {
                        lowPoints.add(Point(x, y, data[y][x]))
                    }
                }
            }

            return lowPoints
        }

        fun getBasins() {
            val basinCandidates = getLowPoints()

            val basinSizes = basinCandidates.map { calculateBasinSize(it) }

            val top3 = basinSizes.sorted().takeLast(3)

            val reduced = top3.reduce { acc, i -> acc * i }

            println(
                """
                basinSizes: $basinSizes,
                top3: $top3
                reduced: $reduced
            """.trimIndent()
            )
        }

        fun get(point: Point): Int {
            return data[point.y][point.x]
        }

        fun calculateBasinSize(point: Point): Int {
            println("calculate basin size for $point")
            val searched = mutableSetOf<Point>()

            val toSearch = mutableListOf<Point>()
            toSearch.add(point)

            while (toSearch.isNotEmpty()) {
                val curr = toSearch.removeFirst()
                searched.add(curr)
                val neighbors = getNeighbors(curr)
                val filtered = neighbors.filter {
                    it.num != 9 && curr.num < it.num && !searched.contains(it)
                }
                println(
                    """
                    curr: $curr,
                    neighbors: $neighbors,
                    filtered: $filtered
                    searched: $searched
                """.trimIndent()
                )
                toSearch.addAll(filtered)
            }

            return searched.size
        }

        fun getNeighbors(point: Point): List<Point> {
            return listOfNotNull(
                getPointOrNull(point.x - 1, point.y),
                getPointOrNull(point.x + 1, point.y),
                getPointOrNull(point.x, point.y - 1),
                getPointOrNull(point.x, point.y + 1),
            )
        }
    }

    fun part1(inputs: List<String>) {
        val data = inputs.map { it.toList().map { char -> char.digitToInt() } }
        val puzzleInput = PuzzleInput(data)

        puzzleInput.print2dArray()
        puzzleInput.getLowPoints()
    }

    fun part2(inputs: List<String>) {
        val data = inputs.map { it.toList().map { char -> char.digitToInt() } }
        val puzzleInput = PuzzleInput(data)

        puzzleInput.print2dArray()
        puzzleInput.getBasins()
    }

    val testInput = readInput("day9_test")
    val mainInput = readInput("day9")

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    part2(mainInput)
}