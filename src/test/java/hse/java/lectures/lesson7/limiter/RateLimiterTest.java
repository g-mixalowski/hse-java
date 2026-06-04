package hse.java.lectures.lesson7.limiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("limiter")
class RateLimiterTest {

    @Test
    void illegalUnit_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(ChronoUnit.HOURS, 5));
    }

    @Test
    void zeroMaxRequests_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(ChronoUnit.SECONDS, 0));
    }

    @Test
    void negativeMaxRequests_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(ChronoUnit.SECONDS, -1));
    }

    @Test
    void withinLimit_allCallsSucceed() {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 3);

        assertTrue(limiter.check());
        assertTrue(limiter.check());
        assertTrue(limiter.check());
    }

    @Test
    void overLimit_extraCallFails() {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 3);

        limiter.check();
        limiter.check();
        limiter.check();

        assertFalse(limiter.check());
    }

    @Test
    void failedCall_doesNotOccupySlot() {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 2);

        limiter.check();
        limiter.check();
        limiter.check();

        assertFalse(limiter.check());
    }

    @Test
    void slidingWindow_oldCallsExpire() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 2);

        assertTrue(limiter.check());
        assertTrue(limiter.check());
        assertFalse(limiter.check());

        Thread.sleep(1100);

        assertTrue(limiter.check());
        assertTrue(limiter.check());
        assertFalse(limiter.check());
    }

    @Test
    void slidingWindow_partialExpiry() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 2);

        assertTrue(limiter.check());
        Thread.sleep(600);
        assertTrue(limiter.check());
        assertFalse(limiter.check());

        Thread.sleep(500);

        assertTrue(limiter.check());
        assertFalse(limiter.check());
    }
}
