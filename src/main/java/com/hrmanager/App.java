package com.hrmanager;

import com.hrmanager.ui.LoginFrame;
import com.hrmanager.util.RememberMeManager;
import com.hrmanager.util.SessionManager;
import com.hrmanager.api.ApiClient;
import com.hrmanager.api.AuthService;
import com.hrmanager.model.User;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        UIManager.put("OptionPane.messageFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    "Une erreur inattendue s'est produite.\n\nErreur: " +
                    (throwable != null ? throwable.getMessage() : "Inconnu") +
                    "\n\nVeuillez redemarrer l'application.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            });
            throwable.printStackTrace();
        });

        SwingUtilities.invokeLater(() -> {
            RememberMeManager.Credentials identifiants = RememberMeManager.loadCredentials();
            if (identifiants != null) {
                ApiClient.getInstance().setTokens(identifiants.getAccessToken(), identifiants.getRefreshToken());
                try {
                    AuthService serviceAuth = new AuthService();
                    User utilisateur = serviceAuth.recupererUtilisateur();
                    if ("HR".equalsIgnoreCase(utilisateur.getRole()) && !utilisateur.isMustChangePassword()) {
                        SessionManager.getInstance().login(utilisateur);
                        com.hrmanager.ui.MainFrame fenetrePrincipale = new com.hrmanager.ui.MainFrame(SessionManager.getInstance());
                        fenetrePrincipale.setVisible(true);
                        return;
                    }
                } catch (java.net.http.HttpConnectTimeoutException | java.net.SocketTimeoutException e) {
                    int choix = JOptionPane.showConfirmDialog(null,
                        "La connexion au serveur est lente ou indisponible.\nVoulez-vous quand meme essayer de vous connecter?",
                        "Delai de connexion depasse", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choix == JOptionPane.YES_OPTION) {
                        LoginFrame fenetreLogin = new LoginFrame();
                        fenetreLogin.setVisible(true);
                    } else {
                        RememberMeManager.clearCredentials();
                        LoginFrame fenetreLogin = new LoginFrame();
                        fenetreLogin.setVisible(true);
                    }
                    return;
                } catch (Exception e) {
                    RememberMeManager.clearCredentials();
                }
            }
            LoginFrame fenetreLogin = new LoginFrame();
            fenetreLogin.setVisible(true);
        });
    }
}