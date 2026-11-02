import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    public static final String HOME = System.getProperty("user.home");
    public static final Path path = Paths.get(HOME, "Documents", "tasks.json");
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a");
    public static int uniqueID = initializeUniqueID();

    private static int initializeUniqueID() {
        try {
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                if (!content.isEmpty() && !content.equals("[]")) {
                    String arrayContent = content.substring(1, content.length() - 1).trim();
                    String[] tasks = arrayContent.split("},");
                    int maxId = 0;
                    for (String taskJson : tasks) {
                        String json = taskJson.trim();
                        if (!json.endsWith("}")) json += "}";
                        int idIndex = json.indexOf("\"id\":");
                        if (idIndex != -1) {
                            int commaIndex = json.indexOf(",", idIndex);
                            String idStr = (commaIndex != -1)
                                    ? json.substring(idIndex + 5, commaIndex).trim()
                                    : json.substring(idIndex + 5).replaceAll("[^0-9]", "").trim();
                            try {
                                int id = Integer.parseInt(idStr);
                                if (id > maxId) maxId = id;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    return maxId + 1;
                }
            }
        } catch (IOException e) {
        }
        return 1;
    }
    
    
    public static String taskToJson(String task, int id, String createdAt, String updatedAt, String status){
        return """
                {
                    "id": %d,
                    "description": "%s",
                    "createdAt": "%s",
                    "updatedAt": "%s",
                    "status": "%s"
                }
                """.formatted(id, task, createdAt, updatedAt, status);
    }
    
    public static String taskToJsonComma(String task, int id, String createdAt, String updatedAt, String status){
        return """
                {
                    "id": %d,
                    "description": "%s",
                    "createdAt": "%s",
                    "updatedAt": "%s",
                    "status": "%s"
                },
                """.formatted(id, task, createdAt, updatedAt, status);
    }
    public static void jsonHandle(String command, String task){

        try {
            Files.createDirectories(path.getParent());

            if (Files.notExists(path)) Files.createFile(path);
        }
        catch (IOException e){
            throw new RuntimeException("Failed to create JSON file: " + path, e);
        }
        switch (command){
            case "add" ->{
                addTask(task);
            }
            case "update" ->{
                //updateTask(uniqueID, task);
            }
            case "delete" ->{

            }
        }
    }

    public static void addTask(String task){
        try {
            LocalDateTime now = LocalDateTime.now();
            String status = "unfinished";
            
            String existsing = Files.readString(path).trim();
            String newTask = taskToJson(task, uniqueID, now.format(formatter), now.format(formatter), status);
            String content = existsing.isEmpty() || existsing.equals("[]")
                    ? ""
                    : existsing.substring(1, existsing.length() - 1).trim();
            if (!content.isEmpty()) content += ",";
            String updated = "[\n" + content + "\n" + newTask.trim() + "\n]";

            Files.writeString(path, updated);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void updateTask(int id, String task) {
        try {
            String content = Files.readString(path).trim();
            if (content.isEmpty() || content.equals("[]")) return;

            String arrayContent = content.substring(1, content.length() - 1).trim();
            List<String> result = new ArrayList<>();
            boolean found = false;

            for (String piece : arrayContent.split("},")) {
                String json = piece.trim();
                if (!json.endsWith("}")) json += "}";   // split removed the brace

                if (!found && json.contains("\"id\": " + id + ",")) {
                    int start = json.indexOf("\"createdAt\": \"") + 14;
                    int end = json.indexOf("\"", start);
                    String createdAt = json.substring(start, end);
                    json = taskToJson(task, id, createdAt,
                            LocalDateTime.now().format(formatter), "unfinished").trim();
                    found = true;
                }
                result.add(json);
            }

            if (!found) {
                System.out.println("Task ID not found");
                return;
            }
            Files.writeString(path, "[\n" + String.join(",\n", result) + "\n]");
        } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
