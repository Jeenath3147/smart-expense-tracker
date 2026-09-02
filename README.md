# Smart Expense Tracker

A console-based personal expense tracker written in Java, backed by a MySQL database. Add, view, update, and delete expenses, see monthly summaries, and filter by category — all from a simple text menu.

## Features

- Add an expense (amount, category, description, date)
- View all expenses (most recent first)
- View expenses filtered by category
- Monthly summary of spending
- Update or delete an existing expense
- Categories: Food, Travel, Rent, Utilities, Entertainment, Healthcare, Shopping, Other

## Tech stack

- Java (plain JDBC, no frameworks)
- MySQL
- Eclipse project (`.classpath` / `.project`)

## Setup

### 1. Create the database table

Connect to your MySQL server and run:

```sql
CREATE DATABASE expense_tracker;
USE expense_tracker;

CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DOUBLE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    expense_date DATE NOT NULL
);
```

### 2. Set your database credentials as environment variables

The app reads its database connection details from environment variables — no credentials are stored in the code. Set these before running:

- `DB_URL` — e.g. `jdbc:mysql://localhost:3306/expense_tracker`
- `DB_USERNAME` — your MySQL username
- `DB_PASSWORD` — your MySQL password

On Windows (Command Prompt):
```
set DB_URL=jdbc:mysql://localhost:3306/expense_tracker
set DB_USERNAME=root
set DB_PASSWORD=yourpassword
```

Or set them permanently under System Properties → Environment Variables.

### 3. Add the MySQL Connector/J driver

Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and add the `.jar` file to your project's build path (in Eclipse: right-click project → Build Path → Add External JARs). This project was built against version 9.7.0.

### 4. Compile and run

From the `src` folder:

```
javac *.java
java Main
```

## Project structure

```
src/
├── Main.java                    # console menu / entry point
├── ExpenseManager.java          # business logic
├── Expense.java                 # expense model
├── ExpenseCategory.java         # category enum
├── ExpenseRepository.java       # repository interface
├── MySqlExpenseRepository.java  # MySQL-backed implementation
├── FakeExpenseRepository.java   # in-memory implementation (for testing)
├── DtabaseConfig.java           # reads DB_URL / DB_USERNAME / DB_PASSWORD from env
├── FileHandler.java
├── InvalidExpenseException.java
└── TestRunner.java
```
