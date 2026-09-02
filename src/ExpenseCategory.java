public enum ExpenseCategory {
    FOOD, TRAVEL, RENT, UTILITIES, ENTERTAINMENT, HEALTHCARE, SHOPPING, OTHER;

    public static ExpenseCategory fromString(String input) throws InvalidExpenseException {
        try {
            return ExpenseCategory.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidExpenseException("Unknown category: " + input);
        }
    }
}
