import java.time.LocalDate;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * A modal dialog used to both ADD a new expense and EDIT an existing one.
 * Pass an existing {@link Expense} in to pre-fill the form for editing,
 * or null to start with a blank "add" form.
 */
public class ExpenseDialog extends Dialog<ExpenseFormResult> {

    private final TextField amountField = new TextField();
    private final ComboBox<ExpenseCategory> categoryBox = new ComboBox<>();
    private final TextField descriptionField = new TextField();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final Label errorLabel = new Label();

    public ExpenseDialog(Expense existing) {
        setTitle(existing == null ? "Add Expense" : "Edit Expense #" + existing.getId());
        setHeaderText(existing == null
                ? "Enter the details of the new expense."
                : "Update the details below and save your changes.");

        ButtonType saveButtonType = new ButtonType(existing == null ? "Add" : "Save", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        categoryBox.getItems().addAll(ExpenseCategory.values());
        amountField.setPromptText("e.g. 250.00");
        descriptionField.setPromptText("e.g. Grocery shopping");

        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(320);

        grid.add(new Label("Amount (Rs.):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);

        if (existing != null) {
            amountField.setText(String.valueOf(existing.getAmount()));
            categoryBox.setValue(existing.getCategory());
            descriptionField.setText(existing.getDescription());
            datePicker.setValue(existing.getDate());
        } else {
            categoryBox.setValue(ExpenseCategory.OTHER);
        }

        getDialogPane().setContent(grid);
        getDialogPane().getStyleClass().add("expense-dialog");

        // Validate before allowing the dialog to close on "Add"/"Save".
        var saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInput()) {
                event.consume();
            }
        });

        setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                double amount = Double.parseDouble(amountField.getText().trim());
                return new ExpenseFormResult(amount, categoryBox.getValue(), descriptionField.getText().trim(),
                        datePicker.getValue());
            }
            return null;
        });
    }

    private boolean validateInput() {
        String amountText = amountField.getText() == null ? "" : amountField.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Amount must be a valid number.");
            return false;
        }
        if (amount <= 0) {
            errorLabel.setText("Amount must be greater than zero.");
            return false;
        }
        if (categoryBox.getValue() == null) {
            errorLabel.setText("Please choose a category.");
            return false;
        }
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            errorLabel.setText("Description cannot be empty.");
            return false;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("Please choose a date.");
            return false;
        }
        errorLabel.setText("");
        return true;
    }

    /** Convenience helper so callers can write ExpenseDialog.show(owner). */
    public Optional<ExpenseFormResult> showDialog() {
        return showAndWait();
    }
}
