package http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import consnant.StatusType;
import consnant.TaskType;
import taskarea.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class GsonTaskAdapter extends TypeAdapter<Task> {
    @Override
    public void write(JsonWriter writer, Task task) throws IOException {
        writer.beginObject();
        writer.name("id").value(task.getId());
        writer.name("type").value(task.getType().toString());
        writer.name("name").value(task.getName());
        writer.name("description").value(task.getDescription());
        writer.name("status").value(task.getStatus().toString());
        writer.name("start_time").value(task.getStartTime().format(Task.DATE_TIME_FORMATTER));
        writer.name("end_time").value(task.getEndTime().format(Task.DATE_TIME_FORMATTER));
        writer.endObject();
    }

    @Override
    public Task read(JsonReader reader) throws IOException {
        int id = 0;
        TaskType type = null;
        String name = "";
        String description = "";
        StatusType status = null;
        String startTime = "";
        String endTime = "";
        String duration = "";


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
            }
        }
        reader.endObject();
        if (duration.isEmpty()) duration = String.valueOf(Duration.between(
                LocalDateTime.parse(startTime, Task.DATE_TIME_FORMATTER),
                LocalDateTime.parse(endTime, Task.DATE_TIME_FORMATTER)).toMinutes());
        if (id != 0) {
            Task task = new Task(name, description, startTime, duration);
            task.setId(id);
            task.setStatus(status);
            return task;
        }
        return new Task(name, description, startTime, duration);
    }
}