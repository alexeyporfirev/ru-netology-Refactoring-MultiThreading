package ru.netology;

import ru.netology.server.Handler;
import ru.netology.server.Request;
import ru.netology.server.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private final static int PORT = 9999;

    public static void main(String[] args) {
        final var server = new Server();

        // добавление handler'ов (обработчиков)
        // В случае данного запроса GET просто возвращаем страницу index.html
        server.addHandler("GET", "/messages", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    final var filePath = Path.of(".", "public", "index.html");
                    final String mimeType = Files.probeContentType(filePath);
                    final var length = Files.readString(filePath).getBytes().length;
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseStream);
                    responseStream.flush();
                } catch (IOException e) {
                    System.out.println("Ошибка записи в поток: " + e.getMessage());
                }
            }
        });

        // В случае данного запроса POST просто возвращаем обратно содержимое, которое было послано на сервер в текстовом виде
        server.addHandler("POST", "/messages", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    final String mimeType = "text/plain";
                    final var length = request.getContent().getBytes().length;
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n" +
                                    request.getContent()
                    ).getBytes());
                    responseStream.flush();
                } catch (IOException e) {
                    System.out.println("Ошибка записи в поток: " + e.getMessage());
                }
            }
        });

        server.listen(PORT);
    }
}


