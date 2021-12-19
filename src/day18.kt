import kotlin.math.max
import kotlin.math.roundToLong

data class SplitResult(val splitOccurred: Boolean, val result: SnailNode)

sealed interface SnailNode {
    fun print(tab: Int = 0)
    fun split(): SplitResult
    fun explode(): ExplosionResult
    fun explodeInternal(depth: Int): ExplosionResult
    fun calculateMagnitude(): Long
    fun reduce(): SnailNode
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

    data class FinalResultWithExplosion(
        val result: SnailNode
    ) : ExplosionResult()

    data class FinalResultNoExplosion(
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

    override fun explode(): ExplosionResult.FinalResultWithExplosion {
        return explodeInternal(0) as ExplosionResult.FinalResultWithExplosion
    }

    override fun explodeInternal(depth: Int): ExplosionResult {
        return ExplosionResult.FinalResultNoExplosion(this)
    }

    override fun calculateMagnitude(): Long {
        return num
    }

    override fun reduce(): SnailNode {
        return this
    }

    override fun plus(other: SnailNode): SnailPair {
        return SnailPair(this, other)
    }
}


data class SnailPair(val left: SnailNode, val right: SnailNode) : ParsingNode(), SnailNode {
    fun addToRightMostNumber(num: Long): SnailPair {
        return when (right) {
            is SnailNumber -> SnailPair(
                left,
                SnailNumber(num + right.num)
            )
            is SnailPair -> SnailPair(
                left,
                right.addToRightMostNumber(num)
            )
        }
    }

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
                return ExplosionResult.FinalResultNoExplosion(this)
            }
        }

        return when (left) {
            is SnailNumber -> {
                handleRightExplosion(depth, left)
            }
            is SnailPair -> {
                val leftResult = left.explodeInternal(depth + 1)
                when (leftResult) {
                    is ExplosionResult.FinalResultWithExplosion -> {
                        ExplosionResult.FinalResultWithExplosion(
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
                            is SnailPair -> {
                                ExplosionResult.UnusedLeftExplosionResult(
                                    leftResult.leftNumber,
                                    SnailPair(
                                        SnailNumber(0),
                                        right.addToLeftMostNumber(leftResult.rightNumber)
                                    )
                                )
                            }
                        }
                    }
                    is ExplosionResult.UnusedLeftExplosionResult -> {
                        if (depth == 0) {
                            ExplosionResult.FinalResultWithExplosion(
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
                                ExplosionResult.FinalResultWithExplosion(
                                    SnailPair(
                                        leftResult.result,
                                        SnailNumber(right.num + leftResult.num)
                                    )
                                )
                            }
                            is SnailPair -> {
                                ExplosionResult.FinalResultWithExplosion(
                                    SnailPair(
                                        leftResult.result,
                                        right.addToLeftMostNumber(leftResult.num)
                                    )
                                )
                            }
                        }
                    }
                    is ExplosionResult.FinalResultNoExplosion -> {
                        when (right) {
                            is SnailNumber -> {
                                ExplosionResult.FinalResultNoExplosion(
                                    this
                                )
                            }
                            is SnailPair -> handleRightExplosion(depth, left)
                        }
                    }
                }
            }
        }

    }

    override fun calculateMagnitude(): Long {
        return 3 * left.calculateMagnitude() + 2 * right.calculateMagnitude()
    }

    override fun reduce(): SnailNode {
        var result: SnailNode = this

        while (true) {
            var i = 0
            var hasExplosion = true
            while (hasExplosion) {
                val explosionResult = result.explode()

                println(
                    """
                        explosion: $i
                        explosionResult: $explosionResult
                    """.trimIndent()
                )
                i++
                when (explosionResult) {
                    is ExplosionResult.FinalResultNoExplosion -> {
                        result = explosionResult.result
                        result.print()
                        hasExplosion = false
                    }
                    is ExplosionResult.FinalResultWithExplosion -> {
                        result = explosionResult.result
                        result.print()
                        hasExplosion = true
                    }
                    else -> TODO()
                }
            }

            val splitResult = result.split()

            println(
                """
                    splitResult: $splitResult
                """.trimIndent()
            )

            result = splitResult.result
            if (!splitResult.splitOccurred) {
                break
            }
        }

        return result
    }

    private fun handleRightExplosion(depth: Int, left: SnailNode): ExplosionResult {
        val explosionResult = right.explodeInternal(depth + 1)
        return when (explosionResult) {
            is ExplosionResult.FinalResultWithExplosion -> {
                ExplosionResult.FinalResultWithExplosion(
                    SnailPair(left, explosionResult.result),
                )
            }
            is ExplosionResult.UnusedFullExplosionResult -> {
                when (left) {
                    is SnailNumber -> {
                        ExplosionResult.UnusedRightExplosionResult(
                            explosionResult.rightNumber,
                            SnailPair(
                                SnailNumber(explosionResult.leftNumber + left.num),
                                SnailNumber(0)
                            )
                        )
                    }
                    is SnailPair -> {
                        TODO()
                        //                        ExplosionResult.UnusedRightExplosionResult(
                        //                            explosionResult.rightNumber,
                        //                            SnailPair(
                        //                                left.addToRightMostNumber(explosionResult.leftNumber),
                        //                                explosionResult.result
                        //                            )
                        //                        )
                    }
                }

            }
            is ExplosionResult.UnusedLeftExplosionResult -> {
                when (left) {
                    is SnailNumber -> {
                        ExplosionResult.FinalResultWithExplosion(
                            SnailPair(
                                SnailNumber(
                                    explosionResult.num + left.num
                                ),
                                explosionResult.result
                            )
                        )
                    }
                    is SnailPair -> {
                        ExplosionResult.FinalResultWithExplosion(
                            SnailPair(
                                left.addToRightMostNumber(explosionResult.num),
                                explosionResult.result
                            )
                        )
                    }
                }
            }
            is ExplosionResult.UnusedRightExplosionResult -> {
                if (depth == 0) {
                    ExplosionResult.FinalResultWithExplosion(
                        SnailPair(left, explosionResult.result)
                    )
                } else {
                    ExplosionResult.UnusedRightExplosionResult(
                        explosionResult.num,
                        SnailPair(left, explosionResult.result)
                    )
                }
            }
            is ExplosionResult.FinalResultNoExplosion -> {
                ExplosionResult.FinalResultNoExplosion(
                    SnailPair(left, explosionResult.result)
                )
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
        if (leftSplit.splitOccurred) {
            return SplitResult(
                true,
                SnailPair(
                    leftSplit.result,
                    right
                )
            )
        }
        val rightSplit = right.split()
        return SplitResult(
            rightSplit.splitOccurred,
            SnailPair(
                left,
                rightSplit.result
            )
        )
    }

    override fun explode(): ExplosionResult {
        val result = explodeInternal(0)
        return result
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
                (acc + snailNode).reduce()
            }

        println(
            """
            root: $root
        """.trimIndent()
        )

        println("Initial root:")
        root.print()

        println(
            """
            magnitude: ${root.calculateMagnitude()}
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {
        val root = inputs.map { parseSnailNode(it) }

        var maxMagnitude = Long.MIN_VALUE
        for (r1 in root) {
            for (r2 in root) {
                val reduced = (r1.reduce() + r2.reduce()).reduce()
                maxMagnitude = max(maxMagnitude, reduced.calculateMagnitude())
            }
        }

        println(
            """
            maxMagnitude: $maxMagnitude
        """.trimIndent()
        )
    }

    val testInput = readInput("day18_test")
    val mainInput = readInput("day18")

    val num = 23

    println((num / 2.0))
    println((num / 2).toLong())
    println((num / 2.0).roundToLong())

    //    part1(testInput)
    //        part1(mainInput)
    //
    part2(testInput)
    //        part2(mainInput)
}


