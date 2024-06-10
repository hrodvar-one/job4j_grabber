package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

public interface DateTimeParser {
    LocalDateTime parse(String date);
}
