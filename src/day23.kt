import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.math.sign

enum class Sprite {
    EMPTY,
    A,
    B,
    C,
    D,
}

val anthroCounter = AtomicInteger(0)

data class Anthro(
    val sprite: Sprite,
    val id: Int = anthroCounter.getAndIncrement()
)

val EMPTY_HALLWAY = Array(11) { Sprite.EMPTY }
val CORRECT_CONFIG = arrayOf(Sprite.A, Sprite.B, Sprite.C, Sprite.D)
val HALLWAY_SPOTS = listOf(
    0 to 0, 1 to 0, 3 to 0, 5 to 0, 7 to 0, 9 to 0, 10 to 0
)

enum class RoomPosition {
    HALLWAY,
    WRONG_ROOM,
    CORRECT_ROOM
}

data class AnthroPath(
    // The anthro that moved
    val anthro: Anthro,
    // A path of (x,y) coordinates which the anthro moves.
    val path: List<Pair<Int, Int>>
)

fun isInHallway(pos: Pair<Int, Int>): Boolean {
    return pos.second == 0
}

// Returns the path from `from` to `to` but excluding `from`.
fun getPath(from: Pair<Int, Int>, to: Pair<Int, Int>): List<Pair<Int, Int>> {
    val path = mutableListOf<Pair<Int, Int>>()

    // Move up
    (from.second downTo 0).forEach {
        path.add(from.first to it)
    }

    // Move sideways
    val delta = to.first - from.first
    val dx = delta.sign
    for (i in 1..delta.absoluteValue) {
        path.add((from.first + i * dx) to 0)
    }

    // Move down
    (1..to.second).forEach {
        path.add(to.first to it)
    }

    path.removeFirstOrNull()

    return path
}

val spriteToRoomMapping = mapOf(
    Sprite.A to 2,
    Sprite.B to 4,
    Sprite.C to 6,
    Sprite.D to 8,
)

fun getDesiredRoomsForAnthro(anthro: Anthro): List<Pair<Int, Int>> {
    val roomMapping = spriteToRoomMapping[anthro.sprite]!!
    return buildList {
        for (i in 2 downTo 1) {
            add(roomMapping to i)
        }
    }
}

data class AnthrosState(
    // Map of anthros to their current coordinates
    val anthros: Map<Anthro, Pair<Int, Int>>
) {
    fun assign(anthro: Anthro, pos: Pair<Int, Int>): AnthrosState {
        val currPos = anthros[anthro]!!
        if (currPos == pos) {
            return this
        }
        val newState = anthros.toMutableMap()
        newState[anthro] = pos
        return AnthrosState(newState)
    }

    // Returns whether the path is blocked by any sprites
    fun isPathBlocked(path: List<Pair<Int, Int>>): Boolean {
        val currentSpritePositions = anthros.values.toSet()
        return path.any { currentSpritePositions.contains(it) }
    }

    fun getSpriteAt(pos: Pair<Int, Int>): Sprite {
        val anthro = anthros.firstNotNullOfOrNull {
            if (it.value == pos) it else null
        } ?: return Sprite.EMPTY
        return anthro.key.sprite
    }

    fun isGoodPath(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val path = getPath(from, to)
        return !isPathBlocked(path)
    }

    fun getNextStatesForAnthro(anthro: Anthro): List<Pair<Int, Int>> {
        return buildList build@{
            val pos = anthros[anthro]!!

            val desiredSpots = getDesiredRoomsForAnthro(anthro)

            if (isInHallway(pos)) {
                for (desiredSpot in desiredSpots) {
                    val spriteAtDesiredSpot = getSpriteAt(desiredSpot)
                    if (spriteAtDesiredSpot == anthro.sprite) {
                        // Already at the desired spot
                        if (pos == desiredSpot) return@build
                        continue
                    }
                    if (spriteAtDesiredSpot == Sprite.EMPTY) {
                        add(desiredSpot)
                    }
                    break
                }
            } else {
                for (desiredSpot in desiredSpots) {
                    val spriteAtDesiredSpot = getSpriteAt(desiredSpot)
                    if (spriteAtDesiredSpot == anthro.sprite) {
                        if (pos == desiredSpot) return@build
                        continue
                    } else {
                        if (spriteAtDesiredSpot == Sprite.EMPTY) {
                            add(desiredSpot)
                        }
                        break
                    }

                }
                addAll(HALLWAY_SPOTS)
            }
        }
    }

    fun isCorrectConfiguration(): Boolean {
        return getSpriteAt(2 to 2) == Sprite.A &&
                getSpriteAt(4 to 2) == Sprite.B &&
                getSpriteAt(6 to 2) == Sprite.C &&
                getSpriteAt(8 to 2) == Sprite.D &&
                getSpriteAt(2 to 1) == Sprite.A &&
                getSpriteAt(4 to 1) == Sprite.B &&
                getSpriteAt(6 to 1) == Sprite.C &&
                getSpriteAt(8 to 1) == Sprite.D
    }
}

