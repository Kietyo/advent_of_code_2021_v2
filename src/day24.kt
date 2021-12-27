sealed class Token {
    data class Variable(val name: VariableName) : Token()
    data class Number(val num: Int) : Token()
}

enum class VariableName {
    X,
    Y,
    W,
    Z;
}

fun String.toVariableName(): VariableName {
    return when (this) {
        "x" -> VariableName.X
        "y" -> VariableName.Y
        "z" -> VariableName.Z
        "w" -> VariableName.W
        else -> TODO()
    }
}

fun String.toToken(): Token {
    if (this.last().isDigit()) {
        return Token.Number(this.toInt())
    }
    return Token.Variable(this.toVariableName())
}

sealed class Operations {
    data class Input(val name: VariableName, var digit: Int?) : Operations()
    data class Mul(val name: VariableName, val token: Token) : Operations()
    data class Add(val name: VariableName, val token: Token) : Operations()
    data class Div(val name: VariableName, val token: Token) : Operations()
    data class Eql(val name: VariableName, val token: Token) : Operations()
    data class Mod(val name: VariableName, val token: Token) : Operations()
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
    private val vars: LongArray
) {
    fun get(a: VariableName): Long {
        return vars[a.ordinal]
    }

    fun toMutableALU(): MutableALU {
        return MutableALU(vars)
    }
}

class MutableALU(
    val vars: LongArray = LongArray(4) { 0L }
) {
    fun toALU(): ALU {
        return ALU(vars.clone())
    }

    fun get(a: VariableName): Long {
        return vars[a.ordinal]
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

    fun inp(a: VariableName) {
        val input = readLine()
        vars[a.ordinal] = input!!.toLong()
    }

    fun inp(a: VariableName, num: Int) {
        vars[a.ordinal] = num.toLong()
    }

    fun add(a: VariableName, b: Token) {
        vars[a.ordinal] = vars[a.ordinal]!! + when (b) {
            is Token.Number -> b.num
            is Token.Variable -> get(b.name)
        }.toLong()
    }

    fun mul(a: VariableName, b: Token) {
        vars[a.ordinal] = vars[a.ordinal]!! * when (b) {
            is Token.Number -> b.num
            is Token.Variable -> get(b.name)
        }.toLong()
    }

    fun div(a: VariableName, b: Token) {
        val denom = when (b) {
            is Token.Number -> b.num
            is Token.Variable -> get(b.name)
        }.toLong()
        require(denom != 0L)
        vars[a.ordinal] = vars[a.ordinal]!! / denom
    }

    fun mod(a: VariableName, b: Token) {
        val aVal = vars[a.ordinal]!!
        if (aVal < 0L) {
            println("failed precon")
        }
        require(aVal >= 0L)
        val bVal = when (b) {
            is Token.Number -> b.num
            is Token.Variable -> get(b.name)
        }.toLong()
        require(bVal > 0L)
        vars[a.ordinal] = aVal % bVal
    }

    fun eql(a: VariableName, b: Token) {
        val otherNum = when (b) {
            is Token.Number -> b.num
            is Token.Variable -> get(b.name)
        }.toLong()
        val curr = get(a)
        vars[a.ordinal] = if (otherNum == curr) 1L else 0L
    }

    fun print() {
        println(
            vars.withIndex().joinToString {
                val varName = VariableName.values()[it.index]
                "$varName: ${it.value}"
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

            if (prevAlu.get(VariableName.Z) == 0L) {
                TODO("z==0: $prevAlu")
            }
            return
        }
        val operations = problemContainer.operationsStack[idx]

        for (i in 1..9) {
            val currALU = prevAlu.toMutableALU()
            currALU.inp(VariableName.W, i)
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
                    Operations.Input(varName.toString().toVariableName(), null)
                }
                input.startsWith("mul") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Mul(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("add") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Add(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("div") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Div(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("eql") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Eql(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("mod") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operations.Mod(varName.toVariableName(), numOrVar.toToken())
                }
                else -> TODO()
            }
        }

        val inputStack = mutableListOf<Token.Variable>()
        val operationsStack = mutableListOf<List<Operations>>()
        val currOperations = mutableListOf<Operations>()
        for (input in operations) {
            when (input) {
                is Operations.Input -> {
                    operationsStack.add(currOperations.toList())
                    currOperations.clear()
                }
                is Operations.Add,
                is Operations.Div,
                is Operations.Eql,
                is Operations.Mod,
                is Operations.Mul -> {
                    currOperations.add(input)
                }
            }
        }
        operationsStack.add(currOperations.toList())
        operationsStack.removeFirst()

        println(
            """
        inputStack: $inputStack
        operationsStack (size=${operationsStack.size}): ${operationsStack.joinToString("\n")}
        currOperations: $currOperations
        """.trimIndent()
        )

        val problemContainer = ProblemContainer(
            inputStack,
            operationsStack
        )

        calculate(0, ALU(LongArray(4) { 0L }), problemContainer, 0L)
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