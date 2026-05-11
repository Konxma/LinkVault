package com.konxma.linkvault;

/**
 * Клас-обгортка для запуску JavaFX додатку.
 * Необхідний для коректної збірки та запуску проєкту у вигляді єдиного JAR-файлу (fat-jar),
 * оминаючи обмеження модульної системи Java.
 */
public class Launcher {

  /**
   * Головний метод, який делегує запуск основному класу програми.
   *
   * @param args аргументи командного рядка
   */
  public static void main(String[] args) {
    Main.main(args);
  }
}
