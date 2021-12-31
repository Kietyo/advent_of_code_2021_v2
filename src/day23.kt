import kotlin.math.absoluteValue
import kotlin.math.sign

enum class Sprite {
    EMPTY,
    A,
    B,
    C,
    D,
}

data class Anthro(
    val sprite: Sprite,
    val id: Int
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

data class PathSequence(
    val paths: List<AnthroPath>
)

fun isInHallway(pos: Pair<Int, Int>): Boolean {
    return pos.second == 0
}

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

    return path
}

data class Anthros(
    // Map of anthros to their current coordinates
    val anthros: Map<Anthro, Pair<Int, Int>>
) {
    val bottomRoomPositions = setOf(
        2 to 2,
        4 to 2,
        6 to 2,
        8 to 2,
    )
    val topRoomPositions = setOf(
        2 to 1,
        4 to 1,
        6 to 1,
        8 to 1,
    )

    val spriteToRoomMapping = mapOf(
        Sprite.A to 2,
        Sprite.B to 4,
        Sprite.C to 6,
        Sprite.D to 8,
    )

    fun isSpotEmpty(pos: Pair<Int, Int>): Boolean {
        return !anthros.any { it.value == pos }
    }

    // Returns whether or not the path is blocked by any sprites
    fun isPathBlocked(path: List<Pair<Int, Int>>): Boolean {
        val currentSpritePositions = anthros.values.toSet()
        return path.any { currentSpritePositions.contains(it) }
    }

    fun getDesiredRoomsForAnthro(anthro: Anthro): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val roomMapping = spriteToRoomMapping[anthro.sprite]!!
        return Pair(
            roomMapping to 1,
            roomMapping to 2,
        )
    }

    fun getAvailableRoomPositionsForAnthro(anthro: Anthro) {
        val roomMapping = spriteToRoomMapping[anthro.sprite]!!
        val topRoomPos = roomMapping to 1
        val bottomRoomPos = roomMapping to 2
    }

    fun getSpriteAt(pos: Pair<Int, Int>): Sprite {
        val anthro = anthros.firstNotNullOfOrNull {
            if (it.value == pos) it else null
        } ?: return Sprite.EMPTY
        return anthro.key.sprite
    }

    fun getRoomPosition(anthro: Anthro): RoomPosition {
        val pos = anthros[anthro]!!
        if (isInHallway(pos)) {
            return RoomPosition.HALLWAY
        }

        val roomMapping = spriteToRoomMapping[anthro.sprite]!!
        if (pos.first == roomMapping) {
            return RoomPosition.CORRECT_ROOM
        }

        return RoomPosition.WRONG_ROOM
    }

    fun getNextStatesForAnthro(anthro: Anthro): List<Pair<Int, Int>> {
        val pos = anthros[anthro]!!
        val (topDesired, bottomDesired) = getDesiredRoomsForAnthro(anthro)
        val topSprite = getSpriteAt(topDesired)
        val bottomSprite = getSpriteAt(bottomDesired)

        val roomPosition = getRoomPosition(anthro)

        when (roomPosition) {
            // If it's in the hallway, then it's just waiting for the next available room
            RoomPosition.HALLWAY -> {
                if (topSprite == Sprite.EMPTY) {
                    if (bottomSprite == Sprite.EMPTY) {
                        return listOf(bottomDesired)
                    }
                    if (bottomSprite == anthro.sprite) {
                        return listOf(topDesired)
                    }
                }
                return emptyList()
            }
            RoomPosition.WRONG_ROOM -> {
                
            }
            RoomPosition.CORRECT_ROOM -> {
                // We've already reached out desired location
                if (pos == bottomDesired) {
                    return emptyList()
                }

                if (pos == topDesired) {
                    // The bottom spot is already taken up by the same sprite, so the next desired location
                    // would be the top spot.
                    if (bottomSprite == anthro.sprite) {
                        return emptyList()
                    }

                    // The bottom spot is empty, so just go to it
                    if (bottomSprite == Sprite.EMPTY) {
                        return listOf(bottomDesired)
                    }

                    // The bottom spot is a different sprite, have to move to the hallway to make
                    // room for the other sprite to move out.
                    return HALLWAY_SPOTS.filter {
                        val path = getPath(pos, it)
                        !isPathBlocked(path)
                    }
                }
            }
        }

        TODO()
    }
}

class GameMap(
    topRoom: Array<Sprite>,
    bottomRoom: Array<Sprite>,
) {
    val map = Array<Array<Sprite>>(11) { Array(3) { Sprite.EMPTY } }

    init {
        for (i in 0 until 4) {
            map[2 + i * 2][1] = topRoom[i]
            map[2 + i * 2][2] = bottomRoom[i]
        }
    }

    val hallway: Array<Sprite>
        get() {
            val arr = Array<Sprite>(11) { Sprite.EMPTY }
            for (i in 0 until 11) {
                arr[i] = map[i][0]
            }
            return arr
        }

    val topRoom: Array<Sprite>
        get() {
            val arr = Array<Sprite>(4) { Sprite.EMPTY }
            for (i in 0 until 4) {
                arr[i] = map[2 + i * 2][1]
            }
            return arr
        }

    val bottomRoom: Array<Sprite>
        get() {
            val arr = Array<Sprite>(4) { Sprite.EMPTY }
            for (i in 0 until 4) {
                arr[i] = map[2 + i * 2][2]
            }
            return arr
        }

    fun isCorrectConfiguration(): Boolean {
        return topRoom.contentEquals(CORRECT_CONFIG) &&
                bottomRoom.contentEquals(CORRECT_CONFIG) &&
                hallway.contentEquals(EMPTY_HALLWAY)
    }
}

fun main() {
    fun part1(inputs: List<String>) {
        val startingMap = GameMap(
            arrayOf(Sprite.B, Sprite.C, Sprite.B, Sprite.D),
            arrayOf(Sprite.A, Sprite.D, Sprite.C, Sprite.A)
        )

        val correctMap = GameMap(
            arrayOf(Sprite.A, Sprite.B, Sprite.C, Sprite.D),
            arrayOf(Sprite.A, Sprite.B, Sprite.C, Sprite.D)
        )

        println(startingMap.isCorrectConfiguration())
        println(correctMap.isCorrectConfiguration())
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day23_test")
    val mainInput = readInput("day23")

    //    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)

    println(getPath(2 to 2, 4 to 1))

}