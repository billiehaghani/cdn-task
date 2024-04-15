package com.bitmovin.platform.challenge.persistence

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "data_usage")
class DataUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    var distributionId: String = ""
    var time: LocalDate = LocalDate.now()
    var value: Double = 0.0
}
