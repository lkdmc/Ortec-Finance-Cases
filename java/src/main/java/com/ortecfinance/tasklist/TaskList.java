package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Console interface for the task list.
 *
 * <p>Reads commands from the input, delegates the work to {@link TaskService}
 * (the core logic) and the rendering to {@link ConsoleView} (the output). It
 * owns no task state and no output format, so the same {@code TaskService}
 * could equally back a REST API or another interface.
 */
public final class TaskList implements Runnable {
    private static final String QUIT = "quit";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final TaskService service = new TaskService();
    private final ConsoleView view;
    private final BufferedReader in;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.view = new ConsoleView(writer);
    }

    public void run() {
        view.welcome();
        while (true) {
            view.prompt();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                view.showProjects(service.getProjects());
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                setDone(commandRest[1], true);
                break;
            case "uncheck":
                setDone(commandRest[1], false);
                break;
            case "deadline":
                deadline(commandRest[1]);
                break;
            case "today":
                view.showTasksDueToday(service.getProjects());
                break;
            case "view-by-deadline":
                view.showByDeadline(service.getProjects());
                break;
            case "help":
                view.help();
                break;
            default:
                view.unknownCommand(command);
                break;
        }
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            service.addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addTask(String projectName, String description) {
        if (service.addTask(projectName, description).isEmpty()) {
            view.projectNotFound(projectName);
        }
    }

    private void setDone(String idString, boolean done) {
        long id = Long.parseLong(idString);
        if (!service.setDone(id, done)) {
            view.taskNotFound(id);
        }
    }

    private void deadline(String commandLine) {
        String[] idDate = commandLine.split(" ", 2);
        long id = Long.parseLong(idDate[0]);
        LocalDate date = LocalDate.parse(idDate[1], DEADLINE_FORMAT);
        if (!service.setDeadline(id, date)) {
            view.taskNotFound(id);
        }
    }
}
