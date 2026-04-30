package com.konxma.linkvault.ui;

import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.model.User;
import com.konxma.linkvault.repository.LinkRepository;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public class MainController {

  @FXML private ListView<Category> categoryListView;
  @FXML private TableView<Link> linksTable;
  @FXML private TableColumn<Link, String> titleColumn;
  @FXML private TableColumn<Link, String> urlColumn;
  @FXML private TableColumn<Link, String> tagsColumn;

  @FXML private Label currentCategoryLabel;

  private User currentUser;
  private LinkRepository linkRepository;

  public void initData(User user) {
    this.currentUser = user;
    this.linkRepository = new LinkRepository();

    // Зв'язуємо колонки таблиці з полями об'єкта Link
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
    tagsColumn.setCellValueFactory(new PropertyValueFactory<>("tagsAsString"));

    categoryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        currentCategoryLabel.setText("Папка: " + newValue.getName());
        loadLinksForCategory(newValue.getCategoryId());
      }
    });

    loadCategories();
  }

  private void loadCategories() {
    categoryListView.getItems().clear();
    List<Category> categories = linkRepository.getCategoriesByUserId(currentUser.getUserId());
    categoryListView.getItems().addAll(categories);
  }

  private void loadLinksForCategory(int categoryId) {
    linksTable.getItems().clear();
    List<Link> links = linkRepository.getLinksByCategoryId(categoryId);
    linksTable.getItems().addAll(links);
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
        if (linkRepository.addCategory(newCategory)) {
          loadCategories();
        } else {
          showAlert("Помилка", "Не вдалося створити папку.");
        }
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

      if (linkRepository.addLinkWithTags(link, tagsString)) {
        loadLinksForCategory(selectedCategory.getCategoryId());
      } else {
        showAlert("Помилка", "Не вдалося зберегти посилання.");
      }
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
      if (linkRepository.deleteLink(selectedLink.getLinkId())) {
        linksTable.getItems().remove(selectedLink);
      } else {
        showAlert("Помилка", "Не вдалося видалити.");
      }
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