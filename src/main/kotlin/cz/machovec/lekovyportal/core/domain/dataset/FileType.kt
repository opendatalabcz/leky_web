package cz.machovec.lekovyportal.core.domain.dataset

enum class FileType(val extension: String) {
    CSV(".csv"),
    ZIP(".zip");

    override fun toString(): String = extension
}
