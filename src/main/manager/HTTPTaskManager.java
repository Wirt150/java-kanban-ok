package manager;

import com.google.gson.reflect.TypeToken;
import consnant.TaskType;
import http.KVTaskClient;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;
import util.Managers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static consnant.Key.*;
import static util.UtilGson.GSON;

public class HTTPTaskManager extends FileBackedTasksManager implements TaskManager {

    private final URL url;
    private static KVTaskClient kvTaskClient;

    public HTTPTaskManager(URL url) throws IOException {
        this.url = url;
        kvTaskClient = new KVTaskClient(this.url);
    }

    @Override
    public void saveFile() {
        String task = GSON.toJson(this.tasks);
        String epic = GSON.toJson(this.epics);
        String subtask = GSON.toJson(this.subtasks);
        String history = GSON.toJson(this.getHistory());

        kvTaskClient.put(COUNTER_KEY, String.valueOf(this.counterId));
        kvTaskClient.put(TASK_KEY, task);
        kvTaskClient.put(EPIC_KEY, epic);
        kvTaskClient.put(SUBTASK_KEY, subtask);
        kvTaskClient.put(HISTORY_KEY, history);
    }

    public static HTTPTaskManager loadFromServer() {

        try {
            HTTPTaskManager httpTaskManager = new HTTPTaskManager(new URL(Managers.URL));
            String task = kvTaskClient.load(TASK_KEY);
            String epic = kvTaskClient.load(EPIC_KEY);
            String subtask = kvTaskClient.load(SUBTASK_KEY);
            String history = kvTaskClient.load(HISTORY_KEY);

            httpTaskManager.counterId = Integer.valueOf(kvTaskClient.load(COUNTER_KEY));
            httpTaskManager.tasks = GSON.fromJson(task, new TypeToken<HashMap<Integer, Task>>(){}.getType());
            httpTaskManager.subtasks = GSON.fromJson(subtask, new TypeToken<HashMap<Integer, Subtask>>() {}.getType());
            httpTaskManager.epics = GSON.fromJson(epic, new TypeToken<HashMap<Integer, Epic>>() {}.getType());
            httpTaskManager.subtasks.values().forEach(httpTaskManager::updateEpicSubtask);
            httpTaskManager.getHistory(history).forEach(httpTaskManager.historyManager::add);

            return httpTaskManager;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Task> getHistory(String historyGson) {
        List<Task> temp = GSON.fromJson(historyGson, new TypeToken<List<? extends Task>>() {
        }.getType());
        List<Task> trueMap = new ArrayList<>();
        for (Task task : temp) {
            for (TaskType taskType : TaskType.values()) {
                Map<Integer, ? extends Task> taskMap = getCorrectMabByTypeTask(taskType);
                if (taskMap.containsKey(task.getId())) {
                    trueMap.add(taskMap.get(task.getId()));
                }
            }
        }
        return trueMap;
    }
}
