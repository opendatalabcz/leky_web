package cz.machovec.lekovyportal.processor.util

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.net.URI

@Component
class RemoteFileDownloader {

    private val log = KotlinLogging.logger {}

    /**
     * Downloads file from the given [uri] and returns its content as [ByteArray].
     * Returns null if download fails.
     */
    fun downloadFile(uri: URI): ByteArray? {
        return runCatching {
            uri.toURL().openStream().use { it.readBytes() }
        }.onFailure {
            log.error(it) { "Failed to download file from $uri" }
        }.getOrNull()
    }
}
