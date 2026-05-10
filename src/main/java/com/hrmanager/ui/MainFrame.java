package com.hrmanager.ui;

import com.hrmanager.util.RememberMeManager;
import com.hrmanager.util.SessionManager;
import com.hrmanager.model.User;
import com.hrmanager.ui.panels.DashboardPanel;
import com.hrmanager.util.ThemeManager;
import com.hrmanager.util.ThemeManager.Theme;
import com.hrmanager.ui.panels.DashboardPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private final SessionManager session;
    private JTabbedPane tabbedPane;
    private Theme currentTheme = Theme.LIGHT;

    public MainFrame(SessionManager session) {
        this.session = session;
        User currentUser = session.getCurrentUser();
        
        setTitle("Portail RH - Tableau de bord (" + currentUser.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Tableau de bord", createDashboardPanel(currentUser));
        tabbedPane.addTab("Offres d'emploi", new JobPanel(session));
        tabbedPane.addTab("Candidatures", new ApplicationPanel(session));
        tabbedPane.addTab("Entretiens", new InterviewPanel(session));
        tabbedPane.addTab("Profil", new ProfilePanel(session, currentTheme));
        
        add(tabbedPane);
        
        createMenuBar();
    }

    private JPanel createDashboardPanel(User currentUser) {
        return new DashboardPanel(currentUser, this::logout, currentTheme);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem logoutItem = new JMenuItem("Deconnexion");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);

        JMenu navMenu = new JMenu("Navigation");
        JMenuItem dashboardItem = new JMenuItem("Tableau de bord");
        dashboardItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        navMenu.add(dashboardItem);
        
        JMenuItem jobsItem = new JMenuItem("Offres d'emploi");
        jobsItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        navMenu.add(jobsItem);
        
        JMenuItem appsItem = new JMenuItem("Candidatures");
        appsItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        navMenu.add(appsItem);
        
        JMenuItem interviewsItem = new JMenuItem("Entretiens");
        interviewsItem.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        navMenu.add(interviewsItem);
        
        menuBar.add(navMenu);
    }

    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Etes-vous sur de vouloir vous deconnecter?",
            "Confirmation de deconnexion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            RememberMeManager.clearCredentials();
            session.logout();
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
