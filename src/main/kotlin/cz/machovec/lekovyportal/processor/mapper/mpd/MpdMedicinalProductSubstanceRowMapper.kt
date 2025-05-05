package cz.machovec.lekovyportal.processor.mapper.mpd

import cz.machovec.lekovyportal.core.domain.mpd.MpdMedicinalProductSubstance
import cz.machovec.lekovyportal.processor.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.processor.mapper.ColumnAlias
import cz.machovec.lekovyportal.processor.mapper.CsvRow
import cz.machovec.lekovyportal.processor.mapper.FailureReason
import cz.machovec.lekovyportal.processor.mapper.RowFailure
import cz.machovec.lekovyportal.processor.mapper.RowMappingResult
import cz.machovec.lekovyportal.processor.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate

enum class MpdMedicinalProductSubstanceColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    SUKL_CODE(listOf("KOD_SUKL")),
    SUBSTANCE_CODE(listOf("KOD_LATKY")),
    SEQUENCE_NUMBER(listOf("SQ"), required = false),
    COMPOSITION_FLAG(listOf("S"), required = false),
    AMOUNT_FROM(listOf("AMNT_OD"), required = false),
    AMOUNT_TO(listOf("AMNT"), required = false),
    MEASUREMENT_UNIT(listOf("UN"), required = false)
}

class MpdMedicinalProductSubstanceRowMapper(
    private val validFrom: LocalDate,
    refProvider: MpdReferenceDataProvider
) : BaseRefRowMapper<MpdMedicinalProductSubstanceColumn, MpdMedicinalProductSubstance>(refProvider) {

    override fun map(row: CsvRow<MpdMedicinalProductSubstanceColumn>, rawLine: String): RowMappingResult<MpdMedicinalProductSubstance> {

        /* ---------- mandatory attributes ---------- */
        val suklCode = row[MpdMedicinalProductSubstanceColumn.SUKL_CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdMedicinalProductSubstanceColumn.SUKL_CODE.name, rawLine)
            )

        val substanceCode = row[MpdMedicinalProductSubstanceColumn.SUBSTANCE_CODE].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdMedicinalProductSubstanceColumn.SUBSTANCE_CODE.name, rawLine)
            )

        val medicinalProduct = product(suklCode)
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdMedicinalProductSubstanceColumn.SUKL_CODE.name, rawLine)
            )

        val substance = ref.getSubstances()[substanceCode]
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdMedicinalProductSubstanceColumn.SUBSTANCE_CODE.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val sequenceNumber = row[MpdMedicinalProductSubstanceColumn.SEQUENCE_NUMBER]
            ?.trim()
            ?.toIntOrNull()

        val compositionFlag = row[MpdMedicinalProductSubstanceColumn.COMPOSITION_FLAG]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { ref.getCompositionFlags()[it] }

        val amountFrom = row[MpdMedicinalProductSubstanceColumn.AMOUNT_FROM]
            ?.trim()
            ?.ifBlank { null }

        val amountTo = row[MpdMedicinalProductSubstanceColumn.AMOUNT_TO]
            ?.trim()
            ?.ifBlank { null }

        val measurementUnit = row[MpdMedicinalProductSubstanceColumn.MEASUREMENT_UNIT]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { ref.getMeasurementUnits()[it] }

        /* ---------- entity construction ---------- */
        val entity = MpdMedicinalProductSubstance(
            firstSeen = validFrom,
            missingSince = null,
            medicinalProduct = medicinalProduct,
            substance = substance,
            sequenceNumber = sequenceNumber,
            compositionFlag = compositionFlag,
            amountFrom = amountFrom,
            amountTo = amountTo,
            measurementUnit = measurementUnit
        )

        return RowMappingResult.Success(entity)
    }
}
