sealed class Token {
    data class Variable(val name: String) : Token()
    data class Number(val num: Int) : Token()
}

fun String.toVarToken(): Token.Variable {
    return Token.Variable(this)
}

fun String.toToken(): Token {
    if (this.last().isDigit()) {
        return Token.Number(this.toInt())
    }
    return Token.Variable(this)
}

fun Char.toToken(): Token.Variable {
    return Token.Variable(this.toString())
}

fun Int.toToken(): Token.Number {
    return Token.Number(this)
}

sealed class ProcessedInput {
    data class Input(val name: Token.Variable, var digit: Int?) : ProcessedInput()
    data class Mul(val name: Token.Variable, val token: Token) : ProcessedInput()
    data class Add(val name: Token.Variable, val token: Token) : ProcessedInput()
    data class Div(val name: Token.Variable, val token: Token) : ProcessedInput()
    data class Eql(val name: Token.Variable, val token: Token) : ProcessedInput()
    data class Mod(val name: Token.Variable, val token: Token) : ProcessedInput()
}

class LongStore(var num: Long)

enum class ConsumeResult {
    STANDARD,
    INPUT;
}

fun main() {



    class ALU {
        val vars = buildMap {
            put("x", LongStore(0L))
            put("y", LongStore(0L))
            put("z", LongStore(0L))
            put("w", LongStore(0L))
        }

        fun reset() {
            vars.forEach { key, value ->
                value.num = 0
            }
        }

        fun clone(): ALU {
            val newAlu = ALU()
            vars.forEach { (k, v) -> newAlu.get(k.toVarToken()).num = v.num }
            return newAlu
        }

        fun get(a: Token.Variable): LongStore {
            return vars[a.name]!!
        }

        fun consume(input: ProcessedInput, inputDigit: Int): ConsumeResult {
            when (input) {
                is ProcessedInput.Input -> {
                    this.inp(input.name, inputDigit)
                    return ConsumeResult.INPUT
                }
                is ProcessedInput.Add -> this.add(input.name, input.token)
                is ProcessedInput.Div -> this.div(input.name, input.token)
                is ProcessedInput.Eql -> this.eql(input.name, input.token)
                is ProcessedInput.Mod -> this.mod(input.name, input.token)
                is ProcessedInput.Mul -> this.mul(input.name, input.token)
                else -> TODO("unsupported operation: $input")
            }
            return ConsumeResult.STANDARD
        }

        fun inp(a: Token.Variable) {
            val input = readLine()
            get(a).num = input!!.toLong()
        }

        fun inp(a: Token.Variable, num: Int) {
            get(a).num = num.toLong()
        }

        fun add(a: Token.Variable, b: Token) = get(a).run {
            this.num += when (b) {
                is Token.Number -> b.num
                is Token.Variable -> get(b).num
            }.toLong()
        }

        fun mul(a: Token.Variable, b: Token) = get(a).run {
            this.num *= when (b) {
                is Token.Number -> b.num
                is Token.Variable -> get(b).num
            }.toLong()
        }

        fun div(a: Token.Variable, b: Token) = get(a).run {
            this.num /= when (b) {
                is Token.Number -> b.num
                is Token.Variable -> get(b).num
            }.toLong()
        }

        fun mod(a: Token.Variable, b: Token) = get(a).run {
            this.num %= when (b) {
                is Token.Number -> b.num
                is Token.Variable -> get(b).num
            }.toLong()
        }

        fun eql(a: Token.Variable, b: Token) = get(a).run {
            val otherNum = when (b) {
                is Token.Number -> b.num
                is Token.Variable -> get(b).num
            }.toLong()
            val res = if (otherNum == this.num) 1 else 0
            this.num = res.toLong()
        }

        fun print() {
            println(
                vars.entries.joinToString("\n") {
                    "${it.key}: ${it.value.num}"
                }
            )
        }
    }

    fun part1(inputs: List<String>) {
        val highestNumber = 99999945727429
        val modelNumLength = 14
        println(highestNumber.toString().length)
        println(inputs)

        val processedInputs = inputs.map { input ->
            when {
                input.startsWith("inp") -> {
                    val varName = input.last()
                    ProcessedInput.Input(varName.toToken(), null)
                }
                input.startsWith("mul") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    ProcessedInput.Mul(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("add") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    ProcessedInput.Add(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("div") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    ProcessedInput.Div(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("eql") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    ProcessedInput.Eql(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("mod") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    ProcessedInput.Mod(varName.toVarToken(), numOrVar.toToken())
                }
                else -> TODO()
            }
        }

        val digitStack = mutableListOf<Int>()
        var aluStack = mutableListOf<ALU>()
        val operationsStack = mutableListOf<List<ProcessedInput>>()

        var alu = ALU()
        val currOperations = mutableListOf<ProcessedInput>()
        for (input in processedInputs) {
            val result = alu.consume(input, 9)
            currOperations.add(input)

            when (result) {
                ConsumeResult.STANDARD -> continue
                ConsumeResult.INPUT -> {
                    digitStack.add(9)
                    aluStack.add(alu.clone())
                    operationsStack.add(currOperations.toList())
                    currOperations.clear()
                }
            }
        }
        operationsStack.add(currOperations.toList())
        operationsStack.removeFirst()

        println(
            """
            digitStack (size=${digitStack.size}): $digitStack,
            aluStack (size=${aluStack.size}): $aluStack,
            operationsStack (size=${operationsStack.size}): ${operationsStack.joinToString("\n")}
            currOperations: $currOperations
        """.trimIndent()
        )

        var numNumbersProcessedPerSecond = 0.0
        var numNumbersProcessed = 0
        var startTime = System.currentTimeMillis()
        var prevNumber = highestNumber
        for (currNum in (highestNumber - 1) downTo 0L) {
            val firstUpdatedDiff = prevNumber.toString().asSequence().zip(
                currNum.toString().asSequence()
            ).withIndex().first { it.value.first != it.value.second }
            numNumbersProcessed++
            val delta = System.currentTimeMillis() - startTime
            if (delta > 1000) {
                numNumbersProcessedPerSecond = numNumbersProcessed / delta.toDouble()
                numNumbersProcessed = 0
                startTime = System.currentTimeMillis()
            }

            alu = aluStack[firstUpdatedDiff.index].clone()
            aluStack = aluStack.subList(0, firstUpdatedDiff.index)
            val remainingOperations = operationsStack[firstUpdatedDiff.index]
            val asString = currNum.toString()
            if (asString.contains("0")) continue
            println(
                "$asString (qps=$numNumbersProcessedPerSecond), prevNumber: $prevNumber, " +
                        "currNum: $currNum, firstUpdatedDiff: $firstUpdatedDiff"
            )
            val digitItr = asString.subSequence(firstUpdatedDiff.index, 14).map {
                it.digitToInt()
            }.iterator()
            var currDigit = digitItr.next()
            for (operations in remainingOperations) {
                when (alu.consume(operations, currDigit)) {
                    ConsumeResult.STANDARD -> continue
                    ConsumeResult.INPUT -> {
                        if (digitItr.hasNext()) {
                            currDigit = digitItr.next()
                        }
                    }
                }
            }
            if (alu.get("z".toVarToken()).num == 0L) break

            prevNumber = currNum
        }
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day24_test")
    val mainInput = readInput("day24")

    println(Long.MAX_VALUE)

    //    val alu = ALU()
    //    alu.inp("x".toVarToken(), 9)
    //    alu.eql("x".toVarToken(), Token.Number(9))
    //
    //    alu.print()

    //            part1(testInput)
    part1(mainInput)

    //
    //    part2(testInput)
    //    part2(mainInput)

}