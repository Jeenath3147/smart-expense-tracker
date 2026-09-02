import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FakeExpenseRepository implements ExpenseRepository {
    private final List<Expense> expenses;

    public FakeExpenseRepository(List<Expense> initialExpenses) {
        this.expenses = new ArrayList<>(initialExpenses);
    }

    @Override
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    @Override
    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    @Override
    public boolean updateExpense(int id, double amount, ExpenseCategory category, String description, LocalDate date) throws InvalidExpenseException {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId() == id) {
                Expense updated = Expense.fromDatabase(id, amount, category, description, date);
                expenses.set(i, updated);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteExpense(int id) {
        return expenses.removeIf(e -> e.getId() == id);
    }
}