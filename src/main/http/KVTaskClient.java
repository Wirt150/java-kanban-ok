package http;

import consnant.Key;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final URL url;
    private final HttpClient httpClient;
    private final String apiToken;

    public KVTaskClient(URL url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
        this.apiToken = register();
    }

    public void put(Key key, String json) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url + "/save/" + key.toString() + "?" + "API_TOKEN=" + apiToken))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Упал метод put: " + this.httpClient.getClass());
        }
    }

    public String load(Key key) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url + "/load/" + key.toString() + "?" + "API_TOKEN=" + apiToken))
                .GET()
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return httpResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Упал метод load: " + this.httpClient.getClass());
        }
    }

    private String register() {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url + "/register/")).GET().build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return httpResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Упал метод register: " + this.httpClient.getClass());
        }
    }
}
