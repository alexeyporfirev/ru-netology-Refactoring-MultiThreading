package ru.netology.server;

import java.io.BufferedOutputStream;

/**
 * Функционаяльнйы интерфейс для обработчика запросов
 */
@FunctionalInterface
public interface Handler {

    public void handle(Request request, BufferedOutputStream responseStream);
}
