package taskarea;

import consnant.TaskType;

import java.util.Objects;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, String startTime, String duration, Integer epicId) {
        super(name, description, startTime, duration);
        super.type = TaskType.SUBTASK;
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(epicId, subtask.epicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s"
                , TaskType.SUBTASK
                , id
                , name
                , status.toString()
                , description
                , startTime
                , getEndTime()
                , epicId
        );
    }
}