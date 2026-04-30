package com.konxma.linkvault;

import com.konxma.linkvault.repository.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/LoginView.fxml")));
    Scene scene = new Scene(root, 400, 350);
    primaryStage.setTitle("LinkVault - Авторизація");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  public static void main(String[] args) {
    // ЗАПУСКАЄМО МІГРАЦІЇ ПЕРЕД СТАРТОМ ПРОГРАМИ
    try {
      DatabaseConnection.migrateDatabase();
    } catch (Exception e) {
      System.err.println("Помилка при виконанні міграцій: " + e.getMessage());
      e.printStackTrace();
      return; // Зупиняємо програму, якщо база даних не налаштувалася
    }

    launch(args);
  }
}