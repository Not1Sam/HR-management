package com.hrmanager.ui;

import com.hrmanager.util.SessionManager;
import com.hrmanager.api.JobService;
import com.hrmanager.model.Job;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JobPanel extends JPanel {
    private final SessionManager session;
    private final JobService jobService;
    private JTable jobTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnClose;

    public JobPanel(SessionManager session) {
        this.session = session;
        this.jobService = new JobService();
        initializeComponents();
        setupLayout();
        loadJobs();
    }

    private void initializeComponents() {
        String[] columnNames = {"ID", "Titre", "Departement", "Lieu", "Type", "Niveau", "Statut", "Echeance", "Candidatures"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        jobTable = new JTable(tableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnAdd = new JButton("Ajouter");
        btnEdit = new JButton("Modifier");
        btnDelete = new JButton("Supprimer");
        btnClose = new JButton("Fermer");
        btnRefresh = new JButton("Actualiser");
        
        btnAdd.addActionListener(e -> addJob());
        btnEdit.addActionListener(e -> editJob());
        btnDelete.addActionListener(e -> deleteJob());
        btnClose.addActionListener(e -> closeJob());
        btnRefresh.addActionListener(e -> loadJobs());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(jobTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadJobs() {
        tableModel.setRowCount(0);
        try {
            List<Job> jobs = jobService.recupererOffres();
            for (Job job : jobs) {
                String deadline = job.getDeadline() != null ? formatDate(job.getDeadline()) : "-";
                tableModel.addRow(new Object[]{
                    job.getId(), job.getTitle(), job.getDepartment(), job.getLocation(),
                    job.getType(), job.getExperienceLevel(), job.getStatus(), deadline, job.getApplicationsCount()
                });
            }
            jobTable.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "-";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr.substring(0, 10));
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void addJob() {
        JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        if (dialog.showDialog()) {
            try {
                jobService.creerOffre(dialog.getJob());
                loadJobs();
                JOptionPane.showMessageDialog(this, "Offre creee avec succes");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editJob() {
        int row = jobTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une offre");
            return;
        }
        try {
            int jobId = (Integer) tableModel.getValueAt(row, 0);
            Job job = jobService.recupererOffre(jobId);
            JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), job);
            if (dialog.showDialog()) {
                jobService.modifierOffre(jobId, dialog.getJob());
                Thread.sleep(500);
                loadJobs();
                JOptionPane.showMessageDialog(this, "Offre mise a jour");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteJob() {
        int row = jobTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une offre");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette offre?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int jobId = (Integer) tableModel.getValueAt(row, 0);
                jobService.supprimerOffre(jobId);
                loadJobs();
                JOptionPane.showMessageDialog(this, "Offre supprimee");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void closeJob() {
        int row = jobTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une offre");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Fermer cette offre?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int jobId = (Integer) tableModel.getValueAt(row, 0);
                jobService.fermerOffre(jobId);
                loadJobs();
                JOptionPane.showMessageDialog(this, "Offre fermee");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class JobDialog extends JDialog {
    private JTextField txtTitle, txtLocation, txtSalary;
    private JComboBox<String> cmbType, cmbLevel, cmbStatus;
    private JTextArea txtDescription;
    private JSpinner spnDeadline;
    private boolean confirmed = false;
    private Job resultJob;

    private static final String[] JOB_TYPES = {"CDI", "CDD", "Stage", "Freelance"};
    private static final String[] EXPERIENCE_LEVELS = {"JUNIOR", "MID", "SENIOR", "EXPERT"};
    private static final String[] STATUSES = {"ACTIVE", "CLOSED"};
    private static final String[] DEPARTMENTS = {
        "Informatique & Digital",
        "Ressources Humaines",
        "Finance & Comptabilite",
        "Commercial & Vente",
        "Marketing & Communication",
        "Juridique",
        "Logistique & Supply Chain",
        "Production & Operations",
        "R&D & Innovation",
        "Direction Generale"
    };

    public JobDialog(Frame parent, Job job) {
        super(parent, job == null ? "Nouvelle Offre" : "Modifier Offre", true);
        this.resultJob = job != null ? job : new Job();
        initComponents();
        if (job != null) populateFields();
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel("Titre:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTitle = new JTextField(25);
        form.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Departement:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JComboBox<String> cmbDepartment = new JComboBox<>(DEPARTMENTS);
        form.add(cmbDepartment, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Lieu:*"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(25);
        form.add(txtLocation, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Type:*"), gbc);
        gbc.gridx = 1;
        cmbType = new JComboBox<>(JOB_TYPES);
        form.add(cmbType, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Niveau:*"), gbc);
        gbc.gridx = 1;
        cmbLevel = new JComboBox<>(EXPERIENCE_LEVELS);
        form.add(cmbLevel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("Salaire:"), gbc);
        gbc.gridx = 1;
        txtSalary = new JTextField(25);
        form.add(txtSalary, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        form.add(new JLabel("Echeance:*"), gbc);
        gbc.gridx = 1;
        spnDeadline = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnDeadline, "dd/MM/yyyy");
        spnDeadline.setEditor(dateEditor);
        spnDeadline.setPreferredSize(new Dimension(150, 25));
        form.add(spnDeadline, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        form.add(new JLabel("Statut:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(STATUSES);
        form.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        form.add(new JLabel("Description:*"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        txtDescription = new JTextArea(6, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescription), gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Enregistrer");
        JButton btnCancel = new JButton("Annuler");
        btnSave.addActionListener(e -> save(cmbDepartment));
        btnCancel.addActionListener(e -> dispose());
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);
    }

    private void populateFields() {
        txtTitle.setText(resultJob.getTitle());
        txtLocation.setText(resultJob.getLocation());
        txtSalary.setText(resultJob.getSalary());
        txtDescription.setText(resultJob.getDescription());
        cmbType.setSelectedItem(resultJob.getType());
        cmbLevel.setSelectedItem(resultJob.getExperienceLevel());
        cmbStatus.setSelectedItem(resultJob.getStatus());
        
        if (resultJob.getDeadline() != null && !resultJob.getDeadline().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = sdf.parse(resultJob.getDeadline().substring(0, 10));
                spnDeadline.setValue(date);
            } catch (Exception e) {
                spnDeadline.setValue(new java.util.Date());
            }
        }
    }

    private void save(JComboBox<String> cmbDepartment) {
        if (txtTitle.getText().trim().isEmpty() 
            || txtLocation.getText().trim().isEmpty() || txtDescription.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir les champs obligatoires");
            return;
        }

        resultJob.setTitle(txtTitle.getText().trim());
        resultJob.setDepartment((String) cmbDepartment.getSelectedItem());
        resultJob.setLocation(txtLocation.getText().trim());
        resultJob.setType((String) cmbType.getSelectedItem());
        resultJob.setExperienceLevel((String) cmbLevel.getSelectedItem());
        resultJob.setStatus((String) cmbStatus.getSelectedItem());
        resultJob.setSalary(txtSalary.getText().trim());
        resultJob.setDescription(txtDescription.getText().trim());
        
        java.util.Date selectedDate = (java.util.Date) spnDeadline.getValue();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        resultJob.setDeadline(sdf.format(selectedDate));

        confirmed = true;
        dispose();
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public Job getJob() {
        return resultJob;
    }
}