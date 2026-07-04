package org.mainshop.service;

import org.mainshop.dto.BalanceResponse;
import org.mainshop.dto.CreateAccountResponse;
import org.mainshop.dto.TopUpRequest;

import java.util.UUID;

public interface AccountService {

    CreateAccountResponse create (UUID userId);

    BalanceResponse getBalance (UUID userId);

    BalanceResponse topUpBalance (UUID userId, TopUpRequest request);

}
