package com.example.askfm.service;

import com.example.askfm.dto.DepositRequestDTO;
import com.example.askfm.dto.TransactionDTO;
import com.example.askfm.enums.TransactionStatus;
import com.example.askfm.enums.TransactionType;
import com.example.askfm.maper.TransactionMapper;
import com.example.askfm.model.Photo;
import com.example.askfm.model.Transaction;
import com.example.askfm.model.User;
import com.example.askfm.repository.TransactionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;



    public List<TransactionDTO> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Transaction> transactions = transactionRepository.findByBuyerOrderByCreatedAtDesc(user);
        return transactionMapper.toDTOList(transactions);
    }

    public List<TransactionDTO> getSellerTransactions(String username) {
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Transaction> transactions = transactionRepository.findBySellerOrderByCreatedAtDesc(seller);
        return transactionMapper.toDTOList(transactions);
    }

    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    public TransactionDTO createTransaction(User buyer, User seller, Photo photo, BigDecimal amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setPhoto(photo);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setType(type); // Теперь используем enum из правильного пакета
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }


    @Transactional
    public TransactionDTO updateTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(status);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(updatedTransaction);
    }

    public Page<TransactionDTO> getAllTransactionsPaged(Pageable pageable) {
        Page<Transaction> transactionsPage = transactionRepository.findAll(pageable);
        return transactionsPage.map(transactionMapper::toDTO);
    }
    public Page<TransactionDTO> getAllTransactionsPaged(String username, Pageable pageable) {
        Page<Transaction> transactionsPage = transactionRepository
                .findByBuyerUsernameOrSellerUsername(username, username, pageable);
        return transactionsPage.map(transactionMapper::toDTO);
    }

    public List<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return transactionMapper.toDTOList(transactions);
    }
    @Transactional
    public TransactionDTO depositBalance(String username, DepositRequestDTO depositRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // В реальном приложении здесь должна быть интеграция с платежной системой
        // Сейчас просто имитируем успешную оплату

        try {
            // Создаем транзакцию
            Transaction transaction = new Transaction();
            transaction.setBuyer(user);
            transaction.setSeller(user); // При пополнении отправитель и получатель один и тот же
            transaction.setAmount(depositRequest.getAmount());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setType(TransactionType.BALANCE_DEPOSIT);
            transaction.setStatus(TransactionStatus.PENDING);

            transaction = transactionRepository.save(transaction);

            // Обновляем баланс пользователя
            user.setBalance(user.getBalance().add(depositRequest.getAmount()));
            userRepository.save(user);

            // Помечаем транзакцию как успешную
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);

            return transactionMapper.toDTO(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process deposit: " + e.getMessage());
        }
    }
}
