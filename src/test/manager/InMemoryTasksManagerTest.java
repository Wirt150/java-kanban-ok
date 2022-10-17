package manager;

import consnant.TaskManagerType;
import util.Managers;

import java.io.IOException;

public class InMemoryTasksManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() throws IOException {
        return (InMemoryTaskManager) Managers.getDefault(TaskManagerType.IN_MEMORY_TASK_MANAGER);
    }
}
