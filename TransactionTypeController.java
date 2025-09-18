package com.carddemo.transactiontype.controller;

import com.carddemo.transactiontype.domain.TransactionType;
import com.carddemo.transactiontype.service.TransactionTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transaction-types")
@RequiredArgsConstructor
public class TransactionTypeController {

    private final TransactionTypeService service;

    @GetMapping
    public ResponseEntity<List<TransactionType>> getAllTransactionTypes() {
        List<TransactionType> types = service.getAllTransactionTypes();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<TransactionType> getTransactionTypeById(@PathVariable String typeCode) {
        return service.getTransactionTypeById(typeCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionType> createTransactionType(@Valid @RequestBody TransactionType transactionType) {
        TransactionType createdType = service.createTransactionType(transactionType);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{typeCode}")
                .buildAndExpand(createdType.getTypeCode())
                .toUri();
        return ResponseEntity.created(location).body(createdType);
    }
}