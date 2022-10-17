package http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import consnant.StatusType;
import consnant.TaskType;
import taskarea.Epic;
import taskarea.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GsonEpicAdapter extends TypeAdapter<Epic> {
    @Override
    public void write(JsonWriter writer, Epic epic) throws IOException {
        writer.beginObject();
        writer.name("id").value(epic.getId());
        writer.name("type").value(epic.getType().toString());
        writer.name("name").value(epic.getName());
        writer.name("description").value(epic.getDescription());
        writer.name("status").value(epic.getStatus().toString());
        if (epic.getStartTime() != null && epic.getEndTime() != null) {
            writer.name("start_time").value(epic.getStartTime().format(Task.DATE_TIME_FORMATTER));
            writer.name("end_time").value(epic.getEndTime().format(Task.DATE_TIME_FORMATTER));
        }
        writer.name("idSubtask");
        writer.beginArray();
        for (Integer id : epic.getSubTaskIdList()) {
            writer.value(id);
        }
        writer.endArray();
        writer.endObject();
    }

    @Override
    public Epic read(JsonReader reader) throws IOException {
        int id = 0;
        TaskType type = null;
        String name = "";
        String description = "";
        StatusType status = null;
        String startTime = "";
        String endTime = "";
        List<Integer> idSubtask = new ArrayList<>();

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
                case "idSubtask":
                    reader.peek();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        idSubtask.add(reader.nextInt());
                    }
                    reader.endArray();
                    break;
            }
        }
        reader.endObject();
        if (id != 0) {
            Epic epic = new Epic(name, description);
            if (startTime.isEmpty() && endTime.isEmpty()) {
                epic.setStartTime(null);
                epic.setEndTime(null);
            } else {
                epic.setStartTime(LocalDateTime.parse(startTime, Task.DATE_TIME_FORMATTER));
                epic.setEndTime(LocalDateTime.parse(endTime, Task.DATE_TIME_FORMATTER));
            }
            epic.setId(id);
            epic.setStatus(status);
            if (!idSubtask.isEmpty()) {
                idSubtask.forEach(epic::setSubTaskIdList);
            }
            return epic;
        }
        return new Epic(name, description);
    }
}
