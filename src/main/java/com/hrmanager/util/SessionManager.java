package com.hrmanager.util;

import com.hrmanager.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User utilisateurActuel;
    private boolean estConnecte = false;

    private SessionManager() {}

    public void login(User user) {
        this.utilisateurActuel = user;
        this.estConnecte = true;
    }

    public void logout() {
        this.estConnecte = false;
        this.utilisateurActuel = null;
    }

    public boolean isLoggedIn() {
        return this.estConnecte;
    }

    public User getCurrentUser() {
        return this.utilisateurActuel;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
}