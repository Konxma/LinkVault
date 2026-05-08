package com.konxma.linkvault.viewmodel;

import com.konxma.linkvault.dto.UserDTO;
import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.service.CategoryService;
import com.konxma.linkvault.service.LinkService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.CompletableFuture;

/**
 * ViewModel для головного вікна програми.
 * Зберігає стан і виконує всю бізнес-логіку асинхронно, щоб не блокувати UI.
 */
public class MainViewModel {
  private final CategoryService categoryService;
  private final LinkService linkService;
  private UserDTO currentUser;

  // Реактивні списки
  private final ObservableList<Category> categories = FXCollections.observableArrayList();
  private final ObservableList<Link> links = FXCollections.observableArrayList();

  // Реактивні властивості стану
  private final StringProperty currentCategoryLabel = new SimpleStringProperty("Оберіть папку зліва");
  private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
  private final ObjectProperty<Category> selectedCategory = new SimpleObjectProperty<>(null);

  public MainViewModel() {
    this.categoryService = new CategoryService();
    this.linkService = new LinkService();
  }

  public void initData(UserDTO user) {
    this.currentUser = user;
    loadCategories();
  }

  // --- Геттери для UI (Binding) ---
  public ObservableList<Category> getCategories() { return categories; }
  public ObservableList<Link> getLinks() { return links; }
  public StringProperty currentCategoryLabelProperty() { return currentCategoryLabel; }
  public BooleanProperty isLoadingProperty() { return isLoading; }
  public ObjectProperty<Category> selectedCategoryProperty() { return selectedCategory; }
  public UserDTO getCurrentUser() { return currentUser; }

  // ==================== ЛОГІКА ДЛЯ КАТЕГОРІЙ ====================

  public CompletableFuture<Void> loadCategories() {
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> categoryService.getUserCategories(currentUser.getUserId()))
        .thenAccept(result -> Platform.runLater(() -> {
          categories.setAll(result);
          isLoading.set(false);
        }));
  }

  public CompletableFuture<Boolean> addCategory(String name) {
    Category newCategory = new Category(0, currentUser.getUserId(), name, "");
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> {
          categoryService.addCategory(newCategory);
          return true;
        }).thenCompose(success -> loadCategories().thenApply(v -> true))
        .whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }

  public CompletableFuture<Boolean> updateCategory(Category category, String newName) {
    category.setName(newName);
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> {
          categoryService.updateCategory(category);
          return true;
        }).thenCompose(success -> loadCategories().thenApply(v -> true))
        .whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }

  public CompletableFuture<Boolean> deleteCategory(Category category) {
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> {
          categoryService.deleteCategory(category.getCategoryId());
          return true;
        }).thenCompose(success -> loadCategories().thenApply(v -> true))
        .whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }

  // ==================== ЛОГІКА ДЛЯ ПОСИЛАНЬ ====================

  public CompletableFuture<Void> loadLinksForCategory(Category category) {
    if (category == null) return CompletableFuture.completedFuture(null);

    isLoading.set(true);
    Platform.runLater(() -> currentCategoryLabel.set("Папка: " + category.getName() + " (Завантаження...)"));

    return CompletableFuture.supplyAsync(() -> linkService.getLinksByCategory(category.getCategoryId()))
        .thenAccept(result -> Platform.runLater(() -> {
          links.setAll(result);
          currentCategoryLabel.set("Папка: " + category.getName());
          isLoading.set(false);
        }));
  }

  public CompletableFuture<Boolean> addLink(Link link, String tagsString) {
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> linkService.addLinkWithTags(link, tagsString))
        .thenCompose(success -> {
          if (success && selectedCategory.get() != null) {
            return loadLinksForCategory(selectedCategory.get()).thenApply(v -> true);
          }
          return CompletableFuture.completedFuture(false);
        })
        .whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }

  public CompletableFuture<Boolean> updateLink(Link link) {
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> {
      linkService.updateLink(link);
      return true;
    }).thenCompose(success -> {
      if (selectedCategory.get() != null) {
        return loadLinksForCategory(selectedCategory.get()).thenApply(v -> true);
      }
      return CompletableFuture.completedFuture(true);
    }).whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }

  public CompletableFuture<Boolean> deleteLink(Link link) {
    isLoading.set(true);
    return CompletableFuture.supplyAsync(() -> linkService.deleteLink(link.getLinkId()))
        .thenApply(success -> {
          if (success) {
            Platform.runLater(() -> links.remove(link));
          }
          return success;
        })
        .whenComplete((res, ex) -> Platform.runLater(() -> isLoading.set(false)));
  }
}