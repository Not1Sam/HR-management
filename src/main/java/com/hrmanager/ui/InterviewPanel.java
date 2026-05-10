package com.hrmanager.ui;

import com.hrmanager.util.SessionManager;
import com.hrmanager.api.InterviewService;
import com.hrmanager.api.ApplicationService;
import com.hrmanager.api.ApiClient;
import com.hrmanager.model.Interview;
import com.hrmanager.model.Application;
import com.hrmanager.util.ThemeManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InterviewPanel extends JPanel {
    private final SessionManager session;
    private final InterviewService interviewService = new InterviewService();
    private final ApplicationService appService = new ApplicationService();
    private final Gson gson = new Gson();
    private final ThemeManager.Theme theme = ThemeManager.Theme.LIGHT;

    private JTable interviewTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton btnRefresh;
    private JButton btnAdd;
    private List<Interview> interviews = new ArrayList<>();
    private List<Application> shortlistedApps = new ArrayList<>();

    private final String[] STATUSES = {"PLANNED", "CONFIRMED", "CANCELLED", "DONE"};
    private final String[] STATUS_LABELS = {"Planifie", "Confirme", "Annule", "Termine"};
    private final String[] INTERVIEW_TYPES = {"PRESENTIEL", "VISIOCONFERENCE", "TELEPHONE"};
    private final String[] TYPE_LABELS = {"Presentiel", "Visioconference", "Telephone"};

    public InterviewPanel(SessionManager session) {
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

        JLabel title = new JLabel("Gestion des entretiens");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(ThemeManager.getTextColor(theme));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);

        JLabel countLabel = new JLabel("0 entretien(s) planifie(s)");
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
        searchField.addActionListener(e -> filterAndShow());
        filterPanel.add(new JLabel("Recherche:"));
        filterPanel.add(searchField);

        String[] filterOptions = new String[STATUSES.length + 1];
        filterOptions[0] = "Tous";
        System.arraycopy(STATUS_LABELS, 0, filterOptions, 1, STATUS_LABELS.length);
        statusFilter = new JComboBox<>(filterOptions);
        styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> filterAndShow());
        filterPanel.add(new JLabel("Statut:"));
        filterPanel.add(statusFilter);

        panel.add(filterPanel);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Candidat", "Poste", "Date", "Heure", "Duree", "Type", "Statut", "Voir", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        interviewTable = new JTable(tableModel);
        interviewTable.setRowHeight(52);
        interviewTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        interviewTable.setBackground(ThemeManager.getCardBackgroundColor(theme));
        interviewTable.setForeground(ThemeManager.getTextColor(theme));
        interviewTable.setGridColor(ThemeManager.getBorderColor(theme));
        interviewTable.setSelectionBackground(ThemeManager.getAccentColor(theme));
        interviewTable.setSelectionForeground(Color.WHITE);
        interviewTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        interviewTable.getTableHeader().setBackground(ThemeManager.getBackgroundColor(theme));
        interviewTable.getTableHeader().setForeground(ThemeManager.getTextColor(theme));

        interviewTable.getColumnModel().getColumn(0).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            Interview i = interviews.get(row);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);

            String name = i.getCandidateName() != null && !i.getCandidateName().isEmpty() ? i.getCandidateName() : "Candidat";
            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            nameLabel.setForeground(ThemeManager.getTextColor(theme));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(nameLabel);

            JLabel jobLabel = new JLabel(i.getJobTitle() != null ? i.getJobTitle() : "");
            jobLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            jobLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
            jobLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(jobLabel);
            return p;
        });
        interviewTable.getColumnModel().getColumn(0).setPreferredWidth(180);

        interviewTable.getColumnModel().getColumn(1).setPreferredWidth(130);

        interviewTable.getColumnModel().getColumn(2).setPreferredWidth(90);

        interviewTable.getColumnModel().getColumn(3).setPreferredWidth(70);

        interviewTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        interviewTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            String type = (String) value;
            Color bg = new Color(238, 242, 255);
            Color fg = new Color(79, 70, 229);
            if ("VISIOCONFERENCE".equals(type)) {
                bg = new Color(219, 234, 254);
                fg = new Color(30, 64, 175);
            } else if ("TELEPHONE".equals(type)) {
                bg = new Color(254, 240, 138);
                fg = new Color(146, 64, 14);
            }
            JPanel badge = new JPanel();
            badge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            badge.setBackground(bg);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(4, 8, 4, 8));
            JLabel lbl = new JLabel(getTypeLabel(type));
            lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            lbl.setForeground(fg);
            badge.add(lbl);
            return badge;
        });
        interviewTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        interviewTable.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            String status = (String) value;
            Color bg;
            Color fg;
            switch (status != null ? status.toUpperCase() : "PLANNED") {
                case "CONFIRMED":
                    bg = new Color(209, 250, 229);
                    fg = new Color(6, 95, 70);
                    break;
                case "CANCELLED":
                    bg = new Color(254, 226, 226);
                    fg = new Color(153, 27, 27);
                    break;
                case "DONE":
                    bg = new Color(219, 234, 254);
                    fg = new Color(30, 64, 175);
                    break;
                default:
                    bg = new Color(254, 240, 138);
                    fg = new Color(146, 64, 14);
            }
            JPanel badge = new JPanel();
            badge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            badge.setBackground(bg);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(4, 8, 4, 8));
            JLabel lbl = new JLabel(getStatusLabel(status));
            lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            lbl.setForeground(fg);
            badge.add(lbl);
            return badge;
        });
        interviewTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        interviewTable.getColumnModel().getColumn(7).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(false);
            JButton viewBtn = new JButton("Voir");
            viewBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            viewBtn.setForeground(Color.WHITE);
            viewBtn.setBackground(ThemeManager.getAccentColor(theme));
            viewBtn.setFocusPainted(false);
            viewBtn.setBorderPainted(false);
            viewBtn.setOpaque(true);
            viewBtn.setBorder(new EmptyBorder(6, 10, 6, 10));
            viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(viewBtn);
            return panel;
        });
        interviewTable.getColumnModel().getColumn(7).setPreferredWidth(60);

        interviewTable.getColumnModel().getColumn(8).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(false);
            JButton btn = new JButton("Statut");
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(245, 158, 11));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(6, 10, 6, 10));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btn);
            return panel;
        });
        interviewTable.getColumnModel().getColumn(8).setPreferredWidth(70);

        interviewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = interviewTable.rowAtPoint(e.getPoint());
                int col = interviewTable.columnAtPoint(e.getPoint());

                if (col == 7) {
                    showInterviewDetail(row);
                } else if (col == 8) {
                    showStatusChangeDialog(row);
                } else if (e.getClickCount() == 2) {
                    showInterviewDetail(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(interviewTable);
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

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftPanel.setOpaque(false);

        btnRefresh = new JButton("Actualiser");
        btnRefresh.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(ThemeManager.getSuccessColor());
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setOpaque(true);
        btnRefresh.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnRefresh.addActionListener(e -> loadData());
        leftPanel.add(btnRefresh);

        btnAdd = new JButton("+ Planifier un entretien");
        btnAdd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(ThemeManager.getAccentColor(theme));
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setOpaque(true);
        btnAdd.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnAdd.addActionListener(e -> showCreateDialog());
        leftPanel.add(btnAdd);

        panel.add(leftPanel, BorderLayout.WEST);
        return panel;
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    interviews = interviewService.recupererEntretiens();
                    shortlistedApps = appService.recupererCandidatures();
                    shortlistedApps.removeIf(a -> !"SHORTLISTED".equals(a.getStatus()));
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

        for (Interview i : interviews) {
            String candidateName = i.getCandidateName() != null ? i.getCandidateName() : "";
            String jobTitle = i.getJobTitle() != null ? i.getJobTitle() : "";

            boolean matchSearch = search.isEmpty()
                    || candidateName.toLowerCase().contains(search)
                    || jobTitle.toLowerCase().contains(search);

            boolean matchStatus = statusIdx == 0 || STATUSES[statusIdx - 1].equals(i.getStatus());

            if (matchSearch && matchStatus) {
                tableModel.addRow(new Object[]{
                        i,
                        i.getJobTitle(),
                        formatDate(i.getDate()),
                        i.getTime() != null ? i.getTime().substring(0, 5) : "-",
                        i.getDuration() + " min",
                        i.getType(),
                        i.getStatus()
                });
            }
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "-";
        try {
            return dateStr.substring(0, 10);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void showInterviewDetail(int row) {
        Interview i = (Interview) tableModel.getValueAt(row, 0);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detail de l'entretien", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(createDetailSection("Candidat", i.getCandidateName()));
        content.add(createDetailSection("Poste", i.getJobTitle()));
        content.add(createDetailSection("Date", formatDate(i.getDate())));
        content.add(createDetailSection("Heure", i.getTime() != null ? i.getTime().substring(0, 5) : "-"));
        content.add(createDetailSection("Duree", i.getDuration() + " minutes"));
        content.add(createDetailSection("Type", getTypeLabel(i.getType())));
        if (i.getLocation() != null && !i.getLocation().isEmpty()) {
            content.add(createDetailSection("Lieu", i.getLocation()));
        }
        if (i.getLink() != null && !i.getLink().isEmpty()) {
            content.add(createDetailSection("Lien", i.getLink()));
        }
        content.add(createDetailSection("Statut", getStatusLabel(i.getStatus())));
        if (i.getNotes() != null && !i.getNotes().isEmpty()) {
            content.add(createDetailSection("Notes", i.getNotes()));
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
        section.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        titleLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);

        JLabel valueLabel = new JLabel(value != null ? value : "-");
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        valueLabel.setForeground(ThemeManager.getTextColor(theme));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(valueLabel);

        return section;
    }

    private void showStatusChangeDialog(int row) {
        Interview i = (Interview) tableModel.getValueAt(row, 0);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Changer le statut", true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Changer le statut de l'entretien");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        headerLabel.setForeground(ThemeManager.getTextColor(theme));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(headerLabel);

        content.add(Box.createVerticalStrut(8));

        JLabel nameLabel = new JLabel(i.getCandidateName() + " - " + formatDate(i.getDate()) + " " + (i.getTime() != null ? i.getTime().substring(0, 5) : ""));
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        nameLabel.setForeground(ThemeManager.getSecondaryTextColor(theme));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLabel);

        content.add(Box.createVerticalStrut(16));

        String[] statusOptions = new String[STATUSES.length];
        for (int idx = 0; idx < STATUSES.length; idx++) {
            statusOptions[idx] = STATUS_LABELS[idx];
        }
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        styleComboBox(statusCombo);
        statusCombo.setMaximumSize(new Dimension(300, 36));

        for (int idx = 0; idx < STATUSES.length; idx++) {
            if (STATUSES[idx].equals(i.getStatus())) {
                statusCombo.setSelectedIndex(idx);
                break;
            }
        }
        content.add(statusCombo);

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
            updateInterviewStatus(i.getId(), newStatus);
            dialog.dispose();
        });
        buttonRow.add(saveBtn);

        content.add(buttonRow);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Planifier un entretien", true);
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(ThemeManager.getBackgroundColor(theme));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getBackgroundColor(theme));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Planifier un entretien");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        headerLabel.setForeground(ThemeManager.getTextColor(theme));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(headerLabel);

        content.add(Box.createVerticalStrut(16));

        JComboBox<String> appCombo = new JComboBox<>();
        for (Application app : shortlistedApps) {
            String label = (app.getCandidateName() != null ? app.getCandidateName() : "Candidat") + " - " + (app.getJobTitle() != null ? app.getJobTitle() : "");
            appCombo.addItem(label);
        }
        styleComboBox(appCombo);
        appCombo.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Candidature *", appCombo));

        JComboBox<String> typeCombo = new JComboBox<>(TYPE_LABELS);
        styleComboBox(typeCombo);
        typeCombo.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Type *", typeCombo));

        JTextField dateField = new JTextField();
        dateField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        dateField.setBackground(ThemeManager.getCardBackgroundColor(theme));
        dateField.setForeground(ThemeManager.getTextColor(theme));
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        dateField.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Date * (YYYY-MM-DD)", dateField));

        JTextField timeField = new JTextField();
        timeField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        timeField.setBackground(ThemeManager.getCardBackgroundColor(theme));
        timeField.setForeground(ThemeManager.getTextColor(theme));
        timeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        timeField.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Heure * (HH:MM)", timeField));

        JTextField durationField = new JTextField("60");
        durationField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        durationField.setBackground(ThemeManager.getCardBackgroundColor(theme));
        durationField.setForeground(ThemeManager.getTextColor(theme));
        durationField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        durationField.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Duree (minutes)", durationField));

        JTextField locationField = new JTextField();
        locationField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        locationField.setBackground(ThemeManager.getCardBackgroundColor(theme));
        locationField.setForeground(ThemeManager.getTextColor(theme));
        locationField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.getBorderColor(theme)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        locationField.setMaximumSize(new Dimension(350, 36));
        content.add(createFieldPanel("Lieu (pour presentiel)", locationField));

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

        JButton createBtn = new JButton("Creer");
        createBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(ThemeManager.getAccentColor(theme));
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setOpaque(true);
        createBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        createBtn.addActionListener(e -> {
            int appIdx = appCombo.getSelectedIndex();
            int typeIdx = typeCombo.getSelectedIndex();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            String duration = durationField.getText().trim();
            String location = locationField.getText().trim();

            if (date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            if (appIdx < 0 || shortlistedApps.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Aucune candidature disponible.");
                return;
            }

            try {
                int appId = shortlistedApps.get(appIdx).getId();
                JsonObject payload = new JsonObject();
                payload.addProperty("application", appId);
                payload.addProperty("type", INTERVIEW_TYPES[typeIdx]);
                payload.addProperty("date", date);
                payload.addProperty("time", time);
                payload.addProperty("duration", Integer.parseInt(duration.isEmpty() ? "60" : duration));
                if (!location.isEmpty()) {
                    payload.addProperty("location", location);
                }

                HttpRequest request = ApiClient.getInstance().request("/interviews/")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> response = ApiClient.getInstance().getClient().send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201) {
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur lors de la creation de l'entretien.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage());
            }
        });
        buttonRow.add(createBtn);

        content.add(buttonRow);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(350, Short.MAX_VALUE));
        panel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lbl.setForeground(ThemeManager.getSecondaryTextColor(theme));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);

        return panel;
    }

    private void updateInterviewStatus(int interviewId, String status) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("status", status);
                    HttpRequest request = ApiClient.getInstance().request("/interviews/" + interviewId + "/")
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                            .build();
                    ApiClient.getInstance().getClient().send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private String getTypeLabel(String type) {
        for (int i = 0; i < INTERVIEW_TYPES.length; i++) {
            if (INTERVIEW_TYPES[i].equals(type)) return TYPE_LABELS[i];
        }
        return type != null ? type : "-";
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
}
