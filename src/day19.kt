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
    // The orientation in scanner 2 that resulted in overlaps with scanner 2
    val orientationIdxForScanner2: Int,
    // Corresponds to the offsets which resulted in the most amount of point overlaps between
    // scanner 1 and scanner 2
    val offsets: Triple<Int, Int, Int>,
) {
    // The orientation config to translate scanner 2 to scanner 1 coordinates
    val orientationConfig = sourceScanner.orientationConfigs[orientationIdxForScanner2]
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
            sourceScanner.allUniqueOrientations[orientationIdxForScanner2].map {
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

    // scannerIdCoord: The id of the scanner coordinate to use when translating
    fun getOverlappingUsingMap(
        other: ScannerData, translations: List<TranslationConfig>,
        scannerIdCoord: Int
    ): Set<Vector3D> {
        val translatedPoints = if (id == scannerIdCoord) {
            initialOrientation
        } else {
            val translation = translations.first {
                it.sourceScannerId == id && it.destScannerId == scannerIdCoord
            }
            translation.translate(initialOrientation)
        }

        val translatedOtherPoints = if (other.id == scannerIdCoord) {
            initialOrientation
        } else {
            val translation = translations.first {
                it.sourceScannerId == other.id && it.destScannerId == scannerIdCoord
            }
            translation.translate(other.initialOrientation)
        }

        return translatedPoints.toSet().intersect(
            translatedOtherPoints.toSet()
        )
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
}

fun main() {

    fun String.toVector3D(): Vector3D {
        val split = this.split(",")
        require(split.size == 3)
        return Vector3D(split[0].toInt(), split[1].toInt(), split[2].toInt())
    }

    fun part1(inputs: List<String>) {
        val scannerData = buildList<ScannerData> {
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

        //                val scanner1 = scannerData[0]
        //                val scanner4 = scannerData[2]
        //
        //                scanner4.getOverlapping(scanner1)!!.getOverlappingPointsRelativeToScanner1()
        //                    .forEach {
        //                    println(it)
        //                }
        //
        //                println(scannerData.joinToString("\n") { scannerData ->
        //                    "Scanner Data ${scannerData.id}:\n" + scannerData.points.joinToString("\n") {
        //                        "\t${it}"
        //                    }
        //                })

        var overlappingResults = buildList<OverlappingResults> {
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
                    "${it.orientationIdxForScanner2} offsets: ${it.offsets}"
        })

        val firstResult = overlappingResults.first()

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

            //            translationConfigs.filter {
            //                it.destScannerId == currentTranslationConfig.sourceScannerId
            //                        && it.sourceScannerId != currentTranslationConfig.destScannerId
            //                        && !coveredTranslations.contains(
            //                    Pair(
            //                        it.sourceScannerId,
            //                        currentTranslationConfig.destScannerId
            //                    )
            //                )
            //            }.forEach {
            //                val newConfig = it.combine(currentTranslationConfig)
            //                translationConfigs.add(
            //                    newConfig
            //                )
            //                coveredTranslations.add(Pair(newConfig.sourceScannerId, newConfig.destScannerId))
            //            }
            //
            //            translationConfigs.filter {
            //                currentTranslationConfig.destScannerId == it.sourceScannerId
            //                        && currentTranslationConfig.sourceScannerId != it.destScannerId &&
            //
            //            }.forEach {
            //                val newConfig = currentTranslationConfig.combine(it)
            //                translationConfigs.add(
            //                    newConfig
            //                )
            //                coveredTranslations.add(Pair(newConfig.sourceScannerId, newConfig.destScannerId))
            //            }
        }

        while (true) {
            var currrentSize = coveredTranslations.size
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
            if (currrentSize == newSize) {
                // No more changes!
                break
            }
        }

        println(translationConfigs.joinToString("\n"))

        println(coveredTranslations.sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy {
            it
                .second
        }).joinToString("\n"))


        val overlappingPointsInScannerZeroConfig = overlappingResults.flatMap { result ->
            val overlappingPoints = result.getOverlappingPointsRelativeToDestination()
            if (result.destinationScanner.id == 0) {
                overlappingPoints
            } else {
                val translation = translationConfigs.first {
                    it.sourceScannerId == result.destinationScanner.id &&
                            it.destScannerId == 0
                }
                translation.translate(overlappingPoints)
            }
        }.toSet()

        println(overlappingPointsInScannerZeroConfig)

        println(overlappingPointsInScannerZeroConfig.size)

        var results2 = mutableSetOf<Vector3D>()

        for (scanner1 in scannerData) {
            val translated = if (scanner1.id == 0) {
                scanner1.initialOrientation
            } else {
                val translation = translationConfigs.first {
                    it.sourceScannerId == scanner1.id && it.destScannerId == 0
                }
                translation.translate(scanner1.initialOrientation)
            }
            results2.addAll(translated)
        }

        println("results2: $results2")
        println(results2.size)


        //        val firstScanner = scannerData[0]
        //        val secondScanner = scannerData[1]
        //
        //        val overlapping = firstScanner.getOverlapping(secondScanner)
        //
        //        println(
        //            """
        //            overlapping: $overlapping
        //        """.trimIndent()
        //        )
        //        println()
        //
        //        println("overlapping points")
        //        println(overlapping.getOverlappingPointsRelativeToScanner1().joinToString("\n"))

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