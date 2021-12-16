import java.lang.IllegalStateException


fun main() {
    data class Point(
        val x: Int, val y: Int
    )

    data class SearchState(
        val path: List<Point>,
        val riskCost: Int
    ) {
        fun getLast(): Point {
            return path.last()
        }

        fun getSecondToLastOrNull(): Point? {
            return path.getOrNull(path.size - 2)
        }
    }


    //    data class PuzzleInput(
    //        val data: List<List<Point>>
    //    )
    //    {
    //        val height = data.size
    //        val width = data.first().size
    //        val garbagePoint = Point(-1, -1, 0)
    //
    //        fun print2dArray() {
    //            println(data.joinToString("\n") {
    //                it.joinToString {
    //                    it.num.toString()
    //                }
    //            })
    //        }
    //
    //        fun getPoint(x: Int, y: Int): Point {
    //            if (x in 0 until width && y in 0 until height) {
    //                return data[y][x]
    //            }
    //            return garbagePoint
    //        }
    //
    //    }


    data class Grid(
        val grid: List<IntArray>
    ) {
        val width = grid.first().size
        val height = grid.size

        fun get(point: Point): Int {
            return grid[point.y][point.x]
        }

        fun getPointOrNull(x: Int, y: Int): Point? {
            if (x in 0 until width && y in 0 until height) {
                return Point(x, y)
            }
            return null
        }

        fun getNeighbors(point: Point): List<Point> {
            return listOfNotNull(
                getPointOrNull(point.x - 1, point.y),
                getPointOrNull(point.x + 1, point.y),
                getPointOrNull(point.x, point.y - 1),
                getPointOrNull(point.x, point.y + 1),
            )
        }

        fun print2dGrid() {
            println(
                """
                width: $width,
                height: $height
            """.trimIndent()
            )
            println(grid.joinToString("\n") { it.joinToString("") })

        }

    }


    fun dfs(searchState: SearchState, grid: Grid): SearchState {
        val stack = mutableListOf<SearchState>()
        stack.add(searchState)

        var result: SearchState? = null

        while (stack.isNotEmpty()) {
            val curr = stack.removeLast()
            val last = curr.getLast()

            if (last.x == grid.width - 1 && last.y == grid.height - 1) {
                result = curr
                break
            }

            val nextPoints = grid.getNeighbors(last).filter { !curr.path.contains(it) }
            for (nextPoint in nextPoints) {
                stack.add(SearchState(curr.path + nextPoint, curr.riskCost + grid.get(nextPoint)))
            }
        }

        return result!!
    }

    fun bfs(searchState: SearchState, grid: Grid, maxRiskCost: Int) {
        val queue = mutableListOf<SearchState>()
        queue.add(searchState)

        var result: SearchState? = null
        val searchedStates = mutableSetOf<SearchState>()

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (curr.riskCost >= maxRiskCost) {
                // The current solution is already at or greater than the dfs solution
                continue
            }
            if (searchedStates.contains(curr)) {
                continue
            }
            searchedStates.add(curr)
            val last = curr.getLast()
            if (last.x == grid.width - 1 && last.y == grid.height - 1) {
                result = curr
                break
            }
            val secondToLast = curr.getSecondToLastOrNull()

            val nextPoints = grid.getNeighbors(last).filter { !curr.path.contains(it) }
            for (nextPoint in nextPoints) {
                queue.add(SearchState(curr.path + nextPoint, curr.riskCost + grid.get(nextPoint)))
            }
        }

        println(result)
    }

    fun part1(inputs: List<String>) {
        val grid = Grid(inputs.map { row -> row.toList().map { it.digitToInt() }.toIntArray() })

        val initialState = SearchState(listOf(Point(0, 0)), grid.get(Point(0, 0)))

        val dfsSolution = dfs(initialState, grid)

        println(dfsSolution)

        bfs(initialState, grid, dfsSolution.riskCost)

    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day15_test")
    val mainInput = readInput("day15")

    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}