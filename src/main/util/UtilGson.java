package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.adapter.GsonEpicAdapter;
import http.adapter.GsonSubtaskAdapter;
import http.adapter.GsonTaskAdapter;
import taskarea.Epic;
import taskarea.Subtask;
import taskarea.Task;

public class UtilGson {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting().serializeNulls()
            .registerTypeAdapter(Task.class, new GsonTaskAdapter())
            .registerTypeAdapter(Subtask.class, new GsonSubtaskAdapter())
            .registerTypeAdapter(Epic.class, new GsonEpicAdapter())
            .create();

}
