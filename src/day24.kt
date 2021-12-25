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

class LongStore(var num: Long)

enum class ConsumeResult {
    STANDARD,
    INPUT;
}

fun main() {


    class ALU {
        val vars = buildMap<String, LongStore> {
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

        fun consume(input: String, inputDigit: Int): ConsumeResult {
            when {
                input.startsWith("inp") -> {
                    val varName = input.last()
                    this.inp(varName.toToken(), inputDigit)
                    return ConsumeResult.INPUT
                }
                input.startsWith("mul") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    this.mul(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("add") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    this.add(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("div") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    this.div(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("eql") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    this.eql(varName.toVarToken(), numOrVar.toToken())
                }
                input.startsWith("mod") -> {
                    val (_, varName, numOrVar) = input.split(" ")
                    this.mod(varName.toVarToken(), numOrVar.toToken())
                }
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
        val highestNumber = 99999996452482
        val modelNumLength = 14
        println(highestNumber.toString().length)
        println(inputs)

        val digitStack = mutableListOf<Int>()
        val aluStack = mutableListOf<ALU>()
        val operationsStack = mutableListOf<List<String>>()

        var alu = ALU()
        val currOperations = mutableListOf<String>()
        for (input in inputs) {
            val result = alu.consume(input, 9)
            when (result) {
                ConsumeResult.STANDARD -> {
                    currOperations.add(input)
                }
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
            operationsStack (size=${operationsStack.size}): $operationsStack
            currOperations: $currOperations
        """.trimIndent()
        )

        //        while (true) {
        //            println("curr num: " + digitStack.joinToString(""))
        //            if (alu.get("z".toVarToken()).num == 0L) break
        //            val currDigit = digitStack.removeLast()
        //            if (currDigit == 1) {
        //
        //            }
        //        }

        //        repeat(modelNumLength) {
        //            digitStack.add(9)
        //
        //        }
        //
        //        // initialize alu stack
        //        for (digit in digitStack) {
        //
        //        }
        //
        //
        for (currNum in highestNumber downTo 0L) {
            alu.reset()
            val asString = currNum.toString()
            if (asString.contains("0")) continue
            println(asString)
            val digitItr = asString.map {
                it.digitToInt().apply {
                    require(this in 1..9)
                }
            }.iterator()
            var currDigit = digitItr.next()
            for (input in inputs) {
                when (alu.consume(input, currDigit)) {
                    ConsumeResult.STANDARD -> continue
                    ConsumeResult.INPUT -> {
                        if (digitItr.hasNext()) {
                            currDigit = digitItr.next()
                        }
                    }
                }
            }
            if (alu.get("z".toVarToken()).num == 0L) break
        }

        //
        //        alu.inp("x".toToken())
        //
        //        alu.add(Token.Variable("x"), Token.Number(9))
        //
        //        alu.div(Token.Variable("x"), Token.Number(4))
        //
        //        println(alu.get(Token.Variable("x")).num)
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

    //        part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)
}