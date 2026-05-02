package com.konxma.linkvault.repository;

import java.util.HashMap;
import java.util.Map;

public class IdentityMap<T> {
  // Кеш, де ключ - це ID запису, а значення - сам об'єкт
  private final Map<Integer, T> cache = new HashMap<>();

  public void put(Integer id, T entity) {
    cache.put(id, entity);
  }

  public T get(Integer id) {
    return cache.get(id);
  }

  public void remove(Integer id) {
    cache.remove(id);
  }

  public void clear() {
    cache.clear();
  }
}