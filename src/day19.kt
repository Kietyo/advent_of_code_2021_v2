import kotlin.math.abs
import kotlin.math.max

data class Vector3D(val x: Int, val y: Int, val z: Int) {
    // Rotate 90 degrees (counter clockwise) around the z axis.
    fun rotateAroundZAxis() = Vector3D(y, -x, z)

    // Rotate 90 degrees (counter clockwise) around the y axis.
    fun rotateAroundYAxis() = Vector3D(z, y, -x)

    // Rotate 90 degrees (counter clockwise) around the y axis.
    fun rotateAroundXAxis() = Vector3D(x, z, -y)

    fun flipX() = Vector3D(-x, y, z)
    fun flipY() = Vector3D(x, -y, z)
    fun flipZ() = Vector3D(x, y, -z)

    // Returns a new point with the following config applied to this point.
    fun applyConfig(config: OrientationConfig): Vector3D {
        var curr = this
        repeat(config.numXRotations) {
            curr = curr.rotateAroundXAxis()
        }
        repeat(config.numYRotations) {
            curr = curr.rotateAroundYAxis()
        }
        repeat(config.numZRotations) {
            curr = curr.rotateAroundZAxis()
        }
        repeat(config.numXFlips) {
            curr = curr.flipX()
        }
        repeat(config.numYFlips) {
            curr = curr.flipY()
        }
        return curr
    }
}

fun <E1, E2> Iterable<E1>.cross(list2: Iterable<E2>): List<Pair<E1, E2>> {
    val cross = mutableListOf<Pair<E1, E2>>()
    for (e1 in this) {
        for (e2 in list2) {
            cross.add(Pair(e1, e2))
        }
    }
    return cross
}

data class OverlappingResults(
    val destinationScanner: ScannerData,
    val sourceScanner: ScannerData,
    // The orientation used in the source scanner which resulted in the destination scanner.
    val orientationIdxUsedForSource: Int,
    // Corresponds to the offsets used to translate the source points to the destination points
    // which resulted in at least 12 point overlaps.
    val offsets: Triple<Int, Int, Int>,
) {
    // The orientation config to translate scanner 2 to scanner 1 coordinates
    val orientationConfig = sourceScanner.orientationConfigs[orientationIdxUsedForSource]
    val translationConfig = TranslationConfig(
        sourceScanner.id,
        destinationScanner.id,
        listOf(orientationConfig),
        listOf(offsets)
    )

    init {
        require(getOverlappingPointsRelativeToDestination().size == 12)
    }

    fun getOverlappingPointsRelativeToDestination(): List<Vector3D> {
        return destinationScanner.initialOrientation.intersect(
            sourceScanner.allUniqueOrientations[orientationIdxUsedForSource].map {
                Vector3D(
                    it.x + offsets.first,
                    it.y + offsets.second,
                    it.z + offsets.third
                )
            }.toSet()
        ).toList()
    }
}

data class OrientationConfig(
    val numXRotations: Int,
    val numYRotations: Int,
    val numZRotations: Int,
    val numXFlips: Int,
    val numYFlips: Int
)

// Stores data needed to translate from source scanner points to
// destination scanner point coordinates.
data class TranslationConfig(
    val sourceScannerId: Int,
    val destScannerId: Int,
    // The orientation config describes the rotations/flips needed to turn source points
    // to destination points.
    val orientationConfigs: List<OrientationConfig>,
    val offsets: List<Triple<Int, Int, Int>>
) {
    init {
        require(sourceScannerId != destScannerId)
    }

    fun translate(scannerData: ScannerData): List<Vector3D> {
        if (scannerData.id == destScannerId) {
            return scannerData.initialOrientation
        }
        require(scannerData.id == sourceScannerId)
        return translate(scannerData.initialOrientation)
    }

    fun translate(scanner2Points: List<Vector3D>): List<Vector3D> {
        return scanner2Points.map {
            orientationConfigs.zip(offsets).fold(it) { acc, pair ->
                val offset = pair.second
                val orientedPoint = acc.applyConfig(pair.first)
                Vector3D(
                    orientedPoint.x + offset.first,
                    orientedPoint.y + offset.second,
                    orientedPoint.z + offset.third
                )
            }
        }
    }

    // Combines this (1 -> 2) with other translation config (2 -> 3)
    // to get the translation config for (1->3)
    fun combine(other: TranslationConfig): TranslationConfig {
        require(destScannerId == other.sourceScannerId)
        require(sourceScannerId != other.destScannerId)
        return TranslationConfig(
            sourceScannerId,
            other.destScannerId,
            orientationConfigs + other.orientationConfigs,
            offsets + other.offsets
        )
    }
}

