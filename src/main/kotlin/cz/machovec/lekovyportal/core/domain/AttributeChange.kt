package cz.machovec.lekovyportal.core.domain

data class AttributeChange<T>(
    val attribute: String,
    val oldValue: T?,
    val newValue: T?
)
