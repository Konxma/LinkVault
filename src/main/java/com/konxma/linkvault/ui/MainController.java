package com.konxma.linkvault.ui;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.infrastructure.ExportService;
import com.konxma.linkvault.infrastructure.SessionManager;
import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainController {
  @FXML private ListView<Category> categoryListView;
  @FXML private TableView<Link> linksTable;
  @FXML private TableColumn<Link, String> titleColumn;
  @FXML private TableColumn<Link, String> urlColumn;
  @FXML private TableColumn<Link, String> tagsColumn;
  @FXML private Label currentCategoryLabel;
  @FXML private TextField searchField;

  private final MainViewModel viewModel = new MainViewModel();

  public void initData(UserDTO user) {
    categoryListView.setItems(viewModel.getCategories());
    currentCategoryLabel.textProperty().bind(viewModel.currentCategoryLabelProperty());

    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tagsAsString"));

    FilteredList<Link> filteredData = new FilteredList<>(viewModel.getLinks(), p -> true);
    if (searchField != null) {
      searchField.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(link -> {
          if (newValue == null || newValue.isEmpty()) return true;
          String lowerCaseFilter = newValue.toLowerCase();
          return link.getTitle().toLowerCase().contains(lowerCaseFilter) ||
              link.getUrl().toLowerCase().contains(lowerCaseFilter) ||
              link.getTagsAsString().toLowerCase().contains(lowerCaseFilter);
        });
      });
    }

    SortedList<Link> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(linksTable.comparatorProperty());
    linksTable.setItems(sortedData);

    categoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      viewModel.selectedCategoryProperty().set(newValue);
      if (newValue != null) {
        if (searchField != null) searchField.setText("");
        viewModel.loadLinksForCategory(newValue);
      }
    });

    // ==================== КОНТЕКСТНЕ МЕНЮ ДЛЯ ПАПОК (КЛІК ПРАВОЮ КНОПКОЮ) ====================
    ContextMenu categoryContextMenu = new ContextMenu();
    MenuItem editCategoryItem = new MenuItem("Редагувати назву");
    MenuItem deleteCategoryItem = new MenuItem("Видалити папку");

    editCategoryItem.setOnAction(e -> {
      Category selected = categoryListView.getSelectionModel().getSelectedItem();
      if (selected != null) handleEditCategory(selected);
    });

    deleteCategoryItem.setOnAction(e -> {
      Category selected = categoryListView.getSelectionModel().getSelectedItem();
      if (selected != null) handleDeleteCategory(selected);
    });

    categoryContextMenu.getItems().addAll(editCategoryItem, deleteCategoryItem);
    categoryListView.setContextMenu(categoryContextMenu);

    // Ініціалізація даних користувача
    viewModel.initData(user);
  }

  // ==================== ЛОГІКА ДЛЯ КАТЕГОРІЙ (ПАПОК) ====================
  @FXML
  private void handleAddCategory() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Нова папка");
    dialog.setHeaderText("Створення нової папки");
    dialog.setContentText("Введіть назву:");

    dialog.showAndWait().ifPresent(name -> {
      if (!name.trim().isEmpty()) {
        viewModel.addCategory(name).exceptionally(ex -> {
          Platform.runLater(() -> showAlert("Помилка", "Не вдалося створити папку."));
          return false;
        });
      }
    });
  }

  private void handleEditCategory(Category category) {
    TextInputDialog dialog = new TextInputDialog(category.getName());
    dialog.setTitle("Редагування папки");
    dialog.setHeaderText("Зміна назви папки");
    dialog.setContentText("Введіть нову назву:");

    dialog.showAndWait().ifPresent(newName -> {
      if (!newName.trim().isEmpty() && !newName.equals(category.getName())) {
        viewModel.updateCategory(category, newName).thenAccept(success -> {
          if (!success) Platform.runLater(() -> showAlert("Помилка", "Не вдалося оновити папку."));
        });
      }
    });
  }

  private void handleDeleteCategory(Category category) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Підтвердження");
    alert.setHeaderText("Видалити папку?");
    alert.setContentText("Ви впевнені, що хочете видалити папку '" + category.getName() + "'?\nУВАГА: Усі посилання в ній також будуть видалені!");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        viewModel.deleteCategory(category).thenAccept(success -> {
          if (success) {
            Platform.runLater(() -> {
              viewModel.selectedCategoryProperty().set(null);
              viewModel.getLinks().clear();
              currentCategoryLabel.setText("Оберіть папку зліва");
            });
          } else {
            Platform.runLater(() -> showAlert("Помилка", "Не вдалося видалити папку."));
          }
        });
      }
    });
  }

  // ==================== ЛОГІКА ДЛЯ ПОСИЛАНЬ ====================
  @FXML
  private void handleAddLink() {
    if (viewModel.selectedCategoryProperty().get() == null) {
      showAlert("Помилка", "Спочатку оберіть папку зліва!");
      return;
    }

    Dialog<Pair<Link, String>> dialog = createLinkDialog("Нова закладка", "Додавання посилання", null);
    dialog.showAndWait().ifPresent(pair -> {
      viewModel.addLink(pair.getKey(), pair.getValue()).thenAccept(success -> {
        if (!success) Platform.runLater(() -> showAlert("Помилка", "Не вдалося зберегти посилання."));
      });
    });
  }

  @FXML
  private void handleEditLink() {
    Link selectedLink = linksTable.getSelectionModel().getSelectedItem();
    if (selectedLink == null) {
      showAlert("Увага", "Оберіть посилання в таблиці для редагування.");
      return;
    }

    Dialog<Pair<Link, String>> dialog = createLinkDialog("Редагувати закладку", "Оновлення посилання", selectedLink);
    dialog.showAndWait().ifPresent(pair -> {
      Link updatedLink = pair.getKey();
      updatedLink.setLinkId(selectedLink.getLinkId());
      viewModel.updateLink(updatedLink).thenAccept(success -> {
        if (success) {
          Platform.runLater(() -> showAlert("Успіх", "Посилання оновлено!"));
        } else {
          Platform.runLater(() -> showAlert("Помилка", "Не вдалося оновити."));
        }
      });
    });
  }

  @FXML
  private void handleDeleteLink() {
    Link selectedLink = linksTable.getSelectionModel().getSelectedItem();
    if (selectedLink == null) {
      showAlert("Увага", "Оберіть посилання для видалення.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Підтвердження");
    alert.setHeaderText("Видалити це посилання?");
    alert.setContentText(selectedLink.getTitle());

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        viewModel.deleteLink(selectedLink).thenAccept(success -> {
          if (!success) Platform.runLater(() -> showAlert("Помилка", "Не вдалося видалити."));
        });
      }
    });
  }

  // ==================== ЕКСПОРТ В EXCEL ====================
  @FXML
  private void handleExportToExcel() {
    List<Link> currentLinks = linksTable.getItems();
    if (currentLinks.isEmpty()) {
      showAlert("Увага", "Немає даних для експорту. Оберіть папку з посиланнями.");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Зберегти як Excel");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
    fileChooser.setInitialFileName("LinkVault_Export.xlsx");

    File file = fileChooser.showSaveDialog(linksTable.getScene().getWindow());

    if (file != null) {
      CompletableFuture.runAsync(() -> {
        try {
          ExportService exportService = new ExportService();
          exportService.exportLinksToExcel(new ArrayList<>(currentLinks), file);
          Platform.runLater(() -> showAlert("Успіх", "Дані збережено у файл:\n" + file.getAbsolutePath()));
        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> showAlert("Помилка", "Не вдалося експортувати: " + e.getMessage()));
        }
      });
    }
  }

  // ==================== ВИХІД З АКАУНТУ ====================
  @FXML
  private void handleLogout() {
    SessionManager sessionManager = new SessionManager();
    sessionManager.clearSession();

    Stage currentStage = (Stage) currentCategoryLabel.getScene().getWindow();
    currentStage.close();

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
      Parent root = loader.load();
      Stage loginStage = new Stage();
      loginStage.setScene(new Scene(root, 400, 450));
      loginStage.setTitle("LinkVault - Авторизація");
      loginStage.setResizable(false);
      loginStage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ==================== ДОПОМІЖНІ МЕТОДИ ====================
  private Dialog<Pair<Link, String>> createLinkDialog(String title, String header, Link existingLink) {
    Dialog<Pair<Link, String>> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.setHeaderText(header);

    ButtonType saveButtonType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField titleField = new TextField(existingLink != null ? existingLink.getTitle() : "");
    titleField.setPromptText("Наприклад: Google");
    TextField urlField = new TextField(existingLink != null ? existingLink.getUrl() : "");
    urlField.setPromptText("https://...");
    TextField tagsField = new TextField(existingLink != null ? existingLink.getTagsAsString() : "");
    tagsField.setPromptText("Java, Навчання, Важливе");

    grid.add(new Label("Назва:"), 0, 0);
    grid.add(titleField, 1, 0);
    grid.add(new Label("URL:"), 0, 1);
    grid.add(urlField, 1, 1);

    if (existingLink == null) {
      grid.add(new Label("Теги (через кому):"), 0, 2);
      grid.add(tagsField, 1, 2);
    }

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType && !urlField.getText().isEmpty() && !titleField.getText().isEmpty()) {
        int categoryId = viewModel.selectedCategoryProperty().get().getCategoryId();
        Link link = new Link(0, categoryId, urlField.getText(), titleField.getText());
        return new Pair<>(link, tagsField.getText());
      }
      return null;
    });

    return dialog;
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
