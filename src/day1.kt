fun main() {
    fun countNumIncreasing(input: List<String>) {
        println(input)
        println(input.size)
        var numIncreasing = 0
        for (i in 1 until input.size) {
            val num1 = input[i].toInt()
            val num2 = input[i - 1].toInt()
            println("$i: ${input[i]}")
            if (num1 > num2) {
                numIncreasing++
            }
        }
        println(numIncreasing)
    }

    data class Window(val nums: MutableList<Int> = mutableListOf()) {
        val sum: Int
            get() = nums.sum()

        val size: Int
            get() = nums.size

        fun add(v: Int) {
            nums.add(v)
        }
    }

    fun countNumSlidingWindowIncreasing(input: List<String>) {
        println(input)
        println(input.size)
        val windows = mutableListOf<Window>()
        var numIncreasing = 0
        for (i in 0 until input.size) {
            val num = input[i].toInt()

            for (window in windows) {
                if (window.size < 3) {
                    window.add(num)
                }
            }

            val newWindow = Window()
            newWindow.add(num)
            windows.add(newWindow)

            println("$i: ${num}")
        }

        for (window in windows) {
            println(window)
        }

        for (i in 1 until windows.size) {
            val window1 = windows[i]
            val window2 = windows[i - 1]
            if (window1.size == 3 && window2.size == 3) {
                if (window1.sum > window2.sum) {
                    numIncreasing++

                }
            }
        }
        println(numIncreasing)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day1_test")
    val input = readInput("day1")

//    println(countNumIncreasing(testInput))
//    println(countNumIncreasing(input))

    println(countNumSlidingWindowIncreasing(testInput))
    println(countNumSlidingWindowIncreasing(input))
}
