package hse.java.lectures.lesson7.limiter;

import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Скользящий рейтлимитер: не больше заданного числа успешных {@link #check()} за последнюю секунду или минуту.
 */
public class RateLimiter {

    private final long windowNanos;
    private final int maxRequests;
    private final Deque<Long> successTimestamps = new ArrayDeque<>();

    /**
     * @param unit        длина окна — только {@link ChronoUnit#SECONDS} или {@link ChronoUnit#MINUTES}
     *                    (скользящее окно 1 секунда или 1 минута)
     * @param maxRequests максимум успешных {@link #check()} за окно (должно быть > 0)
     */
    public RateLimiter(ChronoUnit unit, int maxRequests) {
        if (unit != ChronoUnit.SECONDS && unit != ChronoUnit.MINUTES) {
            throw new IllegalArgumentException("Only SECONDS or MINUTES are supported");
        }
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be > 0");
        }
        this.windowNanos = unit.getDuration().toNanos();
        this.maxRequests = maxRequests;
    }

    /**
     * Регистрирует попытку и возвращает, разрешена ли она в пределах лимита.
     */
    public synchronized boolean check() {
        long now = System.nanoTime();
        long windowStart = now - windowNanos;
 
        while (!successTimestamps.isEmpty() && successTimestamps.peekFirst() <= windowStart) {
            successTimestamps.pollFirst();
        }
 
        if (successTimestamps.size() < maxRequests) {
            successTimestamps.addLast(now);
            return true;
        }
 
        return false;
    }

}
