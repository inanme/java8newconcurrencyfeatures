package org.inanme.java8newconcurrencyfeatures;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class Demo extends Infra {

    @Test
    public void slide8() {
        Future<Long> value = ioPool.submit(new FutureLong(2000));
        try {
            //Waits if necessary for the computation to complete,
            // and then retrieves its result.
            Long aLong = value.get();
            log(aLong);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void slide9() {
        CompletableFuture<Long> completableFuture =
                CompletableFuture.supplyAsync(() -> new FutureLong(2000).call(), ioPool);

        completableFuture.whenComplete((val, ex) -> log("Completed val ", val))
                .thenApplyAsync(val -> val * 2, processingPool)
                .thenAccept(val -> log("Transformed val", val))
                .thenRunAsync(() -> log("Send an email"), ioPool);

        giveMeSomeTime(3000);
    }

    @Test
    public void runningAll() {
        CompletableFuture<Boolean> bool = CompletableFuture.supplyAsync(new FutureRandomBoolean(2000)::call, thread0);
        CompletableFuture<Long> wait2 = CompletableFuture.supplyAsync(new FutureLong(2000)::call, thread1);
        CompletableFuture<Long> buggy = CompletableFuture.supplyAsync(new FutureLong(3000)::call, thread2);

        CompletableFuture.allOf(bool, wait2, buggy).thenAccept(it -> log("all finished successfully"));
        bool.thenAccept(this::logReturn);
        wait2.thenAccept(this::logReturn);
        buggy.exceptionally(ex -> {
            log(ex);
            return 0l;
        });

        giveMeSomeTime(5000);
    }

    @Test
    public void anyOf() {
        CompletableFuture<Long> wait2 = CompletableFuture.supplyAsync(new FutureLong(4000)::call, thread1);
        CompletableFuture<Long> buggy = CompletableFuture.supplyAsync(new FutureLong(3000)::call, thread2);

        CompletableFuture.anyOf(wait2, buggy).thenAccept(it -> log("all finished successfully"));

        giveMeSomeTime(5000);
    }

    @Test
    public void HelloWorld() {
        CompletableFuture.supplyAsync(() -> logReturn("Hello"))
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> logReturn(" World")),
                        (s1, s2) -> log(s1 + s2));
    }

    @Test
    public void HelloBeautifulWorld() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> logReturn("Hello"));
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> logReturn("Beautiful"));
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> logReturn("World"));

        String combined = Stream
                .of(future1, future2, future3)
                .map(CompletableFuture::join)
                .collect(Collectors.joining(" "));

        assertEquals("Hello Beautiful World", combined);
    }

    @Test
    public void slide91() {
        FutureLong wait2 = new FutureLong(2000);
        FutureLong wait3 = new FutureLong(2000);
        CompletableFuture
                .supplyAsync(new FutureRandomBoolean(2000)::call)
                .thenApply(this::logReturn)
                .thenApplyAsync(it -> it ? wait2.call() : wait3.call())
                .thenAccept(this::log);
        giveMeSomeTime(5000);
    }

    @Test
    public void slide11() {
        CompletableFuture<Long> completableFuture =
                CompletableFuture.supplyAsync(() -> new FutureLong(2000).call(), ioPool);

        completableFuture.whenComplete((val, ex) -> log("Completed val ", val))
                .thenApplyAsync(val -> val * 2, processingPool)
                .thenAccept(val -> log("Transformed val ", val))
                .thenRunAsync(() -> log("Send an email"), ioPool);

        giveMeSomeTime(3000);
    }

    @Test
    public void usingSubsequentPool() {
        CompletableFuture.supplyAsync(() -> new FutureLong(2000).call(), ioPool)
                .thenApply(val -> {
                    log("Transformed1 val", val);
                    return val * 2;
                })
                .thenApplyAsync(val -> {
                    log("Transformed2 val", val);
                    return val * 2;
                }, processingPool)
                .thenApply(val -> {
                    log("Transformed3 val", val);
                    return val * 2;
                });
        giveMeSomeTime(3000);
    }

    @Test
    public void slide12() {
        int count = 0;

        ReentrantLock lock1 = new ReentrantLock();
        lock1.lock();
        try {
            count++;
        } finally {
            lock1.unlock();
        }

        ReadWriteLock lock2 = new ReentrantReadWriteLock();
        lock2.writeLock().lock();
        try {
            count++;
        } finally {
            lock2.writeLock().unlock();
        }

        lock2.readLock().lock();
        try {
            int x = count;
        } finally {
            lock2.readLock().unlock();
        }
    }

    @Test
    public void slide13() {
        StampedLock lock = new StampedLock();
        long stamp = lock.tryOptimisticRead();

        //do work;

        if (lock.validate(stamp)) {
            log("Got away with it!");
        } else {
            log("shite!");
            long stamp1 = lock.readLock();
            try {
                //do work;
            } finally {
                lock.unlockRead(stamp1);
            }
        }
    }
}
