import java.time.LocalDate;

/**
 * Simple data holder used to pass the values a user typed into the
 * {@link ExpenseDialog} back to the caller. It is not a JPA/DB entity —
 * just a carrier for form input before it becomes a real {@link Expense}.
 */
public class ExpenseFormResult {
    private final double amount;
    private final ExpenseCategory category;
    private final String description;
    private final LocalDate date;

    public ExpenseFormResult(double amount, ExpenseCategory category, String description, LocalDate date) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public double getAmount() { return amount; }
    public ExpenseCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
}
