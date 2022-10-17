package manager;

import consnant.StatusType;
import consnant.TaskType;
import history.HistoryManager;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;
import util.Managers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

public class InMemoryTaskManager implements TaskManager {

    public static final int DURATION_LUNE = 15;
    public static final int MIN_IN_YEAR = (364 * 24 * 60) / 15;
    protected final Map<LocalDateTime, Boolean> dateTimeMap;
    protected Integer counterId = 1;
    protected Map<Integer, Task> tasks;
    protected Map<Integer, Epic> epics;
    protected Map<Integer, Subtask> subtasks;
    protected final Set<Task> prioritizedTasks;
    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                if (o1.getStartTime() == null) {
                    return 1;
                } else if (o2.getStartTime() == null) {
                    return -1;
                } else if (o1.getStartTime().isBefore(o2.getStartTime())) {
                    return -1;
                } else if (o1.getStartTime().equals(o2.getStartTime())) {
                    return 1;
                }
                return 1;
            }
        });
        this.dateTimeMap = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        Consumer<Integer> dateTimeMapGet = year -> {
            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.ofYearDay(2022, 1), LocalTime.of(0, 0));
            dateTimeMap.put(localDateTime, true);
            for (int i = 0; i < year; i++) {
                localDateTime = localDateTime.plus(Duration.ofMinutes(15));
                dateTimeMap.put(localDateTime, true);
            }
        };
        dateTimeMapGet.accept(MIN_IN_YEAR);
    }

    @Override
    public List<Task> getAllTaskByType(TaskType taskType) {
        Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
        List<Task> taskArrayList;
        if (!taskMap.isEmpty()) {
            taskArrayList = new ArrayList<>(taskMap.values());
            return taskArrayList;
        }
        return null;
    }

    @Override
    public Set<Task> getPrioritizedTask() {
        prioritizedTasks.clear();
        for (TaskType taskType : TaskType.values()) {
            Collection<? extends Task> taskMap = getCorrectMabByTypeTask(taskType).values();
            prioritizedTasks.addAll(taskMap);
        }
        return prioritizedTasks;
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer id) {
        List<Subtask> subtaskListFromEpicId;
        if (epics.containsKey(id) && !epics.get(id).getSubTaskIdList().isEmpty()) {
            subtaskListFromEpicId = new ArrayList<>();
            Epic epic = epics.get(id);
            for (Integer subtaskId : epic.getSubTaskIdList()) {
                subtaskListFromEpicId.add(subtasks.get(subtaskId));
            }
            return subtaskListFromEpicId;
        }
        return null;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTaskById(TaskType taskType, Integer id) {
        Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
        if (taskMap != null) {
            historyManager.add(taskMap.get(id));
            return taskMap.get(id);
        }
        return null;
    }

    @Override
    public void deleteAllTasksByType(TaskType taskType) {
        Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
        if (taskMap != null) {
            if (taskMap.equals(epics)) {
                for (Epic deleteTask : epics.values()) {
                    historyManager.remove(deleteTask.getId());
                    deleteEpicSubtask(deleteTask);
                }
            } else if (taskMap.equals(subtasks)) {
                for (Subtask deleteSubtask : subtasks.values()) {
                    epics.get(deleteSubtask.getEpicId()).deleteSubtaskFromEpic(deleteSubtask);
                }
            }
            for (Task deleteTask : taskMap.values()) {
                historyManager.remove(deleteTask.getId());
                if (!taskMap.equals(epics)) {
                    dateTimeMapCleaner(deleteTask);
                }
            }
            taskMap.clear();
        }
    }

    @Override
    public void deleteTaskByIdAndType(TaskType taskType, Integer id) {
        Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
        if (taskMap != null && taskMap.containsKey(id)) {
            if (taskMap.equals(epics)) {
                deleteEpicSubtask((Epic) taskMap.get(id));
            }
            if (taskMap.equals(subtasks)) {
                Subtask subtask = (Subtask) taskMap.get(id);
                epics.get(subtask.getEpicId()).deleteSubtaskFromEpic(taskMap.get(id));
            }
            if (!taskMap.equals(epics)) {
                dateTimeMapCleaner(taskMap.get(id));
            }
            taskMap.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public Task createNewTask(Task task) {
        task.setStatus(StatusType.NEW);
        task.setId(counterId);
        if (task.getClass() == Task.class && isValid(task)) {
            tasks.put(counterId, task);
            counterId++;
        } else if (task.getClass() == Epic.class) {
            epics.put(counterId, (Epic) task);
            counterId++;
        } else if (task.getClass() == Subtask.class
                && epics.containsKey(((Subtask) task).getEpicId())) {
            if (isValid(task)) {
                updateEpicSubtask((Subtask) task);
                subtasks.put(counterId, (Subtask) task);
                counterId++;
            }
        } else {
            return null;
        }
        return task;
    }

    @Override
    public boolean updateStatusTaskByTypeAndId(Task task, Integer id, StatusType status) {
        if (!(task instanceof Epic)) {
            if (task instanceof Subtask && subtasks.containsKey(id)) {
                updateTaskData(task, id, status);
                subtasks.put(task.getId(), (Subtask) task);
                updateEpicStatus((Subtask) task);
                return true;
            } else if (tasks.containsKey(id)) {
                updateTaskData(task, id, status);
                tasks.put(task.getId(), task);
                return true;
            }
        }
        return false;
    }

    protected Map<Integer, ? extends Task> getCorrectMabByTypeTask(TaskType taskType) {
        switch (taskType) {
            case TASK:
                return tasks;
            case EPIC:
                return epics;
            case SUBTASK:
                return subtasks;
            default:
                return null;
        }
    }

    private void updateTaskData(Task task, Integer id, StatusType statusLine) {
        task.setStatus(statusLine);
        task.setId(id);
        historyManager.updateInfo(task);
    }

    protected void updateEpicSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        epic.setSubTaskIdList(subtask.getId());
        epic.setSubTask(subtask);
    }

    private void updateEpicStatus(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        List<Integer> subtaskIdFromEpic = epic.getSubTaskIdList();
        int newLine = 0;
        int inProgressLine = 0;
        int doneLine = 0;
        for (Integer subtaskId : subtaskIdFromEpic) {
            Subtask subtaskFromMap = subtasks.get(subtaskId);
            if (subtaskFromMap.getStatus() == StatusType.NEW) {
                newLine++;
            } else if (subtaskFromMap.getStatus() == StatusType.DONE) {
                doneLine++;
            } else {
                inProgressLine++;
            }
        }
        if (newLine != 0 && doneLine == 0 && inProgressLine == 0) {
            epic.setStatus(StatusType.NEW);
        } else if (doneLine != 0 && newLine == 0 && inProgressLine == 0) {
            epic.setStatus(StatusType.DONE);
        } else {
            epic.setStatus(StatusType.IN_PROGRESS);
        }
    }

    private void deleteEpicSubtask(Epic epic) {
        List<Integer> subtasks = new ArrayList<>(epic.getSubTaskIdList());
        for (Integer subtaskId : subtasks) {
            epic.deleteSubtaskFromEpic(this.subtasks.get(subtaskId));
            this.subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    private boolean isValid(Task task) {
        LocalDateTime statTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();
        Duration duration = Duration.between(statTime, endTime);
        if (dateTimeMap.get(task.getStartTime())) {
            for (int i = 0; i < duration.toMinutes() / DURATION_LUNE; i++) {
                dateTimeMap.put(statTime, false);
                statTime = statTime.plus(Duration.ofMinutes(DURATION_LUNE));
            }
            return true;
        } else {
            return false;
        }
    }

    private void dateTimeMapCleaner(Task task) {
        LocalDateTime statTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();
        Duration duration = Duration.between(statTime, endTime);
        for (int i = 0; i < duration.toMinutes() / DURATION_LUNE; i++) {
            dateTimeMap.put(statTime, true);
            statTime = statTime.plus(Duration.ofMinutes(DURATION_LUNE));
        }
//        }
    }

}