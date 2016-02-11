package org.inanme.java8newconcurrencyfeatures;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class Demo extends Infra {

    @Test
    public void slide8() {
        Future<Long> value = ioPool.submit(new WaitThisLongAndReturnThisValue(2));
        try {
            //Waits if necessary for the computation to complete,
            // and then retrieves its result.
            Long aLong = value.get();
            System.out.println(aLong);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void slide9() {
        CompletableFuture<Long> completableFuture =
            CompletableFuture.supplyAsync(() -> new WaitThisLongAndReturnThisValue(2).call(), ioPool);

        completableFuture.whenComplete((val, ex) -> log("Completed val " + val))
                         .thenApplyAsync(val -> val * 2, processingPool)
                         .thenAccept(val -> log("Transformed val " + val))
                         .thenRunAsync(() -> log("Send an email"), ioPool);

        giveMeSomeTime(3);
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
