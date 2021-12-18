sealed class ParsingNode {
    abstract fun print(tab: Int = 0): Unit

    object Comma : ParsingNode() {
        override fun print(tab: Int) {
            print("\t".repeat(tab) + ',')
        }
    }

    object LeftBrace : ParsingNode() {
        override fun print(tab: Int) {
            TODO("Not yet implemented")
        }
    }

    object RightBrace : ParsingNode() {
        override fun print(tab: Int) {
            TODO("Not yet implemented")
        }
    }

    data class Number(val num: Long) : ParsingNode() {
        override fun print(tab: Int) {
            print("\t".repeat(tab) + "Number: $num")
        }
    }

    data class Pair(val left: ParsingNode, val right: ParsingNode) : ParsingNode() {
        override fun print(tab: Int) {
            println("\t".repeat(tab) + "Pair:")
            left.print(tab + 1)
            println()
            right.print(tab + 1)
            if (tab == 0) {
                println()
            }
        }
    }
}

class Parser(val str: String) {
    var currentIndex = 0

    fun hasNextNode(): Boolean {
        return currentIndex < str.length
    }

    fun takeNextNode(): ParsingNode {
        val currChar = str[currentIndex]
        return when {
            currChar == '[' -> {
                currentIndex++
                ParsingNode.LeftBrace
            }
            currChar == ',' -> {
                currentIndex++
                ParsingNode.Comma
            }
            currChar == ']' -> {
                currentIndex++
                ParsingNode.RightBrace
            }
            currChar.isDigit() -> {
                val sb = StringBuilder()
                while (str[currentIndex].isDigit()) {
                    sb.append(str[currentIndex])
                    currentIndex++
                }
                ParsingNode.Number(sb.toString().toLong())
            }
            else -> TODO("Not implemented: $currChar")
        }
    }
}

fun parseSnailNode(str: String) {
    val stack = mutableListOf<ParsingNode>()

    val parser = Parser(str)

    while (parser.hasNextNode()) {
        val currNode = parser.takeNextNode()
        when (currNode) {
            ParsingNode.Comma,
            ParsingNode.LeftBrace,
            is ParsingNode.Number -> {
                stack.add(currNode)
            }
            ParsingNode.RightBrace -> {
                val rightNode = stack.removeLast()
                val comma = stack.removeLast()
                val leftNode = stack.removeLast()
                val leftBrace = stack.removeLast()
                require(rightNode is ParsingNode.Number || rightNode is ParsingNode.Pair)
                require(comma == ParsingNode.Comma)
                require(leftNode is ParsingNode.Number || leftNode is ParsingNode.Pair)
                require(leftBrace == ParsingNode.LeftBrace)
                stack.add(ParsingNode.Pair(leftNode, rightNode))
            }
        }

        println("stack: $stack")


    }

    println(stack)

    val rootNode = stack.first()
    rootNode.print()
}

fun main() {

    fun part1(inputs: List<String>) {
        println(inputs)
        val first = inputs.first()



        for (input in inputs) {
            val root = parseSnailNode(first)
            println(
                """
                root: $root
            """.trimIndent()
            )
        }

    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day18_test")
    val mainInput = readInput("day18")


    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}


