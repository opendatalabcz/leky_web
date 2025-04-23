package cz.machovec.lekovyportal.importer.mpd

import cz.machovec.lekovyportal.domain.entity.mpd.MpdDatasetType
import cz.machovec.lekovyportal.importer.common.MissingColumnException
import mu.KotlinLogging

class MpdCsvTableRunner(private val csvMap: Map<MpdDatasetType, ByteArray>) {

    private val log = KotlinLogging.logger {}

    // TODO: review
    fun run(steps: List<TableStep>) {
        steps.forEach { step ->
            val csv = csvMap[step.file]
            if (csv != null) {
                step.action(csv)
            } else if (step.required) {
                throw MissingColumnException("MPD ZIP neobsahuje povinný soubor ${step.file.fileName}")
            } else {
                log.warn { "Soubor ${step.file.fileName} v ZIPu chybí – přeskakuji." }
            }
        }
    }

    data class TableStep(
        val file: MpdDatasetType,
        val required: Boolean = true,
        val action: (ByteArray) -> Unit
    )
}

