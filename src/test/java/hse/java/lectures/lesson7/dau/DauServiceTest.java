package hse.java.lectures.lesson7.dau;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("dau")
class DauServiceTest {

    private DauServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DauServiceImpl();
        service.postEvent(new Event(1, 10));
        service.postEvent(new Event(2, 10));
        service.postEvent(new Event(3, 20));
        service.nextDay();
    }

    @Test
    void sameUserClickedTwice_countedOnce() {
        service.postEvent(new Event(5, 10));
        service.postEvent(new Event(5, 10));
        service.nextDay();

        assertEquals(1L, service.getAuthorDauStatistics(10));
    }

    @Test
    void twoUniqueUsers_countedCorrectly() {
        assertEquals(2L, service.getAuthorDauStatistics(10));
    }

    @Test
    void authorWithNoClicks_returnsZero() {
        assertEquals(0L, service.getAuthorDauStatistics(99));
    }

    @Test
    void differentAuthors_countedIndependently() {
        Map<Integer, Long> stats = service.getDauStatistics(List.of(10, 20));
        assertEquals(2L, stats.get(10));
        assertEquals(1L, stats.get(20));
    }

    @Test
    void getDauStatistics_unknownAuthorReturnsZero() {
        Map<Integer, Long> stats = service.getDauStatistics(List.of(999));
        assertEquals(0L, stats.get(999));
    }

    @Test
    void eventsPostedToday_notVisibleUntilTomorrow() {
        service.postEvent(new Event(100, 10));
        assertEquals(2L, service.getAuthorDauStatistics(10));
    }

    @Test
    void afterNewDay_previousDataReplaced() {
        service.postEvent(new Event(7, 10));
        service.postEvent(new Event(8, 10));
        service.postEvent(new Event(9, 10));
        service.nextDay();

        assertEquals(3L, service.getAuthorDauStatistics(10));
    }
}
