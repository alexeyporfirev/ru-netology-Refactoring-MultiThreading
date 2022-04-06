package ru.netology.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс объектов парсеров входящих запросов в объекты запросов
 */
public class RequestParser {

    /**
     * Распарсить входной поток и сформировать на основе его данных объект запроса
     * @param in Входной поток
     * @return Сформированный объект запроса
     * @throws IOException В случае ошибок чтения входного потока
     */
    public static Request parse(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        StringBuilder content = new StringBuilder("");
        String requestLine = in.readLine();
        final var parts = requestLine.strip().split(" ");
        if (parts.length != 3) {
            // just close socket
            return null;
        }
        final var type = parts[0];
        final var path = parts[1];
        final var HTTPVersion = parts[2];
        while (in.ready()) {
            requestLine = in.readLine();
            if (requestLine.matches("[A-Za-z-]+: .*")) {
                final var headersLine = requestLine.split(": ", 2);
                headers.put(headersLine[0], headersLine[1]);
            } else {
                content.append(requestLine)
                        .append("\r\n");
            }
        }
        return new Request(type, path, HTTPVersion, headers, content.toString());
    }
}
