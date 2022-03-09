package dev.shermende.mps.processing.api;

import dev.shermende.mps.processing.service.impl.DistributedLockBalanceServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/balance")
public class BalanceController {
    private final DistributedLockBalanceServiceImpl balanceService;

    public BalanceController(
            DistributedLockBalanceServiceImpl balanceService
    ) {
        this.balanceService = balanceService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public void transfer(
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam BigInteger amount
    ) {
        balanceService.transfer(from, to, amount);
    }

}
