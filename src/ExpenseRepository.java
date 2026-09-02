import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository {
    void addExpense(Expense expense) throws Exception;

    List<Expense> getAllExpenses() throws Exception;

    boolean updateExpense(int id, double amount, ExpenseCategory category, String description, LocalDate date) throws Exception;

    boolean deleteExpense(int id) throws Exception;
}