import java.math.BigInteger

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

enum class OperatorResult {
    NO_ERROR,
    INPUT,
    ERROR;
}

data class ProblemContainer(
    // Corresponds to the input variable that the ALU accepts
    val inputStack: List<Token.Variable>,
    // Corresponds to the list of operations after accepting the input variable at the
    // corresponding index.
    val operationsStack: List<List<Operations>>
)

data class ALU(
    private val vars: Array<BigInteger>
) {
    fun get(a: VariableName): BigInteger {
        return vars[a.ordinal]
    }

    fun toMutableALU(): MutableALU {
        return MutableALU(vars.clone())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ALU

        if (!vars.contentEquals(other.vars)) return false

        return true
    }

    override fun hashCode(): Int {
        return vars.contentHashCode()
    }
}

class MutableALU(
    val vars: Array<BigInteger>
) {
    fun toALU(): ALU {
        return ALU(vars.clone())
    }

    fun get(a: VariableName): BigInteger {
        return vars[a.ordinal]
    }

    fun consume(input: Operations, inputDigit: Int): OperatorResult {
        return when (input) {
            is Operations.Input -> {
                this.inp(input.name, inputDigit)
            }
            is Operations.Add -> this.add(input.name, input.token)
            is Operations.Div -> this.div(input.name, input.token)
            is Operations.Eql -> this.eql(input.name, input.token)
            is Operations.Mod -> this.mod(input.name, input.token)
            is Operations.Mul -> this.mul(input.name, input.token)
            else -> TODO("unsupported operation: $input")
        }
    }

    fun inp(a: VariableName) {
        val input = readLine()
        vars[a.ordinal] = input!!.toBigInteger()
    }

    fun inp(a: VariableName, num: Int): OperatorResult {
        vars[a.ordinal] = num.toBigInteger()
        return OperatorResult.NO_ERROR
    }

    fun add(a: VariableName, b: Token): OperatorResult {
        vars[a.ordinal] = vars[a.ordinal]!! + when (b) {
            is Token.Number -> b.num.toBigInteger()
            is Token.Variable -> get(b.name)
        }
        return OperatorResult.NO_ERROR
    }

    fun mul(a: VariableName, b: Token): OperatorResult {
        vars[a.ordinal] = vars[a.ordinal]!! * when (b) {
            is Token.Number -> b.num.toBigInteger()
            is Token.Variable -> get(b.name)
        }
        return OperatorResult.NO_ERROR
    }

    fun div(a: VariableName, b: Token): OperatorResult {
        val denom = when (b) {
            is Token.Number -> b.num.toBigInteger()
            is Token.Variable -> get(b.name)
        }
        require(denom != BigInteger.ZERO)
        vars[a.ordinal] = vars[a.ordinal]!! / denom
        return OperatorResult.NO_ERROR
    }

    fun mod(a: VariableName, b: Token): OperatorResult {
        val aVal = vars[a.ordinal]!!
        if (aVal < BigInteger.ZERO) {
            return OperatorResult.ERROR
        }
        require(aVal >= BigInteger.ZERO)
        val bVal = when (b) {
            is Token.Number -> b.num.toBigInteger()
            is Token.Variable -> get(b.name)
        }
        require(bVal > BigInteger.ZERO)
        vars[a.ordinal] = aVal % bVal
        return OperatorResult.NO_ERROR
    }

    fun eql(a: VariableName, b: Token): OperatorResult {
        val otherNum = when (b) {
            is Token.Number -> b.num.toBigInteger()
            is Token.Variable -> get(b.name)
        }
        val curr = get(a)
        vars[a.ordinal] = if (otherNum == curr) BigInteger.ONE else BigInteger.ZERO
        return OperatorResult.NO_ERROR
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

data class State(
    val idx: Int,
    val prevALU: ALU,
)

fun main() {

    var numNumbersProcessedPerSecond = 0.0
    var numNumbersProcessed = 0
    var startTime = System.currentTimeMillis()

    // States that we know will not lead to z=0.
    val dp = mutableSetOf<State>()

    fun calculate(state: State, problemContainer: ProblemContainer, currNum: Long) {
        if (dp.contains(state)) return
        if (state.idx == 14) {
            numNumbersProcessed++
            val delta = System.currentTimeMillis() - startTime
            if (delta > 1000) {
                numNumbersProcessedPerSecond = numNumbersProcessed / delta.toDouble()
                numNumbersProcessed = 0
                startTime = System.currentTimeMillis()
                println("$currNum (qps=$numNumbersProcessedPerSecond)")
            }

            if (state.prevALU.get(VariableName.Z) == BigInteger.ZERO) {
                TODO("z==0: ${state}, currNum: $currNum")
            }
            return
        }
        val operations = problemContainer.operationsStack[state.idx]

        for (i in 1..9) {
            val currALU = state.prevALU.toMutableALU()
            currALU.inp(VariableName.W, i)
            var successfulOperations = true
            for (operator in operations) {
                val result = currALU.consume(operator, i)
                require(result != OperatorResult.INPUT)
                if (result == OperatorResult.ERROR) {
                    successfulOperations = false
                    break
                }
            }
            if (!successfulOperations) {
                continue
            }
            calculate(
                State(
                    state.idx + 1, currALU.toALU()
                ), problemContainer, currNum * 10 + i
            )
        }

        dp.add(state)
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

        calculate(
            State(
                0, ALU(Array<BigInteger>(4) { BigInteger.ZERO })
            ), problemContainer, 0L
        )
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