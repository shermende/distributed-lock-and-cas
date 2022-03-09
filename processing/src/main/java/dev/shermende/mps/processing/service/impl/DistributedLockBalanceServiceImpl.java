package dev.shermende.mps.processing.service.impl;

import dev.shermende.mps.processing.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
public class DistributedLockBalanceServiceImpl implements BalanceService {

    private final BalanceService balanceService;
    private final RedisLockRegistry redisLockRegistry;

    public DistributedLockBalanceServiceImpl(
            @Qualifier("balanceServiceImpl") BalanceService balanceService,
            RedisLockRegistry redisLockRegistry
    ) {
        this.balanceService = balanceService;
        this.redisLockRegistry = redisLockRegistry;
    }

    @Override
    public void transfer(
            Long from,
            Long to,
            BigInteger amount
    ) {
        final String firstKey = (from < to) ? from.toString() : to.toString();
        final String secondKey = (from < to) ? to.toString() : from.toString();
        final Lock firstLock = redisLockRegistry.obtain(firstKey);
        final Lock secondLock = redisLockRegistry.obtain(secondKey);
        try {
            final boolean firstLocked = firstLock.tryLock(5, TimeUnit.SECONDS);
            final boolean secondLocked = secondLock.tryLock(5, TimeUnit.SECONDS);
            if (!firstLocked || !secondLocked) throw new IllegalStateException();
            log.info("{}. First lock: {}", firstKey, firstLocked);
            log.info("{}. Second lock: {}", secondKey, secondLocked);
            balanceService.transfer(from, to, amount);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            firstLock.unlock();
            secondLock.unlock();
        }
    }
}
