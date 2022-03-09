package dev.shermende.mps.processing.service.impl;

import dev.shermende.mps.processing.db.repository.BalanceRepository;
import dev.shermende.mps.processing.service.BalanceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class DistributedLockBalanceServiceTest {
    public static PostgreSQLContainer<?> postgreDBContainer;
    public static GenericContainer<?> redisDBContainer;

    static {
        postgreDBContainer = new PostgreSQLContainer<>("postgres:14.2");
        redisDBContainer = new GenericContainer<>("redis:6.2.6").withExposedPorts(6379);
        postgreDBContainer.start();
        redisDBContainer.start();
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DistributedLockBalanceServiceTest.postgreDBContainer::getJdbcUrl);
        registry.add("spring.datasource.username", DistributedLockBalanceServiceTest.postgreDBContainer::getUsername);
        registry.add("spring.datasource.password", DistributedLockBalanceServiceTest.postgreDBContainer::getPassword);
        registry.add("spring.redis.host", redisDBContainer::getContainerIpAddress);
        registry.add("spring.redis.port", () -> redisDBContainer.getMappedPort(6379));
    }

    @Autowired
    private BalanceRepository repository;
    @Autowired
    @Qualifier("distributedLockBalanceServiceImpl")
    private BalanceService balanceService;

    private final ExecutorService executors = Executors.newCachedThreadPool();

    @Test
    void transfer() throws InterruptedException {
        IntStream.range(0, 20).parallel().forEach(value -> {
            executors.execute(() -> balanceService.transfer(100L, 200L, BigInteger.valueOf(10000000)));
            if (value % 2 == 0)
                executors.execute(() -> balanceService.transfer(300L, 100L, BigInteger.valueOf(10000000)));
        });
        executors.shutdown();
        final boolean awaitTermination = executors.awaitTermination(1, TimeUnit.MINUTES);
        Assertions.assertEquals(BigInteger.valueOf(0), repository.getAmount(100L));
        Assertions.assertEquals(BigInteger.valueOf(150000000), repository.getAmount(200L));
        Assertions.assertEquals(BigInteger.ZERO, repository.getAmount(300L));
    }

}