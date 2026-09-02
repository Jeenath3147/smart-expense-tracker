import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestRunner {
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) throws Exception {
        run("fromFileLine parses a valid record", TestRunner::testFromFileLineParsesValidRecord);
        run("fromFileLine rejects invalid saved records", TestRunner::testFromFileLineRejectsInvalidRecords);
        run("FileHandler saves and loads expenses", TestRunner::testFileSaveAndLoad);
        run("ExpenseManager deletes by ID", TestRunner::testDeleteExpense);
        run("ExpenseManager updates by ID", TestRunner::testUpdateExpense);
        run("ExpenseManager category and monthly summaries", TestRunner::testSummaries);

        System.out.printf("%nPassed %d/%d tests.%n", testsPassed, testsRun);
    }

    private static void testFromFileLineParsesValidRecord() throws Exception {
        Expense expense = Expense.fromFileLine("42,125.5,FOOD,Lunch,2026-08-18");

        assertEquals(42, expense.getId(), "id");
        assertEquals(125.5, expense.getAmount(), "amount");
        assertEquals(ExpenseCategory.FOOD, expense.getCategory(), "category");
        assertEquals("Lunch", expense.getDescription(), "description");
        assertEquals(LocalDate.of(2026, 8, 18), expense.getDate(), "date");
    }

    private static void testFromFileLineRejectsInvalidRecords() {
        assertThrowsInvalidExpense(() -> Expense.fromFileLine("0,125.5,FOOD,Lunch,2026-08-18"), "zero id");
        assertThrowsInvalidExpense(() -> Expense.fromFileLine("12,-50,FOOD,Lunch,2026-08-18"), "negative amount");
        assertThrowsInvalidExpense(() -> Expense.fromFileLine("12,50,UNKNOWN,Lunch,2026-08-18"), "unknown category");
        assertThrowsInvalidExpense(() -> Expense.fromFileLine("12,50,FOOD,   ,2026-08-18"), "empty description");
        assertThrowsInvalidExpense(() -> Expense.fromFileLine("12,50,FOOD,Lunch,18-08-2026"), "bad date");
    }

    private static void testFileSaveAndLoad() throws Exception {
        Path tempFile = Files.createTempFile("expenses-test-", ".txt");
        try {
            FileHandler fileHandler = new FileHandler(tempFile.toString());
            List<Expense> original = Arrays.asList(
                    new Expense(100, ExpenseCategory.FOOD, "Breakfast", LocalDate.of(2026, 8, 1)),
                    new Expense(2500, ExpenseCategory.RENT, "August rent", LocalDate.of(2026, 8, 2))
            );

            fileHandler.saveExpenses(original);
            List<Expense> loaded = fileHandler.loadExpenses();

            assertEquals(2, loaded.size(), "loaded size");
            assertEquals(original.get(0).getAmount(), loaded.get(0).getAmount(), "first amount");
            assertEquals(original.get(0).getCategory(), loaded.get(0).getCategory(), "first category");
            assertEquals(original.get(0).getDescription(), loaded.get(0).getDescription(), "first description");
            assertEquals(original.get(0).getDate(), loaded.get(0).getDate(), "first date");
            assertEquals(original.get(1).getAmount(), loaded.get(1).getAmount(), "second amount");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testDeleteExpense() throws Exception {
        Expense first = new Expense(75, ExpenseCategory.TRAVEL, "Bus ticket", LocalDate.of(2026, 8, 3));
        Expense second = new Expense(40, ExpenseCategory.FOOD, "Tea", LocalDate.of(2026, 8, 3));
        ExpenseManager manager = new ExpenseManager(new FakeExpenseRepository(Arrays.asList(first, second)));

        assertTrue(manager.deleteExpense(first.getId()), "existing expense should be deleted");
        assertEquals(1, manager.getAllExpenses().size(), "expense count after delete");
        assertEquals(second.getId(), manager.getAllExpenses().get(0).getId(), "remaining expense id");
        assertFalse(manager.deleteExpense(999999), "unknown id should not be deleted");
    }

    private static void testUpdateExpense() throws Exception {
        Expense expense = new Expense(75, ExpenseCategory.TRAVEL, "Bus ticket", LocalDate.of(2026, 8, 3));
        ExpenseManager manager = new ExpenseManager(new FakeExpenseRepository(Arrays.asList(expense)));

        boolean updated = manager.updateExpense(
                expense.getId(),
                120,
                ExpenseCategory.FOOD,
                "Dinner",
                LocalDate.of(2026, 8, 4)
        );

        Expense changed = manager.getAllExpenses().get(0);
        assertTrue(updated, "existing expense should update");
        assertEquals(expense.getId(), changed.getId(), "updated expense should keep same id");
        assertEquals(120.0, changed.getAmount(), "updated amount");
        assertEquals(ExpenseCategory.FOOD, changed.getCategory(), "updated category");
        assertEquals("Dinner", changed.getDescription(), "updated description");
        assertEquals(LocalDate.of(2026, 8, 4), changed.getDate(), "updated date");
        assertFalse(manager.updateExpense(999999, 10, ExpenseCategory.OTHER, "Missing", LocalDate.now()), "unknown id should not update");
    }

    private static void testSummaries() throws Exception {
        ExpenseManager manager = new ExpenseManager(new FakeExpenseRepository(Arrays.asList(
                new Expense(100, ExpenseCategory.FOOD, "Lunch", LocalDate.of(2026, 8, 1)),
                new Expense(50, ExpenseCategory.FOOD, "Snacks", LocalDate.of(2026, 8, 2)),
                new Expense(300, ExpenseCategory.TRAVEL, "Train", LocalDate.of(2026, 9, 1))
        )));

        Map<ExpenseCategory, Double> categorySummary = manager.getCategoryWiseSummary();
        assertEquals(150.0, categorySummary.get(ExpenseCategory.FOOD).doubleValue(), "food total");
        assertEquals(300.0, categorySummary.get(ExpenseCategory.TRAVEL).doubleValue(), "travel total");
        assertEquals(0.0, categorySummary.get(ExpenseCategory.RENT).doubleValue(), "empty category total");

        Map<YearMonth, Double> monthlySummary = manager.getMonthlySummary();
        assertEquals(150.0, monthlySummary.get(YearMonth.of(2026, 8)).doubleValue(), "august total");
        assertEquals(300.0, monthlySummary.get(YearMonth.of(2026, 9)).doubleValue(), "september total");
    }

    private static void run(String name, TestCase test) throws Exception {
        testsRun++;
        try {
            test.run();
            testsPassed++;
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            System.out.println("[FAIL] " + name + ": " + e.getMessage());
            throw e;
        }
    }

    private static void assertThrowsInvalidExpense(ThrowingAction action, String message) {
        try {
            action.run();
        } catch (InvalidExpenseException e) {
            return;
        } catch (Exception e) {
            throw new AssertionError(message + " threw the wrong exception: " + e.getClass().getSimpleName());
        }
        throw new AssertionError(message + " should throw InvalidExpenseException");
    }

    private static void assertTrue(boolean actual, String message) {
        if (!actual) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean actual, String message) {
        if (actual) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        double tolerance = 0.000001;
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " expected <" + expected + "> but got <" + actual + ">");
        }
    }

    private interface TestCase {
        void run() throws Exception;
    }

    private interface ThrowingAction {
        void run() throws Exception;
    }
}