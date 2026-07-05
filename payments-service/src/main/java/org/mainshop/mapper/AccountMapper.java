package org.mainshop.mapper;

import org.mainshop.dto.AccountResponse;
import org.mainshop.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getUserId(),
                account.getBalance(),
                "geocredits"
        );
    }
}
