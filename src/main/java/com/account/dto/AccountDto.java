package com.account.dto;

import java.math.BigDecimal;

public record AccountDto(Long id,
                         String owner,
                         BigDecimal balance) {
}
