package cz.machovec.lekovyportal.importer.mapper

data class DataImportResult<T>(
    val successes : List<T>,
    val failures  : List<RowFailure>,
    val totalRows : Int
) {
    val totalFailures: Int get() = failures.size

    /**
     * Groups failures by [FailureReason] and counts occurrences.
     */
    fun failuresByReason(): Map<FailureReason, Int> =
        failures.groupingBy { it.reason }.eachCount()

    /**
     * Groups failures by (reason + column) and counts occurrences.
     */
    fun failuresByReasonAndColumn(): Map<Pair<FailureReason, String?>, Int> =
        failures.groupingBy { it.reason to it.column }.eachCount()

    /**
     * Calculates success rate (0.0 – 1.0).
     */
    fun successRate(): Double =
        if (totalRows == 0) 1.0 else successes.size.toDouble() / totalRows
}

data class RowFailure(
    val reason : FailureReason,
    val column : String? = null,
    val raw    : String
)

enum class FailureReason {
    MISSING_ATTRIBUTE,
    UNKNOWN_REFERENCE,
    DUPLICATE_KEY,
    PARSE_ERROR
}
