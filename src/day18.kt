import kotlin.math.roundToLong

data class ExplosionResult(
    val explosionOcurred: Boolean,
    val result: SnailNode
)

sealed interface SnailNode {
    fun print(tab: Int = 0)
    fun split(): SnailNode
    fun explode(depth: Int): ExplosionResult
    operator fun plus(other: SnailNode): SnailNode
}

sealed class ParsingNode {

    object Comma : ParsingNode()

    object LeftBrace : ParsingNode()

    object RightBrace : ParsingNode()
}

data class SnailNumber(val num: Long) : ParsingNode(), SnailNode {
    override fun print(tab: Int) {
        print("\t".repeat(tab) + "Number: $num")
    }

    override fun split(): SnailNode {
        return if (num >= 10)
            SnailPair(
                SnailNumber(num / 2),
                SnailNumber((num / 2.0).roundToLong())
            )
        else this
    }

    override fun explode(depth: Int): ExplosionResult {
        return ExplosionResult(false, this)
    }

    override fun plus(other: SnailNode): SnailPair {
        return SnailPair(this, other)
    }
}

data class SnailPair(val left: SnailNode, val right: SnailNode) : ParsingNode(), SnailNode {
    fun getRightNumber(): SnailNumber {
        require(right is SnailNumber)
        return right as SnailNumber
    }

    fun getLeftNumber(): SnailNumber {
        require(left is SnailNumber)
        return left as SnailNumber
    }

    fun isExplodablePair(): Boolean {
        return left is SnailNumber && right is SnailNumber
    }

    override fun explode(depth: Int): ExplosionResult {
        if (depth >= 3) {
            return when {
                left is SnailPair && right is SnailNumber -> {
                    val leftExploded = left.explode(depth + 1)
                    if (leftExploded.explosionOcurred) {
                        ExplosionResult(
                            true,
                            SnailPair(
                                SnailNumber(0),
                                SnailNumber(
                                    leftExploded.getRightNumber().num + right
                                        .num
                                )
                            )
                        )
                    } else {
                        ExplosionResult(
                            false,
                        )
                    }


                }
                left is SnailNumber && right is SnailPair -> {
                    val rightExploded = right.explode(depth + 1)
                    ExplosionResult(
                        true,
                        SnailPair(
                            SnailNumber(rightExploded.getLeftNumber().num + left.num),
                            SnailNumber(0)
                        )
                    )

                }
                left is SnailNumber && right is SnailNumber -> {
                    return ExplosionResult(false, this)
                }
                else -> TODO(
                    """
                    Unhandled.
                    left is: $left
                    right is $right
                """.trimIndent()
                )
            }
        }

        return when (left) {
            is SnailNumber -> {
                val newRight = right.explode(depth + 1)
                if (newRight.explosionOcurred) {
                    ExplosionResult(
                        true,
                        SnailPair(left, newRight.result)
                    )

                } else {
                    ExplosionResult(
                        false,
                        this
                    )
                }
            }
            is SnailPair -> {
                val newLeft = left.explode(depth + 1)
                if (newLeft.explosionOcurred) {
                    ExplosionResult(true, SnailPair(newLeft.result, right))
                } else {
                    ExplosionResult(false, this)
                }
            }
        }

    }

    override fun print(tab: Int) {
        println("\t".repeat(tab) + "Pair:")
        left.print(tab + 1)
        println()
        right.print(tab + 1)
        if (tab == 0) {
            println()
        }
    }

    override fun split(): SnailNode {
        return SnailPair(
            left.split(),
            right.split()
        )
    }

    override fun plus(other: SnailNode): SnailNode {
        return SnailPair(this, other)
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
                SnailNumber(sb.toString().toLong())
            }
            else -> TODO("Not implemented: $currChar")
        }
    }
}

fun parseSnailNode(str: String): SnailNode {
    val stack = mutableListOf<ParsingNode>()

    val parser = Parser(str)

    while (parser.hasNextNode()) {
        val currNode = parser.takeNextNode()
        when (currNode) {
            ParsingNode.Comma,
            ParsingNode.LeftBrace,
            is SnailNumber -> {
                stack.add(currNode)
            }
            ParsingNode.RightBrace -> {
                val rightNode = stack.removeLast() as SnailNode
                val comma = stack.removeLast()
                val leftNode = stack.removeLast() as SnailNode
                val leftBrace = stack.removeLast()
                require(rightNode is SnailNumber || rightNode is SnailPair)
                require(comma == ParsingNode.Comma)
                require(leftNode is SnailNumber || leftNode is SnailPair)
                require(leftBrace == ParsingNode.LeftBrace)
                stack.add(SnailPair(leftNode, rightNode))
            }
        }

        println("stack: $stack")


    }

    println(stack)

    val rootNode = stack.first() as SnailNode
    rootNode.print()

    return rootNode
}

fun main() {

    fun part1(inputs: List<String>) {
        println(inputs)
        val first = inputs.first()

        val root =
            inputs.map { parseSnailNode(it) }.reduce { acc: SnailNode, snailNode: SnailNode ->
                acc + snailNode
            }

        println(
            """
            root: $root
        """.trimIndent()
        )

        root.print()

        var result = root
        result = result.explode(depth + 1)
        println("result 1:")
        result.print()

        result = result.split()
        println("result 2:")
        result.print()

        result = result.explode(depth + 1)
        println("result 3:")
        result.print()
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day18_test")
    val mainInput = readInput("day18")

    val num = 23

    println((num / 2.0))
    println((num / 2).toLong())
    println((num / 2.0).roundToLong())

    part1(testInput)
    //        part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}


