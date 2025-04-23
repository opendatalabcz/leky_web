package cz.machovec.lekovyportal.importer.mpd

enum class MpdCsvFile(val fileName: String) {
    MPD_COUNTRY("dlp_zeme.csv"),
    MPD_DISPENSE_TYPE("dlp_vydej.csv"),
    MPD_VALIDITY("dlp_platnost.csv");

    companion object {
        private val nameToEnum = entries.associateBy { it.fileName.lowercase() }
        fun fromFileName(name: String): MpdCsvFile? = nameToEnum[name.lowercase()]
    }
}