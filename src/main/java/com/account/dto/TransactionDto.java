package com.account.dto;

import com.account.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private AccountDto account;
    private TransactionType type;
    private BigDecimal amount;
    private Instant time;
}
