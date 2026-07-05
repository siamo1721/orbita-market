package org.mainshop.service;

import org.mainshop.dto.AccountResponse;
import org.mainshop.dto.TopUpRequest;

import java.util.UUID;

public interface AccountService {

    AccountResponse create (UUID userId);

    AccountResponse getBalance (UUID userId);

    AccountResponse topUpBalance (UUID userId, TopUpRequest request);

}
