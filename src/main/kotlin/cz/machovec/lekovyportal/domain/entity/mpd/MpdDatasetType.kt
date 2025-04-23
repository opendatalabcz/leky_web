package cz.machovec.lekovyportal.domain.entity.mpd

enum class MpdDatasetType(
    val fileName: String,
    val description: String
) {
    MPD_COUNTRY("dlp_zeme.csv", "Country"),
    MPD_ADDICTION_CATEGORY("dlp_zavislost.csv", "Addiction category"),
    MPD_DOPING_CATEGORY("dlp_doping.csv", "Doping category"),
    MPD_GOVERNMENT_REGULATION_CATEGORY("dlp_narvla.csv", "Government regulation category"),
    MPD_SOURCE("dlp_zdroje.csv", "Source"),
    MPD_COMPOSITION_FLAG("dlp_slozenipriznak.csv", "Composition flag"),
    MPD_DISPENSE_TYPE("dlp_vydej.csv", "Dispense type"),
    MPD_MEASUREMENT_UNIT("dlp_jednotky.csv", "Measurement unit"),
    MPD_REGISTRATION_PROCESS("dlp_regproc.csv", "Registration process"),
    MPD_REGISTRATION_STATUS("dlp_stavyreg.csv", "Registration status"),
    MPD_INDICATION_GROUP("dlp_indikacniskupiny.csv", "Indication group"),
    MPD_ATC_GROUP("dlp_atc.csv", "ATC group"),
    MPD_PACKAGE_TYPE("dlp_obaly.csv", "Package type"),
    MPD_ADMINISTRATION_ROUTE("dlp_cesty.csv", "Administration route"),
    MPD_DOSAGE_FORM("dlp_formy.csv", "Dosage form"),
    MPD_ORGANISATION("dlp_organizace.csv", "Organisation"),
    MPD_ACTIVE_SUBSTANCE("dlp_lecivelatky.csv", "Active substance"),
    MPD_SUBSTANCE("dlp_latky.csv", "Substance"),
    MPD_SUBSTANCE_SYNONYM("dlp_synonyma.csv", "Substance synonym"),
    MPD_MEDICINAL_PRODUCT("dlp_lecivepripravky.csv", "Medicinal product"),
    MPD_REGISTRATION_EXCEPTION("dlp_splp.csv", "Registration exception"),
    MPD_CANCELLED_REGISTRATION("dlp_zruseneregistrace.csv", "Cancelled registration"),
    MPD_MEDICINAL_PRODUCT_SUBSTANCE("dlp_slozeni.csv", "Medicinal product composition"),
    MPD_VALIDITY("dlp_platnost.csv", "Validity");

    companion object {
        private val fileNameToEnum = entries.associateBy { it.fileName.lowercase() }

        fun fromFileName(fileName: String): MpdDatasetType? =
            fileNameToEnum[fileName.lowercase()]
    }
}
