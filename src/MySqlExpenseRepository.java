import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class MySqlExpenseRepository implements ExpenseRepository{

	@Override
	public void addExpense(Expense expense) throws Exception {
		String sql="INSERT INTO expenses (amount,category,description,expense_date) VALUES (?,?,?,?)";
		try(Connection conn=DriverManager.getConnection(DtabaseConfig.DB_URL,
				DtabaseConfig.DB_USERNAME,
				DtabaseConfig.DB_PASSWORD);
				PreparedStatement pstmt=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
				pstmt.setDouble(1,expense.getAmount());
				pstmt.setString(2,expense.getCategory().name());
				pstmt.setString(3,expense.getDescription());
				pstmt.setDate(4, Date.valueOf(expense.getDate()));
				pstmt.executeUpdate();
		}
				// TODO Auto-generated method stub
		
	}

	@Override
	public List<Expense> getAllExpenses() throws Exception {
		List<Expense> expenses=new ArrayList<>();
		String sql="SELECT id,amount,category,description,expense_date FROM expenses  ORDER BY expense_date DESC";
		try(Connection conn=DriverManager.getConnection(DtabaseConfig.DB_URL,
				DtabaseConfig.DB_USERNAME,
				DtabaseConfig.DB_PASSWORD);
				PreparedStatement pstmt= conn.prepareStatement(sql);
				ResultSet rs=pstmt.executeQuery();){
					while(rs.next()) {
						int id=rs.getInt("id");
						Double amount=rs.getDouble("amount");
						ExpenseCategory category=ExpenseCategory.valueOf(rs.getString("category"));
						String description =rs.getString("description");
						LocalDate date=rs.getDate("expense_date").toLocalDate();
						expenses.add(Expense.fromDatabase(id, amount, category, description, date));
					}
				}
				
		return expenses;
	}

	@Override
	public boolean updateExpense(int id, double amount, ExpenseCategory category, String description, LocalDate date)
			throws Exception {
		String sql="UPDATE expenses SET amount=?,category=?,description=?,expense_date=? WHERE id=?";
		 try (Connection conn = DriverManager.getConnection(
				 DtabaseConfig.DB_URL,
					DtabaseConfig.DB_USERNAME,
					DtabaseConfig.DB_PASSWORD);
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setDouble(1, amount);
	            stmt.setString(2, category.name());
	            stmt.setString(3, description);
	            stmt.setDate(4, Date.valueOf(date));
	            stmt.setInt(5, id);

	            int rowsAffected = stmt.executeUpdate();
	            return rowsAffected > 0;
	        }
		
	}

	@Override
	public boolean deleteExpense(int id) throws Exception {
		String sql="DELETE FROM expenses WHERE id=?";
		try(Connection conn=DriverManager.getConnection(DtabaseConfig.DB_URL,
				DtabaseConfig.DB_USERNAME,
				DtabaseConfig.DB_PASSWORD);
				PreparedStatement pstmt= conn.prepareStatement(sql)){
					pstmt.setInt(1, id);
				
				int rowsAffected=pstmt.executeUpdate();
		return rowsAffected>0;
				}
	}
	
	
	

}
