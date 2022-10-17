package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import consnant.StatusType;
import consnant.TaskManagerType;
import consnant.TaskType;
import error.HttpTaskException;
import org.junit.platform.commons.util.StringUtils;
import util.Managers;
import manager.TaskManager;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static util.UtilGson.GSON;

public class HttpTaskServer {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String PUT = "PUT";
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final TaskManager taskManager;
    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault(TaskManagerType.HTTP_TASK_MANAGER);
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks", new AllTasksHandler());
        server.createContext("/history", new HistoryHandler());
        server.createContext("/tasks/task", new TaskHandler());
        server.createContext("/tasks/epic", new TaskHandler());
        server.createContext("/tasks/subtask", new TaskHandler());
        server.createContext("/tasks/subtask/epic", new SubtaskEpicHandler());
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private class AllTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "";

            try {
                if (httpExchange.getRequestMethod().equals(GET)) {
                    response = GSON.toJson(taskManager.getPrioritizedTask());
                    httpExchange.sendResponseHeaders(200, 0);
                } else {
                    response = httpExchange.getRequestMethod();
                    httpExchange.sendResponseHeaders(405, 0);
                    throw new HttpTaskException("Ожидался метод GET, а получил" + httpExchange.getRequestURI());
                }
            } catch (HttpTaskException e) {
                response = e.getMessage();
                System.out.println(response);
            } finally {
                try (OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "";

            try {
                if (httpExchange.getRequestMethod().equals(GET)) {
                    response = GSON.toJson(taskManager.getHistory());
                    httpExchange.sendResponseHeaders(200, 0);
                } else {
                    response = httpExchange.getRequestMethod();
                    httpExchange.sendResponseHeaders(405, 0);
                    throw new HttpTaskException("Ожидался метод GET, а получил: " + httpExchange.getRequestMethod());
                }
            } catch (HttpTaskException e) {
                response = e.getMessage();
                System.out.println(response);
            } finally {
                try (OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private class SubtaskEpicHandler implements HttpHandler {
        private static final int TASK_ID_LINE = 1;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "";
            String uriQuery = httpExchange.getRequestURI().getQuery();
            String method = httpExchange.getRequestMethod();
            try {
                if (uriQuery != null && uriQuery.length() >= 3) {
                    switch (method) {
                        case GET:
                            response = GSON.toJson(
                                    taskManager.getEpicSubtasks(Integer.valueOf(getUriData(uriQuery, TASK_ID_LINE))));
                            httpExchange.sendResponseHeaders(200, 0);
                            break;
                        default:
                            httpExchange.sendResponseHeaders(405, 0);
                            throw new HttpTaskException("Ожидался метод GET, а получил: " + httpExchange.getRequestMethod());
                    }
                } else {
                    httpExchange.sendResponseHeaders(404, 0);
                    throw new HttpTaskException("Неверный запрос со стороны пользователя.");
                }
            } catch (HttpTaskException e) {
                response = e.getMessage();
                System.out.println(response);
            } finally {
                try (OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private class TaskHandler implements HttpHandler {
        private static final int TASK_ID_LINE = 1;
        private static final int TASK_STATUS_LINE = 3;
        private static final int TASK_TYPE_LINE = 2;
        private TaskType taskType;
        private String response;

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            String method = httpExchange.getRequestMethod();
            URI uri = httpExchange.getRequestURI();
            String[] uriPath = uri.getPath().split("/");
            String uriQuery = uri.getQuery();

            for (TaskType taskTypeEnum : TaskType.values()) {
                if (uriPath[TASK_TYPE_LINE].equals(taskTypeEnum.toString().toLowerCase())) {
                    this.taskType = taskTypeEnum;
                }
            }
            try {
                switch (method) {
                    case GET:
                        response = getMethod(taskType, uriQuery, TASK_ID_LINE);
                        httpExchange.sendResponseHeaders(200, 0);
                        break;
                    case POST:
                        if (taskManager.createNewTask(taskByJson(taskType, httpExchange)) != null) {
                            response = String.valueOf(true);
                            httpExchange.sendResponseHeaders(201, 0);
                        } else {
                            response = String.valueOf(false);
                            httpExchange.sendResponseHeaders(402, 0);
                        }
                        break;
                    case DELETE:
                        response = String.valueOf(deleteMethod(taskType, uriQuery, TASK_ID_LINE));
                        httpExchange.sendResponseHeaders(200, 0);
                        break;
                    case PUT:
                        response = String.valueOf(taskManager.updateStatusTaskByTypeAndId(
                                taskByJson(taskType, httpExchange),
                                Integer.parseInt(getUriData(uriQuery, TASK_ID_LINE)),
                                StatusType.valueOf(getUriData(uriQuery, TASK_STATUS_LINE).toUpperCase())));
                        httpExchange.sendResponseHeaders(202, 0);
                        break;
                    default:
                        httpExchange.sendResponseHeaders(405, 0);
                        throw new HttpTaskException("Вызван недопустимый метод: " + httpExchange.getRequestMethod());
                }
            } catch (HttpTaskException e) {
                response = e.getMessage();
            } finally {
                try (OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                    response = null;
                }
            }
        }
    }

    private Task gsonByTaskType(TaskType taskType, String body) {
        switch (taskType) {
            case TASK:
                return GSON.fromJson(body, Task.class);
            case EPIC:
                return GSON.fromJson(body, Epic.class);
            case SUBTASK:
                return GSON.fromJson(body, Subtask.class);
            default:
                return null;
        }
    }

    private String getMethod(TaskType taskType, String uriQuery, int id) throws IOException {
        if (StringUtils.isBlank(uriQuery)) {
            return GSON.toJson(taskManager.getAllTaskByType(taskType));
        } else {
            return GSON.toJson(
                    taskManager.getTaskById(taskType, Integer.valueOf(getUriData(uriQuery, id))));
        }
    }

    private Task taskByJson(TaskType taskType, HttpExchange httpExchange) {
        InputStreamReader inputStream = new InputStreamReader(httpExchange.getRequestBody(), DEFAULT_CHARSET);
        BufferedReader bufferedReader = new BufferedReader(inputStream);
        String body = bufferedReader.lines().collect(Collectors.joining("\n"));
        return gsonByTaskType(taskType, body);
    }

    private boolean deleteMethod(TaskType taskType, String uriQuery, int id) {
        if (uriQuery == null) {
            taskManager.deleteAllTasksByType(taskType);
        } else {
            taskManager.deleteTaskByIdAndType(taskType, Integer.valueOf(getUriData(uriQuery, id)));
        }
        return true;
    }

    private String getUriData(String uriQuery, Integer line) {
        return uriQuery.split("[&=]")[line];
    }

}

