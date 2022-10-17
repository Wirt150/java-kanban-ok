package manager;

import consnant.StatusType;
import consnant.TaskType;
import taskarea.Subtask;
import taskarea.Task;

import java.util.List;
import java.util.Set;

public interface TaskManager {

    List<Task> getAllTaskByType(TaskType taskType);

    Set<Task> getPrioritizedTask();

    List<Subtask> getEpicSubtasks(Integer id);

    List<Task> getHistory();

    Task getTaskById(TaskType taskType, Integer id);

    Task createNewTask(Task taskObject);

    void deleteAllTasksByType(TaskType mapTaskType);

    void deleteTaskByIdAndType(TaskType taskType, Integer id);

    boolean updateStatusTaskByTypeAndId(Task taskObject, Integer id, StatusType statusLine);

}
