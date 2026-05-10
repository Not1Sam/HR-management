package com.hrmanager.ui;

import com.hrmanager.util.SessionManager;
import com.hrmanager.api.ApplicationService;
import com.hrmanager.api.ApiClient;
import com.hrmanager.model.Application;
import com.hrmanager.model.Job;
import com.hrmanager.api.JobService;
import com.hrmanager.util.ThemeManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ApplicationPanel extends JPanel {
    private final SessionManager session;
    private final ApplicationService applicationService = new ApplicationService();
    private final Gson gson = new Gson();
    private final ThemeManager.Theme theme = ThemeManager.Theme.LIGHT;
    private java.awt.Desktop javaDesktop;

    private JTable applicationTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> jobFilter;
    private JButton btnRefresh;
    private List<Application> applications = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();

    private final String[] STATUSES = {"SENT", "REVIEWING", "SHORTLISTED", "INTERVIEW", "ACCEPTED", "REJECTED"};
    private final String[] STATUS_LABELS = {"Envoyee", "En cours", "Preselectionnee", "Entretien", "Acceptee", "Refusee"};

    public ApplicationPanel(SessionManager session) {
        this.session = session;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBackgroundColor(theme));

        add(createTopBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.getCardBackgroundColor(theme));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderColor(theme)));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 8, 20));

        JLabel title = new JLabel("Gestion des candidatures");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(ThemeManager.getTextColor(theme));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);

        JLabel countLabel = new JLabel("0 candidature(s)");
        countLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        countLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(countLabel);

        panel.add(header);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 16, 12, 16));

        searchField = new JTextField(20);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        searchField.setBackground(ThemeManager.getCardBackgroundColor(theme));
        searchField.setForeground(ThemeManager.getTextColor(theme));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher par candidat ou poste...");
        searchField.addActionListener(e -> filterAndShow());
        filterPanel.add(new JLabel("Recherche:"));
        filterPanel.add(searchField);

        String[] filterOptions = new String[STATUSES.length + 1];
        filterOptions[0] = "Tous les statuts";
        System.arraycopy(STATUS_LABELS, 0, filterOptions, 1, STATUS_LABELS.length);
        statusFilter = new JComboBox<>(filterOptions);
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> filterAndShow());
        filterPanel.add(new JLabel("Statut:"));
        filterPanel.add(statusFilter);

        jobFilter = new JComboBox<>(new String[]{"Tous les postes"});
        styleComboBox(jobFilter);
        jobFilter.addActionListener(e -> filterAndShow());
        filterPanel.add(new JLabel("Poste:"));
        filterPanel.add(jobFilter);

        panel.add(filterPanel);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Candidat", "Poste", "Score", "Date", "Statut", "Voir", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        applicationTable = new JTable(tableModel);
        applicationTable.setRowHeight(52);
        applicationTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        applicationTable.setBackground(ThemeManager.getCardBackgroundColor(theme));
        applicationTable.setForeground(ThemeManager.getTextColor(theme));
        applicationTable.setGridColor(ThemeManager.getBorderColor(theme));
        applicationTable.setSelectionBackground(ThemeManager.getAccentColor(theme));
        applicationTable.setSelectionForeground(Color.WHITE);
        applicationTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        applicationTable.getTableHeader().setBackground(ThemeManager.getBackgroundColor(theme));
        applicationTable.getTableHeader().setForeground(ThemeManager.getTextColor(theme));

        applicationTable.getColumnModel().getColumn(0).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            Application app = applications.get(row);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);

            String name = app.getCandidateName() != null && !app.getCandidateName().isEmpty() ? app.getCandidateName() : "Candidat";
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            nameLabel.setForeground(ThemeManager.getTextColor(theme));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(nameLabel);

            String email = app.getEmail() != null ? app.getEmail() : (app.getCandidateEmail() != null ? app.getCandidateEmail() : "");
            if (!email.isEmpty()) {
                JLabel emailLabel = new JLabel(email);
                emailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                emailLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
                emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(emailLabel);
            }
            return p;
        });
        applicationTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        applicationTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        applicationTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        applicationTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        applicationTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        applicationTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        applicationTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        applicationTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(false);
            JButton viewBtn = new JButton("Voir");
            viewBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            viewBtn.setForeground(Color.WHITE);
            viewBtn.setBackground(ThemeManager.getAccentColor(theme));
            viewBtn.setFocusPainted(false);
            viewBtn.setBorderPainted(false);
            viewBtn.setOpaque(true);
            viewBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
            viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(viewBtn);
            return panel;
        });

        applicationTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        applicationTable.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(false);
            panel.setName("status_" + row);
            JButton btn = new JButton("Statut");
            btn.setName("statusBtn_" + row);
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(245, 158, 11));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(6, 12, 6, 12));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btn);
            return panel;
        });

        applicationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = applicationTable.rowAtPoint(e.getPoint());
                int col = applicationTable.columnAtPoint(e.getPoint());

                if (col == 5) {
                    showApplicationDetail(row);
                } else if (col == 6) {
                    showStatusChangeDialog(row);
                } else if (e.getClickCount() == 2) {
                    showApplicationDetail(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(applicationTable);
        scrollPane.setBackground(ThemeManager.getBackgroundColor(theme));
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor(theme));
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getCardBackgroundColor(theme));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getBorderColor(theme)));
        panel.setPreferredSize(new Dimension(0, 48));

        btnRefresh = new JButton("Actualiser");
        btnRefresh.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(ThemeManager.getAccentColor(theme));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setOpaque(true);
        btnRefresh.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnRefresh.addActionListener(e -> loadData());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        leftPanel.setOpaque(false);
        leftPanel.add(btnRefresh);
        panel.add(leftPanel, BorderLayout.WEST);

        return panel;
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    applications = applicationService.recupererCandidatures();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                filterAndShow();
            }
        }.execute();
    }

    private void filterAndShow() {
        tableModel.setRowCount(0);
        String search = searchField.getText().toLowerCase().trim();
        int statusIdx = statusFilter.getSelectedIndex();
        int jobIdx = jobFilter.getSelectedIndex() - 1;

        for (Application app : applications) {
            String candidateName = app.getCandidateName() != null ? app.getCandidateName() : "";
            String jobTitle = app.getJobTitle() != null ? app.getJobTitle() : "";

            boolean matchSearch = search.isEmpty()
                    || candidateName.toLowerCase().contains(search)
                    || jobTitle.toLowerCase().contains(search);

            boolean matchStatus = statusIdx == 0 || getStatusKey(statusIdx - 1).equals(app.getStatus());
            boolean matchJob = jobIdx < 0 || app.getJobId() == jobs.get(jobIdx).getId();

            if (matchSearch && matchStatus && matchJob) {
                tableModel.addRow(new Object[]{
                        app,
                        app.getJobTitle(),
                        app.getScore() != null ? app.getScore() : 0,
                        formatDate(app.getAppliedAt()),
                        app.getStatus()
                });
            }
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "-";
        try {
            String date = dateStr.substring(0, 10);
            return date;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showApplicationDetail(int row) {
        Application app = (Application) tableModel.getValueAt(row, 0);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detail de la candidature", true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(createDetailSection("Candidat", app.getCandidateName() + "\n" + app.getEmail() + "\n" + (app.getPhone() != null ? app.getPhone() : "")));
        content.add(createDetailSection("Poste", app.getJobTitle()));
        content.add(createDetailSection("Score", app.getScore() != null ? app.getScore() + "/100" : "Non note"));
        content.add(createDetailSection("Date de candidature", formatDate(app.getAppliedAt())));
        content.add(createDetailSection("Statut", getStatusLabel(app.getStatus())));

        if (app.getCoverLetter() != null && !app.getCoverLetter().isEmpty()) {
            content.add(createDetailSection("Lettre de motivation", app.getCoverLetter()));
        }

        if (app.getNotes() != null && !app.getNotes().isEmpty()) {
            content.add(createDetailSection("Notes HR", app.getNotes()));
        }

        JPanel docPanel = new JPanel();
        docPanel.setLayout(new BoxLayout(docPanel, BoxLayout.Y_AXIS));
        docPanel.setOpaque(false);

        JLabel docTitle = new JLabel("Documents");
        docTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        docTitle.setForeground(ThemeManager.getSecondaryTextColor(theme));
        docTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        docPanel.add(docTitle);

        JPanel docButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        docButtons.setOpaque(false);

        if (app.getCvUrl() != null && !app.getCvUrl().isEmpty()) {
            JButton cvBtn = new JButton("Telecharger CV");
            cvBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            cvBtn.setForeground(Color.WHITE);
            cvBtn.setBackground(ThemeManager.getInfoColor());
            cvBtn.setFocusPainted(false);
            cvBtn.setBorderPainted(false);
            cvBtn.setOpaque(true);
            cvBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
            cvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cvBtn.addActionListener(e -> openUrl(app.getCvUrl()));
            docButtons.add(cvBtn);
        }

        if (app.getCoverLetterUrl() != null && !app.getCoverLetterUrl().isEmpty()) {
            JButton clBtn = new JButton("Telecharger Lettre");
            clBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            clBtn.setForeground(Color.WHITE);
            clBtn.setBackground(ThemeManager.getWarningColor());
            clBtn.setFocusPainted(false);
            clBtn.setBorderPainted(false);
            clBtn.setOpaque(true);
            clBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
            clBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            clBtn.addActionListener(e -> openUrl(app.getCoverLetterUrl()));
            docButtons.add(clBtn);
        }

        if (docButtons.getComponentCount() > 0) {
            docPanel.add(docButtons);
            content.add(docPanel);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(ThemeManager.getAccentColor(theme));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setOpaque(true);
        closeBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttons.add(closeBtn);

        content.add(buttons);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private JPanel createDetailSection(String title, String value) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        section.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);

        JLabel valueLabel = new JLabel("<html><body style='width:480px'>" + value.replace("\n", "<br>") + "</body></html>");
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        valueLabel.setForeground(ThemeManager.getTextColor(theme));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(valueLabel);

        return section;
    }

    private void showStatusChangeDialog(int row) {
        Application app = (Application) tableModel.getValueAt(row, 0);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Changer le statut", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Changer le statut de la candidature");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        headerLabel.setForeground(ThemeManager.getTextColor(theme));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(headerLabel);

        content.add(Box.createVerticalStrut(8));

        JLabel nameLabel = new JLabel(app.getCandidateName() + " - " + app.getJobTitle());
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        nameLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLabel);

        content.add(Box.createVerticalStrut(16));

        String[] statusOptions = new String[STATUSES.length];
        for (int i = 0; i < STATUSES.length; i++) {
            statusOptions[i] = STATUS_LABELS[i];
        }
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        styleComboBox(statusCombo);
        statusCombo.setMaximumSize(new Dimension(300, 36));

        for (int i = 0; i < STATUSES.length; i++) {
            if (STATUSES[i].equals(app.getStatus())) {
                statusCombo.setSelectedIndex(i);
                break;
            }
        }
        content.add(statusCombo);

        content.add(Box.createVerticalStrut(8));

        JTextArea notesArea = new JTextArea(3, 30);
        notesArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        notesArea.setBackground(ThemeManager.getCardBackgroundColor(theme));
        notesArea.setForeground(ThemeManager.getTextColor(theme));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        if (app.getNotes() != null) notesArea.setText(app.getNotes());
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setMaximumSize(new Dimension(300, 80));
        content.add(notesScroll);

        content.add(Box.createVerticalStrut(16));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        cancelBtn.setForeground(ThemeManager.getTextColor(theme));
        cancelBtn.setBackground(ThemeManager.getCardBackgroundColor(theme));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setOpaque(true);
        cancelBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelBtn);

        JButton saveBtn = new JButton("Enregistrer");
        saveBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(ThemeManager.getAccentColor(theme));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        saveBtn.addActionListener(e -> {
            String newStatus = STATUSES[statusCombo.getSelectedIndex()];
            String notes = notesArea.getText().trim();
            updateApplicationStatus(app.getId(), newStatus, notes);
            dialog.dispose();
        });
        buttonRow.add(saveBtn);

        content.add(buttonRow);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void updateApplicationStatus(int appId, String status, String notes) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                applicationService.changerStatut(appId, status, notes.isEmpty() ? null : notes);
                return null;
            }

            @Override
            protected void done() {
                loadData();
            }
        }.execute();
    }

    private String getStatusLabel(String status) {
        for (int i = 0; i < STATUSES.length; i++) {
            if (STATUSES[i].equals(status)) return STATUS_LABELS[i];
        }
        return status != null ? status : "-";
    }

    private String getStatusKey(int idx) {
        if (idx < 0 || idx >= STATUSES.length) return STATUSES[0];
        return STATUSES[idx];
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        combo.setBackground(ThemeManager.getCardBackgroundColor(theme));
        combo.setForeground(ThemeManager.getTextColor(theme));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null ? value : "");
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            if (isSelected) {
                label.setBackground(ThemeManager.getAccentColor(theme));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(ThemeManager.getCardBackgroundColor(theme));
                label.setForeground(ThemeManager.getTextColor(theme));
            }
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            return label;
        });
    }

    private void openUrl(String url) {
        try {
            if (javaDesktop == null) {
                javaDesktop = java.awt.Desktop.getDesktop();
            }
            java.net.URI uri = new java.net.URI(url);
            javaDesktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le lien: " + e.getMessage());
        }
    }
}
