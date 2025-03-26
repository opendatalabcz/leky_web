package cz.machovec.lekovyportal.domain.entity.mpd

enum class MpdDatasetType(val description: String) {
    MPD_ACTIVE_SUBSTANCE("Active substance"),
    MPD_ADDICTION_CATEGORY("Addiction category"),
    MPD_ADMINISTRATION_ROUTE("Administration route"),
    MPD_ATC_GROUP("ATC group"),
    MPD_CANCELLED_REGISTRATION("Cancelled registration"),
    MPD_COMPOSITION_FLAG("Composition flag"),
    MPD_COUNTRY("Country"),
    MPD_DISPENSE_TYPE("Dispense type"),
    MPD_DOPING_CATEGORY("Doping category"),
    MPD_DOSAGE_FORM("Dosage form"),
    MPD_GOVERNMENT_REGULATION_CATEGORY("Government regulation category"),
    MPD_INDICATION_GROUP("Indication group"),
    MPD_MEASUREMENT_UNIT("Measurement unit"),
    MPD_MEDICINAL_PRODUCT("Medicinal product"),
    MPD_ORGANISATION("Organisation"),
    MPD_PACKAGE_TYPE("Package type"),
    MPD_REGISTRATION_EXCEPTION("Registration exception"),
    MPD_REGISTRATION_PROCESS("Registration process"),
    MPD_REGISTRATION_STATUS("Registration status"),
    MPD_SOURCE("Source"),
    MPD_SUBSTANCE("Substance"),
    MPD_SUBSTANCE_COMPOSITION("Substance composition"),
    MPD_SUBSTANCE_SYNONYM("Substance synonym");
}
