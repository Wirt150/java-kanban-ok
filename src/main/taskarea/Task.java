package taskarea;

import consnant.StatusType;
import consnant.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy,HH:mm");
    protected TaskType type;
    protected String name;
    protected String description;
    protected StatusType status;
    protected LocalDateTime startTime;
    protected Duration duration;
    protected int id;

    protected Task(String name, String description) {
        this.status = StatusType.NEW;
        this.type = TaskType.TASK;
        this.name = name;
        this.description = description;
    }

    public Task(String name, String description, String startTime, String duration) {
        this.status = StatusType.NEW;
        this.type = TaskType.TASK;
        this.name = name;
        this.description = description;
        this.startTime = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
        this.duration = Duration.ofMinutes(Long.parseLong(duration));
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StatusType getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public TaskType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && type == task.type && Objects.equals(name, task.name) && Objects.equals(description, task.description) && status == task.status && Objects.equals(startTime, task.startTime) && Objects.equals(duration, task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, description, status, startTime, duration, id);
    }

    @Override
    public String toString() {
        return String.format(
                "%s,%s,%s,%s,%s,%s,%s", type, id, name, status.toString(), description, startTime, getEndTime());
    }

}
