package http;

import com.google.gson.reflect.TypeToken;
import consnant.StatusType;
import org.junit.jupiter.api.Test;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.UtilGson.GSON;

public class HttpTaskServerTest {

    private final URI uri = URI.create("http://localhost:8080");
    private HttpTaskServer httpTaskServerTest;
    private HttpClient httpClient;
    private KVServer kvServer;

    public HttpTaskServerTest() throws IOException {
        this.kvServer = new KVServer();
        this.kvServer.start();
        this.httpTaskServerTest = new HttpTaskServer();
        this.httpTaskServerTest.start();
        this.httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        this.kvServer.stop();
        this.httpTaskServerTest.stop();
    }


    @Test
    void whenGetEpicIdSubtaskMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());

        final Subtask newSubtaskOne =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);
        final Subtask newSubtaskTwo =
                new Subtask("New", "Description", "01.01.2022,01:00", "15", 1);
        httpClient.send(requestPostBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPostBuilderByTask(newSubtaskTwo), HttpResponse.BodyHandlers.ofString());
        newSubtaskOne.setId(2);
        newSubtaskTwo.setId(3);

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(uri + "/tasks/subtask/epic/?id=1"))
                .GET()
                .build();

        final HttpResponse<String> httpResponse =
                httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasks = GSON.fromJson(httpResponse.body(), new TypeToken<LinkedList<Subtask>>() {
        }.getType());

        //test
        assertEquals(httpResponse.statusCode(), 200, "Ожидаемый код ответа от сервера: 200 ");
        assertEquals(subtasks.size(), 2, "Id Задачи должны совпадать");
        assertEquals(subtasks.get(0), newSubtaskOne, "Id Задачи должны совпадать");
        assertEquals(subtasks.get(1), newSubtaskTwo, "Id Задачи должны совпадать");
    }


    @Test
    void whenGetHistoryAndAllTasksMangerByAPI() throws IOException, InterruptedException {
        final Task newTask = new Task("New", "Description", "01.01.2022,00:00", "15");
        httpClient.send(requestPostBuilderByTask(newTask), HttpResponse.BodyHandlers.ofString());

        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());

        final Subtask newSubtask =
                new Subtask("New", "Description", "01.01.2022,01:00", "15", 2);
        httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());

        httpClient.send(requestGetBuilderByTaskById(newEpic, 2), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestGetBuilderByTaskById(newTask, 1), HttpResponse.BodyHandlers.ofString());

        newTask.setId(1);
        newEpic.setId(2);

        final HttpRequest history = HttpRequest.newBuilder().uri(URI.create(uri + "/history")).GET().build();

        final HttpResponse<String> httpResponseHistory = httpClient.send(history, HttpResponse.BodyHandlers.ofString());

        List<Task> historyList = GSON.fromJson(httpResponseHistory.body(), new TypeToken<LinkedList<? extends Task>>() {
        }.getType());

        //test
        assertEquals(httpResponseHistory.statusCode(), 200, "Ожидаемый код ответа от сервера: 200 ");
        assertEquals(historyList.get(0).getId(), newEpic.getId(), "Id Задачи должны совпадать");
        assertEquals(historyList.get(1).getId(), newTask.getId(), "Id Задачи должны совпадать");

        final HttpRequest allTasks = HttpRequest.newBuilder().uri(URI.create(uri + "/tasks")).GET().build();

        final HttpResponse<String> httpResponseTasks = httpClient.send(allTasks, HttpResponse.BodyHandlers.ofString());

        List<Task> tasksList = GSON.fromJson(httpResponseTasks.body(), new TypeToken<LinkedList<? extends Task>>() {
        }.getType());

        //test
        assertEquals(httpResponseTasks.statusCode(), 200, "Ожидаемый код ответа от сервера: 200 ");
        assertEquals(tasksList.size(), 3, "Ожидаемый размер списка: 3");
    }

    @Test
    void whenCreateTaskThenSaveMangerByAPI() throws IOException, InterruptedException {
        final Task newTask = new Task("New", "Description", "01.01.2022,00:00", "15");
        final HttpResponse<String> httpResponse = httpClient.send(requestPostBuilderByTask(newTask), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponse.statusCode(), 201, "Ожидаемый код ответа от сервера: 201.");
        assertEquals(httpResponse.body(), "true", "Ожидаемое сообщение в теле: true.");
    }

    @Test
    void whenCreateEpicThenSaveMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        final HttpResponse<String> httpResponse = httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponse.statusCode(), 201, "Ожидаемый код ответа от сервера: 201.");
        assertEquals(httpResponse.body(), "true", "Ожидаемое сообщение в теле: true.");
    }

    @Test
    void whenCreateSubtaskThenSaveMangerByAPI() throws IOException, InterruptedException {
        final Subtask newSubtask =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);

        final HttpResponse<String> httpResponseOne =
                httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseOne.statusCode(), 402, "Ожидаемый код ответа от сервера: 201.");
        assertEquals(httpResponseOne.body(), "false", "Ожидаемое сообщение в теле: false.");

        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseThree =
                httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseThree.statusCode(), 201, "Ожидаемый код ответа от сервера: 201.");
        assertEquals(httpResponseThree.body(), "true", "Ожидаемое сообщение в теле: true.");
    }

    @Test
    void whenCreateTasksThenGetMangerByAPI() throws IOException, InterruptedException {
        final Task newTaskOne = new Task("New", "Description", "01.01.2022,00:00", "15");
        final Task newTaskTwo = new Task("New", "Description", "01.01.2022,01:00", "15");

        httpClient.send(requestPostBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPostBuilderByTask(newTaskTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Task>>() {
        }.getType());
        newTaskOne.setId(1);
        newTaskTwo.setId(2);

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasks.size(), 2, "Ожидаемый размер листа: 2.");
        assertEquals(tasks.get(0), newTaskOne, "Задачи должны быть идентичны.");
        assertEquals(tasks.get(1), newTaskTwo, "Задачи должны быть идентичны.");
    }

    @Test
    void whenCreateEpicsThenGetMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpicOne = new Epic("newEpic", "newEpicDescription");
        final Epic newEpicTwo = new Epic("newEpic", "newEpicDescription");

        httpClient.send(requestPostBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPostBuilderByTask(newEpicTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Epic>>() {
        }.getType());
        newEpicOne.setId(1);
        newEpicTwo.setId(2);

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasks.size(), 2, "Ожидаемый размер листа: 2.");
        assertEquals(tasks.get(0), newEpicOne, "Задачи должны быть идентичны.");
        assertEquals(tasks.get(1), newEpicTwo, "Задачи должны быть идентичны.");
    }

    @Test
    void whenCreateSubtasksThenGetMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        final Subtask newSubtaskOne =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);
        final Subtask newSubtaskTwo =
                new Subtask("New", "Description", "01.01.2022,01:00", "15", 1);

        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPostBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPostBuilderByTask(newSubtaskTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Subtask>>() {
        }.getType());
        newSubtaskOne.setId(2);
        newSubtaskTwo.setId(3);

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasks.size(), 2, "Ожидаемый размер листа: 2.");
        assertEquals(tasks.get(0), newSubtaskOne, "Задачи должны быть идентичны.");
        assertEquals(tasks.get(1), newSubtaskTwo, "Задачи должны быть идентичны.");
    }

    @Test
    void whenCreateTasksThenDeleteMangerByAPI() throws IOException, InterruptedException {
        final Task newTaskOne = new Task("New", "Description", "01.01.2022,00:00", "15");

        httpClient.send(requestPostBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseOne =
                httpClient.send(requestGetBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksOne = GSON.fromJson(httpResponseOne.body(), new TypeToken<LinkedList<Task>>() {
        }.getType());

        //test
        assertEquals(httpResponseOne.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksOne.size(), 1, "Ожидаемый размер листа: 1.");

        final Task newTaskTwo = new Task("New", "Description", "01.01.2022,01:00", "15");
        httpClient.send(requestPostBuilderByTask(newTaskTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksTwo = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Task>>() {
        }.getType());

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksTwo.size(), 2, "Ожидаемый размер листа: 2.");

        httpClient.send(requestDelBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseLast =
                httpClient.send(requestGetBuilderByTask(newTaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksThree = GSON.fromJson(httpResponseLast.body(), new TypeToken<LinkedList<Task>>() {
        }.getType());

        //test
        assertEquals(httpResponseLast.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseLast.body(), "null", "Ожидаемое сообщение в теле: null.");
        assertNull(tasksThree, "Должен вернуться null.");
    }

    @Test
    void whenCreateEpicsThenDeleteMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpicOne = new Epic("newEpic", "newEpicDescription");

        httpClient.send(requestPostBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseOne =
                httpClient.send(requestGetBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksOne = GSON.fromJson(httpResponseOne.body(), new TypeToken<LinkedList<Epic>>() {
        }.getType());

        //test
        assertEquals(httpResponseOne.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksOne.size(), 1, "Ожидаемый размер листа: 1.");

        final Epic newEpicTwo = new Epic("newEpic", "newEpicDescription");

        httpClient.send(requestPostBuilderByTask(newEpicTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newEpicTwo), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksTwo = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Epic>>() {
        }.getType());

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksTwo.size(), 2, "Ожидаемый размер листа: 2.");

        httpClient.send(requestDelBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseLast =
                httpClient.send(requestGetBuilderByTask(newEpicOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksThree = GSON.fromJson(httpResponseLast.body(), new TypeToken<LinkedList<Epic>>() {
        }.getType());

        //test
        assertEquals(httpResponseLast.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseLast.body(), "null", "Ожидаемое сообщение в теле: null.");
        assertNull(tasksThree, "Должен вернуться null.");
    }

    @Test
    void whenCreateSubtasksThenDeleteMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());

        final Subtask newSubtaskOne =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);

        httpClient.send(requestPostBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseOne =
                httpClient.send(requestGetBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksOne = GSON.fromJson(httpResponseOne.body(), new TypeToken<LinkedList<Subtask>>() {
        }.getType());

        //test
        assertEquals(httpResponseOne.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksOne.size(), 1, "Ожидаемый размер листа: 1.");

        final Subtask newSubtaskTwo =
                new Subtask("New", "Description", "01.01.2022,01:00", "15", 1);

        httpClient.send(requestPostBuilderByTask(newSubtaskTwo), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseTwo =
                httpClient.send(requestGetBuilderByTask(newSubtaskTwo), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksTwo = GSON.fromJson(httpResponseTwo.body(), new TypeToken<LinkedList<Subtask>>() {
        }.getType());

        //test
        assertEquals(httpResponseTwo.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(tasksTwo.size(), 2, "Ожидаемый размер листа: 2.");

        httpClient.send(requestDelBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseLast =
                httpClient.send(requestGetBuilderByTask(newSubtaskOne), HttpResponse.BodyHandlers.ofString());
        List<Task> tasksThree = GSON.fromJson(httpResponseLast.body(), new TypeToken<LinkedList<Subtask>>() {
        }.getType());

        //test
        assertEquals(httpResponseLast.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseLast.body(), "null", "Ожидаемое сообщение в теле: null.");
        assertNull(tasksThree, "Должен вернуться null.");
    }

    @Test
    void whenCreateTaskThenGetAndDelByIdMangerByAPI() throws IOException, InterruptedException {
        final Task newTask = new Task("New", "Description", "01.01.2022,00:00", "15");

        httpClient.send(requestPostBuilderByTask(newTask), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseGet =
                httpClient.send(requestGetBuilderByTaskById(newTask, 1), HttpResponse.BodyHandlers.ofString());
        newTask.setId(1);
        final Task serverTask = GSON.fromJson(httpResponseGet.body(), Task.class);

        //test
        assertEquals(httpResponseGet.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGet.body(), GSON.toJson(newTask), "Json должны быть идентичны");
        assertEquals(newTask, serverTask, "Задачи должны совпадать");

        final HttpResponse<String> httpResponseDel =
                httpClient.send(requestDelBuilderByTaskById(newTask), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseDel.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");

        final HttpResponse<String> httpResponseGetById =
                httpClient.send(requestGetBuilderByTaskById(newTask, 1), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseGetById.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGetById.body(), "null", "Должен вернуться null");
    }

    @Test
    void whenCreateEpicThenGetAndDelByIdMangerByAPI() throws IOException, InterruptedException {
        final Epic newEpic = new Epic("newEpic", "newEpicDescription");

        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());
        final HttpResponse<String> httpResponseGet =
                httpClient.send(requestGetBuilderByTaskById(newEpic, 1), HttpResponse.BodyHandlers.ofString());
        newEpic.setId(1);
        final Epic serverTask = GSON.fromJson(httpResponseGet.body(), Epic.class);

        //test
        assertEquals(httpResponseGet.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGet.body(), GSON.toJson(newEpic), "Json должны быть идентичны");
        assertEquals(newEpic, serverTask, "Задачи должны совпадать");

        final HttpResponse<String> httpResponseDel =
                httpClient.send(requestDelBuilderByTaskById(newEpic), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseDel.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");

        final HttpResponse<String> httpResponseGetById =
                httpClient.send(requestGetBuilderByTaskById(newEpic, 1), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseGetById.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGetById.body(), "null", "Должен вернуться null");
    }

    @Test
    void whenCreateSubtaskThenGetAndDelByIdMangerByAPI() throws IOException, InterruptedException {
        Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());

        final Subtask newSubtask =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);

        httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(newSubtask.getType()).toLowerCase() + "/?id=2"))
                .GET()
                .build();

        final HttpResponse<String> httpResponseGet = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        newSubtask.setId(2);
        final Subtask serverTask = GSON.fromJson(httpResponseGet.body(), Subtask.class);

        //test
        assertEquals(httpResponseGet.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGet.body(), GSON.toJson(newSubtask), "Json должны быть идентичны");
        assertEquals(newSubtask, serverTask, "Задачи должны совпадать");

        final HttpResponse<String> httpResponseDel =
                httpClient.send(requestDelBuilderByTaskById(newSubtask), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseDel.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");

        final HttpResponse<String> httpResponseGetById =
                httpClient.send(requestGetBuilderByTaskById(newSubtask, 1), HttpResponse.BodyHandlers.ofString());

        //test
        assertEquals(httpResponseGetById.statusCode(), 200, "Ожидаемый код ответа от сервера: 200.");
        assertEquals(httpResponseGetById.body(), "null", "Должен вернуться null");
    }

    @Test
    void whenCreateTaskAndUpdateStatusMangerByAPI() throws IOException, InterruptedException {
        final Task newTask = new Task("New", "Description", "01.01.2022,00:00", "15");

        httpClient.send(requestPostBuilderByTask(newTask), HttpResponse.BodyHandlers.ofString());
        httpClient.send(requestPutBuilderByTask(newTask), HttpResponse.BodyHandlers.ofString());

        final HttpResponse<String> httpResponseGet =
                httpClient.send(requestGetBuilderByTaskById(newTask, 1), HttpResponse.BodyHandlers.ofString());

        final Task serverTask = GSON.fromJson(httpResponseGet.body(), Task.class);

        //test
        assertNotEquals(newTask, serverTask, "Задачи не должны совпадать.");
        assertEquals(newTask.getStatus(), StatusType.NEW, "Ожидаемый статус NEW.");
        assertEquals(serverTask.getStatus(), StatusType.IN_PROGRESS, "Ожидаемый статус IN_PROGRESS.");
    }

    @Test
    void whenCreateSubtaskAndUpdateStatusMangerByAPI() throws IOException, InterruptedException {
        Epic newEpic = new Epic("newEpic", "newEpicDescription");
        httpClient.send(requestPostBuilderByTask(newEpic), HttpResponse.BodyHandlers.ofString());
        final Subtask newSubtask =
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 1);

        final String gsonTask = GSON.toJson(newSubtask);
        httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());
        HttpRequest httpRequestPut = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(newSubtask.getType()).toLowerCase() + "/?id=2&status=in_progress"))
                .PUT(HttpRequest.BodyPublishers.ofString(gsonTask))
                .build();

        httpClient.send(requestPostBuilderByTask(newSubtask), HttpResponse.BodyHandlers.ofString());
        httpClient.send(httpRequestPut, HttpResponse.BodyHandlers.ofString());

        HttpRequest httpRequestGet = HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(newSubtask.getType()).toLowerCase() + "/?id=2"))
                .GET()
                .build();

        final HttpResponse<String> httpResponseGet =
                httpClient.send(httpRequestGet, HttpResponse.BodyHandlers.ofString());

        final Subtask serverTask = GSON.fromJson(httpResponseGet.body(), Subtask.class);

        //test
        assertNotEquals(newSubtask, serverTask, "Задачи не должны совпадать.");
        assertEquals(newSubtask.getStatus(), StatusType.NEW, "Ожидаемый статус NEW.");
        assertEquals(serverTask.getStatus(), StatusType.IN_PROGRESS, "Ожидаемый статус IN_PROGRESS.");
    }


    private HttpRequest requestPostBuilderByTask(Task task) {
        final String gsonTask = GSON.toJson(task);
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/"))
                .POST(HttpRequest.BodyPublishers.ofString(gsonTask))
                .build();

    }

    private HttpRequest requestGetBuilderByTask(Task task) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/"))
                .GET()
                .build();

    }

    private HttpRequest requestGetBuilderByTaskById(Task task, Integer id) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/?id=" + id))
                .GET()
                .build();
    }

    private HttpRequest requestDelBuilderByTask(Task task) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/"))
                .DELETE()
                .build();

    }

    private HttpRequest requestDelBuilderByTaskById(Task task) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/?id=1"))
                .DELETE()
                .build();

    }

    private HttpRequest requestPutBuilderByTask(Task task) {
        final String gsonTask = GSON.toJson(task);
        return HttpRequest.newBuilder()
                .uri(URI.create(uri + "/tasks/" + String.valueOf(task.getType()).toLowerCase() + "/?id=1&status=in_progress"))
                .PUT(HttpRequest.BodyPublishers.ofString(gsonTask))
                .build();
    }

}



