package com.ortecfinance.tasklist;

import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class ApplicationTest {
    public static final String PROMPT = "> ";
    private final PipedOutputStream inStream = new PipedOutputStream();
    private final PrintWriter inWriter = new PrintWriter(inStream, true);

    private final PipedInputStream outStream = new PipedInputStream();
    private final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

    private Thread applicationThread;

    public ApplicationTest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new PipedInputStream(inStream)));
        PrintWriter out = new PrintWriter(new PipedOutputStream(outStream), true);
        TaskList taskList = new TaskList(in, out);
        applicationThread = new Thread(taskList);
    }

    @BeforeEach
    public void start_the_application() throws IOException {
        applicationThread.start();
        readLines("Welcome to TaskList! Type 'help' for available commands.");
    }

    @AfterEach
    public void kill_the_application() throws IOException, InterruptedException {
        if (!stillRunning()) {
            return;
        }

        Thread.sleep(1000);
        if (!stillRunning()) {
            return;
        }

        applicationThread.interrupt();
        throw new IllegalStateException("The application is still running.");
    }

    @Test
    void it_works() throws IOException {
        execute("show");

        execute("add project secrets");
        execute("add task secrets Eat more donuts.");
        execute("add task secrets Destroy all humans.");

        execute("show");
        readLines(
            "secrets",
            "    [ ] 1: Eat more donuts.",
            "    [ ] 2: Destroy all humans.",
            ""
        );

        execute("add project training");
        execute("add task training Four Elements of Simple Design");
        execute("add task training SOLID");
        execute("add task training Coupling and Cohesion");
        execute("add task training Primitive Obsession");
        execute("add task training Outside-In TDD");
        execute("add task training Interaction-Driven Design");

        execute("check 1");
        execute("check 3");
        execute("check 5");
        execute("check 6");

        execute("show");
        readLines(
                "secrets",
                "    [x] 1: Eat more donuts.",
                "    [ ] 2: Destroy all humans.",
                "",
                "training",
                "    [x] 3: Four Elements of Simple Design",
                "    [ ] 4: SOLID",
                "    [x] 5: Coupling and Cohesion",
                "    [x] 6: Primitive Obsession",
                "    [ ] 7: Outside-In TDD",
                "    [ ] 8: Interaction-Driven Design",
                ""
        );

        execute("quit");
    }

    @Test
    void deadline_command_is_accepted_for_an_existing_task() throws IOException {
        execute("add project secrets");
        execute("add task secrets Eat more donuts.");

        execute("deadline 1 20-06-2026");

        execute("show");
        readLines(
            "secrets",
            "    [ ] 1: Eat more donuts.",
            ""
        );

        execute("quit");
    }

    @Test
    void deadline_command_reports_when_the_task_does_not_exist() throws IOException {
        execute("deadline 99 20-06-2026");
        readLines("Could not find a task with an ID of 99.");

        execute("quit");
    }

    @Test
    void today_shows_only_tasks_with_a_deadline_of_today() throws IOException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String today = LocalDate.now().format(format);
        String tomorrow = LocalDate.now().plusDays(1).format(format);

        execute("add project secrets");
        execute("add task secrets Eat more donuts.");
        execute("add task secrets Destroy all humans.");
        execute("add project training");
        execute("add task training Refactor the codebase");

        execute("deadline 1 " + today);
        execute("deadline 2 " + tomorrow);

        execute("today");
        readLines(
            "secrets",
            "    [ ] 1: Eat more donuts.",
            ""
        );

        execute("quit");
    }

    @Test
    void view_by_deadline_groups_tasks_by_date_then_by_project_with_no_deadline_last() throws IOException {
        execute("add project secrets");
        execute("add task secrets Eat more donuts.");
        execute("add project training");
        execute("add task training Refactor the codebase");
        execute("add task training Interaction-Driven Design");
        execute("add task training Four Elements of Simple Design");

        execute("deadline 3 13-11-2021");
        execute("deadline 1 11-11-2021");
        execute("deadline 4 11-11-2021");

        execute("view-by-deadline");
        readLines(
            "11-11-2021:",
            "     secrets:",
            "       \t1: Eat more donuts.",
            "     training:",
            "       \t4: Four Elements of Simple Design",
            "13-11-2021:",
            "     training:",
            "       \t3: Interaction-Driven Design",
            "No deadline:",
            "     training:",
            "       \t2: Refactor the codebase"
        );

        execute("quit");
    }

    @Test
    void reports_an_invalid_date_and_keeps_running() throws IOException {
        execute("add project secrets");
        execute("add task secrets Eat more donuts.");

        execute("deadline 1 not-a-date");
        readLines("Could not parse \"not-a-date\" as a date. Use the format dd-MM-yyyy.");

        // The application must recover and keep processing commands.
        execute("show");
        readLines(
            "secrets",
            "    [ ] 1: Eat more donuts.",
            ""
        );

        execute("quit");
    }

    @Test
    void reports_an_invalid_task_id() throws IOException {
        execute("check abc");
        readLines("Could not parse \"abc\" as a task ID.");

        execute("quit");
    }

    @Test
    void reports_missing_arguments() throws IOException {
        execute("deadline 1");
        readLines("The \"deadline\" command needs more arguments. Type 'help' for usage.");

        execute("add task secrets");
        readLines("The \"add task\" command needs more arguments. Type 'help' for usage.");

        execute("check");
        readLines("The \"check\" command needs more arguments. Type 'help' for usage.");

        execute("quit");
    }

    @Test
    void reports_when_adding_a_task_to_an_unknown_project() throws IOException {
        execute("add task missing Some task");
        readLines("Could not find a project with the name \"missing\".");

        execute("quit");
    }

    @Test
    void reports_an_unknown_command() throws IOException {
        execute("foobar");
        readLines("I don't know what the command \"foobar\" is.");

        execute("quit");
    }

    private void execute(String command) throws IOException {
        read(PROMPT);
        write(command);
    }

    private void read(String expectedOutput) throws IOException {
        int length = expectedOutput.length();
        char[] buffer = new char[length];
        // Reader.read may return fewer characters than requested, so keep
        // reading until the buffer is full (or the stream ends).
        int total = 0;
        while (total < length) {
            int count = outReader.read(buffer, total, length - total);
            if (count == -1) {
                break;
            }
            total += count;
        }
        assertThat(String.valueOf(buffer, 0, total), is(expectedOutput));
    }

    private void readLines(String... expectedOutput) throws IOException {
        for (String line : expectedOutput) {
            read(line + lineSeparator());
        }
    }

    private void write(String input) {
        inWriter.println(input);
    }

    private boolean stillRunning() {
        return applicationThread != null && applicationThread.isAlive();
    }
}
