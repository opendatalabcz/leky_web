package cz.machovec.lekovyportal.importer.mapper

sealed interface RowMappingResult<out T> {
    data class Success<T>(val entity: T) : RowMappingResult<T>
    data class Failure(val failure: RowFailure) : RowMappingResult<Nothing>
}
