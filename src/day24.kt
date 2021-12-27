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

sealed class Operation {
    data class Input(val name: VariableName, var digit: Int?) : Operation()
    data class Mul(val name: VariableName, val token: Token) : Operation()
    data class Add(val name: VariableName, val token: Token) : Operation()
    data class Div(val name: VariableName, val token: Token) : Operation()
    data class Eql(val name: VariableName, val token: Token) : Operation()
    data class Mod(val name: VariableName, val token: Token) : Operation()
}

data class ALU(
    private val vars: Array<Long>
) {
    fun get(a: VariableName): Long {
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
    val vars: Array<Long>
) {
    fun toALU(): ALU {
        return ALU(vars.clone())
    }

    fun get(a: VariableName): Long {
        return vars[a.ordinal]
    }

    fun consume(input: Operation, inputDigit: Int) {
        when (input) {
            is Operation.Input -> {
                this.inp(input.name, inputDigit)
            }
            is Operation.Add -> this.add(input.name, input.token)
            is Operation.Div -> this.div(input.name, input.token)
            is Operation.Eql -> this.eql(input.name, input.token)
            is Operation.Mod -> this.mod(input.name, input.token)
            is Operation.Mul -> this.mul(input.name, input.token)
            else -> TODO("unsupported operation: $input")
        }
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
            is Token.Number -> b.num.toLong()
            is Token.Variable -> get(b.name)
        }
    }

    fun mul(a: VariableName, b: Token) {
        vars[a.ordinal] = vars[a.ordinal]!! * when (b) {
            is Token.Number -> b.num.toLong()
            is Token.Variable -> get(b.name)
        }
    }

    fun div(a: VariableName, b: Token) {
        val denom = when (b) {
            is Token.Number -> b.num.toLong()
            is Token.Variable -> get(b.name)
        }
        require(denom != 0L)
        vars[a.ordinal] = vars[a.ordinal]!! / denom
    }

    fun mod(a: VariableName, b: Token) {
        val aVal = vars[a.ordinal]!!
        if (aVal < 0L) {
        }
        require(aVal >= 0L)
        val bVal = when (b) {
            is Token.Number -> b.num.toLong()
            is Token.Variable -> get(b.name)
        }
        require(bVal > 0L)
        vars[a.ordinal] = aVal % bVal
    }

    fun eql(a: VariableName, b: Token) {
        val otherNum = when (b) {
            is Token.Number -> b.num.toLong()
            is Token.Variable -> get(b.name)
        }
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

    fun calculate(state: State, operations: List<List<Operation>>, currNum: Long) {
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

            if (state.prevALU.get(VariableName.Z) == 0L) {
                TODO("FOUND z==0: ${state}, currNum: $currNum")
            }
            dp.add(state)
            return
        }
        for (i in 9 downTo 1) {
            val currALU = state.prevALU.toMutableALU()
            currALU.inp(VariableName.W, i)
            for (operator in operations[state.idx]) {
                currALU.consume(operator, i)
            }
            calculate(
                State(
                    state.idx + 1, currALU.toALU()
                ), operations, currNum * 10 + i
            )
        }

        dp.add(state)
    }

    fun part1(inputs: List<String>) {
        val allOperations = inputs.map { input ->
            when {
                input.startsWith("inp") -> {
                    val varName = input.last()
                    Operation.Input(varName.toString().toVariableName(), null)
                }
                input.startsWith("mul") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operation.Mul(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("add") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operation.Add(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("div") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operation.Div(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("eql") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operation.Eql(varName.toVariableName(), numOrVar.toToken())
                }
                input.startsWith("mod") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    Operation.Mod(varName.toVariableName(), numOrVar.toToken())
                }
                else -> TODO()
            }
        }

        val operations = mutableListOf<List<Operation>>()
        val currOperations = mutableListOf<Operation>()
        for (input in allOperations) {
            when (input) {
                is Operation.Input -> {
                    operations.add(currOperations.toList())
                    currOperations.clear()
                }
                is Operation.Add,
                is Operation.Div,
                is Operation.Eql,
                is Operation.Mod,
                is Operation.Mul -> {
                    currOperations.add(input)
                }
            }
        }
        operations.add(currOperations.toList())

        // The first list of operations will be empty since there are no operations
        // before the first input.
        operations.removeFirst()

        require(
            operations.size == 14
        )

        println(
            """
        operationsStack (size=${operations.size}): ${operations.joinToString("\n")}
        currOperations: $currOperations
        """.trimIndent()
        )

        calculate(
            State(
                0, ALU(Array<Long>(4) { 0L })
            ), operations, 0L
        )
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day24_test")
    val mainInput = readInput("day24")

    println(Long.MAX_VALUE)

    //            part1(testInput)
    part1(mainInput)

    //
    //    part2(testInput)
    //    part2(mainInput)

}