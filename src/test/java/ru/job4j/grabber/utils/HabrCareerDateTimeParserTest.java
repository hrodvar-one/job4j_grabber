package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    void whenParseValidDateTimeThenReturnLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTimeString = "2024-06-10T14:30:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 6, 10, 14, 30, 0);

        LocalDateTime result = parser.parse(dateTimeString);

        assertEquals(expectedDateTime, result);
    }

    @Test
    void whenParseInvalidDateTimeThenThrowException() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String invalidDateTimeString = "invalid-date-time";

        assertThrows(DateTimeParseException.class, () -> {
            parser.parse(invalidDateTimeString);
        });
    }

    @Test
    void whenParseAnotherValidDateTimeThenReturnLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTimeString = "2022-01-01T00:00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0);

        LocalDateTime result = parser.parse(dateTimeString);

        assertEquals(expectedDateTime, result);
    }
}