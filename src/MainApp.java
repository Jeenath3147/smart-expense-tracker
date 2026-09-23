import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX front end for the Smart Expense Tracker.
 *
 * This class is purely a "view + controller" layer: every piece of business
 * logic (validation, totals, summaries, persistence) is delegated to the
 * existing {@link ExpenseManager} / {@link ExpenseRepository} classes that
 * already power the console version of this app. That separation is what
 * makes it possible to bolt a GUI onto the project without rewriting the
 * data or domain layer.
 */
public class MainApp extends Application {

    private ExpenseManager manager;
    private final ObservableList<Expense> tableData = FXCollections.observableArrayList();

    private TableView<Expense> table;
    private ComboBox<String> categoryFilter;
    private Label totalLabel;
    private Label statusLabel;

    private PieChart categoryChart;
    private BarChart<String, Number> monthlyChart;

    @Override
    public void start(Stage stage) {
        ExpenseRepository repository = new MySqlExpenseRepository();
        manager = new ExpenseManager(repository);

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildExpensesTab());
        tabPane.getTabs().add(buildReportsTab(tabPane));
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.setCenter(tabPane);

        statusLabel = new Label("Ready.");
        statusLabel.setPadding(new Insets(6, 12, 6, 12));
        statusLabel.getStyleClass().add("status-bar");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 980, 640);
        var css = getClass().getResource("/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Smart Expense Tracker");
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.show();

        refreshAll();
    }

    // ---------------------------------------------------------------- header

    private HBox buildHeader() {
        Label title = new Label("Smart Expense Tracker");
        title.getStyleClass().add("app-title");

        totalLabel = new Label("Total spent: Rs. 0.00");
        totalLabel.getStyleClass().add("total-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(16, title, spacer, totalLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add("app-header");
        return header;
    }

    // ------------------------------------------------------------ expenses

    private Tab buildExpensesTab() {
        Tab tab = new Tab("Expenses");

        table = new TableView<>(tableData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(buildColumns());
        table.setPlaceholder(new Label("No expenses yet. Click \"Add Expense\" to create one."));

        ToolBar toolBar = buildToolBar();

        VBox content = new VBox(10, toolBar, table);
        content.setPadding(new Insets(14));
        VBox.setVgrow(table, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private List<TableColumn<Expense, ?>> buildColumns() {
        TableColumn<Expense, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDate().toString()));
        dateCol.setPrefWidth(110);

        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCategory().name()));
        categoryCol.setPrefWidth(130);

        TableColumn<Expense, String> amountCol = new TableColumn<>("Amount (Rs.)");
        amountCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(String.format("%.2f", data.getValue().getAmount())));
        amountCol.setPrefWidth(110);
        amountCol.getStyleClass().add("amount-column");

        TableColumn<Expense, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));

        return List.of(idCol, dateCol, categoryCol, amountCol, descriptionCol);
    }

    private ToolBar buildToolBar() {
        Button addButton = new Button("Add Expense");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> onAdd());

        Button editButton = new Button("Edit Selected");
        editButton.setOnAction(e -> onEdit());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(e -> onDelete());

        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        for (ExpenseCategory c : ExpenseCategory.values()) {
            categoryFilter.getItems().add(c.name());
        }
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> refreshTable());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshAll());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolBar = new ToolBar(addButton, editButton, deleteButton,
                new Label("   Filter:"), categoryFilter, refreshButton);
        return toolBar;
    }

    private void onAdd() {
        ExpenseDialog dialog = new ExpenseDialog(null);
        Optional<ExpenseFormResult> result = dialog.showDialog();
        result.ifPresent(form -> {
            try {
                Expense expense = new Expense(form.getAmount(), form.getCategory(), form.getDescription(), form.getDate());
                manager.addExpense(expense);
                setStatus("Added expense: " + form.getDescription());
                refreshAll();
            } catch (Exception ex) {
                showError("Could not add expense", ex);
            }
        });
    }

    private void onEdit() {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an expense in the table first.");
            return;
        }
        ExpenseDialog dialog = new ExpenseDialog(selected);
        Optional<ExpenseFormResult> result = dialog.showDialog();
        result.ifPresent(form -> {
            try {
                boolean updated = manager.updateExpense(selected.getId(), form.getAmount(), form.getCategory(),
                        form.getDescription(), form.getDate());
                setStatus(updated ? "Updated expense #" + selected.getId() : "Expense no longer exists.");
                refreshAll();
            } catch (Exception ex) {
                showError("Could not update expense", ex);
            }
        });
    }

    private void onDelete() {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an expense in the table first.");
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Delete expense #" + selected.getId() + " (" + selected.getDescription() + ")?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm deletion");
        confirm.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) {
                try {
                    boolean removed = manager.deleteExpense(selected.getId());
                    setStatus(removed ? "Deleted expense #" + selected.getId() : "Expense no longer exists.");
                    refreshAll();
                } catch (Exception ex) {
                    showError("Could not delete expense", ex);
                }
            }
        });
    }

    // ------------------------------------------------------------- reports

    private Tab buildReportsTab(TabPane owner) {
        Tab tab = new Tab("Reports");

        categoryChart = new PieChart();
        categoryChart.setTitle("Spending by Category");
        categoryChart.setLabelsVisible(true);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (Rs.)");
        monthlyChart = new BarChart<>(xAxis, yAxis);
        monthlyChart.setTitle("Monthly Spending");
        monthlyChart.setLegendVisible(false);

        HBox charts = new HBox(20, categoryChart, monthlyChart);
        charts.setPadding(new Insets(14));
        HBox.setHgrow(categoryChart, Priority.ALWAYS);
        HBox.setHgrow(monthlyChart, Priority.ALWAYS);

        // Refresh the charts whenever this tab becomes visible.
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                refreshCharts();
            }
        });

        tab.setContent(charts);
        return tab;
    }

    private void refreshCharts() {
        try {
            Map<ExpenseCategory, Double> categorySummary = manager.getCategoryWiseSummary();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<ExpenseCategory, Double> entry : categorySummary.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
                }
            }
            categoryChart.setData(pieData);

            Map<YearMonth, Double> monthlySummary = manager.getMonthlySummary();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<YearMonth, Double> entry : monthlySummary.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
            }
            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
        } catch (Exception ex) {
            showError("Could not load reports", ex);
        }
    }

    // --------------------------------------------------------------- shared

    private void refreshAll() {
        refreshTable();
        refreshCharts();
    }

    private void refreshTable() {
        try {
            List<Expense> all = manager.getAllExpenses();
            String filter = categoryFilter == null ? "All Categories" : categoryFilter.getValue();

            tableData.setAll(all.stream()
                    .filter(e -> "All Categories".equals(filter) || e.getCategory().name().equals(filter))
                    .toList());

            double total = tableData.stream().mapToDouble(Expense::getAmount).sum();
            totalLabel.setText(String.format("Total spent: Rs. %.2f", total));
        } catch (Exception ex) {
            showError("Could not load expenses", ex);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String context, Exception ex) {
        setStatus(context + ": " + ex.getMessage());
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(context);
        alert.setContentText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
