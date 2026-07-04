package org.mainshop.controller;

import lombok.AllArgsConstructor;
import org.mainshop.dto.BalanceResponse;
import org.mainshop.dto.CreateAccountResponse;
import org.mainshop.dto.TopUpRequest;
import org.mainshop.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController()
@RequestMapping("api/v1/payments/")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public CreateAccountResponse create(@RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return accountService.create(userId);
    }

    @GetMapping("/accounts/balance")
    public BalanceResponse getBalance (@RequestHeader(value = "X-User-Id", required = false) UUID userId){
        return accountService.getBalance(userId);
    }

    @PostMapping("/accounts/top-up")
    public BalanceResponse topUpBalance(@RequestHeader(value = "X-User-Id", required = false) UUID userId, @RequestBody TopUpRequest request){
        return accountService.topUpBalance(userId, request);
    }

}
