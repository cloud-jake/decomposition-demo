package com.carddemo.transactiontype.service;

import com.carddemo.transactiontype.domain.TransactionType;
import com.carddemo.transactiontype.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionTypeService {

    private final TransactionTypeRepository repository;

    @Transactional(readOnly = true)
    public List<TransactionType> getAllTransactionTypes() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TransactionType> getTransactionTypeById(String typeCode) {
        return repository.findById(typeCode);
    }

    @Transactional
    public TransactionType createTransactionType(TransactionType transactionType) {
        // In a real application, add validation to ensure the typeCode doesn't already exist.
        return repository.save(transactionType);
    }
}