package ru.job4j.gc.leak;

import ru.job4j.gc.leak.models.Post;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PostStore {

    private final Map<Integer, Post> posts = new HashMap<>();

    private final AtomicInteger atomicInteger = new AtomicInteger(1);

    public Collection<Post> getPosts() {
        return posts.values();
    }

    public Post add(Post post) {
        int id = atomicInteger.getAndIncrement();
        post.setId(id);
        posts.put(id, post);
        return post;
    }

    public void removeAll() {
        posts.clear();
    }

    public void createPost(CommentGenerator commentGenerator,
                            UserGenerator userGenerator,
                            String text) {
        userGenerator.generate();
        commentGenerator.generate();
        var post = new Post();
        post.setText(text);
        post.setComments(commentGenerator.getComments());
        this.add(post);
    }
}