package dev.shermende.mps.processing.service;

import java.math.BigInteger;

public interface BalanceService {

    void transfer(
            Long from,
            Long to,
            BigInteger amount
    );

}
