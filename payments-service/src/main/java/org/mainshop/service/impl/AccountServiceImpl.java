package org.mainshop.service.impl;

import org.mainshop.dto.TopUpRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.mainshop.dto.BalanceResponse;
import org.mainshop.dto.CreateAccountResponse;
import org.mainshop.entity.Account;
import org.mainshop.enums.ErrorType;
import org.mainshop.exception.BusinessException;
import org.mainshop.repository.AccountRepository;
import org.mainshop.service.AccountService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;


    @Override
    @Transactional
    public CreateAccountResponse create(UUID userId) {
        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }

        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(
                    ErrorType.ACCOUNT_ALREADY_EXISTS,
                    "Аккаунт у пользователя с  " + userId + " уже существует"
            );
        }

        Account account = new Account();
        account.setBalance(0L);
        account.setUserId(userId);

        accountRepository.save(account);

        return new CreateAccountResponse(
                account.getUserId(),
                account.getBalance(),
                "geocredits");
    }

    @Override
    public BalanceResponse getBalance(UUID userId) {
        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorType.ACCOUNT_NOT_FOUND,
                        "Аккаунт для пользователя с ID " + userId + " не найден"
                ));

        return new BalanceResponse(
                account.getUserId(),
                account.getBalance(),
                "geocredits"
        );
    }

    @Override
    @Transactional
    public BalanceResponse topUpBalance(UUID userId, TopUpRequest request) {

        if (userId == null) {
            throw new BusinessException(
                    ErrorType.MISSING_USER_ID,
                    "userId отсутствует"
            );
        }
        if ( request == null || request.amount() == null || request.amount() <= 0) {
            throw new BusinessException(
                    ErrorType.INVALID_AMOUNT,
                    "Некорректное число для пополнения, должно быть больше 0"
            );
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorType.ACCOUNT_NOT_FOUND,
                        "Аккаунт для пользователя с ID " + userId + " не найден"
                ));

        account.setBalance(account.getBalance() + request.amount());
        accountRepository.save(account);

        return new BalanceResponse(
                account.getUserId(),
                account.getBalance(),
                "geocredits"
        );
    }

}
