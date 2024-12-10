package com.account.mapper;

import com.account.dto.AccountDto;
import com.account.model.Account;
import org.mapstruct.Mapper;

@Mapper
public interface AccountMapper {
    AccountDto toAccountDto(Account account);
}
