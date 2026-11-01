public class ArgumentParser {
    public static void main(String[] args){
        switch (args[0]){
            case "add" -> {
                JsonHandler.jsonHandle(args[0], args[1]);
            }
            case "update" ->{
                JsonHandler.updateTask(Integer.parseInt(args[1]), args[2]);
            }
            case "delete" ->{
                //JsonHandler.deleteTask(Integer.parseInt(args[1], arg[2]));
            }
            default -> System.out.println("Command not found");
        }
    }
}

