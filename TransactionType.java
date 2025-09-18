package com.carddemo.transactiontype.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the TRANSACTION_TYPE table in the CardDemo DB2 database.
 */
@Entity
@Table(name = "TRANSACTION_TYPE", schema = "CARDDEMO")
@Data
@NoArgsConstructor
public class TransactionType {

    @Id
    @Column(name = "TR_TYPE", length = 2, nullable = false)
    @NotBlank
    @Size(min = 2, max = 2)
    private String typeCode;

    @Column(name = "TR_DESCRIPTION", length = 50)
    @Size(max = 50)
    private String description;
}