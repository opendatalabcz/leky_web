import cz.machovec.lekovyportal.domain.entity.mpd.MpdRegistrationException
import cz.machovec.lekovyportal.importer.mapper.BaseRefRowMapper
import cz.machovec.lekovyportal.importer.mapper.ColumnAlias
import cz.machovec.lekovyportal.importer.mapper.CsvRow
import cz.machovec.lekovyportal.importer.mapper.FailureReason
import cz.machovec.lekovyportal.importer.mapper.RowFailure
import cz.machovec.lekovyportal.importer.mapper.RowMappingResult
import cz.machovec.lekovyportal.importer.processing.mpd.MpdReferenceDataProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class MpdRegistrationExceptionColumn(
    override val aliases: List<String>,
    override val required: Boolean = true
) : ColumnAlias {
    SUKL_CODE(listOf("KOD_SUKL")),
    VALID_FROM(listOf("DATOD", "DAT_OD")),
    VALID_TO(listOf("DATDO", "DAT_DO"), required = false),
    ALLOWED_PACKAGE_COUNT(listOf("POVOL_BALENI"), required = false),
    PURPOSE(listOf("UCEL"), required = false),
    WORKPLACE(listOf("PRACOVISTE"), required = false),
    DISTRIBUTOR(listOf("DISTRIBUTOR"), required = false),
    NOTE(listOf("POZNAMKA"), required = false),
    SUBMITTER(listOf("PREDKLADATEL"), required = false),
    MANUFACTURER(listOf("VYROBCE"), required = false);
}

class MpdRegistrationExceptionRowMapper(
    private val validFromDate: LocalDate,
    refProvider: MpdReferenceDataProvider
) : BaseRefRowMapper<MpdRegistrationExceptionColumn, MpdRegistrationException>(refProvider) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun map(row: CsvRow<MpdRegistrationExceptionColumn>, rawLine: String): RowMappingResult<MpdRegistrationException> {

        /* ---------- mandatory attributes ---------- */
        val medicinalProduct = product(row[MpdRegistrationExceptionColumn.SUKL_CODE])
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.UNKNOWN_REFERENCE, MpdRegistrationExceptionColumn.SUKL_CODE.name, rawLine)
            )

        val validFromRaw = row[MpdRegistrationExceptionColumn.VALID_FROM].safeTrim()
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.MISSING_ATTRIBUTE, MpdRegistrationExceptionColumn.VALID_FROM.name, rawLine)
            )
        val validFrom = parseDate(validFromRaw)
            ?: return RowMappingResult.Failure(
                RowFailure(FailureReason.INVALID_FORMAT, MpdRegistrationExceptionColumn.VALID_FROM.name, rawLine)
            )

        /* ---------- optional attributes ---------- */
        val validTo = row[MpdRegistrationExceptionColumn.VALID_TO]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { parseDate(it) }

        val allowedPackageCount = row[MpdRegistrationExceptionColumn.ALLOWED_PACKAGE_COUNT]
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.toIntOrNull()

        val purpose = row[MpdRegistrationExceptionColumn.PURPOSE].safeTrim()
        val workplace = row[MpdRegistrationExceptionColumn.WORKPLACE].safeTrim()
        val distributor = row[MpdRegistrationExceptionColumn.DISTRIBUTOR].safeTrim()
        val note = row[MpdRegistrationExceptionColumn.NOTE].safeTrim()
        val submitter = row[MpdRegistrationExceptionColumn.SUBMITTER].safeTrim()
        val manufacturer = row[MpdRegistrationExceptionColumn.MANUFACTURER].safeTrim()

        /* ---------- entity construction ---------- */
        val entity = MpdRegistrationException(
            firstSeen = validFromDate,
            missingSince = null,
            medicinalProduct = medicinalProduct,
            validFrom = validFrom,
            validTo = validTo,
            allowedPackageCount = allowedPackageCount,
            purpose = purpose,
            workplace = workplace,
            distributor = distributor,
            note = note,
            submitter = submitter,
            manufacturer = manufacturer
        )

        return RowMappingResult.Success(entity)
    }

    private fun parseDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value, dateFormatter)
        } catch (e: Exception) {
            null
        }
    }
}
