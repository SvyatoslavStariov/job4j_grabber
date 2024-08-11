package ru.job4j.grabber;

import ru.job4j.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) throws SQLException {
        try {
            Class.forName(config.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        connection = DriverManager.getConnection(config.getProperty("url"),
            config.getProperty("username"),
            config.getProperty("password"));
    }

    @Override
    public void save(Post post) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO posts(name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT(link)"
                                                                               + "DO UPDATE SET"
                                                                               + " name = EXCLUDED.name,\n"
                                                                               + " text = EXCLUDED.text,\n"
                                                                               + " created = EXCLUDED.created;")) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM posts")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Post post = createPost(resultSet);
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM posts WHERE posts.id = ?")) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return createPost(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getInt("id"));
        post.setTitle(resultSet.getString("name"));
        post.setDescription(resultSet.getString("text"));
        post.setLink(resultSet.getString("link"));
        post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
        return post;
    }

    public static void main(String[] args) throws IOException{
        try (InputStream input = PsqlStore.class.getClassLoader()
            .getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(input);
            try (Store store = new PsqlStore(config)) {
                store.save(new Post("title1","link1","description1", LocalDateTime.now()));
                store.save(new Post("title2","link1","description2", LocalDateTime.now()));
                store.save(new Post("title3","link3","description3", LocalDateTime.now()));
                List<Post> posts = store.getAll();
                posts.forEach(System.out::println);
                Post post = store.findById(posts.get(0).getId());
                System.out.println();
                System.out.println(post);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}