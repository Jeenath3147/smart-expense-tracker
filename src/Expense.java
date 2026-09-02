import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Expense {
    private static int nextId = 1;

    private final int id;
    private double amount;
    private ExpenseCategory category;
    private String description;
    private LocalDate date;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public Expense(double amount, ExpenseCategory category, String description, LocalDate date)
            throws InvalidExpenseException {
        validate(amount, category, description, date);
        this.id = nextId++;
        this.amount = amount;
        this.category = category;
        this.description = description.trim();
        this.date = date;
    }

    private Expense(int id, double amount, ExpenseCategory category, String description, LocalDate date)
            throws InvalidExpenseException {
        if (id <= 0) {
            throw new InvalidExpenseException("ID must be greater than zero.");
        }
        validate(amount, category, description, date);
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description.trim();
        this.date = date;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    private static void validate(double amount, ExpenseCategory category, String description, LocalDate date)
            throws InvalidExpenseException {
        if (amount <= 0) {
            throw new InvalidExpenseException("Amount must be greater than zero.");
        }
        if (category == null) {
            throw new InvalidExpenseException("Category cannot be null.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidExpenseException("Description cannot be empty.");
        }
        if (date == null) {
            throw new InvalidExpenseException("Date cannot be null.");
        }
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public ExpenseCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }

    public void update(double amount, ExpenseCategory category, String description, LocalDate date)
            throws InvalidExpenseException {
        validate(amount, category, description, date);
        this.amount = amount;
        this.category = category;
        this.description = description.trim();
        this.date = date;
    }

    public String toFileLine() {
        String safeDescription = description.replace(",", ";");
        return id + "," + amount + "," + category.name() + "," + safeDescription + "," + date.format(DATE_FORMAT);
    }

    public static Expense fromFileLine(String line) throws InvalidExpenseException {
        String[] parts = line.split(",", 5);
        if (parts.length != 5) {
            throw new InvalidExpenseException("Corrupted expense record: " + line);
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            double amount = Double.parseDouble(parts[1].trim());
            ExpenseCategory category = ExpenseCategory.fromString(parts[2].trim());
            String description = parts[3];
            LocalDate date = LocalDate.parse(parts[4].trim(), DATE_FORMAT);
            return new Expense(id, amount, category, description, date);
        } catch (NumberFormatException e) {
            throw new InvalidExpenseException("Corrupted number in record: " + line);
        } catch (java.time.format.DateTimeParseException e) {
            throw new InvalidExpenseException("Corrupted date in record: " + line);
        }
    }
    public static Expense fromDatabase(int id, double amount, ExpenseCategory category, String description, LocalDate date) throws InvalidExpenseException  {
        return new Expense(id, amount, category, description, date);
    }

    @Override
    public String toString() {
        return String.format("#%-3d %-10s Rs.%-10.2f %-13s %s", id, date, amount, category, description);
    }
}
