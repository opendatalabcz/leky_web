package cz.machovec.lekovyportal.processor.mapper

fun interface RowMapper<E, T> where E : Enum<E> {
    fun map(row: CsvRow<E>, rawLine: String): RowMappingResult<T>
}
