package dev.shermende.mps.processing.service.impl;

import dev.shermende.mps.processing.exception.VersionIntegrityException;
import dev.shermende.mps.processing.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CasBalanceServiceImpl implements BalanceService {


    private final BalanceService balanceService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Integer limit;

    public CasBalanceServiceImpl(
            @Qualifier("balanceServiceImpl") BalanceService balanceService) {
        this.balanceService = balanceService;
        this.limit = 10;
    }

    @SneakyThrows
    public void transfer(
            Long from,
            Long to,
            BigInteger amount
    ) {
        var count = new AtomicInteger(1);
        retry(from, to, amount, count);
    }

    private void retry(
            Long from,
            Long to,
            BigInteger amount,
            AtomicInteger count
    ) {
        try {
            balanceService.transfer(from, to, amount);
        } catch (VersionIntegrityException e) {
            wait(count);
            retry(from, to, amount, count);
            if (count.getAndIncrement() >= limit) throw e;
        }
    }

    private void wait(AtomicInteger count) {
        try {
            executorService.submit(new Waiter(count)).get(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.warn(e.getMessage());
        }
    }

    @RequiredArgsConstructor
    public static class Waiter implements Callable<Boolean> {
        private final AtomicInteger count;
        private final Random random = new Random();

        @Override
        public Boolean call() throws Exception {
            Thread.sleep((count.get() * 150L) + random.nextLong(100L));
            return true;
        }
    }
}