sealed class Day23Result {
    object NotPossible : Day23Result()
    data class GoodResult(
        val pathSequence: List<AnthroPath>,
    ) : Day23Result() {
        val cost = run {
            pathSequence.sumOf {
                when (it.anthro.sprite) {
                    Sprite.EMPTY -> TODO()
                    Sprite.A -> 1
                    Sprite.B -> 10
                    Sprite.C -> 100
                    Sprite.D -> 1000
                } * it.path.size
            }
        }
    }
}


val dp = mutableMapOf<AnthrosState, Day23Result>()

fun solveProblem(state: AnthrosState, currPath: List<AnthroPath>, depth: Int = 0): Day23Result {
    if (dp.containsKey(state)) return dp[state]!!

    if (state.isCorrectConfiguration()) {
        val res = Day23Result.GoodResult(currPath)
        dp[state] = res
        return res
    }

    var bestSolution: Day23Result = Day23Result.NotPossible
    var lowestCost = Int.MAX_VALUE
    for (anthro in state.anthros) {
        val nextPositions = state.getNextStatesForAnthro(anthro.key)
        for (nextPos in nextPositions) {
            val nextPath = getPath(anthro.value, nextPos)
            if (!state.isPathBlocked(nextPath)) {
                val res = solveProblem(
                    state.assign(anthro.key, nextPos),
                    emptyList(),
                    depth + 1
                )
                when (res) {
                    is Day23Result.GoodResult -> {
                        val newGoodRes = Day23Result.GoodResult(
                            currPath + AnthroPath(anthro.key, nextPath) + res.pathSequence
                        )
                        if (newGoodRes.cost < lowestCost) {
                            lowestCost = newGoodRes.cost
                            bestSolution = newGoodRes
                        }
                    }
                    Day23Result.NotPossible -> continue
                }
            }
        }
    }

    dp[state] = bestSolution
    return bestSolution
}

fun main() {
    fun part1(inputs: List<String>) {
        val testMap = AnthrosState(
            mutableMapOf(
                // Top rooms
                Anthro(Sprite.B) to (2 to 1),
                Anthro(Sprite.C) to (4 to 1),
                Anthro(Sprite.B) to (6 to 1),
                Anthro(Sprite.D) to (8 to 1),

                // Bottom rooms
                Anthro(Sprite.A) to (2 to 2),
                Anthro(Sprite.D) to (4 to 2),
                Anthro(Sprite.C) to (6 to 2),
                Anthro(Sprite.A) to (8 to 2),
            )
        )

        val problemMap = AnthrosState(
            mutableMapOf(
                // Top rooms
                Anthro(Sprite.D) to (2 to 1),
                Anthro(Sprite.C) to (4 to 1),
                Anthro(Sprite.D) to (6 to 1),
                Anthro(Sprite.B) to (8 to 1),

                // Bottom rooms
                Anthro(Sprite.C) to (2 to 2),
                Anthro(Sprite.A) to (4 to 2),
                Anthro(Sprite.A) to (6 to 2),
                Anthro(Sprite.B) to (8 to 2),
            )
        )

        val res = solveProblem(testMap, emptyList())
        when (res) {
            is Day23Result.GoodResult -> {
                println(
                    """
                    res: $res
                    cost: ${res.cost}
                """.trimIndent()
                )

                println(res.pathSequence.joinToString("\n"))
            }
            Day23Result.NotPossible -> {
                println("no solution found")
            }
        }

        val correctMap = AnthrosState(
            mutableMapOf(
                // Top rooms
                Anthro(Sprite.A) to (2 to 1),
                Anthro(Sprite.B) to (4 to 1),
                Anthro(Sprite.C) to (6 to 1),
                Anthro(Sprite.D) to (8 to 1),

                // Bottom rooms
                Anthro(Sprite.A) to (2 to 2),
                Anthro(Sprite.B) to (4 to 2),
                Anthro(Sprite.C) to (6 to 2),
                Anthro(Sprite.D) to (8 to 2),
            )
        )
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day23_test")
    val mainInput = readInput("day23")

    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)

}