fun main() {

    val byteMap = mapOf(
        '0' to "0000",
        '1' to "0001",
        '2' to "0010",
        '3' to "0011",
        '4' to "0100",
        '5' to "0101",
        '6' to "0110",
        '7' to "0111",
        '8' to "1000",
        '9' to "1001",
        'A' to "1010",
        'B' to "1011",
        'C' to "1100",
        'D' to "1101",
        'E' to "1110",
        'F' to "1111",
    )


    data class PacketResult(val data: Long, val numBitsConsumed: Int)

    data class LiteralResult(val literal: Long, val numBitsConsumed: Int)

    data class OperatorResult(
        val packets: List<PacketResult>,
        val numBitsConsumed: Int
    )

    class BitIterator(val data: String) {
        var versionSum = 0
        var currentIndex = 0

        fun consume(numBits: Int): String {
            val consumed = data.substring(currentIndex, currentIndex + numBits)
            currentIndex += numBits
            return consumed
        }

        fun consumeInt(numBits: Int): Int {
            return consume(numBits).toInt(2)
        }

        fun consumeBoolean(): Boolean {
            return consumeInt(1) == 1
        }

        fun consumeLiteral(): LiteralResult {
            val sb = StringBuilder()
            var numBitsConsumed = 0
            while (true) {
                val isLastGroup = !consumeBoolean()
                val groupData = consume(4)
                numBitsConsumed += 5
                sb.append(groupData)
                println(
                    """
                    isLastGroup: $isLastGroup
                    groupData: $groupData
                    sb: $sb
                """.trimIndent()
                )
                println()
                if (isLastGroup) {
                    return LiteralResult(sb.toString().toLong(2), numBitsConsumed)
                }
            }
        }

        fun consumeOperator(): OperatorResult {
            val lengthTypeId = consumeInt(1)
            var numBitsConsumedForOperator = 1
            val packets = mutableListOf<PacketResult>()
            when (lengthTypeId) {
                0 -> {
                    val length = consumeInt(15)
                    println("Processing operator. lengthTypeId: $lengthTypeId, length: $length")
                    numBitsConsumedForOperator += 15
                    var numBitsConsumed = 0
                    while (numBitsConsumed != length) {
                        require(numBitsConsumed <= length) {
                            "numBitsConsumed: $numBitsConsumed, length: $length"
                        }
                        val packet = consumePacket()
                        numBitsConsumed += packet.numBitsConsumed
                        packets.add(packet)
                    }
                    numBitsConsumedForOperator += numBitsConsumed
                }
                1 -> {
                    val numPackets = consumeInt(11)
                    numBitsConsumedForOperator += 11
                    repeat(numPackets) {
                        val packet = consumePacket()
                        numBitsConsumedForOperator += packet.numBitsConsumed
                        packets.add(packet)
                    }
                }
                else -> TODO()
            }
            return OperatorResult(packets, numBitsConsumedForOperator)
        }

        fun consumePacket(): PacketResult {
            val version = consumeInt(3)
            versionSum += version
            val type = consumeInt(3)
            var numBitsConsumed = 6
            println("Processing packet. version: $version, type: $type")
            when (type) {
                4 -> {
                    val literal = consumeLiteral()
                    numBitsConsumed += literal.numBitsConsumed
                    println(literal)
                    return PacketResult(literal.literal, numBitsConsumed)
                }
                else -> {
                    val operator = consumeOperator()
                    numBitsConsumed += operator.numBitsConsumed

                    val data = when (type) {
                        0 -> {
                            operator.packets.sumOf { it.data }
                        }
                        1 -> {
                            operator.packets.fold(1L) { acc: Long, packetResult: PacketResult ->
                                acc * packetResult.data
                            }
                        }
                        2 -> {
                            operator.packets.minOf { it.data }
                        }
                        3 -> {
                            operator.packets.maxOf { it.data }
                        }
                        5 -> {
                            require(operator.packets.size == 2)
                            val first = operator.packets.first()
                            val second = operator.packets.last()
                            if (first.data > second.data) 1L else 0L
                        }
                        6 -> {
                            require(operator.packets.size == 2)
                            val first = operator.packets.first()
                            val second = operator.packets.last()
                            if (first.data < second.data) 1L else 0L
                        }
                        7 -> {
                            require(operator.packets.size == 2)
                            val first = operator.packets.first()
                            val second = operator.packets.last()
                            if (first.data == second.data) 1L else 0L
                        }
                        else -> TODO()
                    }

                    return PacketResult(data, numBitsConsumed)
                }
            }
        }
    }

    fun part1(inputs: List<String>) {
        val byteString = inputs.first()

        val bitString = byteString.asSequence().map { byteMap[it] }.joinToString("")

        println(
            """
            byteString: $byteString
            bitString: $bitString
        """.trimIndent()
        )

        val bitIterator = BitIterator(bitString)

        val packetResult = bitIterator.consumePacket()




        println(
            """
            versionSum: ${bitIterator.versionSum}
            packetResult: $packetResult
        """.trimIndent()
        )
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day16_test")
    val testInput2 = readInput("day16_test2")
    val testInput3 = readInput("day16_test3")
    val testInput4 = readInput("day16_test4")
    val testInput5 = readInput("day16_test5")
    val testInput6 = readInput("day16_test6")
    val mainInput = readInput("day16")
    //
    //    part1(testInput)
    //    part1(testInput2)
    //    part1(testInput3)
    //    part1(testInput4)
    //    part1(testInput5)
    //    part1(testInput6)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}