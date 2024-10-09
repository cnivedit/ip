package bitwise.chat;

import java.io.IOException;
import java.util.Scanner;

import bitwise.constants.Commands;
import bitwise.constants.Constants;
import bitwise.exceptions.*;
import bitwise.tasks.*;
import bitwise.utils.FileManager;
import bitwise.utils.OutputManager;
import java.util.ArrayList;

public class Bitwise {

    private static ArrayList<Task> tasksList = new ArrayList<Task>();
    private static int numberOfTasks = 0;

    public static void main(String[] args) {
        try {
            numberOfTasks = FileManager.getTasks(tasksList);
        } catch (IOException ignored) {
        }
        OutputManager.printWelcomeMessage();
        mainManager();
        OutputManager.printExitMessage();
    }

    public static void mainManager() {
        String userInput;
        Status status = Status.RUNNING;
        while (status != Status.EXIT) {
            userInput = getUserInput();
            try {
                status = inputHandler(userInput);
            } catch (InvalidCommandException e) {
                OutputManager.printMessage("Invalid command: " + userInput);
                OutputManager.printListCommands();
            } catch (InvalidFormatException e) {
                OutputManager.printMessage("Invalid format: " + e);
                OutputManager.printLineBreak();
            }
        }
    }

    public static Status inputHandler(String userInput) {
        if (userInput.equalsIgnoreCase(Commands.COMMAND_END)) {
            return Status.EXIT;
        }
        OutputManager.printLineBreak();
        if (userInput.equalsIgnoreCase(Commands.COMMAND_LIST)) {
            OutputManager.printTasksList(tasksList, numberOfTasks);
        } else if (userInput.startsWith(Commands.COMMAND_UNMARK)) {
            try {
                int taskNumber = Integer.parseInt(userInput.substring(userInput.indexOf(" ") + 1));
                markCompletionStatus(taskNumber, false);
            } catch (NumberFormatException e) {
                throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_UNMARK);
            }
        } else if (userInput.startsWith(Commands.COMMAND_MARK)) {
            try {
                int taskNumber = Integer.parseInt(userInput.substring(userInput.indexOf(" ") + 1));
                markCompletionStatus(taskNumber, true);
            } catch (NumberFormatException e) {
                throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_MARK);
            }
        } else if (userInput.startsWith(Commands.COMMAND_DELETE)) {
            try {
                int taskNumber = Integer.parseInt(userInput.substring(userInput.indexOf(" ") + 1));
                deleteTask(taskNumber);
            } catch (IndexOutOfBoundsException e) {
                OutputManager.printMessage("Invalid task number: Current list size is " + numberOfTasks);
                return Status.RUNNING;
            }
        }else {
            addToList(userInput);
        }
        OutputManager.printLineBreak();
        return Status.RUNNING;
    }

    public static String getUserInput() {
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    public static void echoUserInput() {
        String userInput;
        while (true) {
            userInput = getUserInput();
            if (userInput.equalsIgnoreCase(Commands.COMMAND_END)) {
                return;
            }
            OutputManager.printEchoInput(userInput);
        }
    }

    public static void addToList(String userInput) {
        Task newTask;
        String description;
        if (userInput.startsWith(Commands.COMMAND_TODO)) {
            description = userInput.substring(userInput.indexOf(" ") + 1);
            if (description.isBlank() || !userInput.contains(" ")) {
                throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_TODO);
            }
            newTask = new Todo(description);
        } else if (userInput.startsWith(Commands.COMMAND_DEADLINE)) {
            try {
                description = userInput.substring(userInput.indexOf(" ") + 1, userInput.indexOf(Commands.COMMAND_INFIX_BY));
                String deadline = userInput.substring(userInput.indexOf(Commands.COMMAND_INFIX_BY) + 4);
                if (description.isBlank() || deadline.isBlank() || !userInput.contains(Commands.COMMAND_INFIX_BY)) {
                    throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_DEADLINE);
                }
                newTask = new Deadline(description, deadline);
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_DEADLINE);
            }
        } else if (userInput.startsWith(Commands.COMMAND_EVENT)) {
            try {
                description = userInput.substring(userInput.indexOf(" ") + 1, userInput.indexOf(Commands.COMMAND_INFIX_FROM));
                String eventFrom = userInput.substring(userInput.indexOf(Commands.COMMAND_INFIX_FROM) + 6, userInput.indexOf(Commands.COMMAND_INFIX_TO));
                String eventTo = userInput.substring(userInput.indexOf(Commands.COMMAND_INFIX_TO) + 4);
                if (description.isBlank() || eventFrom.isBlank() || eventTo.isBlank() || !userInput.contains(Commands.COMMAND_INFIX_FROM) || !userInput.contains(Commands.COMMAND_INFIX_TO)) {
                    throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_DEADLINE);
                }
                newTask = new Event(description, eventFrom, eventTo);
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidFormatException(userInput + "\n" + Constants.DESCRIPTION_COMMAND_EVENT);
            }
        } else {
            throw new InvalidCommandException(userInput);
        }
        tasksList.add(newTask);
        numberOfTasks++;
        try {
            FileManager.saveTask(newTask);
        } catch (IOException e) {
            System.out.println("Error saving task list");
            return;
        }
        OutputManager.printMessageAddedTask(newTask.toString());
        OutputManager.printNumberOfTasks(numberOfTasks);
    }

    public static void markCompletionStatus(int taskNumber, boolean isCompleted) {
        int taskIndex = taskNumber - 1;
        try {
            tasksList.get(taskIndex).markCompletionStatus(isCompleted);
        } catch (IndexOutOfBoundsException e) {
            OutputManager.printMessage("Invalid task number: Current list size is " + numberOfTasks);
            return;
        }
        try {
            FileManager.saveTasksList(tasksList, numberOfTasks);
        } catch (IOException e) {
            System.out.println("Error saving task list");
            return;
        }
        String message = isCompleted ? Constants.MESSAGE_MARKED : Constants.MESSAGE_UNMARKED;
        OutputManager.printMessage(message);
        OutputManager.printTasksList(tasksList, numberOfTasks);
    }

    public static void deleteTask(int taskNumber) {
        int taskIndex = taskNumber - 1;
        Task deletedTask = tasksList.remove(taskIndex);
        numberOfTasks--;
        try {
            FileManager.saveTasksList(tasksList, numberOfTasks);
        } catch (IOException e) {
            System.out.println("Error saving task list");
            return;
        }
        OutputManager.printMessageDeletedTask(deletedTask.toString());
        OutputManager.printNumberOfTasks(numberOfTasks);
    }
}
