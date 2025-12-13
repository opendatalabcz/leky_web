package cz.machovec.lekovyportal.processor.mapper

class MutableImportStats {

    var totalRows: Long = 0
        private set

    var successCount: Long = 0
        private set

    private val failuresByReason =
        mutableMapOf<FailureReason, Long>()

    private val failuresByReasonAndColumn =
        mutableMapOf<Pair<FailureReason, String?>, Long>()

    val totalFailures: Long
        get() = totalRows - successCount

    fun successRatePercent(): Long =
        if (totalRows == 0L) 100 else (successCount * 100 / totalRows)

    fun recordSuccess() {
        totalRows++
        successCount++
    }

    fun recordFailure(f: RowFailure) {
        totalRows++
        failuresByReason.merge(f.reason, 1, Long::plus)
        failuresByReasonAndColumn.merge(
            f.reason to f.column,
            1,
            Long::plus
        )
    }

    /** Read-only views for logging */
    fun failuresByReason(): Map<FailureReason, Long> =
        failuresByReason

    fun failuresByReasonAndColumn():
            Map<Pair<FailureReason, String?>, Long> =
        failuresByReasonAndColumn
}
