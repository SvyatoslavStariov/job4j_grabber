package ru.job4j.gc.leak.models;

import java.util.Objects;

@SuppressWarnings("unused")
public class Comment {
    private String text;

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return Objects.equals(text, comment.text) && Objects.equals(user, comment.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, user);
    }
}
