package com.app.ticketbooking;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class DoubleBookingTest {

    @Test
    void sameHolderPaysManyTimesAtOnce() throws Exception {
        int taps = 20;
        long seatId = 15; // A5 of show 4
        String runId = String.valueOf(System.nanoTime());

        HttpClient client = HttpClient.newHttpClient();
        ExecutorService pool = Executors.newFixedThreadPool(taps);
        CountDownLatch ready = new CountDownLatch(taps);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        for (int i = 0; i < taps; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/payments"))
                            .header("Content-Type", "application/json")
                            .header("Idempotency-Key", "race-" + runId + "-" + n)
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    "{\"seatId\": " + seatId
                                            + ", \"customerName\": \"Ravi\", \"simulateFailure\": false}"))
                            .build();
                    ready.countDown();
                    start.await();
                    HttpResponse<String> res =
                            client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() == 200) success.incrementAndGet();
                    else if (res.statusCode() == 409) conflict.incrementAndGet();
                    else other.incrementAndGet();
                } catch (Exception e) {
                    other.incrementAndGet();
                }
            });
        }

        ready.await();
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        System.out.println("SUCCESS  (200) = " + success.get());
        System.out.println("CONFLICT (409) = " + conflict.get());
        System.out.println("OTHER          = " + other.get());
    }
}