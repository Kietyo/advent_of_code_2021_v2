sealed class Node {
    object Start : Node()
    object Finish : Node()
    data class BigCave(val id: String) : Node()
    data class SmallCave(val id: String) : Node()

    companion object {
        fun parse(input: String): Node {
            return when {
                input == "start" -> Start
                input == "end" -> Finish
                input.all { it.isUpperCase() } -> BigCave(input)
                input.all { it.isLowerCase() } -> SmallCave(input)
                else -> TODO()
            }
        }
    }
}


fun main() {
    data class Edge(val from: Node, val to: Node)

    data class SearchState(
        val currentPath: List<Node>,
    ) {
        val exploredSmallCaves: Set<Node.SmallCave>
            get() = currentPath.filterIsInstance<Node.SmallCave>().toSet()
    }

    data class SearchState2(
        val currentPath: List<Node>,
        val allowedSmallCaveToVisitTwice: Node.SmallCave
    ) {
        val exploredSmallCaves: Set<Node.SmallCave>
            get() = currentPath.filterIsInstance<Node.SmallCave>().toSet()

        val allowedSmallCaveHasBeenVisitedTwice: Boolean
            get() = currentPath.count { it == allowedSmallCaveToVisitTwice } == 2
    }

    fun getEdgesWithFrom(edges: List<Edge>, from: Node): List<Edge> {
        return edges.filter { it.from == from }
    }

    fun getAllAvailableSmallCaves(edges: List<Edge>): Set<Node.SmallCave> {
        return edges.map { it.from }.filterIsInstance<Node.SmallCave>().toSet()
    }

    fun part1(inputs: List<String>) {
        val edges = mutableListOf<Edge>()
        for (input in inputs) {
            val t1 = input.split("-")
            val from = Node.parse(t1.first())
            val to = Node.parse(t1.last())
            edges.add(Edge(from, to))
            edges.add(Edge(to, from))
            println(t1)
        }

        println(edges.joinToString("\n"))

        val endPaths = mutableListOf<List<Node>>()

        val queue = mutableListOf<SearchState>()
        queue.add(SearchState(listOf(Node.Start)))

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentLastNode = current.currentPath.last()
            if (currentLastNode is Node.Finish) {
                endPaths.add(current.currentPath)
                continue
            }
            val availableEdges = getEdgesWithFrom(edges, currentLastNode).filter {
                it.to !is Node.Start &&
                        !current.exploredSmallCaves.contains(it.to)
            }
            println(
                """
                current: $current
                availableEdges: $availableEdges
            """.trimIndent()
            )
            for (edge in availableEdges) {
                queue.add(
                    SearchState(
                        current.currentPath + edge.to
                    )
                )
            }
        }

        println(endPaths.joinToString("\n"))
        println(
            """
            num end paths: ${endPaths.size}
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {
        val edges = mutableListOf<Edge>()
        for (input in inputs) {
            val t1 = input.split("-")
            val from = Node.parse(t1.first())
            val to = Node.parse(t1.last())
            edges.add(Edge(from, to))
            edges.add(Edge(to, from))
            println(t1)
        }

        println(edges.joinToString("\n"))

        val allAvailableSmallCaves = getAllAvailableSmallCaves(edges)

        println(
            """
            allAvailableSmallCaves: $allAvailableSmallCaves
        """.trimIndent()
        )

        val endPaths = mutableSetOf<List<Node>>()

        val queue = mutableListOf<SearchState2>()
        for (smallCave in allAvailableSmallCaves) {
            queue.add(SearchState2(listOf(Node.Start), smallCave))
        }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentLastNode = current.currentPath.last()
            if (currentLastNode is Node.Finish) {
                endPaths.add(current.currentPath)
                continue
            }
            val availableEdges = getEdgesWithFrom(edges, currentLastNode).filter {
                it.to !is Node.Start &&
                        (!current.exploredSmallCaves.contains(it.to) ||
                                (it.to == current.allowedSmallCaveToVisitTwice && !current.allowedSmallCaveHasBeenVisitedTwice))
            }
            println(
                """
                current: $current
                availableEdges: $availableEdges
            """.trimIndent()
            )
            for (edge in availableEdges) {
                queue.add(
                    SearchState2(
                        current.currentPath + edge.to,
                        current.allowedSmallCaveToVisitTwice
                    )
                )
            }
        }

        println(endPaths.joinToString("\n"))
        println(
            """
            num end paths: ${endPaths.size}
        """.trimIndent()
        )
    }

    val testInput = readInput("day12_test")
    val test2Input = readInput("day12_test2")
    val test3Input = readInput("day12_test3")
    val mainInput = readInput("day12")

    //    part1(testInput)
    //    part1(test2Input)
    //    part1(test3Input)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //        part2(test2Input)
    //        part2(test3Input)
    part2(mainInput)
}