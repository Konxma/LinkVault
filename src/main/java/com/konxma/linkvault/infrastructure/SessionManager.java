package com.konxma.linkvault.infrastructure;

import java.util.prefs.Preferences;

/**
 * Інфраструктурний сервіс для керування локальною сесією користувача.
 * Використовує Java Preferences API для збереження даних між запусками програми.
 */
public class SessionManager {
  private static final String PREF_NAME = "linkvault_session";
  private static final String KEY_USER_EMAIL = "saved_user_email";
  private static final String KEY_REMEMBER = "remember_me";

  private final Preferences prefs;

  public SessionManager() {
    this.prefs = Preferences.userRoot().node(PREF_NAME);
  }

  public void saveSession(String email, boolean rememberMe) {
    prefs.putBoolean(KEY_REMEMBER, rememberMe);
    if (rememberMe) {
      prefs.put(KEY_USER_EMAIL, email);
    } else {
      prefs.remove(KEY_USER_EMAIL);
    }
  }

  public String getSavedEmail() {
    return prefs.get(KEY_USER_EMAIL, null);
  }

  public boolean isRememberMeEnabled() {
    return prefs.getBoolean(KEY_REMEMBER, false);
  }

  public void clearSession() {
    prefs.remove(KEY_USER_EMAIL);
    prefs.putBoolean(KEY_REMEMBER, false);
  }
}