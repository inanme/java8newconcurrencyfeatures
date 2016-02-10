package org.inanme.java8newconcurrencyfeatures;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Infra {

    @Rule
    final public TestName name = new TestName();

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    final Random random = new Random();

    @Before
    public void markStart() {
        System.out.printf("Test %s started: %s\n", name.getMethodName(), sdf.format(new Date()));
    }

    @After
    public void markFinish() {
        System.out.printf("Test %s ended  : %s\n", name.getMethodName(), sdf.format(new Date()));
    }

    final ExecutorService es = Executors.newSingleThreadExecutor();

    @After
    public void shutdownServices() {
        es.shutdown();
    }

    class WaitAndReturnValue implements Callable<Long> {
        final long value;

        WaitAndReturnValue(long value) {
            this.value = value;
        }

        @Override
        public Long call() {
            try {
                TimeUnit.SECONDS.sleep(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (value == 3l) {
                throw new IllegalArgumentException(String.format("%s is a invalid number", value));
            }
            return value;
        }
    }

}
