package com.hrmanager.ui.panels;

import com.hrmanager.api.StatsService;
import com.hrmanager.model.User;
import com.hrmanager.util.ThemeManager;
import com.hrmanager.util.ThemeManager.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DashboardPanel extends JPanel {
    private final User utilisateur;
    private final Runnable actionDeconnexion;
    private final Theme theme;
    private JLabel labelOffresActives;
    private JLabel labelCandidatures;
    private JLabel labelEntretiens;
    private JLabel labelAcceptes;

    public DashboardPanel(User utilisateur, Runnable actionDeconnexion, Theme theme) {
        this.utilisateur = utilisateur;
        this.actionDeconnexion = actionDeconnexion;
        this.theme = theme;
        initialiserComposants();
        chargerStatistiques();
    }

    private void initialiserComposants() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel barreHaut = new JPanel(new BorderLayout());
        barreHaut.setBackground(ThemeManager.getCardBackgroundColor(theme));
        barreHaut.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderColor(theme)));
        barreHaut.setPreferredSize(new Dimension(0, 60));

        JPanel accueil = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        JLabel labelAccueil = new JLabel("Bienvenue, " + utilisateur.getFullName());
        labelAccueil.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        labelAccueil.setForeground(ThemeManager.getTextColor(theme));
        JLabel labelRole = new JLabel(utilisateur.getRole());
        labelRole.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        labelRole.setForeground(ThemeManager.getSecondaryTextColor(theme));
        labelRole.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        JPanel panneauNom = new JPanel();
        panneauNom.setLayout(new BoxLayout(panneauNom, BoxLayout.Y_AXIS));
        panneauNom.add(labelAccueil);
        panneauNom.add(Box.createVerticalStrut(2));
        panneauNom.add(labelRole);
        accueil.add(Box.createHorizontalStrut(20));
        accueil.add(panneauNom);
        barreHaut.add(accueil, BorderLayout.WEST);

        JButton boutonDeconnexion = new JButton("Deconnexion");
        boutonDeconnexion.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        boutonDeconnexion.setForeground(ThemeManager.getDangerColor());
        boutonDeconnexion.setContentAreaFilled(false);
        boutonDeconnexion.setBorderPainted(false);
        boutonDeconnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boutonDeconnexion.addActionListener(e -> actionDeconnexion.run());
        JPanel panneauDeconnexion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panneauDeconnexion.add(boutonDeconnexion);
        barreHaut.add(panneauDeconnexion, BorderLayout.EAST);

        add(barreHaut, BorderLayout.NORTH);

        JPanel contenu = new JPanel(new BorderLayout(15, 15));
        contenu.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contenu.setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel grilleStats = new JPanel(new GridLayout(1, 4, 12, 0));
        grilleStats.setOpaque(false);

        labelOffresActives = new JLabel("0");
        labelCandidatures = new JLabel("0");
        labelEntretiens = new JLabel("0");
        labelAcceptes = new JLabel("0");

        grilleStats.add(creerCarteStat("Offres actives", labelOffresActives, new Color(16, 185, 129)));
        grilleStats.add(creerCarteStat("Candidatures", labelCandidatures, new Color(59, 130, 246)));
        grilleStats.add(creerCarteStat("Entretiens cette semaine", labelEntretiens, new Color(245, 158, 11)));
        grilleStats.add(creerCarteStat("Acceptes ce mois", labelAcceptes, new Color(139, 92, 246)));

        contenu.add(grilleStats, BorderLayout.NORTH);

        JPanel panneauInfo = new JPanel(new GridBagLayout());
        panneauInfo.setBackground(ThemeManager.getCardBackgroundColor(theme));
        panneauInfo.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme), 1));
        panneauInfo.setPreferredSize(new Dimension(0, 150));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel labelTitre = new JLabel("Statistiques globales");
        labelTitre.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        labelTitre.setForeground(ThemeManager.getTextColor(theme));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panneauInfo.add(labelTitre, gbc);

        contenu.add(panneauInfo, BorderLayout.CENTER);

        add(contenu, BorderLayout.CENTER);
    }

    private JPanel creerCarteStat(String titre, JLabel labelValeur, Color couleur) {
        JPanel carte = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBackgroundColor(theme));
                g2.fill(new Rectangle2D.Float(0, 0, getWidth(), getHeight()));
                g2.setColor(couleur);
                g2.fill(new Rectangle2D.Float(0, 0, 4, getHeight()));
            }
        };
        carte.setLayout(new BoxLayout(carte, BoxLayout.Y_AXIS));
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel labelTitre = new JLabel(titre);
        labelTitre.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        labelTitre.setForeground(ThemeManager.getSecondaryTextColor(theme));

        labelValeur.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        labelValeur.setForeground(ThemeManager.getTextColor(theme));

        carte.add(labelTitre);
        carte.add(Box.createVerticalStrut(8));
        carte.add(labelValeur);

        return carte;
    }

    private void chargerStatistiques() {
        new SwingWorker<StatsService.StatistiquesTableauBord, Void>() {
            @Override
            protected StatsService.StatistiquesTableauBord doInBackground() throws Exception {
                return new StatsService().recupererStatistiques();
            }

            @Override
            protected void done() {
                try {
                    StatsService.StatistiquesTableauBord stats = get();
                    if (stats != null && stats.resume != null) {
                        actualiserStats(stats.resume);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void actualiserStats(StatsService.ResumeStatistiques resume) {
        labelOffresActives.setText(String.valueOf(resume.activeJobs));
        labelCandidatures.setText(String.valueOf(resume.totalApplications));
        labelEntretiens.setText(String.valueOf(resume.interviewsThisWeek));
        labelAcceptes.setText(String.valueOf(resume.acceptedThisMonth));
    }
}