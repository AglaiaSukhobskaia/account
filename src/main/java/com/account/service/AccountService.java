package com.account.service;

import com.account.dto.AccountDto;
import com.account.dto.TransactionDto;
import com.account.enums.TransactionType;
import com.account.exception.AccountNotFoundException;
import com.account.exception.NotEnoughMoneyException;
import com.account.model.Account;
import com.account.model.Transaction;
import com.account.repository.AccountRepository;
import com.account.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    AccountRepository accountRepository;
    TransactionRepository transactionRepository;
    ModelMapper mapper;

    public AccountDto createAccount(String owner) {
        var account = new Account();
        account.setOwner(owner);
        return mapper.map(accountRepository.save(account), AccountDto.class);
    }

    @Transactional
    public AccountDto deposit(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        account.setBalance(account.getBalance().add(amount));
        createTransaction(account, TransactionType.DEPOSIT, amount);
        return mapper.map(accountRepository.save(account), AccountDto.class);
    }

    @Transactional
    public AccountDto withdraw(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        if (account.getBalance().compareTo(amount) >= 0) {
            account.setBalance(account.getBalance().subtract(amount));
            createTransaction(account, TransactionType.WITHDRAW, amount);
            return mapper.map(accountRepository.save(account), AccountDto.class);
        } else {
            throw new NotEnoughMoneyException(accountId);
        }
    }

    @Transactional
    public AccountDto transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        var fromAccount = getAccount(fromAccountId);
        var toAccount = getAccount(toAccountId);

        if (fromAccount.getBalance().compareTo(amount) >= 0) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(fromAccount);
            createTransaction(fromAccount, TransactionType.WITHDRAW, amount);
            accountRepository.save(toAccount);
            createTransaction(toAccount, TransactionType.DEPOSIT, amount);
            return mapper.map(fromAccount, AccountDto.class);
        } else {
            throw new NotEnoughMoneyException(fromAccountId);
        }
    }

    public BigDecimal getBalance(Long accountId) {
        var account = getAccount(accountId);
        return account.getBalance();
    }

    @Transactional
    public List<TransactionDto> getTransactions(Long id, Instant fromTime, Instant toTime) {
        var account = getAccount(id);
        return transactionRepository.findAllByTimeBetweenAndAccount(fromTime, toTime, account).stream()
                .map(t -> mapper.map(t, TransactionDto.class))
                .toList();
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void createTransaction(Account account, TransactionType type, BigDecimal amount) {
        var transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTime(Instant.now());
        transactionRepository.save(transaction);
    }
}
