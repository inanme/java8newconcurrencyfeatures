package org.inanme.java8newconcurrencyfeatures;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Infra {

    @Rule
    final public TestName name = new TestName();
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    final Random random = new Random();

    @Before
    public void markStart() {
        log("Test started", name.getMethodName());
    }

    @After
    public void markFinish() {
        log("Test finished", name.getMethodName());
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
        System.out.println(log);
    }

    @After
    public void shutdownServices() {
        processingPool.shutdown();
        ioPool.shutdown();
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
