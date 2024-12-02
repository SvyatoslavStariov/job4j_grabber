package ru.job4j.gc.leak;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public interface Generate {
    void generate();

    default List<String> read(String path) throws IOException {
        return Files.readAllLines(Paths.get(path));
    }
}
