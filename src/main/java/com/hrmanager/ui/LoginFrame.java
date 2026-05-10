package com.hrmanager.ui;

import com.hrmanager.api.ApiClient;
import com.hrmanager.api.AuthService;
import com.hrmanager.model.User;
import com.hrmanager.util.RememberMeManager;
import com.hrmanager.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField champEmail;
    private JPasswordField champMotDePasse;
    private JPasswordField champNouveauMotDePasse;
    private JPasswordField champConfirmerMotDePasse;
    private JButton boutonConnexion;
    private JButton boutonChangerMotDePasse;
    private JCheckBox caseAfficherMotDePasse;
    private JCheckBox caseSeSouvenirDeMoi;
    private JLabel labelErreur;
    private JPanel panneauConnexion;
    private JPanel panneauChangerMotDePasse;
    private CardLayout layoutCartes;
    private JPanel panneauPrincipal;
    private User utilisateurActuel;

    public LoginFrame() {
        setTitle("Portail RH - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        layoutCartes = new CardLayout();
        panneauPrincipal = new JPanel(layoutCartes);
        add(panneauPrincipal);

        panneauConnexion = creerPanneauConnexion();
        panneauPrincipal.add(panneauConnexion, "connexion");

        panneauChangerMotDePasse = creerPanneauChangerMotDePasse();
        panneauPrincipal.add(panneauChangerMotDePasse, "changerMotDePasse");

        layoutCartes.show(panneauPrincipal, "connexion");
        
        verifierSeSouvenirDeMoi();
    }
    
    private void verifierSeSouvenirDeMoi() {
        RememberMeManager.Credentials identifiants = RememberMeManager.loadCredentials();
        if (identifiants != null) {
            champEmail.setText(identifiants.getEmail());
        }
    }

    private JPanel creerPanneauConnexion() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel labelTitre = new JLabel("Connexion au Portail RH");
        labelTitre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panneau.add(labelTitre, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        panneau.add(new JLabel("Email:"), gbc);
        
        champEmail = new JTextField(20);
        champEmail.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        panneau.add(champEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        panneau.add(new JLabel("Mot de passe:"), gbc);
        
        champMotDePasse = new JPasswordField(20);
        champMotDePasse.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        panneau.add(champMotDePasse, gbc);

        caseAfficherMotDePasse = new JCheckBox("Afficher le mot de passe");
        caseAfficherMotDePasse.addActionListener(e -> {
            if (caseAfficherMotDePasse.isSelected()) {
                champMotDePasse.setEchoChar((char) 0);
            } else {
                champMotDePasse.setEchoChar('*');
            }
        });
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        panneau.add(caseAfficherMotDePasse, gbc);
        
        caseSeSouvenirDeMoi = new JCheckBox("Se souvenir de moi");
        caseSeSouvenirDeMoi.setSelected(true);
        gbc.gridy = 4;
        panneau.add(caseSeSouvenirDeMoi, gbc);

        labelErreur = new JLabel(" ");
        labelErreur.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panneau.add(labelErreur, gbc);

        boutonConnexion = new JButton("Se connecter");
        boutonConnexion.setPreferredSize(new Dimension(150, 30));
        boutonConnexion.addActionListener(new ActionConnexion());
        gbc.gridy = 6;
        panneau.add(boutonConnexion, gbc);

        return panneau;
    }

    private JPanel creerPanneauChangerMotDePasse() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        JLabel labelTitre = new JLabel("Changement de mot de passe obligatoire");
        labelTitre.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panneau.add(labelTitre, gbc);

        JLabel labelInfo = new JLabel("Veuillez definir un nouveau mot de passe pour votre compte.");
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridy = 1;
        panneau.add(labelInfo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        panneau.add(new JLabel("Nouveau mot de passe:"), gbc);
        
        champNouveauMotDePasse = new JPasswordField(30);
        champNouveauMotDePasse.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        panneau.add(champNouveauMotDePasse, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        panneau.add(new JLabel("Confirmer le mot de passe:"), gbc);
        
        champConfirmerMotDePasse = new JPasswordField(30);
        champConfirmerMotDePasse.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST;
        panneau.add(champConfirmerMotDePasse, gbc);

        JLabel labelErreurChangement = new JLabel(" ");
        labelErreurChangement.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panneau.add(labelErreurChangement, gbc);

        boutonChangerMotDePasse = new JButton("Changer le mot de passe");
        boutonChangerMotDePasse.setPreferredSize(new Dimension(200, 35));
        boutonChangerMotDePasse.addActionListener(new ActionChangerMotDePasse(labelErreurChangement));
        gbc.gridy = 5;
        panneau.add(boutonChangerMotDePasse, gbc);

        return panneau;
    }

    private class ActionConnexion implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = champEmail.getText().trim();
            String motDePasse = new String(champMotDePasse.getPassword());

            if (email.isEmpty() || motDePasse.isEmpty()) {
                labelErreur.setText("Veuillez remplir tous les champs.");
                return;
            }

            try {
                AuthService serviceAuth = new AuthService();
                AuthService.ResultatAuth resultat = serviceAuth.connexion(email, motDePasse);
                utilisateurActuel = resultat.getUtilisateur();

                if (utilisateurActuel.isMustChangePassword()) {
                    layoutCartes.show(panneauPrincipal, "changerMotDePasse");
                } else {
                    if (caseSeSouvenirDeMoi.isSelected()) {
                        RememberMeManager.saveCredentials(email, resultat.getTokenAcces(), resultat.getTokenRefresh());
                    }
                    
                    SessionManager session = SessionManager.getInstance();
                    session.login(utilisateurActuel);
                    ouvrirApplicationPrincipale();
                }
            } catch (Exception ex) {
                labelErreur.setText(ex.getMessage());
            }
        }
    }

    private class ActionChangerMotDePasse implements ActionListener {
        private final JLabel labelErreur;

        public ActionChangerMotDePasse(JLabel labelErreur) {
            this.labelErreur = labelErreur;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String nouveauMotDePasse = new String(champNouveauMotDePasse.getPassword());
            String confirmerMotDePasse = new String(champConfirmerMotDePasse.getPassword());

            if (nouveauMotDePasse.isEmpty() || confirmerMotDePasse.isEmpty()) {
                labelErreur.setText("Veuillez remplir tous les champs.");
                return;
            }

            if (!nouveauMotDePasse.equals(confirmerMotDePasse)) {
                labelErreur.setText("Les mots de passe ne correspondent pas.");
                return;
            }

            try {
                AuthService serviceAuth = new AuthService();
                serviceAuth.changerMotDePasse(nouveauMotDePasse);
                
                utilisateurActuel.setMustChangePassword(false);
                utilisateurActuel.setActive(true);
                
                RememberMeManager.Credentials identifiants = RememberMeManager.loadCredentials();
                if (identifiants != null) {
                    RememberMeManager.saveCredentials(identifiants.getEmail(), 
                        ApiClient.getInstance().getAccessToken(), 
                        ApiClient.getInstance().getRefreshToken());
                }
                
                SessionManager session = SessionManager.getInstance();
                session.login(utilisateurActuel);
                ouvrirApplicationPrincipale();
            } catch (Exception ex) {
                labelErreur.setText(ex.getMessage());
            }
        }
    }

    private void ouvrirApplicationPrincipale() {
        this.dispose();
        MainFrame fenetrePrincipale = new MainFrame(SessionManager.getInstance());
        fenetrePrincipale.setVisible(true);
    }
}