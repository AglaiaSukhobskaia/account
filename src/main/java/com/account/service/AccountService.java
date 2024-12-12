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
        synchronized (account) {
            account.setBalance(account.getBalance().add(amount));
            createTransaction(account, TransactionType.DEPOSIT, amount);
            accountRepository.save(account);
        }
        return mapper.map(account, AccountDto.class);
    }

    @Transactional
    public AccountDto withdraw(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        synchronized (account) {
            if (account.getBalance().compareTo(amount) >= 0) {
                account.setBalance(account.getBalance().subtract(amount));
                createTransaction(account, TransactionType.WITHDRAW, amount);
                accountRepository.save(account);
            } else {
                throw new NotEnoughMoneyException(accountId);
            }
        }
        return mapper.map(account, AccountDto.class);
    }

    @Transactional
    public AccountDto transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        var sourceAccount = getAccount(fromAccountId);
        var targetAccount = getAccount(toAccountId);

        Long firstLockId = Math.min(fromAccountId, toAccountId);
        Long secondLockId = Math.max(fromAccountId, toAccountId);

        synchronized (firstLockId) {
            synchronized (secondLockId) {
                if (sourceAccount.getBalance().compareTo(amount) >= 0) {
                    sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
                    targetAccount.setBalance(targetAccount.getBalance().add(amount));
                    createTransaction(sourceAccount, TransactionType.WITHDRAW, amount);
                    createTransaction(targetAccount, TransactionType.DEPOSIT, amount);
                    accountRepository.saveAll(List.of(sourceAccount, targetAccount));
                    return mapper.map(sourceAccount, AccountDto.class);
                } else {
                    throw new NotEnoughMoneyException(fromAccountId);
                }
            }
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

    public Account getAccount(Long accountId) {
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
