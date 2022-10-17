package manager;

import consnant.TaskManagerType;
import consnant.TaskType;
import http.KVServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;
import util.Managers;

import java.io.IOException;
import java.util.List;

import static manager.HTTPTaskManager.loadFromServer;
import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskManagerTest extends FileBackedTasksManagerTest {

    final KVServer kvServer;

    public HTTPTaskManagerTest() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
    }

    @AfterEach
    void close() {
        kvServer.stop();
    }

    @Override
    protected HTTPTaskManager createTaskManager() throws IOException {
        return (HTTPTaskManager) Managers.getDefault(TaskManagerType.HTTP_TASK_MANAGER);
    }

    @Test
    @Override
    void whenTestEmptyTasksList() {

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        HTTPTaskManager httpTaskManager = loadFromServer();
                    }
                }
        );

        //test
        assertEquals(exception.getMessage(), "Упал метод load: class jdk.internal.net.http.HttpClientFacade",
                "При пустом неинициализированном сервере должна выброситься ошибка");

        final List<Task> taskList = taskManager.getAllTaskByType(TaskType.TASK);
        final List<Task> epicList = taskManager.getAllTaskByType(TaskType.EPIC);
        final List<Task> subtaskList = taskManager.getAllTaskByType(TaskType.SUBTASK);

        taskManager.saveFile();

        final HTTPTaskManager httpTaskManagerLoad = loadFromServer();

        final List<Task> taskListSave = httpTaskManagerLoad.getAllTaskByType(TaskType.TASK);
        final List<Task> epicListSave = httpTaskManagerLoad.getAllTaskByType(TaskType.EPIC);
        final List<Task> subtaskListSave = httpTaskManagerLoad.getAllTaskByType(TaskType.SUBTASK);

        //test
        assertNotEquals(this.taskManager, httpTaskManagerLoad, "Объекты не должны соответствовать");
        assertEquals(taskList, taskListSave, "Список должен отсутствовать");
        assertEquals(epicList, epicListSave, "Список должен отсутствовать");
        assertEquals(subtaskList, subtaskListSave, "Список должен отсутствовать");
    }

    @Test
    @Override
    void whenCreateEpicThenSavedFile() {

        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final int epicId = newEpic.getId();

        final HTTPTaskManager httpTaskManagerLoad = loadFromServer();
        final List<Task> epicList = httpTaskManagerLoad.getAllTaskByType(TaskType.EPIC);
        final Epic reloadEpic = (Epic) epicList.get(0);
        final int reloadEpicId = reloadEpic.getId();

        //test
        assertNotEquals(this.taskManager, httpTaskManagerLoad, "Объекты не должны соответствовать");
        assertNotNull(epicList, "Список не должен быть пустой");
        assertEquals(1, epicList.size(), "Размер списка должен быть равен 1");
        assertNotSame(newEpic, reloadEpic, "Задачи не должны совпадать по объекту в памяти");
        assertEquals(epicId, reloadEpicId, "Id должны быть идентичны");
        assertEquals(newEpic, reloadEpic, "Задачи должны быть идентичны");
    }

    @Test
    @Override
    void whenFilAndCleanHistoryListThenLoadNewManager() {
        final Task newTask = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final Epic newEpic = (Epic) taskManager.createNewTask(
                new Epic("New", "Description"));
        final Subtask newSubtask = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));

        taskManager.getTaskById(TaskType.TASK, newTask.getId());
        taskManager.getTaskById(TaskType.TASK, newTask.getId());
        taskManager.getTaskById(TaskType.EPIC, newEpic.getId());
        taskManager.getTaskById(TaskType.EPIC, newEpic.getId());
        taskManager.getTaskById(TaskType.SUBTASK, newSubtask.getId());
        taskManager.getTaskById(TaskType.SUBTASK, newSubtask.getId());
        HTTPTaskManager httpTaskManagerLoad = loadFromServer();

        //test
        assertNotEquals(this.taskManager, httpTaskManagerLoad, "Объекты не должны соответствовать");
        assertFalse(taskManager.getHistory().isEmpty(), "Список не должен быть пустой");
        assertFalse(httpTaskManagerLoad.getHistory().isEmpty(), "Список не должен быть пустой");

        taskManager.deleteAllTasksByType(TaskType.SUBTASK);
        taskManager.deleteAllTasksByType(TaskType.EPIC);
        taskManager.deleteAllTasksByType(TaskType.TASK);
        httpTaskManagerLoad = loadFromServer();

        //test
        assertTrue(taskManager.getHistory().isEmpty(), "Список должен быть пустой");
        assertTrue(httpTaskManagerLoad.getHistory().isEmpty(), "Список должен быть пустой");
    }
}
