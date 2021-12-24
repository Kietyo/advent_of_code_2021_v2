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

        fun get(a: Token.Variable): LongStore {
            return vars[a.name]!!
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
        println(inputs)
        val alu = ALU()


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
            for (input in inputs) {
                when {
                    input.startsWith("inp") -> {
                        val varName = input.last()
                        alu.inp(varName.toToken(), digitItr.next())
                    }
                    input.startsWith("mul") -> {
                        val (_, varName, numOrVar) = input.split(" ")
                        alu.mul(varName.toVarToken(), numOrVar.toToken())
                    }
                    input.startsWith("add") -> {
                        val (_, varName, numOrVar) = input.split(" ")
                        alu.add(varName.toVarToken(), numOrVar.toToken())
                    }
                    input.startsWith("div") -> {
                        val (_, varName, numOrVar) = input.split(" ")
                        alu.div(varName.toVarToken(), numOrVar.toToken())
                    }
                    input.startsWith("eql") -> {
                        val (_, varName, numOrVar) = input.split(" ")
                        alu.eql(varName.toVarToken(), numOrVar.toToken())
                    }
                    input.startsWith("mod") -> {
                        val (_, varName, numOrVar) = input.split(" ")
                        alu.mod(varName.toVarToken(), numOrVar.toToken())
                    }
                    else -> TODO("unsupported operation: $input")
                }
            }

            //            alu.print()

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