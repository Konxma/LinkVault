package com.konxma.linkvault.service;

import com.konxma.linkvault.model.Category;
import com.konxma.linkvault.repository.CategoryRepository;
import java.util.List;

public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService() {
    this.categoryRepository = new CategoryRepository();
  }

  public List<Category> getUserCategories(int userId) {
    return categoryRepository.findByUserId(userId);
  }

  public void addCategory(Category category) {
    categoryRepository.save(category);
  }

  public void updateCategory(Category category) {
    categoryRepository.update(category);
  }

  public void deleteCategory(int categoryId) {
    categoryRepository.delete(categoryId);
  }
}