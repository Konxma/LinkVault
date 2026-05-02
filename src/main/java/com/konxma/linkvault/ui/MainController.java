package com.konxma.linkvault.ui;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.repository.LinkRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Контролер головного вікна програми.
 * Управління відображенням категорій, таблицею посилань та реактивним пошуком.
 * Всі звернення до бази даних виконуються асинхронно для збереження плавності інтерфейсу.
 */
public class MainController {

  @FXML private ListView<Category> categoryListView;
  @FXML private TableView<Link> linksTable;
  @FXML private TableColumn<Link, String> titleColumn;
  @FXML private TableColumn<Link, String> urlColumn;
  @FXML private TableColumn<Link, String> tagsColumn;
  @FXML private Label currentCategoryLabel;
  @FXML private TextField searchField;

  private UserDTO currentUser;
  private LinkRepository linkRepository;

  public void initData(UserDTO user) {
    this.currentUser = user;
    this.linkRepository = new LinkRepository();

    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tagsAsString"));

    categoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        currentCategoryLabel.setText("Папка: " + newValue.getName() + " (Завантаження...)");
        loadLinksForCategoryAsync(newValue.getCategoryId(), newValue.getName());
      }
    });

    loadCategoriesAsync();
  }

  private void loadCategoriesAsync() {
    CompletableFuture.supplyAsync(() -> linkRepository.getCategoriesByUserId(currentUser.getUserId()))
        .thenAccept(categories -> {
          Platform.runLater(() -> {
            categoryListView.getItems().clear();
            categoryListView.getItems().addAll(categories);
          });
        })
        .exceptionally(ex -> {
          Platform.runLater(() -> showAlert("Помилка", "Не вдалося завантажити категорії."));
          return null;
        });
  }

  private void loadLinksForCategoryAsync(int categoryId, String categoryName) {
    CompletableFuture.supplyAsync(() -> linkRepository.getLinksByCategoryId(categoryId))
        .thenAccept(links -> {
          Platform.runLater(() -> {
            currentCategoryLabel.setText("Папка: " + categoryName);
            ObservableList<Link> masterData = FXCollections.observableArrayList(links);
            FilteredList<Link> filteredData = new FilteredList<>(masterData, p -> true);

            if (searchField != null) {
              searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(link -> {
                  if (newValue == null || newValue.isEmpty()) return true;
                  String lowerCaseFilter = newValue.toLowerCase();
                  if (link.getTitle().toLowerCase().contains(lowerCaseFilter)) return true;
                  if (link.getUrl().toLowerCase().contains(lowerCaseFilter)) return true;
                  if (link.getTagsAsString().toLowerCase().contains(lowerCaseFilter)) return true;
                  return false;
                });
              });
              searchField.setText("");
            }

            SortedList<Link> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(linksTable.comparatorProperty());
            linksTable.setItems(sortedData);
          });
        })
        .exceptionally(ex -> {
          Platform.runLater(() -> {
            currentCategoryLabel.setText("Помилка завантаження");
            showAlert("Помилка", "Не вдалося завантажити посилання.");
          });
          return null;
        });
  }

  @FXML
  private void handleAddCategory() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Нова папка");
    dialog.setHeaderText("Створення нової папки");
    dialog.setContentText("Введіть назву:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(name -> {
      if (!name.trim().isEmpty()) {
        Category newCategory = new Category(0, currentUser.getUserId(), name, "");
        CompletableFuture.supplyAsync(() -> linkRepository.addCategory(newCategory))
            .thenAccept(success -> {
              Platform.runLater(() -> {
                if (success) {
                  loadCategoriesAsync();
                } else {
                  showAlert("Помилка", "Не вдалося створити папку.");
                }
              });
            });
      }
    });
  }

  @FXML
  private void handleAddLink() {
    Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
    if (selectedCategory == null) {
      showAlert("Помилка", "Спочатку оберіть папку зліва, куди додати посилання!");
      return;
    }

    Dialog<Pair<Link, String>> dialog = new Dialog<>();
    dialog.setTitle("Нова закладка");
    dialog.setHeaderText("Додавання посилання в папку: " + selectedCategory.getName());

    ButtonType saveButtonType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField titleField = new TextField();
    titleField.setPromptText("Наприклад: Google");
    TextField urlField = new TextField();
    urlField.setPromptText("https://...");
    TextField tagsField = new TextField();
    tagsField.setPromptText("Java, Навчання, Важливе");

    grid.add(new Label("Назва:"), 0, 0);
    grid.add(titleField, 1, 0);
    grid.add(new Label("URL:"), 0, 1);
    grid.add(urlField, 1, 1);
    grid.add(new Label("Теги (через кому):"), 0, 2);
    grid.add(tagsField, 1, 2);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType && !urlField.getText().isEmpty() && !titleField.getText().isEmpty()) {
        Link newLink = new Link(0, selectedCategory.getCategoryId(), urlField.getText(), titleField.getText());
        return new Pair<>(newLink, tagsField.getText());
      }
      return null;
    });

    Optional<Pair<Link, String>> result = dialog.showAndWait();
    result.ifPresent(pair -> {
      Link link = pair.getKey();
      String tagsString = pair.getValue();

      CompletableFuture.supplyAsync(() -> linkRepository.addLinkWithTags(link, tagsString))
          .thenAccept(success -> {
            Platform.runLater(() -> {
              if (success) {
                loadLinksForCategoryAsync(selectedCategory.getCategoryId(), selectedCategory.getName());
              } else {
                showAlert("Помилка", "Не вдалося зберегти посилання.");
              }
            });
          });
    });
  }

  @FXML
  private void handleDeleteLink() {
    Link selectedLink = linksTable.getSelectionModel().getSelectedItem();
    if (selectedLink == null) {
      showAlert("Увага", "Оберіть посилання в таблиці для видалення.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Підтвердження");
    alert.setHeaderText("Видалити це посилання?");
    alert.setContentText(selectedLink.getTitle());

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      CompletableFuture.supplyAsync(() -> linkRepository.deleteLink(selectedLink.getLinkId()))
          .thenAccept(success -> {
            Platform.runLater(() -> {
              if (success) {
                linksTable.getItems().remove(selectedLink);
              } else {
                showAlert("Помилка", "Не вдалося видалити.");
              }
            });
          });
    }
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
