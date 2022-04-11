package ru.netology.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс сервера обработки запросов
 */
public class Server {

    private final static String VALID_PATHS = "validPaths.txt";
    private final static int THREAD_NUMBER = 64;
    private final List<String> validPaths;

    private ServerSocket serverSocket;
    private final ExecutorService executor;

    private Map<Map<String, String>, Handler> handlers;

    /**
     * Создание объекта сервера с одновременным получением списка допустимых адресов и созданием пула потоков
     */
    public Server() {
        validPaths = getValidPaths(VALID_PATHS);
        executor = Executors.newFixedThreadPool(THREAD_NUMBER);
        handlers = new ConcurrentHashMap<>();
    }

    /**
     * Запуск сервера
     */
    public void listen(int port) {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                final var socket = serverSocket.accept();
                log("[INFO] Новое подключение: " + socket.getRemoteSocketAddress());
                executor.execute(() -> handleNewConnection(socket));
            }
        } catch (IOException e) {
            log("[ERROR] Ошибка запуска сервера: " + e.getMessage());
        }
    }

    /**
     * Обработка запроса от нового подключения
     * @param socket Сокет обрабатываемого подключения
     */
    public void handleNewConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            Request req = RequestParser.parse(in);
            Handler handler = null;
            for(Map.Entry<Map<String, String>, Handler> entry : handlers.entrySet()) {
                if(entry.getKey().containsKey(req.getType()) && entry.getKey().containsValue(req.getPath())) {
                    handler = entry.getValue();
                }
            }
            if (handler != null) {
                handler.handle(req, out);
            }
        } catch (IOException e) {
            log("[ERROR] Ошибка установки нового соединения: " + e.getMessage());
        }
    }

    /**
     * Получение списка допустимых адресов, находящихся в файле
     * @param filename Имя файла, содержащего допустимые адреса
     * @return Список допустимых адресов для сервера
     */
    private List<String> getValidPaths(String filename) {
        List<String> paths = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/" + filename)))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                paths.add(line);
            }
        } catch (FileNotFoundException e) {
            log("[ERROR] Файл " + filename + " не найден");
        } catch (IOException e) {
            log("[ERROR] Ошибка чтения из файла " + filename);
        }
        return paths;
    }

    /**
     * Логирование сообщений
     * @param message Сообщение для логирования
     */
    private void log(String message) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + ": " + message);
    }

    /**
     * Получение списка допустимых адресов для сервера
     * @return Список допустимых адресов для сервера
     */
    public List<String> getValidPaths() {
        return validPaths;
    }

    /**
     * Добавляем нового обработчика запроса в список
     * @param type Тип запроса
     * @param path Запрашиваемый адрес
     * @param handler Обработчик для запроса такого типа
     */
    public void addHandler(String type, String path, Handler handler) {
        Map<String, String> data = new HashMap<>();
        data.put(type, path);
        handlers.put(data, handler);
    }
}
