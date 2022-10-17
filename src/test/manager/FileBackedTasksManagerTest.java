package manager;

import consnant.TaskManagerType;
import consnant.TaskType;
import org.junit.jupiter.api.Test;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;
import util.Managers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static manager.FileBackedTasksManager.loadFromFile;
import static org.junit.jupiter.api.Assertions.*;


public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    final Path file = Path.of(Managers.PATH_FILE);

    @Override
    protected FileBackedTasksManager createTaskManager() throws IOException {
        return (FileBackedTasksManager) Managers.getDefault(TaskManagerType.IN_FILE_BACKED_TASK_MANAGER);
    }

    @Test
    void whenTestEmptyTasksList() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)){
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<Task> taskList = taskManager.getAllTaskByType(TaskType.TASK);
        final List<Task> epicList = taskManager.getAllTaskByType(TaskType.EPIC);
        final List<Task> subtaskList = taskManager.getAllTaskByType(TaskType.SUBTASK);

        final FileBackedTasksManager fileBackedTasksManagerLoad = loadFromFile(file);

        final List<Task> taskListSave = fileBackedTasksManagerLoad.getAllTaskByType(TaskType.TASK);
        final List<Task> epicListSave = fileBackedTasksManagerLoad.getAllTaskByType(TaskType.EPIC);
        final List<Task> subtaskListSave = fileBackedTasksManagerLoad.getAllTaskByType(TaskType.SUBTASK);

        //test
        assertNotEquals(this.taskManager,fileBackedTasksManagerLoad, "Объекты не должны соответствовать");
        assertEquals(taskList, taskListSave, "Список должен отсутствовать");
        assertEquals(epicList, epicListSave, "Список должен отсутствовать");
        assertEquals(subtaskList, subtaskListSave, "Список должен отсутствовать");
    }

    @Test
    void whenCreateEpicThenSavedFile() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final int epicId = newEpic.getId();

        final FileBackedTasksManager fileBackedTasksManagerLoad = loadFromFile(file);
        final List<Task> epicList = fileBackedTasksManagerLoad.getAllTaskByType(TaskType.EPIC);
        final Epic reloadEpic = (Epic) epicList.get(0);
        final int reloadEpicId = reloadEpic.getId();

        //test
        assertNotEquals(this.taskManager,fileBackedTasksManagerLoad, "Объекты не должны соответствовать");
        assertNotNull(epicList, "Список не должен быть пустой");
        assertEquals(1, epicList.size(), "Размер списка должен быть равен 1");
        assertNotSame(newEpic, reloadEpic, "Задачи не должны совпадать по объекту в памяти");
        assertEquals(epicId, reloadEpicId, "Id должны быть идентичны");
        assertEquals(newEpic, reloadEpic, "Задачи должны быть идентичны");
    }

    @Test
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
         FileBackedTasksManager fileBackedTasksManagerLoad = loadFromFile(file);

        //test
        assertNotEquals(this.taskManager,fileBackedTasksManagerLoad, "Объекты не должны соответствовать");
        assertFalse(taskManager.getHistory().isEmpty(), "Список не должен быть пустой");
        assertFalse(fileBackedTasksManagerLoad.getHistory().isEmpty(), "Список не должен быть пустой");

        taskManager.deleteAllTasksByType(TaskType.SUBTASK);
        taskManager.deleteAllTasksByType(TaskType.EPIC);
        taskManager.deleteAllTasksByType(TaskType.TASK);
        fileBackedTasksManagerLoad = loadFromFile(file);

        //test
        assertTrue(taskManager.getHistory().isEmpty(), "Список должен быть пустой");
        assertTrue(fileBackedTasksManagerLoad.getHistory().isEmpty(), "Список должен быть пустой");
    }
}
