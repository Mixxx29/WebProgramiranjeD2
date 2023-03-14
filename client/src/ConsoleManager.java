import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsoleManager {

    public ConsoleManager() {

    }

    public static void setTitle(String title) {
        execute("title " + title);
    }

    public static void setSize(int width, int height) {
        execute("mode con: cols=" + width + "lines=" + height);
    }

    public static void clearScreen() {
        execute("cls");
    }

    public static void println(String s) {
        System.out.println(" " + s);
    }

    public static void print(String s) {
        System.out.print(" " + s);
    }

    public static void terminate() {
        execute("exit");
    }

    public static void execute(String command) {
        // Initialize command list
        List<String> commands = new ArrayList<>();
        commands.add("cmd");
        commands.add("/c");

        // Split command
        commands.addAll(Arrays.asList(command.split(" ")));

        // Execute commmand
        try {
            new ProcessBuilder(commands).inheritIO().start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
