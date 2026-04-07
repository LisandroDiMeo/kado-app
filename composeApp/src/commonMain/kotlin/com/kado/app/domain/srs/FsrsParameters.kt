package com.kado.app.domain.srs

data class FsrsParameters(
    val weights: DoubleArray = DEFAULT_WEIGHTS,
    val desiredRetention: Double = 0.9,
    val learningStepsSeconds: LongArray = longArrayOf(60L, 600L),
    val relearningStepsSeconds: LongArray = longArrayOf(600L),
    val maximumInterval: Int = 36500,
    val enableFuzzing: Boolean = true,
    val randomSeed: Int = 42
) {
    companion object {
        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069,
            1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191,
            3.0004, 0.7536, 0.3332, 0.1437, 0.2
        )

        val DEFAULT = FsrsParameters()

        fun serialize(params: FsrsParameters): String {
            val learning = params.learningStepsSeconds.joinToString(",")
            val relearning = params.relearningStepsSeconds.joinToString(",")
            return "${params.desiredRetention}|$learning|$relearning|${params.maximumInterval}|${params.enableFuzzing}"
        }

        fun deserialize(raw: String?): FsrsParameters {
            if (raw.isNullOrBlank()) return DEFAULT
            return try {
                val parts = raw.split("|")
                if (parts.size < 5) return DEFAULT
                FsrsParameters(
                    desiredRetention = parts[0].toDouble(),
                    learningStepsSeconds = parts[1].split(",").filter { it.isNotBlank() }.map { it.trim().toLong() }.toLongArray(),
                    relearningStepsSeconds = parts[2].split(",").filter { it.isNotBlank() }.map { it.trim().toLong() }.toLongArray(),
                    maximumInterval = parts[3].toInt(),
                    enableFuzzing = parts[4].toBooleanStrict()
                )
            } catch (_: Exception) {
                DEFAULT
            }
        }

        fun parseStepsString(text: String): LongArray {
            return text.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { token ->
                    when {
                        token.endsWith("h", ignoreCase = true) -> token.dropLast(1).trim().toLongOrNull()?.times(3600)
                        token.endsWith("m", ignoreCase = true) -> token.dropLast(1).trim().toLongOrNull()?.times(60)
                        token.endsWith("s", ignoreCase = true) -> token.dropLast(1).trim().toLongOrNull()
                        else -> token.toLongOrNull()?.times(60)
                    }
                }.toLongArray()
        }

        fun formatStepsString(steps: LongArray): String {
            return steps.joinToString(", ") { seconds ->
                when {
                    seconds >= 3600 && seconds % 3600 == 0L -> "${seconds / 3600}h"
                    seconds >= 60 && seconds % 60 == 0L -> "${seconds / 60}m"
                    else -> "${seconds}s"
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FsrsParameters) return false
        return weights.contentEquals(other.weights) &&
                desiredRetention == other.desiredRetention &&
                learningStepsSeconds.contentEquals(other.learningStepsSeconds) &&
                relearningStepsSeconds.contentEquals(other.relearningStepsSeconds) &&
                maximumInterval == other.maximumInterval &&
                enableFuzzing == other.enableFuzzing &&
                randomSeed == other.randomSeed
    }

    override fun hashCode(): Int {
        var result = weights.contentHashCode()
        result = 31 * result + desiredRetention.hashCode()
        result = 31 * result + learningStepsSeconds.contentHashCode()
        result = 31 * result + relearningStepsSeconds.contentHashCode()
        result = 31 * result + maximumInterval
        result = 31 * result + enableFuzzing.hashCode()
        result = 31 * result + randomSeed
        return result
    }
}
