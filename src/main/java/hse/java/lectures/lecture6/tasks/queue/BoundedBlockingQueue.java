package hse.java.lectures.lecture6.tasks.queue;

public class BoundedBlockingQueue<T> {

    private final Object[] items;
    private final int capacity;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        this.items = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("null elements not allowed");
        }
        synchronized (this) {
            while (count == capacity) {
                wait();
            }
            items[tail] = item;
            tail = (tail + 1) % capacity;
            count++;
            notifyAll();
        }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        synchronized (this) {
            while (count == 0) {
                wait();
            }
            T item = (T) items[head];
            items[head] = null;
            head = (head + 1) % capacity;
            count--;
            notifyAll();
            return item;
        }
    }

    public int size() {
        synchronized (this) {
            return count;
        }
    }

    public int capacity() {
        return capacity;
    }
}