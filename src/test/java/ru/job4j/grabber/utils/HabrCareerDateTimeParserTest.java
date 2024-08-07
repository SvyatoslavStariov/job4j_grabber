package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HabrCareerDateTimeParserTest {

    @Test
    public void whenParserRightTimeIsPositiveResult() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime parse = parser.parse("2024-12-12T12:12:12+00:00");
        assertThat(parse).isEqualTo(LocalDateTime.of(2024, 12, 12, 12, 12, 12));
    }

    @Test
    public void whenParserWrongTimeIsException() {
        assertThrows(
            DateTimeParseException.class,
            () -> new HabrCareerDateTimeParser().parse("1 августа")
        );
    }
}