package ru.job4j.tracker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SqlTracker implements Store {
    private Connection cn;
    private final String tableName = "item";

    public void init() {
//        try (InputStream in = SqlTracker.class.getClassLoader().
//                getResourceAsStream("app.properties")) {
         try (InputStream in = new FileInputStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (cn != null) {
            cn.close();
        }
    }

    @Override
    public Item add(Item item)  {
        try (PreparedStatement statement =
                     cn.prepareStatement("insert into item (name) values (?)",
                             Statement.RETURN_GENERATED_KEYS)) {
       //     statement.setString(1, tableName);
            statement.setString(1, item.getName());
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public boolean replace(String id, Item item) {
        boolean result = false;
        try (PreparedStatement statement =
                     cn.prepareStatement("update ? set name = ? where id = ?")) {
            statement.setString(1, tableName);
            statement.setString(2, item.getName());
            statement.setInt(3, item.getId());
            result = statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean delete(String id) {
        boolean result = false;
        try (PreparedStatement statement =
                     cn.prepareStatement("delete from item where id = ?")) {
         //   statement.setString(1, tableName);
            statement.setInt(1, Integer.parseInt(id));
            result = statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = cn.prepareStatement("select * from item")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new Item(
                            resultSet.getInt("id"),
                            resultSet.getString("name")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<Item> findByName(String key) {
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = cn.prepareStatement(
                "select * from item where name LIKE ?")) {
 //           statement.setString(1, tableName);
            statement.setString(1, "%" + key + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new Item(
                            resultSet.getInt("id"),
                            resultSet.getString("name")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Item findById(String id) {
        Item item = null;
        try (PreparedStatement statement = cn.prepareStatement("select * from item where id = ?")) {
            statement.setInt(1, Integer.parseInt(id));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    item = new Item(
                            resultSet.getInt("id"),
                            resultSet.getString("name")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public static void main(String[] args) throws Exception {
        SqlTracker sqlTracker = new SqlTracker();
        sqlTracker.init();
        sqlTracker.add(new Item(5, "first"));
        sqlTracker.add(new Item(5, "second"));
        sqlTracker.add(new Item(5, "thread"));
        System.out.println("Вывод всей базы");
        sqlTracker.findAll().forEach(System.out::println);
        sqlTracker.delete("3");
        System.out.println("Вывод после удаления записи 3");
        sqlTracker.findAll().forEach(System.out::println);
        System.out.println("вывод id=2 " + sqlTracker.findById("2"));
        System.out.println("Вывод результатов поиска по ключу");
        sqlTracker.findByName("irs").forEach(System.out::println);
        sqlTracker.close();
    }
}
