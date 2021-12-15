data class IdChar(val char: Char, val id: Int) {
    companion object {
        val charIdMap = mutableMapOf<Char, Int>()
        fun createUnique(char: Char, leftChar: IdChar, rightChar: IdChar): IdChar {
            val availableIds = setOf(0, 1, 2)
            val currentIds = setOfNotNull(
                if (leftChar.char == char) leftChar.id else null,
                if (rightChar.char == char) rightChar.id else null,
            )
            val remainingIds = availableIds - currentIds
            return IdChar(char, remainingIds.first())
        }

        fun createUnique(char: Char): IdChar {
            val currId = charIdMap.getOrPut(char) { 0 }
            charIdMap[char] = currId + 1
            require(charIdMap[char]!! <= 2)
            return IdChar(char, currId)
        }
    }
}

data class IdCharTuple private constructor(val left: IdChar, val right: IdChar) {
    fun str(): String {
        return left.char.toString() + right.char.toString()
    }

    companion object {
        fun create(left: IdChar, right: IdChar): IdCharTuple {
            //            if (left.char != right.char) {
            //                return IdCharTuple(left, right)
            //            }
            //            require(left.id != right.id)
            //            val lowId = if (left.id < right.id) left else right
            //            val highId = if (left.id > right.id) left else right
            //            return IdCharTuple(lowId, highId)
            return IdCharTuple(left, right)
        }
    }
}

