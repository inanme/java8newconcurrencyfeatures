package org.inanme.java8newconcurrencyfeatures;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Infra {

    @Rule
    final public TestName name = new TestName();
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    final Random random = new Random();
    private long start;

    private AtomicInteger threadFactory0 = new AtomicInteger();

    private AtomicInteger threadFactory1 = new AtomicInteger();

    private AtomicInteger threadFactory2 = new AtomicInteger();

    private AtomicInteger threadFactory3 = new AtomicInteger();

    protected ExecutorService thread0 =
            Executors.newCachedThreadPool(r -> new Thread(r, "thread0-" + threadFactory0.getAndIncrement()));

    protected ExecutorService thread1 =
            Executors.newCachedThreadPool(r -> new Thread(r, "thread1-" + threadFactory1.getAndIncrement()));

    protected ExecutorService thread2 =
            Executors.newCachedThreadPool(r -> new Thread(r, "thread2-" + threadFactory2.getAndIncrement()));

    protected ExecutorService thread3 =
            Executors.newCachedThreadPool(r -> new Thread(r, "thread3-" + threadFactory3.getAndIncrement()));


    @Before
    public void start() {
        System.err.printf("Test %s started: %s\n", name.getMethodName(), now());
        start = System.currentTimeMillis();
    }

    String now() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_TIME);
    }

    @After
    public void finish() {
        System.err.printf("Test %s ended  : %s\n", name.getMethodName(), now());
        long duration = System.currentTimeMillis() - start;
        System.err.printf("Test %s took   : %s\n", name.getMethodName(),
                DurationFormatUtils.formatDurationWords(duration, false, false));
    }

    final ExecutorService processingPool = Executors.newCachedThreadPool(r -> new Thread(r, "Processing"));

    final ExecutorService ioPool = Executors.newCachedThreadPool(r -> new Thread(r, "Input/Output"));

    void giveMeSomeTime(long milis) {
        try {
            TimeUnit.MILLISECONDS.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    <T> T logReturn(T t) {
        log("logReturn : " + t);
        return t;
    }

    void log(Object... args) {
        String log = Stream.concat(
                Stream.of(StringUtils.rightPad(Thread.currentThread().getName(), 32), LocalDateTime.now().format(dateTimeFormatter)),
                Arrays.stream(args).map(Object::toString))
                .collect(Collectors.joining(" : "));
        System.err.println(log);
    }

    @After
    public void shutdownServices() {
        Arrays.asList(processingPool, ioPool, thread0, thread1, thread1, thread2, thread3)
                .forEach(ExecutorService::shutdown);
    }

    class FutureLong implements Callable<Long> {
        final long milis;

        FutureLong(long milis) {
            this.milis = milis;
        }

        @Override
        public Long call() {
            giveMeSomeTime(milis);
            if (milis == 3000l) {
                throw new IllegalArgumentException(String.format("%s is a invalid number", milis));
            }
            log("FutureLong : " + milis);
            return milis;
        }
    }

    class FutureRandomBoolean implements Callable<Boolean> {
        final long milis;

        FutureRandomBoolean(long milis) {
            this.milis = milis;
        }

        @Override
        public Boolean call() {
            giveMeSomeTime(milis);
            log("FutureRandomBoolean : " + milis);
            return random.nextBoolean();
        }
    }
}
