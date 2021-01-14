package ru.job4j.tracker;

import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SqlTrackerTest {
    public Connection init() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            return DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void createItem() throws Exception {
        try (SqlTracker tracker = new SqlTracker(ConnectionRollback.create(this.init()))) {
            tracker.add(new Item(1, "desc"));
            assertThat(tracker.findByName("desc").size(), is(1));
        }
    }

    @Test
    public void replaceItem() throws Exception {
        try (SqlTracker tracker = new SqlTracker(ConnectionRollback.create(this.init()))) {
            int n = tracker.add(new Item(1, "desc")).getId();
            assertThat(tracker.findByName("desc").size(), is(1));
            tracker.replace(String.valueOf(n), new Item(1,"TEXT"));
            assertThat(tracker.findByName("TEXT").size(), is(1));
            assertThat(tracker.findByName("desc").size(), is(0));
        }
    }
    @Test
    public void deleteItem() throws Exception {
        try (SqlTracker tracker = new SqlTracker(ConnectionRollback.create(this.init()))) {
            int n = tracker.add(new Item(1, "desc")).getId();
            assertThat(tracker.findByName("desc").size(), is(1));
            tracker.delete(String.valueOf(n));
            assertThat(tracker.findByName("desc").size(), is(0));
        }
    }
    @Test
    public void findAllItem() throws Exception {
        try (SqlTracker tracker = new SqlTracker(ConnectionRollback.create(this.init()))) {
            int size = tracker.findAll().size();
            tracker.add(new Item(1, "desc1"));
            tracker.add(new Item(1, "desc2"));
            tracker.add(new Item(1, "desc3"));
            assertThat(tracker.findAll().size() - size, is(3));
        }
    }



}