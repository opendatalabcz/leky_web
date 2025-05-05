package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdSubstanceSynonym
import cz.machovec.lekovyportal.processor.mapper.BaseSimpleRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate

enum class MpdSubstanceSynonymColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    SUBSTANCE(listOf("KOD_LATKY")),
    SEQUENCE(listOf("SQ"), required = false),
    SOURCE(listOf("ZDROJ")),
    NAME(listOf("NAZEV"), required = false);
}

class MpdSubstanceSynonymRowMapper(
    private val validFrom: LocalDate,
    private val refProvider: MpdReferenceDataProvider
) : BaseSimpleRowMapper<MpdSubstanceSynonymColumn, MpdSubstanceSynonym>() {

    override fun map(row: CsvRow<MpdSubstanceSynonymColumn>, rawLine: String): RowMappingResult<MpdSubstanceSynonym> {

        /* ---------- mandatory attributes ---------- */
        val substanceCode = row[MpdSubstanceSynonymColumn.SUBSTANCE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdSubstanceSynonymColumn.SUBSTANCE.name, rawLine)
            )
        val substance = refProvider.getSubstances()[substanceCode]
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdSubstanceSynonymColumn.SUBSTANCE.name, rawLine)
            )

        val sourceCode = row[MpdSubstanceSynonymColumn.SOURCE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdSubstanceSynonymColumn.SOURCE.name, rawLine)
            )
        val source = refProvider.getSources()[sourceCode]
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdSubstanceSynonymColumn.SOURCE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val sequenceNumber = row[MpdSubstanceSynonymColumn.SEQUENCE]?.trim()?.toIntOrNull()
        val name = row[MpdSubstanceSynonymColumn.NAME].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdSubstanceSynonym(
            firstSeen = validFrom,
            missingSince = null,
            substance = substance,
            sequenceNumber = sequenceNumber,
            source = source,
            name = name
        )

        return RowMappingResult.Success(entity)
    }
}