data class ScannerData(
    val id: Int,
    val points: List<Vector3D>
) {
    val initialOrientation = points
    val allUniqueOrientations: List<List<Vector3D>>
    val orientationConfigs: List<OrientationConfig>

    init {
        val orientedPointsSet = mutableSetOf<List<Vector3D>>()
        val orientedPointsList = mutableListOf<List<Vector3D>>()
        val orientationConfigs = mutableListOf<OrientationConfig>()
        for (xRotation in 0 until 4) {
            for (yRotation in 0 until 4) {
                for (zRotation in 0 until 4) {
                    for (xFlips in 0 until 1) {
                        for (yFlips in 0 until 1) {
                            val orientedPoints = points.map {
                                it.applyConfig(
                                    OrientationConfig(
                                        xRotation,
                                        yRotation,
                                        zRotation,
                                        xFlips,
                                        yFlips
                                    )
                                )
                            }
                            if (!orientedPointsSet.contains(orientedPoints)) {
                                orientedPointsSet.add(
                                    orientedPoints
                                )
                                orientedPointsList.add(orientedPoints)
                                orientationConfigs.add(
                                    OrientationConfig(
                                        xRotation,
                                        yRotation,
                                        zRotation,
                                        xFlips,
                                        yFlips
                                    )
                                )
                            }

                        }
                    }
                }
            }
        }

        this.allUniqueOrientations = orientedPointsList
        this.orientationConfigs = orientationConfigs

        println(
            """
            allUniqueOrientations: $allUniqueOrientations
            allUniqueOrientations.size: ${allUniqueOrientations.size}
            orientationConfigs: $orientationConfigs
        """.trimIndent()
        )
    }

    fun getOverlapping(other: ScannerData): OverlappingResults? {
        for ((idx, orientedPoints) in other.allUniqueOrientations.withIndex()) {
            val xDiffCandidates =
                points.map { it.x }.cross(orientedPoints.map { it.x }).map { it.first - it.second }
                    .groupingBy { it }.eachCount().entries.sortedBy { it.value }
            val yDiffCandidates =
                points.map { it.y }.cross(orientedPoints.map { it.y }).map { it.first - it.second }
                    .groupingBy { it }.eachCount().entries.sortedBy { it.value }
            val zDiffCandidates =
                points.map { it.z }.cross(orientedPoints.map { it.z }).map { it.first - it.second }
                    .groupingBy { it }.eachCount().entries.sortedBy { it.value }

            val highestXDiff = xDiffCandidates.last()
            val highestYDiff = yDiffCandidates.last()
            val highestZDiff = zDiffCandidates.last()

            println(
                """
                Checking orientation $idx. highestXDiff: $highestXDiff, highestYDiff: $highestYDiff, highestZDiff: $highestZDiff
            """.trimIndent()
            )

            if (highestXDiff.value >= 12 && highestYDiff.value >= 12 && highestZDiff.value >= 12) {
                return OverlappingResults(
                    this,
                    other,
                    idx,
                    Triple(highestXDiff.key, highestYDiff.key, highestZDiff.key)
                )
            }
        }
        return null
    }

    fun getTranslatedPoints(
        translations: List<TranslationConfig>,
        destScannerId: Int
    ): List<Vector3D> {
        if (id == destScannerId) {
            return initialOrientation
        }

        return translations.first {
            it.sourceScannerId == id && it.destScannerId == destScannerId
        }.translate(initialOrientation)
    }

    fun getTranslatedScannerPoint(
        translations: List<TranslationConfig>,
        destScannerId: Int
    ): Vector3D {
        if (id == destScannerId) {
            return Vector3D(0, 0, 0)
        }
        return translations.first {
            it.sourceScannerId == id && it.destScannerId == destScannerId
        }.translate(listOf(Vector3D(0, 0, 0))).first()
    }
}

