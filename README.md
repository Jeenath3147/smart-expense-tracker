

# Smart Expense Tracker

A desktop expense tracker built in Java, with a JavaFX graphical interface and a MySQL database backend. Add, edit, delete, and filter your expenses, see a running total, and view spending broken down by category and by month.

## Features

- **Expense management** — add, edit, and delete expenses through simple forms, with a confirmation prompt before any delete.
- **Category filtering** — filter the expense table by category, or view everything at once.
- **Running total** — the total for whatever's currently displayed is shown live in the header.
- **Reports** — a pie chart of spending by category and a bar chart of spending by month, generated from the same data as the main table.
- **Persistent storage** — every expense is saved to a MySQL database, so your data is still there the next time you open the app.

## Architecture

The project is built in layers, which keeps the interface, the business rules, and the database completely separate from each other:

- `Expense` — the domain model representing a single expense.
- `ExpenseRepository` — an interface describing how expenses are stored and retrieved, with two implementations:
  - `MySqlExpenseRepository` — talks to a real MySQL database.
  - `FakeExpenseRepository` — an in-memory version used for testing, with no live database required.
- `ExpenseManager` — holds all the business logic (validation, totals, category and monthly summaries) and depends only on the `ExpenseRepository` interface, never on a specific database.
- `MainApp` — the JavaFX window. It only builds the UI and delegates every action (add, edit, delete, summaries) to `ExpenseManager`. It contains no business logic or database code of its own.

Because the UI layer only talks to `ExpenseManager` through the repository abstraction, the interface can be swapped without touching how data is validated or stored underneath it.

## Project Structure

```text
expense-fx-preview/
  README.md
  run.ps1
  .classpath
  .project
  src/
    MainApp.java                 <- JavaFX entry point (GUI)
    ExpenseDialog.java           <- Add/Edit expense form
    ExpenseFormResult.java       <- Data holder for the form
    style.css                    <- GUI styling
    Expense.java
    ExpenseCategory.java
    ExpenseManager.java
    ExpenseRepository.java
    MySqlExpenseRepository.java
    FakeExpenseRepository.java
    FileHandler.java
    InvalidExpenseException.java
    DtabaseConfig.java
    TestRunner.java
```

## Requirements

- JDK 21
- JavaFX SDK 21.0.x (JavaFX is not bundled with the JDK — see setup below)
- A running MySQL server
- MySQL Connector/J (JDBC driver)

## One-time setup

### 1. Database

Create a database and an `expenses` table:

```sql
CREATE DATABASE smart_expense_tracker;
USE smart_expense_tracker;

CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DOUBLE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    expense_date DATE NOT NULL
);
```

The app reads its database connection details from environment variables (see `DtabaseConfig.java`), so set these once:

```powershell
setx DB_URL "jdbc:mysql://localhost:3306/smart_expense_tracker"
setx DB_USERNAME "your_mysql_user"
setx DB_PASSWORD "your_mysql_password"
```

Close and reopen your terminal/IDE afterward so the new values take effect.

### 2. JavaFX SDK

Download the **JavaFX 21.0.x SDK** for Windows from https://gluonhq.com/products/javafx/ (use the version archive if the main download page offers a newer major version — it must match JDK 21) and unzip it, e.g. to `D:\downloads\javafx-sdk-21.0.12`.

### 3. Running from Eclipse

1. Right-click the project → **Build Path → Configure Build Path → Libraries → Add External JARs...** and add every `.jar` from the JavaFX SDK's `lib` folder, plus your MySQL Connector/J `.jar`.
2. Right-click `MainApp.java` → **Run As → Run Configurations → Arguments** tab → **VM arguments**:
   ```
   --module-path "D:\downloads\javafx-sdk-21.0.12\lib" --add-modules javafx.controls
   ```
3. Click **Run**.

### 4. Running from the command line

From the `src` folder, with the two paths in `run.ps1` adjusted to your machine:

```powershell
$FX = "D:\downloads\javafx-sdk-21.0.12\lib"
$MYSQL = "D:\downloads\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar"

javac --module-path $FX --add-modules javafx.controls -cp $MYSQL -d ..\bin *.java
java --module-path $FX --add-modules javafx.controls -cp "..\bin;$MYSQL" MainApp
```

Or simply run `.\run.ps1` from that folder.

## Running tests

The `FakeExpenseRepository` lets the business logic be tested without a live database connection:

```powershell
javac *.java
java TestRunner
```
 ## Demo
[![Watch the demo](https://img.youtube.com/vi/QBSIzuZE0D0/0.jpg)](https://www.youtube.com/watch?v=QBSIzuZE0D0)
