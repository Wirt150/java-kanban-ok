package taskarea;

import consnant.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Integer> subTaskIdList;
    private final List<Subtask> subTaskList;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        super.type = TaskType.EPIC;
        this.subTaskIdList = new ArrayList<>();
        this.subTaskList = new ArrayList<>();
        startTime = null;
        endTime = null;
    }

    public Epic(String name, String description, String startTime, String endTime) {
        super(name, description);
        super.type = TaskType.EPIC;
        this.subTaskIdList = new ArrayList<>();
        this.subTaskList = new ArrayList<>();
        this.startTime = LocalDateTime.parse(startTime, Task.DATE_TIME_FORMATTER);
        this.endTime = LocalDateTime.parse(endTime, Task.DATE_TIME_FORMATTER);
    }

    public List<Integer> getSubTaskIdList() {
        return subTaskIdList;
    }

    public void setSubTask(Subtask subtask) {
        subTaskList.add(subtask);
        timeCounter();
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void deleteSubtaskFromEpic(Task task) {
        if (subTaskIdList.contains(task.getId())) {
            subTaskIdList.removeIf(id -> id.equals(task.getId()));
            subTaskList.remove(task);
        }
        timeCounter();
    }

    public void setSubTaskIdList(Integer subtaskId) {
        if (!subTaskIdList.contains(subtaskId)) this.subTaskIdList.add(subtaskId);
    }

    private void timeCounter() {
        if (!subTaskList.isEmpty()) {
            startTime = subTaskList.get(0).getStartTime();
            endTime = subTaskList.get(0).getEndTime();
            for (Subtask subtask : subTaskList) {
                if (subtask.getStartTime().isBefore(startTime)) startTime = subtask.getStartTime();
                if (subtask.getEndTime().isAfter(endTime)) endTime = subtask.getEndTime();
            }
        } else {
            startTime = null;
            endTime = null;
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTaskIdList, epic.subTaskIdList) && Objects.equals(subTaskList, epic.subTaskList) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTaskIdList, subTaskList, endTime);
    }

    @Override
    public String toString() {
        return String.format(
                "%s,%s,%s,%s,%s,%s,%s,%s", type, id, name, status.toString(), description, startTime, endTime, subTaskIdList.toString());
    }
}