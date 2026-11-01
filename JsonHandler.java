import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;

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
                updateTask(uniqueID, task);
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
            uniqueID += 1;
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

    public static void updateTask(int id, String task){
        try {
            String content = Files.readString(path).trim();
            if (!content.isEmpty() && !content.equals("[]")){
                String arrayContent = content.substring(1, content.length() - 1);
                String[] tasks = arrayContent.split("},");
                for (int i = 0; i < tasks.length; i++) if (!tasks[i].endsWith("}")) tasks[i] += "}";
                tasks[tasks.length - 1] = "\n" + tasks[tasks.length - 1].substring(1, tasks[tasks.length - 1].length() - 1);
                for (int i = 0; i < tasks.length; i++){
                    String jsonTask = tasks[i].trim();
                    int idIndex = jsonTask.indexOf("\"id\":");
                    if (idIndex == -1){
                        System.out.println("Task ID not found");
                        return;
                    }
                    else{
                        int commaIndex = jsonTask.indexOf(",");
                        String idStr = jsonTask.substring(idIndex + 5, commaIndex).trim();
                        int idParsed = Integer.parseInt(idStr);
                        if (id == idParsed){
                            int createdAt = jsonTask.indexOf("\"createdAt\":");
                            int timeCommaIndex = jsonTask.indexOf("m\",");
                            String timeCreated = jsonTask.substring(createdAt + 14, timeCommaIndex + 1);
                            tasks[i] = taskToJson(task, idParsed, timeCreated, LocalDateTime.now().format(formatter), "unfinished");
                            String updated = "[\n" + String.join(",\n", tasks).trim() + "\n]";
                            Files.writeString(path, updated);
                            return;
                        }
                    }
                    
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
