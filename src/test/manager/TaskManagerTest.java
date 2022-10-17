package manager;

import consnant.StatusType;
import consnant.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createTaskManager() throws IOException;

    @BeforeEach
    void createManager() throws IOException {
        taskManager = createTaskManager();
    }

    @Test
    void whenCreateNewTaskAndThenGetFromList() {
        final Task newTask = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15")
        );
        final List<Task> tasks = taskManager.getAllTaskByType(TaskType.TASK);

        //test
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(newTask, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void whenCreateNewEpicAndThenGetFromList() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("newEpic", "newEpicDescription"));
        final List<Task> tasks = taskManager.getAllTaskByType(TaskType.EPIC);

        //test
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(newEpic, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void whenCreateNewSubtaskThenGetFromList() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final Subtask newSubtaskSecond = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));
        final List<Task> tasksFirst = taskManager.getAllTaskByType(TaskType.SUBTASK);

        //test
        assertNotNull(tasksFirst, "Задачи не возвращаются.");
        assertEquals(2, tasksFirst.size(), "Неверное количество задач.");
        assertEquals(newSubtaskFirst, tasksFirst.get(0), "Задачи не совпадают.");
        assertEquals(newSubtaskSecond, tasksFirst.get(1), "Задачи не совпадают.");
    }

    @Test
    void whenGetSubtaskListIdThenGetting() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("newEpic", "newEpicDescription"));
        List<Subtask> subtaskList = taskManager.getEpicSubtasks(newEpic.getId());

        //test
        assertNull(subtaskList, "Подзадач не должно существовать.");

        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final Subtask newSubtaskSecond = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));

        subtaskList = taskManager.getEpicSubtasks(newEpic.getId());

        //test
        assertEquals(2, subtaskList.size(), "Неверное количество задач.");
        assertEquals(subtaskList.get(0), newSubtaskFirst, "Неверная задача");
        assertEquals(subtaskList.get(1), newSubtaskSecond, "Неверная задача");
    }

    @Test
    void whenCreateHistoryThenCreatedAndTest() {
        List<Task> historyList = taskManager.getHistory();

        //test
        assertEquals(0, historyList.size(), "История должна быть пустой");

        final Task newTask = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("newEpic","newEpicDescription"));
        final Subtask newSubtask = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));

        taskManager.getTaskById(TaskType.TASK, newTask.getId());
        taskManager.getTaskById(TaskType.TASK, newTask.getId());
        taskManager.getTaskById(TaskType.EPIC, newEpic.getId());
        taskManager.getTaskById(TaskType.EPIC, newEpic.getId());
        taskManager.getTaskById(TaskType.SUBTASK, newSubtask.getId());
        taskManager.getTaskById(TaskType.SUBTASK, newSubtask.getId());

        historyList = taskManager.getHistory();

        //test
        assertEquals(3, historyList.size(), "В истории должно быть 3 задачи");
        taskManager.deleteAllTasksByType(TaskType.SUBTASK);
        historyList = taskManager.getHistory();
        assertFalse(historyList.contains(newSubtask), "В истории не должно быть удаляемой задачи");

        //test
        assertEquals(2, historyList.size(), "В истории должно быть 2 задачи");
        taskManager.deleteAllTasksByType(TaskType.EPIC);
        historyList = taskManager.getHistory();
        assertFalse(historyList.contains(newEpic), "В истории не должно быть удаляемой задачи");

        //test
        assertEquals(1, historyList.size(), "В истории должно быть 1 задача");
        taskManager.deleteAllTasksByType(TaskType.TASK);
        historyList = taskManager.getHistory();
        assertFalse(historyList.contains(newTask), "В истории не должно быть удаляемой задачи");

        //test
        assertEquals(0, historyList.size(), "История должна быть пустой");
    }

    @Test
    void whenCreateNewTaskThenCreated() {
        final Task newTask = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final int taskId = newTask.getId();
        final Task savedTask = taskManager.getTaskById(TaskType.TASK, newTask.getId());

        //test
        assertNotNull(newTask, "Задача не найдена.");
        assertEquals(newTask, savedTask, "Задачи не совпадают.");
        assertEquals(taskId, savedTask.getId(), "Неверный Id");
    }

    @Test
    void whenCreateNewEpicThenCreated() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final int epicId = newEpic.getId();
        final Epic savedEpic = (Epic) taskManager.getTaskById(TaskType.EPIC, newEpic.getId());

        //test
        assertNotNull(newEpic, "Задача не найдена.");
        assertEquals(newEpic, savedEpic, "Задачи не совпадают.");
        assertEquals(epicId, savedEpic.getId(), "Неверный Id");
    }

    @Test
    void whenCreateNewSubtaskThenNotCreated() {
        final Subtask newSubtask = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", 0));

        //test
        assertNull(newSubtask, "Задача не должна создаваться без Эпика.");
    }

    @Test
    void whenCreateNewSubtaskThenCreated() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final Subtask newSubtask = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final int subtaskId = newSubtask.getId();
        final int epicIdFromNewSubtask = newSubtask.getEpicId();
        Subtask savedSubtask = (Subtask) taskManager.getTaskById(TaskType.SUBTASK, newSubtask.getId());

        //test
        assertNotNull(newSubtask, "Задача не найдена.");
        assertEquals(newSubtask, savedSubtask, "Задачи не совпадают.");
        assertEquals(subtaskId, savedSubtask.getId(), "Неверный Id");
        assertEquals(epicIdFromNewSubtask, savedSubtask.getEpicId(), "Id эпика должны совпасть");
    }

    @Test
    void whenDeleteAllTasksThenDeleted() {
        taskManager.createNewTask(new Task("New", "Description", "01.01.2022,00:00", "15"));
        taskManager.createNewTask(new Task("New", "Description", "01.01.2022,00:15", "15"));
        List<Task> tasksList = taskManager.getAllTaskByType(TaskType.TASK);

        //test
        assertNotNull(tasksList, "Список не должен быть пустым");
        taskManager.deleteAllTasksByType(TaskType.TASK);
        tasksList = taskManager.getAllTaskByType(TaskType.TASK);

        //test
        assertNull(tasksList, "Список не пустой");
    }

    @Test
    void whenDeleteAllEpicThenDeleted() {
        taskManager.createNewTask(new Epic("New", "Description"));
        taskManager.createNewTask(new Epic("New", "Description"));
        List<Task> epicList = taskManager.getAllTaskByType(TaskType.EPIC);

        //test
        assertNotNull(epicList, "Список не должен быть пустым");
        taskManager.deleteAllTasksByType(TaskType.EPIC);
        epicList = taskManager.getAllTaskByType(TaskType.EPIC);

        //test
        assertNull(epicList, "Список не пустой");
    }

    @Test
    void whenDeleteAllSubtaskThenDeleted() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));
        List<Task> subtaskList = taskManager.getAllTaskByType(TaskType.SUBTASK);

        //test
        assertNotNull(subtaskList, "Список не должен быть пустым");
        taskManager.deleteAllTasksByType(TaskType.SUBTASK);
        subtaskList = taskManager.getAllTaskByType(TaskType.SUBTASK);

        //test
        assertNull(subtaskList, "Список не пустой");
    }

    @Test
    void whenDeleteIdTaskThenDeleted() {
        final Task newTaskFirst = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final int taskFirstId = newTaskFirst.getId();
        final Task newTaskSecond = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:15", "15"));
        final int taskSecondId = newTaskSecond.getId();

        taskManager.deleteTaskByIdAndType(TaskType.TASK, taskFirstId);

        List<Task> tasksList = taskManager.getAllTaskByType(TaskType.TASK);
        final Task taskFromList = tasksList.get(0);
        final int taskFromListId = taskFromList.getId();

        //test
        assertNotNull(tasksList, "Список задач не должен быть пустой");
        assertEquals(1, tasksList.size(), "Неверный размер списка");
        assertNotEquals(newTaskFirst, taskFromList, "Задачи не должны совпадать");
        assertEquals(taskSecondId, taskFromListId, "Id должны совпадать");
    }

    @Test
    void whenDeleteIdEpicThenDeleted() {
        final Epic newEpicFirst = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final int epicFirstId = newEpicFirst.getId();
        final Epic newEpicSecond = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final int epicSecondId = newEpicSecond.getId();

        taskManager.deleteTaskByIdAndType(TaskType.EPIC, epicFirstId);

        List<Task> tasksList = taskManager.getAllTaskByType(TaskType.EPIC);
        final Epic epicFromList = (Epic) tasksList.get(0);
        final int taskFromListId = epicFromList.getId();

        //test
        assertNotNull(tasksList, "Список задач не должен быть пустой");
        assertEquals(1, tasksList.size(), "Неверный размер списка");
        assertNotEquals(newEpicFirst, epicFromList, "Задачи не должны совпадать");
        assertEquals(epicSecondId, taskFromListId, "Id должны совпадать");
    }

    @Test
    void whenDeleteIdSubtaskThenDeleted() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final int subtaskIdFirst = newSubtaskFirst.getId();
        final Subtask newSubtaskSecond = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));
        final int subtaskIdSecond = newSubtaskSecond.getId();

        taskManager.deleteTaskByIdAndType(TaskType.SUBTASK, subtaskIdFirst);

        List<Task> tasksList = taskManager.getAllTaskByType(TaskType.SUBTASK);
        final Subtask subtaskFromList = (Subtask) tasksList.get(0);
        final int subtaskFromListId = subtaskFromList.getId();

        //test
        assertNotNull(tasksList, "Список задач не должен быть пустой");
        assertEquals(1, tasksList.size(), "Неверный размер списка");
        assertNotEquals(newSubtaskFirst, subtaskFromList, "Задачи не должны совпадать");
        assertEquals(subtaskIdSecond, subtaskFromListId, "Id должны совпадать");


    }

    @Test
    void whenDeleteIdEpicThenDeletedSubtask() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));

        //test
        assertEquals(taskManager.getAllTaskByType(
                TaskType.SUBTASK).size(),newEpic.getSubTaskIdList().size(),"Cписки должны совпадать");
        taskManager.deleteTaskByIdAndType(TaskType.EPIC, newEpic.getId());

        //test
        assertNull(taskManager.getAllTaskByType(TaskType.SUBTASK), "Список должен быть пустой");
    }

    @Test
    void whenUpdateTaskThenUpdate() {
        final Task newTask = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final int newTaskId = newTask.getId();

        //test
        assertEquals(StatusType.NEW, newTask.getStatus(), "Статус должен быть NEW");

        final Task taskUpdate = new Task("New", "Description", "01.01.2022,00:00", "15");
        taskManager.updateStatusTaskByTypeAndId(taskUpdate, newTaskId, StatusType.IN_PROGRESS);
        final int updateTaskId = taskUpdate.getId();

        //test
        assertNotEquals(newTask, taskUpdate, "Задачи не должны совпадать");
        assertEquals(newTaskId, updateTaskId, "Id должны совпадать");
        assertEquals(StatusType.IN_PROGRESS, taskUpdate.getStatus(), "Поле должно быть IN_PROGRESS");

        final Task taskUpdateTwo = new Task("New", "Description", "01.01.2022,00:00", "15");
        taskManager.updateStatusTaskByTypeAndId(taskUpdateTwo, newTaskId, StatusType.DONE);
        final int updateTaskTwoId = taskUpdate.getId();

        //test
        assertNotEquals(newTask, taskUpdateTwo, "Задачи не должны совпадать");
        assertEquals(newTaskId, updateTaskTwoId, "Id должны совпадать");
        assertEquals(StatusType.DONE, taskUpdateTwo.getStatus(), "Поле должно быть DONE");
    }

    @Test
    void whenUpdateSubtaskThenUpdate() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));
        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final int newSubtaskFirstId = newSubtaskFirst.getId();

        //test
        assertEquals(StatusType.NEW, newSubtaskFirst.getStatus(), "Статус должен быть NEW");

        final Subtask subtaskUpdate = new Subtask(
                "New", "Description","01.01.2022,00:00", "15", newEpic.getId());
        taskManager.updateStatusTaskByTypeAndId(subtaskUpdate, newSubtaskFirstId, StatusType.IN_PROGRESS);
        final int updateSubtaskId = subtaskUpdate.getId();

        //test
        assertNotEquals(newSubtaskFirst, subtaskUpdate, "Задачи не должны совпадать");
        assertEquals(newSubtaskFirstId, updateSubtaskId, "Id должны совпадать");
        assertEquals(StatusType.IN_PROGRESS, subtaskUpdate.getStatus(), "Поле должно быть IN_PROGRESS");

        final Task subtaskUpdateTwo = new Subtask(
                "New", "Description", "01.01.2022,00:00", "15", newEpic.getId());
        taskManager.updateStatusTaskByTypeAndId(subtaskUpdateTwo, newSubtaskFirstId, StatusType.DONE);
        final int updateSubtaskTwoId = subtaskUpdate.getId();

        //test
        assertNotEquals(newSubtaskFirst, subtaskUpdateTwo, "Задачи не должны совпадать");
        assertEquals(newSubtaskFirstId, updateSubtaskTwoId, "Id должны совпадать");
        assertEquals(StatusType.DONE, subtaskUpdateTwo.getStatus(), "Поле должно быть DONE");
    }

    @Test
    void whenUpdateEpicThenUpdate() {
        final Epic newEpic = (Epic) taskManager.createNewTask(new Epic("New", "Description"));

        taskManager.updateStatusTaskByTypeAndId(
                new Epic("New", "Description"), newEpic.getId(), StatusType.IN_PROGRESS);

        //test
        assertEquals(StatusType.NEW, newEpic.getStatus(), "Статус должен быть NEW");

        taskManager.updateStatusTaskByTypeAndId(
                new Epic("New", "Description"), newEpic.getId(), StatusType.DONE);

        //test
        assertEquals(StatusType.NEW, newEpic.getStatus(), "Статус должен быть NEW");

        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        final int newSubtaskFirstId = newSubtaskFirst.getId();
        final Subtask newSubtaskSecond = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));
        final int newSubtaskSecondId = newSubtaskSecond.getId();

        //test
        assertEquals(StatusType.NEW, newEpic.getStatus(), "Статус должен быть NEW");

        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:00", "15",
                        newEpic.getId()), newSubtaskFirstId, StatusType.DONE);
        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:15", "15",
                        newEpic.getId()), newSubtaskSecondId, StatusType.DONE);

        //test
        assertEquals(StatusType.DONE, newEpic.getStatus(), "Статус должен быть DONE");

        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:00", "15",
                        newEpic.getId()), newSubtaskFirstId, StatusType.NEW);
        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:15", "15",
                        newEpic.getId()), newSubtaskSecondId, StatusType.DONE);

        //test
        assertEquals(StatusType.IN_PROGRESS, newEpic.getStatus(), "Статус должен быть IN_PROGRESS");

        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:00", "15",
                        newEpic.getId()), newSubtaskFirstId, StatusType.IN_PROGRESS);
        taskManager.updateStatusTaskByTypeAndId(
                new Subtask("New", "Description","01.01.2022,00:15", "15",
                        newEpic.getId()), newSubtaskSecondId, StatusType.IN_PROGRESS);

        //test
        assertEquals(StatusType.IN_PROGRESS, newEpic.getStatus(), "Статус должен быть IN_PROGRESS");
    }

    @Test
    void whenCreatePrioritizeListAndTest() {
        final Subtask newSubtask = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:30", "15", 1));
        //test
        assertEquals(0, taskManager.getPrioritizedTask().size(), "Список должен быть равен 0");
        assertNull(newSubtask, "Должен лежать null");

        final Epic newEpic = (Epic) taskManager.createNewTask(
                new Epic("New", "Description"));
        //test
        assertEquals(1, taskManager.getPrioritizedTask().size(), "Список должен быть равен 1");

        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:30", "15", newEpic.getId()));

        final Subtask newSubtaskSecond = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:45", "15", newEpic.getId()));
        //test
        assertEquals(3, taskManager.getPrioritizedTask().size(), "Список должен быть равен 3");

        final Task newTaskFirst = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final Task newTaskSecond = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:15", "15"));

        List<Task> taskList = List.of(newTaskFirst, newTaskSecond, newEpic, newSubtaskFirst, newSubtaskSecond);
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTask();

        //test
        assertFalse(prioritizedTasks.isEmpty(), "Список не должен быть пуст");
        assertEquals(taskList.size(), prioritizedTasks.size(), "Списки должны быть одного размера");
        assertEquals(taskList.toString(), prioritizedTasks.toString(), "Списки должны быть идентичны");

        for (Task value : taskList) {
            prioritizedTasks.removeIf(task -> task.equals(value));
        }
        //test
        assertTrue(prioritizedTasks.isEmpty(), "Список должен быть пуст");
    }

    @Test
    void whenCreateAllTasksThenTestValidFun() {
        final Epic newEpic = (Epic) taskManager.createNewTask(
                new Epic("New", "Description"));
        final Subtask newSubtaskFirst = (Subtask) taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:00", "15", newEpic.getId()));
        taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:00", "15"));
        final Task newTaskSecond = taskManager.createNewTask(
                new Task("New", "Description", "01.01.2022,00:15", "15"));
        taskManager.createNewTask(
                new Subtask("New", "Description", "01.01.2022,00:15", "15", newEpic.getId()));

        List<Task> taskList = List.of(newEpic, newSubtaskFirst, newTaskSecond);
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTask();

        //test
        assertFalse(prioritizedTasks.isEmpty(), "Список не должен быть пуст");
        assertEquals(taskList.size(), prioritizedTasks.size(), "Списки должны быть одного размера");
        assertEquals(taskList.toString(), prioritizedTasks.toString(), "Списки должны быть идентичны");

        for (Task value : taskList) {
            prioritizedTasks.removeIf(task -> task.equals(value));
        }
        //test
        assertTrue(prioritizedTasks.isEmpty(), "Список должен быть пуст");
    }
}
