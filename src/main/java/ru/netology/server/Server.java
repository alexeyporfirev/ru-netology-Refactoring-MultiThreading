package ru.netology.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Класс сервера обработки запросов
 */
public class Server {

    private final static String VALID_PATHS = "validPaths.txt";
    private final static int SERVER_PORT = 9999;
    private final static int THREAD_NUMBER = 64;
    private final List<String> validPaths;

    private ServerSocket serverSocket;
    private final ExecutorService executor;

    /**
     * Создание объекта сервера с одновременным получением списка допустимых адресов и созданием пула потоков
     */
    public Server() {
        validPaths = getValidPaths(VALID_PATHS);
        executor = Executors.newFixedThreadPool(THREAD_NUMBER);
    }

    /**
     * Запуск сервера
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
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
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
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
}
