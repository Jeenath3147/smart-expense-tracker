import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ExpenseManager {
    private final ExpenseRepository repository;

    public ExpenseManager(ExpenseRepository repository) {
        this.repository = repository;
    }

    public void addExpense(Expense expense) throws Exception {
        repository.addExpense(expense);
    }

    public boolean deleteExpense(int id) throws Exception {
        return repository.deleteExpense(id);
    }

    public boolean updateExpense(int id, double amount, ExpenseCategory category, String description, LocalDate date) throws Exception {
        return repository.updateExpense(id, amount, category, description, date);
    }

    public List<Expense> getAllExpenses() throws Exception {
        return repository.getAllExpenses();
    }

    public List<Expense> getExpensesByCategory(ExpenseCategory category) throws Exception {
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : repository.getAllExpenses()) {
            if (e.getCategory() == category) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public double getTotalSpent() throws Exception {
        double total = 0;
        for (Expense e : repository.getAllExpenses()) {
            total += e.getAmount();
        }
        return total;
    }

    public Map<ExpenseCategory, Double> getCategoryWiseSummary() throws Exception {
        Map<ExpenseCategory, Double> summary = new LinkedHashMap<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            summary.put(category, 0.0);
        }
        for (Expense e : repository.getAllExpenses()) {
            summary.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return summary;
    }

    public Map<YearMonth, Double> getMonthlySummary() throws Exception {
        Map<YearMonth, Double> summary = new TreeMap<>();
        for (Expense e : repository.getAllExpenses()) {
            YearMonth ym = YearMonth.from(e.getDate());
            summary.merge(ym, e.getAmount(), Double::sum);
        }
        return summary;
    }
}