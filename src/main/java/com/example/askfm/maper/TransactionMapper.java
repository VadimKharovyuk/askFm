package com.example.askfm.maper;

import com.example.askfm.dto.TransactionDTO;
import com.example.askfm.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDTO.builder()
                .id(transaction.getId())
                .buyerId(transaction.getBuyer().getId())
                .buyerUsername(transaction.getBuyer().getUsername())
                .sellerId(transaction.getSeller().getId())
                .sellerUsername(transaction.getSeller().getUsername())
                .photoId(transaction.getPhoto().getId())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .status(transaction.getStatus().name())
                .type(transaction.getType())
                .build();
    }

    public List<TransactionDTO> toDTOList(List<Transaction> transactions) {
        if (transactions == null) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}