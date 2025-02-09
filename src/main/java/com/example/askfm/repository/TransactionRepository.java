package com.example.askfm.repository;

import com.example.askfm.model.Transaction;
import com.example.askfm.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<Transaction> findBySellerOrderByCreatedAtDesc(User seller);

    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    Page<Transaction> findByBuyerUsernameOrSellerUsername(
            String buyerUsername,
            String sellerUsername,
            Pageable pageable
    );
}