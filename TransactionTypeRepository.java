package com.carddemo.transactiontype.repository;

import com.carddemo.transactiontype.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TransactionType entity.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}