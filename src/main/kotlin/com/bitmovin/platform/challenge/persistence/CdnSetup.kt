package com.bitmovin.platform.challenge.persistence

import jakarta.persistence.*

@Entity
@Table(name = "cdn_setup")
class CdnSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    var customerId: String = ""
    var bucketId: String = ""
    var distributionId: String = ""

    @Enumerated(value = EnumType.STRING)
    var status: Status = Status.OPERATIONAL

    enum class Status {
        OPERATIONAL,
        CAP_REACHED,
    }
}
