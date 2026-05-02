package com.konxma.linkvault.repository;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EntityMapper<T> {

  private final Class<T> clazz;

  public EntityMapper(Class<T> clazz) {
    this.clazz = clazz;
  }

  public List<T> mapResultSetToList(ResultSet rs) throws Exception {
    List<T> resultList = new ArrayList<>();

    while (rs.next()) {
      // Створюємо новий пустий об'єкт потрібного класу
      T entity = clazz.getDeclaredConstructor().newInstance();

      // Проходимося по всіх полях цього класу
      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true); // Дозволяємо запис у приватні поля
        String columnName = getColumnName(field);

        try {
          Object value = rs.getObject(columnName);
          if (value != null) {
            field.set(entity, value);
          }
        } catch (Exception e) {
          // Якщо такої колонки немає в ResultSet, просто йдемо далі
        }
      }
      resultList.add(entity);
    }
    return resultList;
  }

  private String getColumnName(Field field) {
    // Автоматично конвертуємо camelCase у snake_case (наприклад userId -> user_id)
    String regex = "([a-z])([A-Z]+)";
    String replacement = "$1_$2";
    return field.getName().replaceAll(regex, replacement).toLowerCase();
  }
}