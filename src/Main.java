import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpenseRepository repository = new MySqlExpenseRepository();
        ExpenseManager manager = new ExpenseManager(repository);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addExpense(scanner, manager);
                    break;
                case "2":
                    viewAllExpenses(manager);
                    break;
                case "3":
                    viewByCategory(scanner, manager);
                    break;
                case "4":
                    monthlySummary(manager);
                    break;
                case "5":
                    updateExpense(scanner, manager);
                    break;
                case "6":
                    deleteExpense(scanner, manager);
                    break;
                case "7":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-7.");
                    break;
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== Smart Expense Tracker (MySQL) =====");
        System.out.println("1. Add Expense");
        System.out.println("2. View All Expenses");
        System.out.println("3. View Expenses by Category");
        System.out.println("4. Monthly Spending Summary");
        System.out.println("5. Update an Expense");
        System.out.println("6. Delete an Expense");
        System.out.println("7. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addExpense(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Category (FOOD, TRAVEL, RENT, UTILITIES, ENTERTAINMENT, HEALTHCARE, SHOPPING, OTHER): ");
            ExpenseCategory category = ExpenseCategory.fromString(scanner.nextLine().trim());

            System.out.print("Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Date (yyyy-MM-dd), or press Enter for today: ");
            String dateInput = scanner.nextLine().trim();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            Expense expense = new Expense(amount, category, description, date);
            manager.addExpense(expense);
            System.out.println("Added: " + expense);

        } catch (NumberFormatException e) {
            System.out.println("Error: amount must be a valid number.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: date must be in yyyy-MM-dd format.");
        } catch (InvalidExpenseException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void viewAllExpenses(ExpenseManager manager) {
        try {
            List<Expense> all = manager.getAllExpenses();
            if (all.isEmpty()) {
                System.out.println("No expenses recorded yet.");
                return;
            }
            for (Expense e : all) {
                System.out.println(e);
            }
            System.out.printf("Total spent: %.2f%n", manager.getTotalSpent());
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void viewByCategory(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Category: ");
            ExpenseCategory category = ExpenseCategory.fromString(scanner.nextLine().trim());
            List<Expense> filtered = manager.getExpensesByCategory(category);
            if (filtered.isEmpty()) {
                System.out.println("No expenses in category " + category);
                return;
            }
            double total = 0;
            for (Expense e : filtered) {
                System.out.println(e);
                total += e.getAmount();
            }
            System.out.printf("Total for %s: %.2f%n", category, total);
        } catch (InvalidExpenseException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void monthlySummary(ExpenseManager manager) {
        try {
            Map<YearMonth, Double> summary = manager.getMonthlySummary();
            if (summary.isEmpty()) {
                System.out.println("No expenses recorded yet.");
                return;
            }
            System.out.println("Monthly summary:");
            for (Map.Entry<YearMonth, Double> entry : summary.entrySet()) {
                System.out.printf("  %s : %.2f%n", entry.getKey(), entry.getValue());
            }

            System.out.println("\nCategory-wise breakdown (all-time):");
            Map<ExpenseCategory, Double> categorySummary = manager.getCategoryWiseSummary();
            for (Map.Entry<ExpenseCategory, Double> entry : categorySummary.entrySet()) {
                if (entry.getValue() > 0) {
                    System.out.printf("  %-13s : %.2f%n", entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void updateExpense(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter the ID of the expense to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("New Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("New Category (FOOD, TRAVEL, RENT, UTILITIES, ENTERTAINMENT, HEALTHCARE, SHOPPING, OTHER): ");
            ExpenseCategory category = ExpenseCategory.fromString(scanner.nextLine().trim());

            System.out.print("New Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("New Date (yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine().trim());

            boolean updated = manager.updateExpense(id, amount, category, description, date);
            System.out.println(updated ? "Updated expense #" + id : "No expense found with that ID.");

        } catch (NumberFormatException e) {
            System.out.println("Error: amount/ID must be a valid number.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: date must be in yyyy-MM-dd format.");
        } catch (InvalidExpenseException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void deleteExpense(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter the ID of the expense to delete: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean removed = manager.deleteExpense(id);
            System.out.println(removed ? "Deleted expense #" + id : "No expense found with that ID.");
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a whole number.");
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}