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

sealed class Operations {
    data class Input(val name: Token.Variable, var digit: Int?) : Operations()
    data class Mul(val name: Token.Variable, val token: Token) : Operations()
    data class Add(val name: Token.Variable, val token: Token) : Operations()
    data class Div(val name: Token.Variable, val token: Token) : Operations()
    data class Eql(val name: Token.Variable, val token: Token) : Operations()
    data class Mod(val name: Token.Variable, val token: Token) : Operations()
}

class LongStore(var num: Long)

enum class ConsumeResult {
    STANDARD,
    INPUT;
}

data class ProblemContainer(
    // Corresponds to the input variable that the ALU accepts
    val inputStack: List<Token.Variable>,
    // Corresponds to the list of operations after accepting the input variable at the
    // corresponding index.
    val operationsStack: List<List<Operations>>
)

data class ALU(
    private val vars: Map<String, Long> = mapOf(
        "x" to 0L,
        "y" to 0L,
        "z" to 0L,
        "w" to 0L,
    )
) {
    fun get(a: Token.Variable): Long {
        return vars[a.name]!!
    }

    fun toMutableALU(): MutableALU {
        val alu = MutableALU()
        vars.forEach { entry ->
            alu.get(entry.key.toVarToken()).num = entry.value
        }
        return alu
    }
}

class MutableALU {
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

    fun toALU(): ALU {
        return ALU(buildMap {
            vars.forEach { entry ->
                put(entry.key, entry.value.num)
            }
        })
    }

    fun clone(): MutableALU {
        val newAlu = MutableALU()
        vars.forEach { (k, v) -> newAlu.get(k.toVarToken()).num = v.num }
        return newAlu
    }

    fun get(a: Token.Variable): LongStore {
        return vars[a.name]!!
    }

    fun consume(input: Operations, inputDigit: Int): ConsumeResult {
        when (input) {
            is Operations.Input -> {
                this.inp(input.name, inputDigit)
                return ConsumeResult.INPUT
            }
            is Operations.Add -> this.add(input.name, input.token)
            is Operations.Div -> this.div(input.name, input.token)
            is Operations.Eql -> this.eql(input.name, input.token)
            is Operations.Mod -> this.mod(input.name, input.token)
            is Operations.Mul -> this.mul(input.name, input.token)
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

fun main() {

    var numNumbersProcessedPerSecond = 0.0
    var numNumbersProcessed = 0
    var startTime = System.currentTimeMillis()

    fun calculate(idx: Int, prevAlu: ALU, problemContainer: ProblemContainer, currNum: Long) {
        if (idx == 14) {
            numNumbersProcessed++
            val delta = System.currentTimeMillis() - startTime
            if (delta > 1000) {
                numNumbersProcessedPerSecond = numNumbersProcessed / delta.toDouble()
                numNumbersProcessed = 0
                startTime = System.currentTimeMillis()
                println("$currNum (qps=$numNumbersProcessedPerSecond)")
            }

            if (prevAlu.get("z".toVarToken()) == 0L) {
                TODO("z==0: $prevAlu")
            }
            return
        }
        val inputVar = problemContainer.inputStack[idx]
        val operations = problemContainer.operationsStack[idx]

        for (i in 9 downTo 1) {
            val currALU = prevAlu.toMutableALU()
            currALU.inp(inputVar, i)
            operations.forEach {
                val result = currALU.consume(it, i)
                require(result != ConsumeResult.INPUT)
            }
            calculate(idx + 1, currALU.toALU(), problemContainer, currNum * 10 + i)
        }
    }

    fun part1(inputs: List<String>) {
        val highestNumber = 99999945727429
        val modelNumLength = 14
        println(highestNumber.toString().length)
        println(inputs)

        val operations = inputs.map { input ->
            when {
                input.startsWith("inp") -> {
                    val varName = input.last()
                    Operations.Input(varName.toToken(), null)
                }
                input.startsWith("mul") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Mul(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("add") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Add(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("div") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Div(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("eql") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Eql(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("mod") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Mod(varName.toVarToken(), numOrVar.toToken())
                }
                else -> TODO()
            }
        }

        val inputStack = mutableListOf<Token.Variable>()
        val digitStack = mutableListOf<Int>()
        var aluStack = mutableListOf<MutableALU>()
        val operationsStack = mutableListOf<List<Operations>>()

        var alu = MutableALU()
        val currOperations = mutableListOf<Operations>()
        for (input in operations) {
            val result = alu.consume(input, 9)

            when (result) {
                ConsumeResult.STANDARD -> {
                    currOperations.add(input)
                }
                ConsumeResult.INPUT -> {
                    inputStack.add((input as Operations.Input).name)
                    digitStack.add(9)
                    aluStack.add(alu.clone())
                    operationsStack.add(currOperations.toList())
                    currOperations.clear()
                }
            }
        }
        operationsStack.add(currOperations.toList())
        operationsStack.removeFirst()

        for (i0 in 1..9) {
            val alu0 = MutableALU()
        }

        println(
            """
        inputStack: $inputStack
        digitStack (size=${digitStack.size}): $digitStack,
        aluStack (size=${aluStack.size}): $aluStack,
        operationsStack (size=${operationsStack.size}): ${operationsStack.joinToString("\n")}
        currOperations: $currOperations
        """.trimIndent()
        )

        val problemContainer = ProblemContainer(
            inputStack,
            operationsStack
        )

        calculate(0, ALU(), problemContainer, 0L)

        //        var numNumbersProcessedPerSecond = 0.0
        //        var numNumbersProcessed = 0
        //        var startTime = System.currentTimeMillis()
        //        var prevNumber = highestNumber
        //        for (currNum in (highestNumber - 1) downTo 0L) {
        //            val firstUpdatedDiff = prevNumber.toString().asSequence().zip(
        //                currNum.toString().asSequence()
        //            ).withIndex().first { it.value.first != it.value.second }
        //            numNumbersProcessed++
        //            val delta = System.currentTimeMillis() - startTime
        //            if (delta > 1000) {
        //                numNumbersProcessedPerSecond = numNumbersProcessed / delta.toDouble()
        //                numNumbersProcessed = 0
        //                startTime = System.currentTimeMillis()
        //            }
        //
        //            alu = aluStack[firstUpdatedDiff.index].clone()
        //            aluStack = aluStack.subList(0, firstUpdatedDiff.index)
        //            val remainingOperations = operationsStack[firstUpdatedDiff.index]
        //            val asString = currNum.toString()
        //            if (asString.contains("0")) continue
        //            println(
        //                "$asString (qps=$numNumbersProcessedPerSecond), prevNumber: $prevNumber, " +
        //                        "currNum: $currNum, firstUpdatedDiff: $firstUpdatedDiff"
        //            )
        //            val digitItr = asString.subSequence(firstUpdatedDiff.index, 14).map {
        //                it.digitToInt()
        //            }.iterator()
        //            var currDigit = digitItr.next()
        //            for (operations in remainingOperations) {
        //                when (alu.consume(operations, currDigit)) {
        //                    ConsumeResult.STANDARD -> continue
        //                    ConsumeResult.INPUT -> {
        //                        if (digitItr.hasNext()) {
        //                            currDigit = digitItr.next()
        //                        }
        //                    }
        //                }
        //            }
        //            if (alu.get("z".toVarToken()).num == 0L) break
        //
        //            prevNumber = currNum
        //        }
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