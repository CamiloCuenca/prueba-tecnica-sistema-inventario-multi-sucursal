package com.camilocuenca.inventorysystem.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "supplier")
open class Supplier {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Size(max = 50)
    @NotNull
    @Column(name = "nit", nullable = false, length = 50)
    open var nit: String? = null

    @Size(max = 50)
    @Column(name = "phone", length = 50)
    open var phone: String? = null

    @Size(max = 255)
    @Column(name = "email")
    open var email: String? = null

    @Size(max = 255)
    @Column(name = "address")
    open var address: String? = null

    @ColumnDefault("now()")
    @Column(name = "created_at")
    open var createdAt: Instant? = null

}