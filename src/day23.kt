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

data class AnthroPath(
    // The anthro that moved
    val anthro: Anthro,
    // A path of (x,y) coordinates which the anthro moves.
    val path: List<Pair<Int, Int>>
)

data class PathSequence(
    val paths: List<AnthroPath>
)

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

    fun isInHallway(pos: Pair<Int, Int>): Boolean {
        return pos.second == 0
    }

    fun isSpotEmpty(pos: Pair<Int, Int>): Boolean {
        return !anthros.any { it.value == pos }
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

    fun getNextStatesForAnthro(anthro: Anthro): List<Pair<Int, Int>> {
        val pos = anthros[anthro]!!
        val (topDesired, bottomDesired) = getDesiredRoomsForAnthro(anthro)
        val topSprite = getSpriteAt(topDesired)
        val bottomSprite = getSpriteAt(bottomDesired)

        if (pos == bottomDesired) {
            return emptyList()
        }

        if (pos == topDesired) {
            if (bottomSprite == anthro.sprite) {
                return emptyList()
            }

            TODO("have to go to the hallway if the bottom sprite is not the same as this")
        }

        // If it's in the hallway, then it's just waiting for the next available room
        if (isInHallway(pos)) {
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

    part1(testInput)
    //    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)


}