fun main() {

    fun String.toVector3D(): Vector3D {
        val split = this.split(",")
        require(split.size == 3)
        return Vector3D(split[0].toInt(), split[1].toInt(), split[2].toInt())
    }

    fun part1(inputs: List<String>) {
        val scannerData = buildList {
            val itr = inputs.iterator()
            var scannerIdx = -1
            while (itr.hasNext()) {
                val curr = itr.next()
                if (curr.contains("scanner")) {
                    scannerIdx++
                    val points = mutableListOf<Vector3D>()
                    while (itr.hasNext()) {
                        val dataInput = itr.next()
                        if (dataInput.isEmpty()) {
                            break
                        }
                        points.add(dataInput.toVector3D())
                    }
                    add(ScannerData(scannerIdx, points.toSet().toList()))
                }
            }
        }

        val overlappingResults = buildList {
            for (scanner1 in scannerData) {
                for (scanner2 in scannerData) {
                    if (scanner1 === scanner2) {
                        continue
                    }
                    val overlapping = scanner1.getOverlapping(scanner2)
                    if (overlapping != null) {
                        add(overlapping)
                    }
                }
            }
        }

        println(overlappingResults.joinToString("\n") {
            "${it.sourceScanner.id} to ${it.destinationScanner.id}, orientationIdxForScanner2: " +
                    "${it.orientationIdxUsedForSource} offsets: ${it.offsets}"
        })

        val translationConfigs = mutableListOf<TranslationConfig>()
        val coveredTranslations = mutableSetOf<Pair<Int, Int>>()
        for (result in overlappingResults) {
            val currentTranslationConfig = result.translationConfig
            translationConfigs.add(currentTranslationConfig)
            coveredTranslations.add(
                Pair(
                    currentTranslationConfig.sourceScannerId,
                    currentTranslationConfig.destScannerId
                )
            )
        }

        // Really inefficient and lazy way to get all the translations from
        // source scanners to dest scanners
        while (true) {
            val currentSize = coveredTranslations.size
            for (config1 in translationConfigs.toList()) {
                for (config2 in translationConfigs.toList()) {
                    if (config1.destScannerId == config2.sourceScannerId &&
                        config1.sourceScannerId != config2.destScannerId &&
                        !coveredTranslations.contains(
                            Pair(
                                config1.sourceScannerId, config2.destScannerId
                            )
                        )
                    ) {
                        translationConfigs.add(config1.combine(config2))
                        coveredTranslations.add(
                            Pair(
                                config1.sourceScannerId, config2.destScannerId
                            )
                        )
                    }
                }
            }
            var newSize = coveredTranslations.size
            if (currentSize == newSize) {
                // No more changes!
                break
            }
        }

        println(translationConfigs.joinToString("\n"))

        println(coveredTranslations.sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy {
            it
                .second
        }).joinToString("\n"))

        var allBeaconPointsInScannerZeroCoords = buildSet<Vector3D> {
            scannerData.forEach {
                addAll(it.getTranslatedPoints(translationConfigs, 0))
            }
        }

        println("beaconPointsInScannerZeroCoords: $allBeaconPointsInScannerZeroCoords")
        println(allBeaconPointsInScannerZeroCoords.size)

        var largestManhattanDistance = Int.MIN_VALUE
        for (scanner1 in scannerData) {
            val translated1 = scanner1.getTranslatedScannerPoint(translationConfigs, 0)

            for (scanner2 in scannerData) {
                val translated2 = scanner2.getTranslatedScannerPoint(translationConfigs, 0)

                val curr =
                    abs(translated2.x - translated1.x) +
                            abs(translated2.y - translated1.y) +
                            abs(translated2.z - translated1.z)
                largestManhattanDistance = max(largestManhattanDistance, curr)
            }
        }

        println("largestManhattanDistance: $largestManhattanDistance")
    }

    fun part2(inputs: List<String>) {

    }

    val testInput = readInput("day19_test")
    val mainInput = readInput("day19")


    //    part1(testInput)
    part1(mainInput)
    //
    //    part2(testInput)
    //    part2(mainInput)

}