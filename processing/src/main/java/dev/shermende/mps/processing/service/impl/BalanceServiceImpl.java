package dev.shermende.mps.processing.service.impl;

import dev.shermende.mps.processing.db.repository.BalanceRepository;
import dev.shermende.mps.processing.exception.VersionIntegrityException;
import dev.shermende.mps.processing.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository repository;

    @SneakyThrows
    @Transactional
    public void transfer(
            Long from,
            Long to,
            BigInteger amount
    ) {
        final Integer fromVersion = repository.getVersion(from);
        final Integer toVersion = repository.getVersion(to);
        final BigInteger fromAmount = repository.getAmount(from);
        if (fromAmount.compareTo(amount) < 0) throw new IllegalStateException("Not enougth balance");
        Thread.sleep(150L);
        if (repository.updateBalance(from, amount.multiply(BigInteger.valueOf(-1)), fromVersion) < 1)
            throw new VersionIntegrityException();
        if (repository.updateBalance(to, amount, toVersion) < 1)
            throw new VersionIntegrityException();
    }
}
