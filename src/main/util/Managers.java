package util;

import consnant.TaskManagerType;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import manager.FileBackedTasksManager;
import manager.HTTPTaskManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

public class Managers {

    public static final String PATH_FILE = "."
            + File.separator + "src"
            + File.separator + "resources"
            + File.separator + "ManagerSave.csv";
    public static final String URL = "http://localhost:8078";

    public static TaskManager getDefault(TaskManagerType taskManagerType) throws IOException {
        switch (taskManagerType) {
            case IN_MEMORY_TASK_MANAGER:
                return new InMemoryTaskManager();
            case IN_FILE_BACKED_TASK_MANAGER:
                return new FileBackedTasksManager(Paths.get(PATH_FILE));
            case HTTP_TASK_MANAGER:
                return new HTTPTaskManager(new URL(URL));
            default:
                return null;
        }
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
