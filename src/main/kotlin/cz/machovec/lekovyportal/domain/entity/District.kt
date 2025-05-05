package cz.machovec.lekovyportal.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "district")
data class District(
    @Id
    @Column(name = "code", nullable = false, length = 10)
    val code: String,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @Column(name = "population", nullable = false)
    val population: Int
)
