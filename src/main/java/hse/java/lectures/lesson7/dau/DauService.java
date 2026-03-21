package hse.java.lectures.lesson7.dau;

import java.util.Map;

public interface DauService {

    void postEvent(Event event);

    Map<Integer, Long> getDauStatistics();

    Long getAuthorDauStatistics(int authorId);

}
