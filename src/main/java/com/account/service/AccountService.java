package com.account.service;

import com.account.exception.AccountNotFoundException;
import com.account.mapper.AccountMapper;
import com.account.repository.AccountRepository;
import com.account.dto.AccountDto;
import com.account.exception.NotEnoughMoneyException;
import com.account.model.Account;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;

    public AccountDto createAccount(String owner) {
        var account = new Account();
        account.setOwner(owner);
        return accountMapper.toAccountDto(accountRepository.save(account));
    }

    public AccountDto deposit(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        account.setBalance(account.getBalance().add(amount));
        return accountMapper.toAccountDto(accountRepository.save(account));
    }

    public AccountDto withdraw(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        if (account.getBalance().compareTo(amount) >= 0) {
            account.setBalance(account.getBalance().subtract(amount));
            return accountMapper.toAccountDto(accountRepository.save(account));
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
            accountRepository.save(toAccount);
            return accountMapper.toAccountDto(fromAccount);
        } else   {
            throw new NotEnoughMoneyException(fromAccountId);
        }
    }

    public BigDecimal getBalance(Long accountId) {
        var account = getAccount(accountId);
        return account.getBalance();
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
