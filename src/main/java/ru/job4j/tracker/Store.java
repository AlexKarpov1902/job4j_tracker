package ru.job4j.tracker;

import java.sql.SQLException;
import java.util.List;

public interface Store extends AutoCloseable {

    void init();

    Item add(Item item) throws SQLException;

    boolean replace(String id, Item item);

    boolean delete(String id);

    List<Item> findAll();

    List<Item> findByName(String key);

    Item findById(String id);

}