fun main() {


    fun step(
        polymerMapping: Map<IdCharTuple, Long>, mapping: Map<String, Char>,
        compensation: Map<Char, Long>
    ): Map<IdCharTuple,
            Long> {
        val newPolymerMapping = mutableMapOf<IdCharTuple, Long>()

        for (tuple in polymerMapping.keys) {
            val tupleStr = tuple.str()
            val currTupleCount = polymerMapping[tuple]!!
            if (mapping.containsKey(tupleStr)) {
                //                if (tuple.left.char == tuple.right.char && mapping[tupleStr]!! == tuple.left.char) {
                //                    println("WTF")
                //                }
                val insertChar = IdChar.createUnique(mapping[tupleStr]!!, tuple.left, tuple.right)
                val firstPart = IdCharTuple.create(
                    tuple.left,
                    insertChar
                )
                val secondPart = IdCharTuple.create(
                    insertChar,
                    tuple.right
                )
                newPolymerMapping[firstPart] = newPolymerMapping.getOrDefault(firstPart, 0L) +
                        currTupleCount
                newPolymerMapping[secondPart] = newPolymerMapping.getOrDefault(secondPart, 0L) +
                        currTupleCount
            } else {
                newPolymerMapping[tuple] = currTupleCount
            }
        }

        return newPolymerMapping
    }

    //    fun part1(inputs: List<String>) {
    //        val polymer = inputs.first()
    //        val mappings = inputs.filter { it.contains("->") }.map {
    //            val split = it.split(" -> ")
    //            Pair(split.first(), split.last())
    //        }.toMap()
    //
    //        println(inputs)
    //
    //        println("""
    //            polymer: $polymer
    //            mappings: $mappings
    //        """.trimIndent())
    //
    //        step(polymer, mappings)
    //
    //        var curr = polymer
    //        repeat(1) {
    //            curr = step(curr, mappings)
    //        }
    //
    //        val charCount = curr.toList().groupingBy { it }.eachCount()
    //        println(curr)
    //        println(charCount)
    //        val highestCount = charCount.values.maxOf { it.toLong() }
    //        val lowestCount = charCount.values.minOf { it.toLong() }
    //        println("""
    //            charCount: $charCount
    //            highestCount: $highestCount
    //            lowestCount: $lowestCount
    //            diff: ${highestCount - lowestCount}
    //        """.trimIndent())
    //    }

    fun part2(inputs: List<String>) {
        val polymer = inputs.first()
        val mappings = inputs.filter { it.contains("->") }.map {
            val split = it.split(" -> ")
            Pair(split.first(), split.last().first())
        }.toMap()

        val polymerMapping = polymer
            .map { IdChar.createUnique(it) }
            .windowed(2)
            .map {
                IdCharTuple.create(it.first(), it.last())
            }
            .groupingBy {
                it
            }
            .eachCount()
            .mapValues {
                it
                    .value.toLong()
            }

        println(inputs)

        println(
            """
            polymer: $polymer
            polymerMapping: $polymerMapping
            mappings: $mappings
        """.trimIndent()
        )

        val compensation = mutableMapOf<Char, Long>()

        var curr = polymerMapping
        repeat(2) {
            curr = step(curr, mappings, compensation)
            println("Calculated $it")
            println("curr:")
            println(curr.asSequence().joinToString("\n"))
        }


        val grouped1 = curr.flatMap {
            listOf(
                Pair(it.key.left, it.value),
                Pair(it.key.right, it.value)
            )
        }

        println("grouped1")
        println(grouped1.joinToString("\n"))

        val charCount = grouped1.groupBy { it.first.char }.mapValues {
            it.value.sumOf { p ->
                p.second
            }
        }


        //        val charCount = curr.toList().groupingBy { it }.eachCount()
        //        //        println(charCount)
        val highestCount = charCount.values.maxOf { it }
        val lowestCount = charCount.values.minOf { it }
        println(
            """
                charCount: $charCount
                highestCount: $highestCount
                lowestCount: $lowestCount
                diff: ${highestCount - lowestCount}
            """.trimIndent()
        )
    }

    fun step_v2(
        polymerMapping: Map<String, Long>, mappings: Map<String, Char>,
        compensation: MutableMap<Char, Long>
    ): MutableMap<String, Long> {
        val newPolymerMapping = mutableMapOf<String, Long>()

        for (entry in polymerMapping.entries) {
            if (mappings.containsKey(entry.key)) {
                val insertChar = mappings[entry.key]!!
                val left = entry.key.first().toString() + insertChar
                val right = insertChar.toString() + entry.key.last()


                newPolymerMapping.getOrPut(left) { 0L }
                newPolymerMapping.computeIfPresent(left) { _, ov -> ov + entry.value }

                newPolymerMapping.getOrPut(right) { 0L }
                newPolymerMapping.computeIfPresent(right) { _, ov -> ov + entry.value }

                compensation.getOrPut(insertChar) { 0L }
                compensation.computeIfPresent(insertChar) { _, ov -> ov + entry.value }
            } else {
                newPolymerMapping.getOrPut(entry.key) { 0L }
                newPolymerMapping.computeIfPresent(entry.key) { _, ov -> ov + entry.value }
            }
        }
        return newPolymerMapping
    }

    fun part2_v2(inputs: List<String>) {
        val polymer = inputs.first()
        val mappings = inputs.filter { it.contains("->") }.map {
            val split = it.split(" -> ")
            Pair(split.first(), split.last().first())
        }.toMap()

        val polymerMapping = mutableMapOf<String, Long>()
        val compensation = mutableMapOf<Char, Long>()

        for (i in 0 until polymer.length - 1) {
            val charA = polymer[i]
            val charB = polymer[i + 1]
            val substr = charA + "" + charB
            println(substr)
            polymerMapping.getOrPut(substr) { 0L }
            polymerMapping.computeIfPresent(substr) { _, ov -> ov + 1 }
            if (i > 0) {
                compensation.getOrPut(charA) { 0L }
                compensation.computeIfPresent(charA) { _, ov -> ov + 1 }
            }
        }


        println(
            """
            poylmer: $polymer,
            polymerMapping: $polymerMapping
            compensation: $compensation
        """.trimIndent()
        )

        var current = polymerMapping
        repeat(40) {
            current = step_v2(current, mappings, compensation)
        }

        val charCounts = current.entries.flatMap {
            listOf(
                Pair(it.key.first(), it.value),
                Pair(it.key.last(), it.value),
            )
        }
            .groupBy { it.first }
            .mapValues { it.value.sumOf { p -> p.second } }
            .mapValues {
                it.value - compensation.getOrDefault(it.key, 0L)
            }

        println(charCounts)

        val highestCount = charCounts.values.maxOf { it }
        val lowestCount = charCounts.values.minOf { it }
        println(
            """
                highestCount: $highestCount
                lowestCount: $lowestCount
                diff: ${highestCount - lowestCount}
            """.trimIndent()
        )
    }

    val testInput = readInput("day14_test")
    val mainInput = readInput("day14")

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2_v2(testInput)
    part2_v2(mainInput)

    //    println(IdChar.createUnique('B', IdChar('B', 0), IdChar('B', 1)))
}