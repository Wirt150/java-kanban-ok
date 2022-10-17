package http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import consnant.StatusType;
import consnant.TaskType;
import taskarea.Subtask;
import taskarea.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class GsonSubtaskAdapter extends TypeAdapter<Subtask> {
    @Override
    public void write(JsonWriter writer, Subtask subtask) throws IOException {
        writer.beginObject();
        writer.name("id").value(subtask.getId());
        writer.name("type").value(subtask.getType().toString());
        writer.name("name").value(subtask.getName());
        writer.name("description").value(subtask.getDescription());
        writer.name("status").value(subtask.getStatus().toString());
        writer.name("start_time").value(subtask.getStartTime().format(Task.DATE_TIME_FORMATTER));
        writer.name("end_time").value(subtask.getEndTime().format(Task.DATE_TIME_FORMATTER));
        writer.name("epicId").value(subtask.getEpicId());
        writer.endObject();
    }

    @Override
    public Subtask read(JsonReader reader) throws IOException {
        int id = 0;
        TaskType type = null;
        String name = "";
        String description = "";
        StatusType status = null;
        String startTime = "";
        String endTime = "";
        String duration = "";
        int epicId = 0;

        reader.beginObject();
        String fieldName = null;
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.NAME)) {
                fieldName = reader.nextName();
            }
            switch (Objects.requireNonNull(fieldName)) {
                case "id":
                    reader.peek();
                    id = reader.nextInt();
                    break;
                case "type":
                    reader.peek();
                    type = TaskType.valueOf(reader.nextString());
                    break;
                case "name":
                    reader.peek();
                    name = reader.nextString();
                    break;
                case "description":
                    reader.peek();
                    description = reader.nextString();
                    break;
                case "status":
                    reader.peek();
                    status = StatusType.valueOf(reader.nextString());
                    break;
                case "start_time":
                    reader.peek();
                    startTime = reader.nextString();
                    break;
                case "end_time":
                    reader.peek();
                    endTime = reader.nextString();
                    break;
                case "duration":
                    reader.peek();
                    duration = reader.nextString();
                    break;
                case "epicId":
                    reader.peek();
                    epicId = reader.nextInt();
                    break;
            }
        }
        reader.endObject();
        if (duration.isEmpty()) duration = String.valueOf(Duration.between(
                LocalDateTime.parse(startTime, Task.DATE_TIME_FORMATTER),
                LocalDateTime.parse(endTime, Task.DATE_TIME_FORMATTER)).toMinutes());
        if (id != 0) {
            Subtask task = new Subtask(name, description, startTime, duration, epicId);
            task.setId(id);
            task.setStatus(status);
            return task;
        }

        return new Subtask(name, description, startTime, duration, epicId);
    }
}
