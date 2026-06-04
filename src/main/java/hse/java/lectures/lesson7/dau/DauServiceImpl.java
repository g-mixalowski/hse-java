package hse.java.lectures.lesson7.dau;
 
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
public class DauServiceImpl implements DauService {
 
    private Map<Integer, Set<Integer>> yesterday = new HashMap<>();
    private Map<Integer, Set<Integer>> today = new HashMap<>();
    private LocalDate currentDate = LocalDate.now();
 
    @Override
    public synchronized void postEvent(Event event) {
        valiDate();
        today.computeIfAbsent(event.authorId(), id -> new HashSet<>())
             .add(event.userId());
    }
 
    @Override
    public synchronized Map<Integer, Long> getDauStatistics(List<Integer> authorIds) {
        valiDate();
        Map<Integer, Long> result = new HashMap<>();
        for (int authorId : authorIds) {
            result.put(authorId, getAuthorDauStatistics(authorId));
        }
        return result;
    }
 
    @Override
    public synchronized Long getAuthorDauStatistics(int authorId) {
        valiDate();
        Set<Integer> users = yesterday.get(authorId);
        return users == null ? 0L : (long) users.size();
    }
    void nextDay() {
        yesterday = today;
        today = new HashMap<>();
    }
 
    private void valiDate() {
        LocalDate now = LocalDate.now();
        if (now.isAfter(currentDate)) {
            yesterday = today;
            today = new HashMap<>();
            currentDate = now;
        }
    }
}
