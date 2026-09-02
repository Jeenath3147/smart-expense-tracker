import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private final String filePath;

    public FileHandler(String filePath) {
        this.filePath = filePath;
    }

    public void saveExpenses(List<Expense> expenses) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense expense : expenses) {
                writer.write(expense.toFileLine());
                writer.newLine();
            }
        }
    }

    public List<Expense> loadExpenses() throws IOException {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return expenses;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                try {
                    expenses.add(Expense.fromFileLine(line));
                } catch (InvalidExpenseException e) {
                    System.out.println("Skipping corrupted record on line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return expenses;
    }
}
