package cz.machovec.lekovyportal.domain.entity

enum class FileType(val extension: String) {
    CSV(".csv"),
    ZIP(".zip");

    override fun toString(): String = extension
}
