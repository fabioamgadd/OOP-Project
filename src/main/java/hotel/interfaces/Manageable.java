package hotel.interfaces;

public interface Manageable<T> {
     void add(T entity);
     boolean update(T entity);
     boolean delete(String id);
     T findById(String id);
}
