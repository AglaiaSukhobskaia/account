package com.account.repository;

import com.account.model.Account;
import com.account.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByTimeBetweenAndAccount(Instant time, Instant time2, Account account);
}
