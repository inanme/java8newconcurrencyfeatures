package org.inanme.java8newconcurrencyfeatures;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
public class Demo extends Infra {

    @Test
    public void slide1() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> cf =
                CompletableFuture.supplyAsync(
                        () -> new WaitAndReturnValue(3).call());

        TimeUnit.SECONDS.sleep(4);
    }
}
