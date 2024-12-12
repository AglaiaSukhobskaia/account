package com.account.service;

import com.account.dto.AccountDto;
import com.account.dto.TransactionDto;
import com.account.exception.AccountNotFoundException;
import com.account.exception.NotEnoughMoneyException;
import com.account.model.Account;
import com.account.model.Transaction;
import com.account.repository.AccountRepository;
import com.account.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AccountServiceTests {
    private AccountRepository mockAccountRepository;
    private TransactionRepository mockTransactionRepository;
    private ModelMapper mockModelMapper;
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        mockAccountRepository = Mockito.mock(AccountRepository.class);
        mockTransactionRepository = Mockito.mock(TransactionRepository.class);
        mockModelMapper = Mockito.mock(ModelMapper.class);
        accountService = new AccountService(mockAccountRepository, mockTransactionRepository, mockModelMapper);
    }

    @Test
    void testCreateAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");

        AccountDto expectedDto = new AccountDto();
        expectedDto.setId(1L);
        expectedDto.setOwner("Test owner");
        expectedDto.setBalance(BigDecimal.ZERO);

        when(mockAccountRepository.save(any(Account.class))).thenReturn(account);
        when(mockModelMapper.map(account, AccountDto.class)).thenReturn(expectedDto);

        AccountDto accountDto = accountService.createAccount("Test owner");

        assertEquals("Test owner", accountDto.getOwner());
        assertEquals(1L, accountDto.getId());
        assertEquals(BigDecimal.ZERO, accountDto.getBalance());
    }

    @Test
    void testDeposit() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");
        account.setBalance(BigDecimal.valueOf(100));

        AccountDto expectedDto = new AccountDto();
        expectedDto.setId(1L);
        expectedDto.setOwner("Test owner");
        expectedDto.setBalance(BigDecimal.valueOf(150));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(mockAccountRepository.save(any(Account.class))).thenReturn(account);
        when(mockModelMapper.map(account, AccountDto.class)).thenReturn(expectedDto);

        AccountDto accountDto = accountService.deposit(1L, BigDecimal.valueOf(50));

        assertEquals(expectedDto.getBalance(), accountDto.getBalance());
        verify(mockTransactionRepository).save(any(Transaction.class));
    }

    @Test
    void testWithdraw() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");
        account.setBalance(BigDecimal.valueOf(100));

        AccountDto expectedDto = new AccountDto();
        expectedDto.setId(1L);
        expectedDto.setOwner("Test owner");
        expectedDto.setBalance(BigDecimal.valueOf(50));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(mockAccountRepository.save(any(Account.class))).thenReturn(account);
        when(mockModelMapper.map(account, AccountDto.class)).thenReturn(expectedDto);

        AccountDto accountDto = accountService.withdraw(1L, BigDecimal.valueOf(50));

        assertEquals(expectedDto.getBalance(), accountDto.getBalance());
        verify(mockTransactionRepository).save(any(Transaction.class));
    }

    @Test
    void testWithdrawNotEnoughMoney() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");
        account.setBalance(BigDecimal.valueOf(100));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(NotEnoughMoneyException.class, () -> {
            accountService.withdraw(1L, BigDecimal.valueOf(150));
        });
    }

    @Test
    void testTransfer() {
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setOwner("Test owner");
        fromAccount.setBalance(BigDecimal.valueOf(200));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setOwner("Test owner");
        toAccount.setBalance(BigDecimal.valueOf(100));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(mockAccountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(mockAccountRepository.saveAll(anyList())).thenReturn(List.of(fromAccount, toAccount));

        AccountDto fromAccountDto = new AccountDto();
        fromAccountDto.setId(1L);
        fromAccountDto.setBalance(BigDecimal.valueOf(150));

        AccountDto toAccountDto = new AccountDto();
        toAccountDto.setId(2L);
        toAccountDto.setBalance(BigDecimal.valueOf(150));

        when(mockModelMapper.map(fromAccount, AccountDto.class)).thenReturn(fromAccountDto);
        when(mockModelMapper.map(toAccount, AccountDto.class)).thenReturn(toAccountDto);

        accountService.transfer(1L, 2L, BigDecimal.valueOf(50));

        assertEquals(fromAccountDto.getBalance(), fromAccount.getBalance());
        assertEquals(toAccountDto.getBalance(), toAccount.getBalance());
        verify(mockTransactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testTransferNotEnoughMoney() {
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setOwner("Test owner");
        fromAccount.setBalance(BigDecimal.valueOf(100));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setOwner("Test owner");
        toAccount.setBalance(BigDecimal.valueOf(200));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(mockAccountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(NotEnoughMoneyException.class, () -> {
            accountService.transfer(1L, 2L, BigDecimal.valueOf(150));
        });
    }

    @Test
    void testGetBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");
        account.setBalance(BigDecimal.valueOf(100));

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(1L);
        assertEquals(BigDecimal.valueOf(100), balance);
    }

    @Test
    void testGetAccountThrowsException() {
        when(mockAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(1L));
    }



    @Test
    void testGetTransactions() {
        Account account = new Account();
        account.setId(1L);
        account.setOwner("Test owner");
        account.setBalance(BigDecimal.valueOf(100));

        AccountDto accountDto = new AccountDto();
        accountDto.setId(1L);
        accountDto.setOwner("Test owner");
        accountDto.setBalance(BigDecimal.valueOf(100));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);

        when(mockAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(mockTransactionRepository.findAllByTimeBetweenAndAccount(any(), any(), any())).thenReturn(Collections.singletonList(transaction));

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccount(accountDto);

        when(mockModelMapper.map(transaction, TransactionDto.class)).thenReturn(transactionDto);

        List<TransactionDto> transactions = accountService.getTransactions(1L, Instant.now().minusSeconds(3600), Instant.now());
        assertEquals(1, transactions.size());

        assertEquals(transactionDto.getAccount(), transactions.get(0).getAccount());
    }

}