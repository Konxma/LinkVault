package com.konxma.linkvault.infrastructure;

public class EmailSender {

  public void sendConfirmationEmail(String toEmail, String username) {
    // У реальному проєкті тут була б логіка JavaMailSender.
    // Для демонстрації архітектури ми виводимо лог відправки в консоль.
    System.out.println("\n================ EMAIL SERVICE ================");
    System.out.println("Відправляємо лист на адресу: " + toEmail);
    System.out.println("Тема: Підтвердження реєстрації у LinkVault");
    System.out.println("Повідомлення: Вітаємо, " + username + "! Ваш акаунт успішно створено.");
    System.out.println("===============================================\n");
  }
}
