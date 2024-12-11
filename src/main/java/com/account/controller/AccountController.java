package com.account.controller;

import com.account.dto.AccountDto;
import com.account.dto.TransactionDto;
import com.account.exception.Handler;
import com.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController implements Handler {
    AccountService accountService;

    @PostMapping
    @Operation(summary = "Создание нового аккаунта")
    public ResponseEntity<AccountDto> createAccount(@RequestParam @Parameter(description = "Владелец счета") String owner) {
        return ResponseEntity.ok(accountService.createAccount(owner));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Зачисление суммы на счет")
    public ResponseEntity<AccountDto> deposit(@PathVariable @Parameter(description = "ID счета") Long id,
                                              @RequestParam @Parameter(description = "Сумма в рублях") BigDecimal amount) {
        return ResponseEntity.ok(accountService.deposit(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Списание суммы со счета")
    public ResponseEntity<AccountDto> withdraw(@PathVariable @Parameter(description = "ID счета") Long id,
                                               @RequestParam @Parameter(description = "Сумма в рублях") BigDecimal amount) {
        return ResponseEntity.ok(accountService.withdraw(id, amount));
    }

    @PostMapping("/{fromId}/transfer/{toId}")
    @Operation(summary = "Перевод суммы с одного счета на другой")
    public ResponseEntity<AccountDto> transfer(@PathVariable @Parameter(description = "ID счета отправителя") Long fromId,
                                               @PathVariable @Parameter(description = "ID счета получателя") Long toId,
                                               @RequestParam @Parameter(description = "Сумма в рублях") BigDecimal amount) {
        return ResponseEntity.ok(accountService.transfer(fromId, toId, amount));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Предоставление текущего баланса по счету")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable @Parameter(description = "ID счета") Long id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    @GetMapping("{id}/transactions")
    @Operation(summary = "Предоставление выписки по операциям за период времени")
    public ResponseEntity<List<TransactionDto>> getTransactions(@PathVariable @Parameter(description = "ID счета") Long id,
                                                                @RequestParam @Parameter(description = "Период с") Instant fromTime,
                                                                @RequestParam @Parameter(description = "Период по") Instant toTime) {
        return ResponseEntity.ok(accountService.getTransactions(id, fromTime, toTime));
    }
}
