package ru.netology.server;

import java.util.Map;

/**
 * Класс объектов запроса
 * Пример ниже:
 * GET /path HTTP/1.1\r\n
 * HTTP/1.1 404 Not Found\r\n +
 * Content-Length: 0\r\n +
 * Connection: close\r\n +
 * \r\n
 * content
 */
public class Request {

    private String type;
    private String path;
    private String HTTPVersion;
    private Map<String, String> headers;
    private String content;

    public Request(String type, String path, String HTTPVersion, Map<String, String> headers, String content) {
        this.type = type;
        this.path = path;
        this.HTTPVersion = HTTPVersion;
        this.headers = headers;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getHTTPVersion() {
        return HTTPVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(type)
                .append(" ")
                .append(path)
                .append(" ")
                .append(HTTPVersion)
                .append("\r\n");
        headers.entrySet().stream().forEach((element)->res.append(element.getKey())
                .append(": ")
                .append(element.getValue())
                .append("\r\n"));
        res.append("\r\n")
                .append(content);
        return res.toString();
    }
}
