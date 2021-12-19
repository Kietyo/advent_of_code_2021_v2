import kotlin.math.roundToLong

data class SplitResult(val splitOccurred: Boolean, val result: SnailNode)

sealed interface SnailNode {
    fun print(tab: Int = 0)
    fun split(): SplitResult
    fun explode(): ExplosionResult.FinalResult
    fun explodeInternal(depth: Int): ExplosionResult
    operator fun plus(other: SnailNode): SnailNode
}

sealed class ParsingNode {

    object Comma : ParsingNode()

    object LeftBrace : ParsingNode()

    object RightBrace : ParsingNode()
}

sealed class ExplosionResult {
    data class UnusedFullExplosionResult(
        val leftNumber: Long,
        val rightNumber: Long,
        val result: SnailNode
    ) : ExplosionResult()

    data class UnusedLeftExplosionResult(
        val num: Long,
        val result: SnailNode
    ) : ExplosionResult()

    data class UnusedRightExplosionResult(
        val num: Long,
        val result: SnailNode
    ) : ExplosionResult()

    data class FinalResult(
        val explosionOcurred: Boolean,
        val result: SnailNode
    ) : ExplosionResult()
}

data class SnailNumber(val num: Long) : ParsingNode(), SnailNode {
    override fun print(tab: Int) {
        print("\t".repeat(tab) + "Number: $num (depth: $tab)")
    }

    override fun split(): SplitResult {
        return if (num >= 10)
            SplitResult(
                true,
                SnailPair(
                    SnailNumber(num / 2),
                    SnailNumber((num / 2.0).roundToLong())
                )
            )
        else SplitResult(false, this)
    }

    override fun explode(): ExplosionResult.FinalResult {
        return explodeInternal(0) as ExplosionResult.FinalResult
    }

    override fun explodeInternal(depth: Int): ExplosionResult {
        return ExplosionResult.FinalResult(false, this)
    }

    override fun plus(other: SnailNode): SnailPair {
        return SnailPair(this, other)
    }
}


data class SnailPair(val left: SnailNode, val right: SnailNode) : ParsingNode(), SnailNode {
    fun addToLeftMostNumber(num: Long): SnailPair {
        return when (left) {
            is SnailNumber -> SnailPair(
                SnailNumber(num + left.num),
                right
            )
            is SnailPair -> SnailPair(
                left.addToLeftMostNumber(num),
                right
            )
        }
    }

    override fun explodeInternal(depth: Int): ExplosionResult {
        if (left is SnailNumber && right is SnailNumber) {
            if (depth >= 4) {
                return ExplosionResult.UnusedFullExplosionResult(
                    left.num,
                    right.num,
                    this
                )
            } else {
                return ExplosionResult.FinalResult(false, this)
            }
        }

        return when (left) {
            is SnailNumber -> {
                val rightResult = right.explodeInternal(depth + 1)
                when (rightResult) {
                    is ExplosionResult.FinalResult -> {
                        ExplosionResult.FinalResult(
                            rightResult.explosionOcurred,
                            SnailPair(left, rightResult.result),
                        )

                    }
                    is ExplosionResult.UnusedFullExplosionResult -> {
                        val num = rightResult.leftNumber + left.num
                        ExplosionResult.UnusedRightExplosionResult(
                            rightResult.rightNumber,
                            SnailPair(SnailNumber(num), SnailNumber(0))
                        )
                    }
                    is ExplosionResult.UnusedLeftExplosionResult -> {
                        val num = rightResult.num + left.num
                        ExplosionResult.FinalResult(
                            true,
                            SnailPair(SnailNumber(num), SnailNumber(0))
                        )
                    }
                    is ExplosionResult.UnusedRightExplosionResult -> {
                        if (depth == 0) {
                            ExplosionResult.FinalResult(
                                true,
                                SnailPair(left, rightResult.result)
                            )
                        } else {
                            ExplosionResult.UnusedRightExplosionResult(
                                rightResult.num,
                                SnailPair(left, rightResult.result)
                            )
                        }
                    }
                }
            }
            is SnailPair -> {
                val leftResult = left.explodeInternal(depth + 1)
                when (leftResult) {
                    is ExplosionResult.FinalResult -> {
                        ExplosionResult.FinalResult(
                            leftResult.explosionOcurred,
                            SnailPair(leftResult.result, right)
                        )
                    }
                    is ExplosionResult.UnusedFullExplosionResult -> {
                        when (right) {
                            is SnailNumber -> {
                                ExplosionResult.UnusedLeftExplosionResult(
                                    leftResult.leftNumber,
                                    SnailPair(
                                        SnailNumber(0),
                                        SnailNumber(leftResult.rightNumber + right.num)
                                    )
                                )
                            }
                            is SnailPair -> TODO()
                        }
                    }
                    is ExplosionResult.UnusedLeftExplosionResult -> {
                        if (depth == 0) {
                            ExplosionResult.FinalResult(
                                true,
                                SnailPair(
                                    leftResult.result,
                                    right
                                )
                            )
                        } else {
                            ExplosionResult.UnusedLeftExplosionResult(
                                leftResult.num,
                                SnailPair(
                                    leftResult.result,
                                    right
                                )
                            )
                        }
                    }
                    is ExplosionResult.UnusedRightExplosionResult -> {
                        when (right) {
                            is SnailNumber -> {
                                ExplosionResult.FinalResult(
                                    true,
                                    SnailPair(
                                        leftResult.result,
                                        SnailNumber(leftResult.num + right.num)
                                    )
                                )
                            }
                            is SnailPair -> {
                                ExplosionResult.FinalResult(
                                    true,
                                    SnailPair(
                                        leftResult.result,
                                        right.addToLeftMostNumber(leftResult.num)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    override fun print(tab: Int) {
        println("\t".repeat(tab) + "Pair: (depth: $tab)")
        left.print(tab + 1)
        println()
        right.print(tab + 1)
        if (tab == 0) {
            println()
        }
    }

    override fun split(): SplitResult {
        val leftSplit = left.split()
        val rightSplit = right.split()
        val splitOccurred = leftSplit.splitOccurred || rightSplit.splitOccurred
        return SplitResult(
            splitOccurred,
            SnailPair(
                leftSplit.result,
                rightSplit.result
            )
        )
    }

    override fun explode(): ExplosionResult.FinalResult {
        val result = explodeInternal(0)
        require(result is ExplosionResult.FinalResult)
        return result as ExplosionResult.FinalResult
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

        println("Initial root:")
        root.print()

        var result = root

        println("result 1")
        result = result.explode().result
        result.print()

        println("result 2")
        val explodResult = result.explode()
        println(explodResult)
        result = explodResult.result
        result.print()

        //        while (true) {
        //            generateSequence { result.explode(0) .take}
        //            var explosionResult = result.explode(0)
        //            while (explosionResult.explosionOcurred) {
        //                explosionResult = explosionResult.result.explode(0)
        //            }
        //
        //            val splitResult = explosionResult.result.split()
        //
        //            println(
        //                """
        //                explosionResult: $explosionResult,
        //                splitResult: $splitResult
        //            """.trimIndent()
        //            )
        //
        //            result = splitResult.result
        //            if (!splitResult.splitOccurred) {
        //                break
        //            }
        //        }
        //        //
        //        println("result")
        //        result.print()
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


