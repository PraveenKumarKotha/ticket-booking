package com.app.ticketbooking;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class DoubleBookingTest {

    @Test
    void manyPeopleBookSameSeat() throws Exception {
        int people = 20;
        long seatId = 12; // A2 of show 4, must be AVAILABLE

        HttpClient client = HttpClient.newHttpClient();
        ExecutorService pool = Executors.newFixedThreadPool(people);
        CountDownLatch ready = new CountDownLatch(people);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (int i = 0; i < people; i++) {
            pool.submit(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/seats/" + seatId + "/book"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    ready.countDown();
                    start.await();               // everyone waits for the whistle
                    HttpResponse<String> res =
                            client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() == 200) success.incrementAndGet();
                    else failed.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();                       // whistle: everyone goes now
        pool.shutdown();
        pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println("SUCCESS = " + success.get());
        System.out.println("FAILED  = " + failed.get());
    }
}