package manager;

import consnant.StatusType;
import consnant.TaskType;
import error.ManagerSaveException;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Integer.parseInt;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    private static final int TYPE_LINE = 0;
    private static final int ID_LINE = 1;
    private static final int NAME_LINE = 2;
    private static final int STATUS_LINE = 3;
    private static final int DESCRIPTION_LINE = 4;
    private static final int START_TIME_LINE = 5;
    private static final int END_TIME_LINE = 6;
    private static final int EPIC_SPEC_LINE = 7;

    private final Path managerSaveFile;

    public FileBackedTasksManager() {
        managerSaveFile = null;
    }

    public FileBackedTasksManager(Path pathManagerSaveFile) {
        this.managerSaveFile = pathManagerSaveFile;
    }

    public static FileBackedTasksManager loadFromFile(Path file) {
        FileBackedTasksManager loadFileBackedTasksManager = new FileBackedTasksManager(file);
        if (loadFileBackedTasksManager.fileIsEmpty(file)) return loadFileBackedTasksManager;
        loadFileBackedTasksManager.readFile();
        return loadFileBackedTasksManager;
    }

    @Override
    public Task getTaskById(TaskType taskType, Integer id) {
        Task tempTask = super.getTaskById(taskType, id);
        saveFile();
        return tempTask;
    }

    @Override
    public void deleteAllTasksByType(TaskType taskType) {
        super.deleteAllTasksByType(taskType);
        saveFile();
    }

    @Override
    public void deleteTaskByIdAndType(TaskType taskType, Integer id) {
        super.deleteTaskByIdAndType(taskType, id);
        saveFile();
    }

    @Override
    public Task createNewTask(Task task) {
        Task tempTask = super.createNewTask(task);
        saveFile();
        return tempTask;
    }

    @Override
    public boolean updateStatusTaskByTypeAndId(Task task, Integer id, StatusType status) {
        boolean temp = super.updateStatusTaskByTypeAndId(task, id, status);
        saveFile();
        return temp;
    }

    private Task fromString(String value) {
        String[] taskLine = value.split(",");
        TaskType taskType = TaskType.valueOf(taskLine[TYPE_LINE]);
        switch (taskType) {
            case TASK:
                Task taskFromString = new Task(taskLine[NAME_LINE],
                        taskLine[DESCRIPTION_LINE],
                        LocalDateTime.parse(taskLine[START_TIME_LINE]).format(Task.DATE_TIME_FORMATTER),
                        String.valueOf(Duration.between(LocalDateTime.parse(taskLine[START_TIME_LINE]),
                                LocalDateTime.parse(taskLine[END_TIME_LINE])).toMinutes()));
                taskFromString.setId(parseInt(taskLine[ID_LINE]));
                taskFromString.setStatus(StatusType.valueOf(taskLine[STATUS_LINE]));
                return taskFromString;
            case EPIC:
                Epic epicFromString = new Epic(taskLine[NAME_LINE], taskLine[DESCRIPTION_LINE]);
                epicFromString.setId(parseInt(taskLine[ID_LINE]));
                epicFromString.setStatus(StatusType.valueOf(taskLine[STATUS_LINE]));
                List<String> subtaskIdFromEpic = new ArrayList<>();
                for (int i = 0; i < taskLine.length - EPIC_SPEC_LINE; i++) {
                    subtaskIdFromEpic.add(taskLine[EPIC_SPEC_LINE + i]
                            .replace("[", "")
                            .replace("]", "")
                            .trim());
                }
                if (!subtaskIdFromEpic.contains("")) {
                    for (String subtaskId : subtaskIdFromEpic) {
                        epicFromString.setSubTaskIdList(Integer.parseInt(subtaskId));
                    }
                }
                return epicFromString;
            case SUBTASK:
                Subtask subtaskFromString = new Subtask(taskLine[NAME_LINE],
                        taskLine[DESCRIPTION_LINE],
                        LocalDateTime.parse(taskLine[START_TIME_LINE]).format(Task.DATE_TIME_FORMATTER),
                        String.valueOf(Duration.between(LocalDateTime.parse(taskLine[START_TIME_LINE]),
                                LocalDateTime.parse(taskLine[END_TIME_LINE])).toMinutes()),
                        Integer.parseInt(taskLine[EPIC_SPEC_LINE]));
                subtaskFromString.setId(parseInt(taskLine[ID_LINE]));
                subtaskFromString.setStatus(StatusType.valueOf(taskLine[STATUS_LINE]));
                return subtaskFromString;
            default:
                return null;
        }
    }

    protected void saveFile() {
        try (FileWriter managerFileWriter = new FileWriter(String.valueOf(this.managerSaveFile), StandardCharsets.UTF_8)) {
            managerFileWriter.write("type,id,name,status,description,startTime,endTime,epicSpec\n");
            managerFileWriter.write(super.counterId + "\n");
            for (TaskType taskType : TaskType.values()) {
                Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
                for (Task task : taskMap.values()) {
                    managerFileWriter.write(task.toString() + "\n");
                }
            }
            if (super.getHistory().size() != 0) {
                StringBuilder stringSaveHistory = new StringBuilder();
                for (Task taskFromHistory : super.getHistory()) {
                    stringSaveHistory.append(taskFromHistory.getId()).append(",");
                }
                stringSaveHistory.deleteCharAt(stringSaveHistory.length() - 1);
                managerFileWriter.append("\n").write(stringSaveHistory.toString());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при записи файла");
        }
    }

    protected void readFile() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(this.managerSaveFile)) {
            Map<Integer, Task> historyMap = new HashMap<>();
            bufferedReader.readLine();
            int counterIdLine = Integer.parseInt(bufferedReader.readLine());
            while (bufferedReader.ready()) {
                String readLine = bufferedReader.readLine();
                if (!readLine.isEmpty()) {
                    Task taskFromString = fromString(readLine);
                    this.counterId = Objects.requireNonNull(taskFromString).getId();
                    super.createNewTask(taskFromString);
                    historyMap.put(taskFromString.getId(), taskFromString);
                } else {
                    String[] stringHistoryLine = bufferedReader.readLine().split(",");
                    for (String id : stringHistoryLine) {
                        if (historyMap.containsKey(Integer.parseInt(id))) {
                            super.historyManager.add(historyMap.get(Integer.parseInt(id)));
                        }
                    }
                }
            }
            this.counterId = counterIdLine;
        } catch (IOException e) {
            throw new ManagerSaveException("Возникла ошибка при чтении файла");
        }
    }

    private boolean fileIsEmpty(Path path) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            if (br.readLine() == null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}