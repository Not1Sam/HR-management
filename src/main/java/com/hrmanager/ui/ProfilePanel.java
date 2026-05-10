package com.hrmanager.ui;

import com.hrmanager.util.SessionManager;
import com.hrmanager.api.ApiClient;
import com.hrmanager.api.AuthService;
import com.hrmanager.util.ThemeManager;
import com.hrmanager.util.ThemeManager.Theme;
import com.hrmanager.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfilePanel extends JPanel {
    private final SessionManager session;
    private final User sessionUser;
    private final ThemeManager.Theme theme;
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField locationField;
    private JTextField departmentField;
    private JTextArea bioArea;
    private JTextField linkedinField;
    private JTextField githubField;
    private JTextField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel messageLabel;

    private final Color defaultBorderColor = new Color(203, 213, 225);
    private final Color errorBorderColor = new Color(239, 68, 68);

    public ProfilePanel(SessionManager session, ThemeManager.Theme theme) {
        this.session = session;
        this.sessionUser = session.getCurrentUser();
        this.theme = theme;
        initComponents();
        loadUserData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBackgroundColor(theme));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(ThemeManager.getBackgroundColor(theme));
        scrollPane.getViewport().setBackground(ThemeManager.getBackgroundColor(theme));
        scrollPane.setBorder(null);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Mon profil");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(ThemeManager.getTextColor(theme));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);

        JLabel subtitle = new JLabel("Gerez vos informations personnelles et professionnelles");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(ThemeManager.getSecondaryTextColor(theme));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(subtitle);

        content.add(header);
        content.add(Box.createVerticalStrut(24));

        content.add(createProfileCard());
        content.add(Box.createVerticalStrut(20));
        content.add(createPasswordCard());

        scrollPane.setViewportView(content);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.getCardBackgroundColor(theme));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JPanel cardHeader = new JPanel();
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(20, 20, 16, 20));

        JLabel sectionTitle = new JLabel("Informations personnelles");
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        sectionTitle.setForeground(ThemeManager.getTextColor(theme));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardHeader.add(sectionTitle);

        card.add(cardHeader);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(0, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Prenom"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        firstNameField = createTextField();
        fields.add(firstNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Nom"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lastNameField = createTextField();
        fields.add(lastNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Email"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        emailField = createTextField();
        emailField.setEditable(false);
        emailField.setBackground(new Color(ThemeManager.getCardBackgroundColor(theme).getRGB() == -13027020 ? 241 : 245, 245, 245));
        fields.add(emailField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Telephone"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        phoneField = createTextField();
        fields.add(phoneField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Lieu"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        locationField = createTextField();
        fields.add(locationField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Departement"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        departmentField = createTextField();
        departmentField.setEditable(false);
        departmentField.setBackground(new Color(241, 245, 245));
        fields.add(departmentField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        fields.add(new JLabel("Bio"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        bioArea = createTextArea();
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setPreferredSize(new Dimension(0, 80));
        bioScroll.setBackground(ThemeManager.getCardBackgroundColor(theme));
        bioScroll.getViewport().setBackground(ThemeManager.getCardBackgroundColor(theme));
        fields.add(bioScroll, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        row++;

        card.add(fields);

        JPanel socialHeader = new JPanel();
        socialHeader.setLayout(new BoxLayout(socialHeader, BoxLayout.Y_AXIS));
        socialHeader.setOpaque(false);
        socialHeader.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel socialTitle = new JLabel("Reseaux professionnels");
        socialTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        socialTitle.setForeground(ThemeManager.getTextColor(theme));
        socialTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        socialHeader.add(socialTitle);

        card.add(socialHeader);

        JPanel socialFields = new JPanel(new GridBagLayout());
        socialFields.setOpaque(false);
        socialFields.setBorder(new EmptyBorder(0, 20, 20, 20));
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(8, 0, 8, 16);
        sgbc.anchor = GridBagConstraints.WEST;
        sgbc.fill = GridBagConstraints.HORIZONTAL;

        int srow = 0;

        sgbc.gridx = 0; sgbc.gridy = srow;
        socialFields.add(new JLabel("LinkedIn"), sgbc);
        sgbc.gridx = 1; sgbc.weightx = 1.0;
        linkedinField = createTextField();
        fields.add(linkedinField, sgbc);
        srow++;

        sgbc.gridx = 0; sgbc.gridy = srow;
        socialFields.add(new JLabel("GitHub"), sgbc);
        sgbc.gridx = 1; sgbc.weightx = 1.0;
        githubField = createTextField();
        socialFields.add(githubField, sgbc);
        srow++;

        card.add(socialFields);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        messageLabel.setForeground(ThemeManager.getDangerColor());
        messageLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        card.add(messageLabel);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        buttonRow.setOpaque(false);

        JButton saveButton = new JButton("Sauvegarder le profil");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(ThemeManager.getAccentColor(theme));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setOpaque(true);
        saveButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        saveButton.addActionListener(e -> saveProfile());
        buttonRow.add(saveButton);

        card.add(buttonRow);

        return card;
    }

    private JPanel createPasswordCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.getCardBackgroundColor(theme));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        JPanel cardHeader = new JPanel();
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(20, 20, 16, 20));

        JLabel sectionTitle = new JLabel("Securite du compte");
        sectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        sectionTitle.setForeground(ThemeManager.getTextColor(theme));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardHeader.add(sectionTitle);

        card.add(cardHeader);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(0, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Nouveau mot de passe"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        newPasswordField = new JPasswordField();
        stylePasswordField(newPasswordField);
        fields.add(newPasswordField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Confirmer le mot de passe"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField();
        stylePasswordField(confirmPasswordField);
        fields.add(confirmPasswordField, gbc);
        row++;

        card.add(fields);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        buttonRow.setOpaque(false);

        JButton saveButton = new JButton("Mettre a jour le mot de passe");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(ThemeManager.getAccentColor(theme));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setOpaque(true);
        saveButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        saveButton.addActionListener(e -> changePassword());
        buttonRow.add(saveButton);

        card.add(buttonRow);

        return card;
    }

    private void loadUserData() {
        if (sessionUser == null) return;

        firstNameField.setText(sessionUser.getFirstName() != null ? sessionUser.getFirstName() : "");
        lastNameField.setText(sessionUser.getLastName() != null ? sessionUser.getLastName() : "");
        emailField.setText(sessionUser.getEmail() != null ? sessionUser.getEmail() : "");
        phoneField.setText(sessionUser.getPhone() != null ? sessionUser.getPhone() : "");
        locationField.setText(sessionUser.getLocation() != null ? sessionUser.getLocation() : "");
        departmentField.setText(sessionUser.getDepartment() != null ? sessionUser.getDepartment() : "");
        bioArea.setText(sessionUser.getBio() != null ? sessionUser.getBio() : "");
        linkedinField.setText(sessionUser.getLinkedin() != null ? sessionUser.getLinkedin() : "");
        githubField.setText(sessionUser.getGithub() != null ? sessionUser.getGithub() : "");
    }

    private void saveProfile() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String location = locationField.getText().trim();
        String bio = bioArea.getText().trim();
        String linkedin = linkedinField.getText().trim();
        String github = githubField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            messageLabel.setText("Le prenom et le nom sont requis.");
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("first_name", firstName);
        payload.addProperty("last_name", lastName);
        if (!phone.isEmpty()) payload.addProperty("phone", phone);
        if (!location.isEmpty()) payload.addProperty("location", location);
        if (!bio.isEmpty()) payload.addProperty("bio", bio);
        if (!linkedin.isEmpty()) payload.addProperty("linkedin", linkedin);
        if (!github.isEmpty()) payload.addProperty("github", github);

        try {
            HttpRequest request = ApiClient.getInstance().request("/users/" + sessionUser.getId() + "/")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = ApiClient.getInstance().getClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                User updated = gson.fromJson(response.body(), User.class);
                updated.setMustChangePassword(sessionUser.isMustChangePassword());
                session.login(updated);
                messageLabel.setForeground(ThemeManager.getSuccessColor());
                messageLabel.setText("Profil sauvegarde avec succes!");
                clearMessageAfterDelay();
            } else {
                messageLabel.setForeground(ThemeManager.getDangerColor());
                messageLabel.setText("Erreur lors de la sauvegarde du profil.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setForeground(ThemeManager.getDangerColor());
            messageLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void changePassword() {
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (newPass.length() < 8) {
            messageLabel.setForeground(ThemeManager.getDangerColor());
            messageLabel.setText("Le mot de passe doit contenir au moins 8 caracteres.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            messageLabel.setForeground(ThemeManager.getDangerColor());
            messageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("new_password", newPass);

            HttpRequest request = ApiClient.getInstance().request("/users/change-password/")
                    .method("POST", HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = ApiClient.getInstance().getClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                messageLabel.setForeground(ThemeManager.getSuccessColor());
                messageLabel.setText("Mot de passe mis a jour avec succes!");
                clearMessageAfterDelay();
            } else {
                messageLabel.setForeground(ThemeManager.getDangerColor());
                messageLabel.setText("Erreur lors de la mise a jour du mot de passe.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setForeground(ThemeManager.getDangerColor());
            messageLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void clearMessageAfterDelay() {
        Timer timer = new Timer(3000, e -> {
            messageLabel.setText(" ");
            messageLabel.setForeground(ThemeManager.getDangerColor());
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        field.setBackground(ThemeManager.getCardBackgroundColor(theme));
        field.setForeground(ThemeManager.getTextColor(theme));
        field.setCaretColor(ThemeManager.getTextColor(theme));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(4, 20);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        area.setBackground(ThemeManager.getCardBackgroundColor(theme));
        area.setForeground(ThemeManager.getTextColor(theme));
        area.setCaretColor(ThemeManager.getTextColor(theme));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return area;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        field.setBackground(ThemeManager.getCardBackgroundColor(theme));
        field.setForeground(ThemeManager.getTextColor(theme));
        field.setCaretColor(ThemeManager.getTextColor(theme));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(defaultBorderColor),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }
}
