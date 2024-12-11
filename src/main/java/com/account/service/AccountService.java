package com.account.service;

import com.account.dto.AccountDto;
import com.account.exception.AccountNotFoundException;
import com.account.exception.NotEnoughMoneyException;
import com.account.model.Account;
import com.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    AccountRepository accountRepository;
    ModelMapper mapper;

    public AccountDto createAccount(String owner) {
        var account = new Account();
        account.setOwner(owner);
        return mapper.map(accountRepository.save(account), AccountDto.class);
    }

    public AccountDto deposit(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        account.setBalance(account.getBalance().add(amount));
        return mapper.map(accountRepository.save(account), AccountDto.class);
    }

    public AccountDto withdraw(Long accountId, BigDecimal amount) {
        var account = getAccount(accountId);
        if (account.getBalance().compareTo(amount) >= 0) {
            account.setBalance(account.getBalance().subtract(amount));
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
            accountRepository.save(toAccount);
            return mapper.map(fromAccount, AccountDto.class);
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
