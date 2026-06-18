package com.ortecfinance.tasklist;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renders task data as the console's textual output.
 *
 * <p>This is the only place that knows the console layout (indentation,
 * checkboxes, date headers). It receives plain domain data and writes the
 * formatted text, keeping presentation separate from command handling
 * ({@link TaskList}) and from the core logic ({@link TaskService}).
 */
final class ConsoleView {
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PrintWriter out;

    ConsoleView(PrintWriter out) {
        this.out = out;
    }

    void welcome() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
    }

    void prompt() {
        out.print("> ");
        out.flush();
    }

    /** Lists every project with its tasks. */
    void showProjects(Collection<Project> projects) {
        for (Project project : projects) {
            out.println(project.getName());
            for (Task task : project.getTasks()) {
                printTask(task);
            }
            out.println();
        }
    }

    /** Lists only the tasks whose deadline is today, omitting projects with none. */
    void showTasksDueToday(Collection<Project> projects) {
        LocalDate today = LocalDate.now();
        for (Project project : projects) {
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

    /** Lists all tasks grouped by deadline (chronological) and then by project. */
    void showByDeadline(Collection<Project> projects) {
        // TreeMap keeps the dates chronological; the inner LinkedHashMap keeps
        // the projects in their original creation order.
        Map<LocalDate, Map<String, List<Task>>> tasksByDeadline = new TreeMap<>();
        Map<String, List<Task>> withoutDeadline = new LinkedHashMap<>();
        for (Project project : projects) {
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

    void help() {
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

    void missingArguments(String command) {
        out.printf("The \"%s\" command needs more arguments. Type 'help' for usage.", command);
        out.println();
    }

    void invalidTaskId(String value) {
        out.printf("Could not parse \"%s\" as a task ID.", value);
        out.println();
    }

    void invalidDate(String value) {
        out.printf("Could not parse \"%s\" as a date. Use the format dd-MM-yyyy.", value);
        out.println();
    }

    void projectNotFound(String name) {
        out.printf("Could not find a project with the name \"%s\".", name);
        out.println();
    }

    void taskNotFound(long id) {
        out.printf("Could not find a task with an ID of %d.", id);
        out.println();
    }

    void unknownCommand(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    private void printTask(Task task) {
        out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
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
}
