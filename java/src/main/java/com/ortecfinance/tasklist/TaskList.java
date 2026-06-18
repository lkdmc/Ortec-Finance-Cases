package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

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
        String args = commandRest.length > 1 ? commandRest[1] : "";
        switch (command) {
            case "show":
                view.showProjects(service.getProjects());
                break;
            case "add":
                add(args);
                break;
            case "check":
                setDone(args, true, "check");
                break;
            case "uncheck":
                setDone(args, false, "uncheck");
                break;
            case "deadline":
                deadline(args);
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

    private void add(String args) {
        String[] subcommandRest = args.split(" ", 2);
        String subcommand = subcommandRest[0];
        String rest = subcommandRest.length > 1 ? subcommandRest[1] : "";
        if (subcommand.equals("project")) {
            if (rest.isBlank()) {
                view.missingArguments("add project");
                return;
            }
            service.addProject(rest);
        } else if (subcommand.equals("task")) {
            String[] projectTask = rest.split(" ", 2);
            if (projectTask.length < 2 || projectTask[1].isBlank()) {
                view.missingArguments("add task");
                return;
            }
            addTask(projectTask[0], projectTask[1]);
        } else {
            view.missingArguments("add");
        }
    }

    private void addTask(String projectName, String description) {
        if (service.addTask(projectName, description).isEmpty()) {
            view.projectNotFound(projectName);
        }
    }

    private void setDone(String idString, boolean done, String command) {
        if (idString.isBlank()) {
            view.missingArguments(command);
            return;
        }
        Optional<Long> id = parseId(idString);
        if (id.isEmpty()) {
            return;
        }
        if (!service.setDone(id.get(), done)) {
            view.taskNotFound(id.get());
        }
    }

    private void deadline(String args) {
        String[] idDate = args.split(" ", 2);
        if (idDate.length < 2 || idDate[1].isBlank()) {
            view.missingArguments("deadline");
            return;
        }
        Optional<Long> id = parseId(idDate[0]);
        if (id.isEmpty()) {
            return;
        }
        Optional<LocalDate> date = parseDeadline(idDate[1]);
        if (date.isEmpty()) {
            return;
        }
        if (!service.setDeadline(id.get(), date.get())) {
            view.taskNotFound(id.get());
        }
    }

    private Optional<Long> parseId(String value) {
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            view.invalidTaskId(value);
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDeadline(String value) {
        try {
            return Optional.of(LocalDate.parse(value.trim(), DEADLINE_FORMAT));
        } catch (DateTimeParseException e) {
            view.invalidDate(value);
            return Optional.empty();
        }
    }
}
