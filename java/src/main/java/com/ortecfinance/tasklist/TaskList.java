package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Map<String, Project> projects = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintWriter out;

    private long lastId = 0;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
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
                show();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "deadline":
                deadline(commandRest[1]);
                break;
            case "today":
                today();
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    private void show() {
        for (Project project : projects.values()) {
            out.println(project.getName());
            for (Task task : project.getTasks()) {
                printTask(task);
            }
            out.println();
        }
    }

    private void today() {
        LocalDate today = LocalDate.now();
        for (Project project : projects.values()) {
            List<Task> dueToday = new ArrayList<>();
            for (Task task : project.getTasks()) {
                if (today.equals(task.getDeadline())) {
                    dueToday.add(task);
                }
            }
            if (dueToday.isEmpty()) {
                continue;
            }
            out.println(project.getName());
            for (Task task : dueToday) {
                printTask(task);
            }
            out.println();
        }
    }

    private void printTask(Task task) {
        out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
    }

    private void viewByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> tasksByDeadline = new TreeMap<>();
        Map<String, List<Task>> withoutDeadline = new LinkedHashMap<>();
        for (Project project : projects.values()) {
            String projectName = project.getName();
            for (Task task : project.getTasks()) {
                if (task.getDeadline() == null) {
                    withoutDeadline.computeIfAbsent(projectName, name -> new ArrayList<>()).add(task);
                } else {
                    tasksByDeadline
                        .computeIfAbsent(task.getDeadline(), date -> new LinkedHashMap<>())
                        .computeIfAbsent(projectName, name -> new ArrayList<>())
                        .add(task);
                }
            }
        }

        for (Map.Entry<LocalDate, Map<String, List<Task>>> entry : tasksByDeadline.entrySet()) {
            out.println(entry.getKey().format(DEADLINE_FORMAT) + ":");
            printTasksByProject(entry.getValue());
        }
        if (!withoutDeadline.isEmpty()) {
            out.println("No deadline:");
            printTasksByProject(withoutDeadline);
        }
    }

    private void printTasksByProject(Map<String, List<Task>> tasksByProject) {
        for (Map.Entry<String, List<Task>> project : tasksByProject.entrySet()) {
            out.println("     " + project.getKey() + ":");
            for (Task task : project.getValue()) {
                printDeadlineTask(task);
            }
        }
    }

    private void printDeadlineTask(Task task) {
        out.printf("       \t%d: %s%n", task.getId(), task.getDescription());
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        projects.put(name, new Project(name));
    }

    private void addTask(String projectName, String description) {
        Project project = projects.get(projectName);
        if (project == null) {
            out.printf("Could not find a project with the name \"%s\".", projectName);
            out.println();
            return;
        }
        project.addTask(new Task(nextId(), description, false));
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
    }

    private void deadline(String commandLine) {
        String[] idDate = commandLine.split(" ", 2);
        long id = Long.parseLong(idDate[0]);
        LocalDate date = LocalDate.parse(idDate[1], DEADLINE_FORMAT);
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.getId() == id) {
                    task.setDeadline(date);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <date (dd-MM-yyyy)>");
        out.println("  today");
        out.println("  view-by-deadline");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    private long nextId() {
        return ++lastId;
    }
}
