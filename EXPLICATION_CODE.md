# Explication complète du code HR Management - Pour la Présentation

## Table des matières
1. Architecture générale
2. Flux d'une action (exemple complet)
3. Explication détaillée par classe
4. Points clés à présenter

---

## 1. Architecture générale

```
App.java (point d'entrée)
    ↓
    ├─→ LoginFrame (authentification)
    │     ↓
    │   AuthService.java (appelle l'API /auth/login/)
    │     ↓
    │   SessionManager (stocke l'utilisateur connecté)
    │     ↓
    │   MainFrame (fenêtre principale)
    │
    └─→ MainFrame.java
          ├─ DashboardPanel → StatsService → /api/stats/
          ├─ JobPanel → JobService → /api/jobs/
          ├─ ApplicationPanel → ApplicationService → /api/applications/
          ├─ InterviewPanel → InterviewService → /api/interviews/
          └─ ProfilePanel → AuthService → /api/users/
```

---

## 2. Exemple complet : Cliquer sur "Ajouter une offre d'emploi"

### 2.1. Le bouton dans `JobPanel.java`

```java
// Ligne 38-39
btnAdd = new JButton("Ajouter");
btnAdd.addActionListener(e -> addJob());  // ← Quand on clique, appelle addJob()
```

### 2.2. La méthode `addJob()` dans `JobPanel.java`

```java
private void addJob() {
    // 1. Ouvre un dialogue (fenêtre modale)
    JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
    
    // 2. Affiche le dialogue et attend la saisie utilisateur
    if (dialog.showDialog()) {  // true si l'utilisateur clique "Enregistrer"
        try {
            // 3. Appelle le service pour créer l'offre
            jobService.creerOffre(dialog.getJob());
            
            // 4. Actualise la liste des offres
            loadJobs();
            
            // 5. Affiche un message de succès
            JOptionPane.showMessageDialog(this, "Offre creee avec succes");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

### 2.3. Le service : `JobService.creerOffre()`

```java
public Job creerOffre(Job offre) throws Exception {
    // 1. Crée un objet JSON à partir de l'offre
    JsonObject donnees = new JsonObject();
    donnees.addProperty("title", offre.getTitle());
    donnees.addProperty("department", offre.getDepartment());
    donnees.addProperty("location", offre.getLocation());
    donnees.addProperty("type", offre.getType());
    donnees.addProperty("experienceLevel", offre.getExperienceLevel());
    donnees.addProperty("status", offre.getStatus() != null ? offre.getStatus() : "ACTIVE");
    donnees.addProperty("description", offre.getDescription());
    
    // 2. Prépare la requête HTTP POST
    HttpRequest requete = client.request("/jobs/")  // ← Endpoint: /api/jobs/
            .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
            .build();
    
    // 3. Envoie la requête au serveur
    HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
    
    // 4. Vérifie si la création a réussi (code 201 = Created)
    if (reponse.statusCode() != 201) {
        throw new Exception("Erreur lors de la creation: " + reponse.body());
    }
    
    // 5. Convertit la réponse JSON en objet Job
    return gson.fromJson(reponse.body(), Job.class);
}
```

### 2.4. Communication réseau : `ApiClient.request()`

```java
public java.net.http.HttpRequest.Builder request(String endpoint) {
    // 1. Crée un builder de requête
    java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(BASE_URL + endpoint));  // ← URL complète: http://84.8.221.29:8001/api/jobs/
    
    // 2. Ajoute le token d'authentification (Bearer token)
    if (isAuthenticated()) {
        builder.header("Authorization", "Bearer " + accessToken);  // ← Important pour l'auth
    }
    
    // 3. Spécifie le format de la réponse
    builder.header("Content-Type", "application/json");
    return builder;
}
```

### 2.5. Résumé du flux complet

```
Utilisateur clique "Ajouter"
    ↓
JobPanel.addJob()
    ↓
Ouvre JobDialog (fenêtre de saisie)
    ↓
Utilisateur remplit le formulaire et clique "Enregistrer"
    ↓
JobService.creerOffre(Job obj)
    ↓
Convertit Job → JSON
    ↓
ApiClient.request("/jobs/") ajoute le token
    ↓
HttpClient.send() envoie POST au serveur
    ↓
Serveur répond avec 201 + JSON du nouvel offre
    ↓
Gson convertit JSON → Job
    ↓
loadJobs() actualise la liste
    ↓
Affiche "Offre creee avec succes"
```

---

## 3. Explication détaillée par classe

### 3.1. `App.java` - Point d'entrée

```java
public class App {
    public static void main(String[] args) {
        // 1. Configure les polices pour les boîtes de dialogue
        UIManager.put("OptionPane.messageFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        // 2. Gère les exceptions non capturées globalement
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Une erreur inattendue...");
            });
        });
        
        // 3. Lance l'UI sur le thread Event Dispatch (thread Swing)
        SwingUtilities.invokeLater(() -> {
            // 4. Essaie de charger les identifiants sauvegardés
            RememberMeManager.Credentials identifiants = RememberMeManager.loadCredentials();
            
            if (identifiants != null) {  // Si "Se souvenir de moi" était coché
                // 5. Configure les tokens dans le client API
                ApiClient.getInstance().setTokens(identifiants.getAccessToken(), 
                                                   identifiants.getRefreshToken());
                try {
                    // 6. Récupère l'utilisateur courant via l'API
                    AuthService serviceAuth = new AuthService();
                    User utilisateur = serviceAuth.recupererUtilisateur();
                    
                    // 7. Vérifie que l'utilisateur est RH et n'a pas besoin de changer son mot de passe
                    if ("HR".equalsIgnoreCase(utilisateur.getRole()) && !utilisateur.isMustChangePassword()) {
                        // 8. Connecte l'utilisateur
                        SessionManager.getInstance().login(utilisateur);
                        
                        // 9. Ouvre la fenêtre principale
                        new MainFrame(SessionManager.getInstance()).setVisible(true);
                        return;
                    }
                } catch (Exception e) {
                    // En cas d'erreur, efface les identifiants et affiche la connexion
                    RememberMeManager.clearCredentials();
                }
            }
            
            // 10. Si pas d'auto-login, ouvre l'écran de connexion
            new LoginFrame().setVisible(true);
        });
    }
}
```

**À dire à la prof :**
> C'est le point d'entrée. On essaie d'abord de se reconnecter automatiquement si les tokens sont sauvegardés. Sinon, on affiche la fenêtre de connexion. Tout se passe sur le thread Swing via `SwingUtilities.invokeLater()` pour éviter les problèmes de concurrence.

---

### 3.2. `LoginFrame.java` - Authentification

```java
public class LoginFrame extends JFrame {
    private JTextField champEmail;
    private JPasswordField champMotDePasse;
    private JCheckBox caseSeSouvenirDeMoi;
    private JButton boutonConnexion;
    
    public LoginFrame() {
        setTitle("Portail RH - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);  // ← Centre la fenêtre
        setResizable(false);
        
        // ... création des composants ...
        
        boutonConnexion.addActionListener(new ActionConnexion());
    }
    
    private class ActionConnexion implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = champEmail.getText().trim();
            String motDePasse = new String(champMotDePasse.getPassword());
            
            // 1. Validation basique
            if (email.isEmpty() || motDePasse.isEmpty()) {
                labelErreur.setText("Veuillez remplir tous les champs.");
                return;
            }
            
            try {
                // 2. Appelle le service d'authentification
                AuthService serviceAuth = new AuthService();
                AuthService.ResultatAuth resultat = serviceAuth.connexion(email, motDePasse);
                utilisateurActuel = resultat.getUtilisateur();
                
                // 3. Si doit changer le mot de passe, affiche le formulaire
                if (utilisateurActuel.isMustChangePassword()) {
                    layoutCartes.show(panneauPrincipal, "changerMotDePasse");  // ← CardLayout
                } else {
                    // 4. Si "Se souvenir de moi" coché, sauvegarde les identifiants
                    if (caseSeSouvenirDeMoi.isSelected()) {
                        RememberMeManager.saveCredentials(email, 
                                                         resultat.getTokenAcces(), 
                                                         resultat.getTokenRefresh());
                    }
                    
                    // 5. Connecte l'utilisateur
                    SessionManager session = SessionManager.getInstance();
                    session.login(utilisateurActuel);
                    
                    // 6. Ouvre la fenêtre principale
                    ouvrirApplicationPrincipale();
                }
            } catch (Exception ex) {
                labelErreur.setText(ex.getMessage());
            }
        }
    }
}
```

**À dire à la prof :**
> Deux panneaux : connexion + changement de mot de passe. On utilise `CardLayout` pour switcher entre les deux. Le bouton "Connexion" appelle `AuthService.connexion()` qui envoie email + mot de passe au serveur. Si succès, on sauvegarde les tokens si "Se souvenir de moi" est coché.

---

### 3.3. `ApiClient.java` - Singleton pour HTTP

```java
public class ApiClient {
    public static final String BASE_URL = "http://84.8.221.29:8001/api";
    private static final ApiClient instance = new ApiClient();  // ← Singleton
    private final HttpClient client;  // ← Client HTTP Java 11+
    private String accessToken;  // ← Token JWT d'authentification
    private String refreshToken;  // ← Token pour renouveler l'accès
    
    private ApiClient() {
        // 1. Crée le client HTTP une seule fois
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))  // ← Timeout 30 sec
                .build();
    }
    
    public static ApiClient getInstance() {
        return instance;  // ← Toujours le même objet
    }
    
    public HttpRequest.Builder request(String endpoint) {
        // 1. Crée l'URL complète
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint));  // Ex: .../api/jobs/
        
        // 2. Ajoute le token si connecté
        if (isAuthenticated()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        
        // 3. Spécifie JSON
        builder.header("Content-Type", "application/json");
        return builder;
    }
    
    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isEmpty();
    }
}
```

**À dire à la prof :**
> C'est un singleton : une seule instance pour toute l'application. Il contient l'URL du serveur, les tokens d'authentification, et une méthode `request()` qui prépare les requêtes HTTP avec le bon header d'authentification. On utilise `HttpClient` de Java 11+, pas l'ancienne classe `HttpURLConnection`.

---

### 3.4. `AuthService.java` - Gestion de l'authentification

```java
public class AuthService {
    private static final Gson gson = new Gson();  // ← JSON ↔ Object
    private final ApiClient client;
    
    public ResultatAuth connexion(String email, String motDePasse) throws Exception {
        // 1. Crée l'objet JSON avec email et mot de passe
        JsonObject donnees = new JsonObject();
        donnees.addProperty("email", email);
        donnees.addProperty("password", motDePasse);
        
        // 2. Prépare la requête POST
        HttpRequest requete = client.request("/auth/login/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();
        
        // 3. Envoie au serveur
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        
        // 4. Gère les codes d'erreur
        if (reponse.statusCode() == 404) {
            throw new Exception("Email non enregistre.");
        }
        if (reponse.statusCode() == 401) {
            throw new Exception("Mot de passe incorrect.");
        }
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur de connexion");
        }
        
        // 5. Parse la réponse JSON
        JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
        String tokenAcces = json.get("access").getAsString();
        String tokenRefresh = json.get("refresh").getAsString();
        
        // 6. Convertit le JSON "user" en objet User
        User utilisateur = gson.fromJson(json.get("user"), User.class);
        
        // 7. Vérifie que c'est un RH
        if (!"HR".equalsIgnoreCase(utilisateur.getRole())) {
            throw new Exception("Acces refuse. Cette application est reservee au personnel RH.");
        }
        
        // 8. Sauvegarde les tokens dans ApiClient
        client.setTokens(tokenAcces, tokenRefresh);
        
        // 9. Retourne le résultat
        return new ResultatAuth(utilisateur, tokenAcces, tokenRefresh);
    }
    
    public User recupererUtilisateur() throws Exception {
        // 1. Requête GET vers /users/me/
        HttpRequest requete = client.request("/users/me/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation du profil");
        }
        
        // 2. Convertit JSON → User
        return gson.fromJson(reponse.body(), User.class);
    }
}
```

**À dire à la prof :**
> Ce service encapsule la logique d'authentification. La méthode `connexion()` envoie email + password au serveur, récupère les tokens JWT, et vérifie que c'est un RH. On utilise Gson pour convertir JSON ↔ objets Java. C'est du pattern "adapter" : on transforme les réponses HTTP en objets métier.

---

### 3.5. `JobService.java` - CRUD des offres

```java
public class JobService {
    private static final Gson gson = new Gson();
    private final ApiClient client;
    
    public List<Job> recupererOffres() throws Exception {
        // 1. Ajoute un timestamp pour éviter le cache du navigateur
        long horodatage = System.currentTimeMillis();
        
        // 2. Requête GET
        HttpRequest requete = client.request("/jobs/?t=" + horodatage).GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation des offres");
        }
        
        // 3. Parse la réponse et convertit en List<Job>
        return analyserReponse(reponse.body());
    }
    
    private List<Job> analyserReponse(String corpsReponse) {
        List<Job> listeOffres = new ArrayList<>();
        try {
            // 1. Première tentative : parse comme {"results": [...]}
            JsonObject json = JsonParser.parseString(corpsReponse).getAsJsonObject();
            JsonArray tableau = json.getAsJsonArray("results");
            if (tableau != null) {
                for (int i = 0; i < tableau.size(); i++) {
                    // 2. Convertit chaque élément JSON en objet Job
                    listeOffres.add(gson.fromJson(tableau.get(i), Job.class));
                }
            }
        } catch (Exception e) {
            // 3. Si pas de "results", essaie direct tableau JSON
            try {
                JsonArray tableau = JsonParser.parseString(corpsReponse).getAsJsonArray();
                for (int i = 0; i < tableau.size(); i++) {
                    listeOffres.add(gson.fromJson(tableau.get(i), Job.class));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return listeOffres;
    }
    
    public Job creerOffre(Job offre) throws Exception {
        // 1. Crée l'objet JSON
        JsonObject donnees = new JsonObject();
        donnees.addProperty("title", offre.getTitle());
        donnees.addProperty("department", offre.getDepartment());
        donnees.addProperty("location", offre.getLocation());
        // ... autres champs ...
        
        // 2. Envoie POST
        HttpRequest requete = client.request("/jobs/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        
        // 3. Vérifie 201 (Created)
        if (reponse.statusCode() != 201) {
            throw new Exception("Erreur lors de la creation: " + reponse.body());
        }
        
        // 4. Convertit réponse JSON en Job
        return gson.fromJson(reponse.body(), Job.class);
    }
    
    public Job modifierOffre(int id, Job offre) throws Exception {
        // Similaire à creerOffre, mais:
        // - Endpoint: /jobs/{id}/
        // - Méthode: PUT
        // - Code d'erreur: 200 (OK)
    }
    
    public void supprimerOffre(int id) throws Exception {
        // - Endpoint: /jobs/{id}/
        // - Méthode: DELETE
        // - Code d'erreur: 204 (No Content)
    }
    
    public Job fermerOffre(int id) throws Exception {
        // - Endpoint: /jobs/{id}/close/
        // - Méthode: PATCH
        // - Body: empty
    }
}
```

**À dire à la prof :**
> C'est le pattern "Service" : encapsule l'accès à l'API pour les offres d'emploi. Les 5 méthodes correspondent aux 5 opérations CRUD + une action custom (fermer). Chaque méthode prépare les données, envoie la requête HTTP, et convertit la réponse en objet. Les erreurs sont levées si le statut HTTP n'est pas correct.

---

### 3.6. `JobPanel.java` - Interface utilisateur

```java
public class JobPanel extends JPanel {
    private final SessionManager session;
    private final JobService jobService;  // ← Service (couche métier)
    private JTable jobTable;  // ← Tableau pour afficher les offres
    private DefaultTableModel tableModel;  // ← Modèle du tableau
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnClose;
    
    public JobPanel(SessionManager session) {
        this.session = session;
        this.jobService = new JobService();
        initializeComponents();
        setupLayout();
        loadJobs();  // ← Charge les offres au démarrage
    }
    
    private void initializeComponents() {
        // 1. Crée les colonnes du tableau
        String[] columnNames = {"ID", "Titre", "Departement", ..., "Candidatures"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { 
                return false;  // ← Tableau non éditable
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 2. Crée les boutons
        btnAdd = new JButton("Ajouter");
        btnAdd.addActionListener(e -> addJob());
        
        btnEdit = new JButton("Modifier");
        btnEdit.addActionListener(e -> editJob());
        
        btnDelete = new JButton("Supprimer");
        btnDelete.addActionListener(e -> deleteJob());
        
        btnClose = new JButton("Fermer");
        btnClose.addActionListener(e -> closeJob());
        
        btnRefresh = new JButton("Actualiser");
        btnRefresh.addActionListener(e -> loadJobs());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Tableau en haut
        JScrollPane scrollPane = new JScrollPane(jobTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Boutons en bas
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadJobs() {
        tableModel.setRowCount(0);  // ← Efface les lignes
        try {
            // 1. Appelle le service pour récupérer les offres
            List<Job> jobs = jobService.recupererOffres();
            
            // 2. Ajoute chaque offre en ligne du tableau
            for (Job job : jobs) {
                String deadline = job.getDeadline() != null ? formatDate(job.getDeadline()) : "-";
                tableModel.addRow(new Object[]{
                    job.getId(), 
                    job.getTitle(), 
                    job.getDepartment(), 
                    job.getLocation(),
                    job.getType(), 
                    job.getExperienceLevel(), 
                    job.getStatus(), 
                    deadline, 
                    job.getApplicationsCount()
                });
            }
            jobTable.repaint();  // ← Rafraîchit l'affichage
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addJob() {
        // 1. Ouvre une boîte de dialogue
        JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        
        if (dialog.showDialog()) {  // Vrai si l'utilisateur clique "Enregistrer"
            try {
                // 2. Envoie l'offre au service
                jobService.creerOffre(dialog.getJob());
                
                // 3. Actualise la liste
                loadJobs();
                
                // 4. Affiche un message
                JOptionPane.showMessageDialog(this, "Offre creee avec succes");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editJob() {
        // 1. Récupère la ligne sélectionnée
        int row = jobTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez une offre");
            return;
        }
        
        try {
            // 2. Récupère l'ID
            int jobId = (Integer) tableModel.getValueAt(row, 0);
            
            // 3. Récupère les détails de l'offre
            Job job = jobService.recupererOffre(jobId);
            
            // 4. Ouvre la boîte de dialogue avec les données
            JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), job);
            
            if (dialog.showDialog()) {
                // 5. Envoie les modifications
                jobService.modifierOffre(jobId, dialog.getJob());
                Thread.sleep(500);  // ← Petite pause
                
                // 6. Actualise la liste
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
        
        // 1. Demande confirmation
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette offre?", "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int jobId = (Integer) tableModel.getValueAt(row, 0);
                
                // 2. Appelle le service
                jobService.supprimerOffre(jobId);
                
                // 3. Actualise
                loadJobs();
                
                JOptionPane.showMessageDialog(this, "Offre supprimee");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
```

**À dire à la prof :**
> C'est la couche présentation. Un panneau Swing avec un tableau et 5 boutons. Chaque bouton appelle une méthode privée qui interagit avec `JobService`. `loadJobs()` remplit le tableau avec les données du serveur. `addJob()` ouvre un dialogue, puis envoie les données au service. C'est du MVC simple : la UI communique avec le service, pas directement avec l'API.

---

### 3.7. `SessionManager.java` - Singleton pour l'utilisateur

```java
public class SessionManager {
    private static SessionManager instance;  // ← Singleton
    private User utilisateurActuel;
    private boolean estConnecte = false;
    
    private SessionManager() {}  // ← Constructeur privé
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;  // ← Toujours le même objet
    }
    
    public void login(User user) {
        this.utilisateurActuel = user;
        this.estConnecte = true;
    }
    
    public void logout() {
        this.estConnecte = false;
        this.utilisateurActuel = null;
    }
    
    public User getCurrentUser() {
        return this.utilisateurActuel;
    }
    
    public boolean isLoggedIn() {
        return this.estConnecte;
    }
}
```

**À dire à la prof :**
> Singleton qui stocke l'utilisateur connecté. Une seule instance pour toute l'app. Permet à toutes les classes d'accéder à l'utilisateur courant via `SessionManager.getInstance().getCurrentUser()`. C'est plus propre que de passer l'utilisateur partout.

---

### 3.8. `RememberMeManager.java` - Persistance

```java
public class RememberMeManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hrdesktop";
    // ← Dossier caché: ~/.hrdesktop/
    
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "auth.properties";
    // ← Fichier: ~/.hrdesktop/auth.properties
    
    public static void saveCredentials(String email, String accessToken, String refreshToken) {
        try {
            // 1. Crée le dossier s'il n'existe pas
            Files.createDirectories(Paths.get(CONFIG_DIR));
            
            // 2. Crée un fichier Properties
            Properties props = new Properties();
            props.setProperty("email", email);
            props.setProperty("accessToken", accessToken);
            props.setProperty("refreshToken", refreshToken);
            
            // 3. Sauvegarde dans le fichier
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "HR Desktop Auth");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Credentials loadCredentials() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return null;  // Pas de fichier, pas de créds
            
            // 1. Charge le fichier
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            }
            
            // 2. Récupère les valeurs
            String email = props.getProperty("email");
            String accessToken = props.getProperty("accessToken");
            String refreshToken = props.getProperty("refreshToken");
            
            // 3. Retourne les credentials si tous les champs existent
            if (email != null && accessToken != null && refreshToken != null) {
                return new Credentials(email, accessToken, refreshToken);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void clearCredentials() {
        try {
            Files.deleteIfExists(Paths.get(CONFIG_FILE));  // ← Supprime le fichier
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**À dire à la prof :**
> Cette classe gère la sauvegarde locale des identifiants. Quand "Se souvenir de moi" est coché, on sauvegarde email + tokens dans `~/.hrdesktop/auth.properties`. Au démarrage, on les charge et on tente l'auto-connexion. C'est basique mais pratique : pas besoin de refaire la connexion à chaque fois.

---

### 3.9. `ThemeManager.java` - Gestion des couleurs

```java
public class ThemeManager {
    public enum Theme { LIGHT, DARK }  // ← Deux thèmes
    
    private static final Color BRAND = new Color(79, 70, 229);      // Bleu
    private static final Color SUCCESS = new Color(16, 185, 129);   // Vert
    private static final Color DANGER = new Color(239, 68, 68);     // Rouge
    private static final Color WARNING = new Color(245, 158, 11);   // Orange
    private static final Color INFO = new Color(59, 130, 246);      // Bleu clair
    
    private static final Color LIGHT_BG = new Color(248, 250, 255);         // Fond blanc
    private static final Color LIGHT_SURFACE = new Color(255, 255, 255);    // Surface blanche
    private static final Color LIGHT_TEXT = new Color(15, 23, 42);          // Texte noir
    private static final Color LIGHT_SECONDARY = new Color(100, 116, 139);  // Gris
    private static final Color LIGHT_BORDER = new Color(226, 232, 240);     // Bordure grise
    
    // ... Idem pour DARK ...
    
    public static Color getBackgroundColor(Theme theme) {
        return theme == Theme.DARK ? DARK_BG : LIGHT_BG;
    }
    
    public static Color getTextColor(Theme theme) {
        return theme == Theme.DARK ? DARK_TEXT : LIGHT_TEXT;
    }
    
    // ... Autres getters ...
}
```

**À dire à la prof :**
> C'est un gestionnaire de thème. Centralise toutes les couleurs. Grâce à ça, on peut facilement changer entre thème clair et sombre. Chaque composant récupère ses couleurs via les méthodes statiques. C'est du pattern "Strategy".

---

## 4. Les modèles (POJOs)

### `User.java`, `Job.java`, `Application.java`, `Interview.java`

```java
public class User {
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;  // "HR" ou autre
    private boolean mustChangePassword;
    
    // Getters/Setters
}

public class Job {
    private int id;
    private String title;
    private String department;
    private String location;
    private String type;  // "CDI", "CDD", etc.
    private String experienceLevel;  // "JUNIOR", "SENIOR", etc.
    private String status;  // "ACTIVE", "CLOSED"
    private String description;
    private int applicationsCount;
    
    // Getters/Setters
}

public class Application {
    private int id;
    private int job;
    private String jobTitle;
    private int candidate;
    private String candidateName;
    private String status;  // "SENT", "REVIEWING", "ACCEPTED", "REJECTED", etc.
    private Integer score;  // Note de 0-100
    private String cvUrl;
    private String coverLetterUrl;
    private String notes;
    
    // Getters/Setters
}

public class Interview {
    private int id;
    private int application;
    private String type;  // "PRESENTIEL", "VISIOCONFERENCE", "TELEPHONE"
    private String date;
    private String time;
    private String status;  // "PLANNED", "CONFIRMED", "CANCELLED", "DONE"
    private String candidateName;
    private String jobTitle;
    
    // Getters/Setters
}
```

**À dire à la prof :**
> Ce sont des POJOs (Plain Old Java Objects). Ils correspondent aux entités du serveur. Gson les convertit automatiquement JSON ↔ objets. Pas de logique, juste des données + getters/setters.

---

## 5. Résumé pour la présentation orale

### Questions souvent posées par la prof :

#### Q1: "Ce bouton, il fait quoi?"
→ Répondre en 3 étapes :
1. Le bouton appelle une méthode de la classe UI (ex: `addJob()`)
2. Cette méthode utilise un service (ex: `jobService.creerOffre()`)
3. Le service envoie une requête HTTP + traite la réponse

#### Q2: "Où sont les tokens stockés?"
→ Répondre :
- En mémoire dans `ApiClient` (accessToken, refreshToken)
- Optionnellement dans `~/.hrdesktop/auth.properties` si "Se souvenir de moi" est coché
- Le service `AuthService` les récupère du serveur après connexion

#### Q3: "Comment l'app communique avec le serveur?"
→ Répondre :
- Via HTTP REST
- `HttpClient` Java 11+
- Endpoint: `http://84.8.221.29:8001/api`
- Requêtes: GET, POST, PUT, DELETE, PATCH
- Réponses: JSON, converties en objets Gson

#### Q4: "Qu'est-ce qu'un Singleton?"
→ Répondre :
- Une classe avec une seule instance
- Constructeur privé
- Méthode statique `getInstance()`
- Exemples: `ApiClient`, `SessionManager`
- Utilité: partager l'état dans toute l'app

#### Q5: "Pourquoi CardLayout dans LoginFrame?"
→ Répondre :
- Permet d'afficher 2 panneaux différents : connexion + changement mot de passe
- `layoutCartes.show(panneauPrincipal, "connexion")` ou `"changerMotDePasse"`

#### Q6: "Qu'est-ce que SwingWorker?"
→ Répondre :
- Thread worker pour les tâches longues (requêtes HTTP)
- Évite de bloquer le thread Swing (interface figée)
- `doInBackground()` : code lourd
- `done()` : mise à jour UI

#### Q7: "Comment on gère les erreurs?"
→ Répondre :
- Vérifier le `statusCode` de la réponse HTTP
- Lever une exception si erreur
- Capturer l'exception dans le UI et afficher `JOptionPane`
- Thread.setDefaultUncaughtExceptionHandler pour les erreurs globales

---

## 6. Flux complet d'une candidature

```
L'utilisateur ouvre ApplicationPanel
  ↓
loadData() lancé
  ↓
ApplicationService.recupererCandidatures()
  ↓
GET /api/applications/
  ↓
Serveur envoie JSON [{id: 1, job: 2, status: "SENT", ...}, ...]
  ↓
Gson parse JSON → List<Application>
  ↓
filterAndShow() remplit le tableau
  ↓
Utilisateur clique "Voir" sur une candidature
  ↓
showApplicationDetail(row)
  ↓
Ouvre un JDialog avec details (nom, email, cv, notes)
  ↓
Utilisateur clique "Statut"
  ↓
showStatusChangeDialog(row)
  ↓
JComboBox avec les statuts possibles
  ↓
Utilisateur sélectionne "INTERVIEW" + ajoute une note
  ↓
Clique "Enregistrer"
  ↓
ApplicationService.changerStatut(appId, "INTERVIEW", "Bon profil")
  ↓
PATCH /api/applications/1/status/
Body: {"status": "INTERVIEW", "notes": "Bon profil"}
Header: Authorization: Bearer <token>
  ↓
Serveur valide, met à jour, envoie 200 + JSON updated
  ↓
loadData() rafraîchit le tableau
  ↓
Candidature affiche nouveau statut
```

---

## 7. Architecture en images texte

```
┌─────────────────────────────────────────────────────────────┐
│                        JFrame (Swing)                       │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           JTabbedPane (MainFrame)                      │ │
│  │  ┌──────────┬──────────┬──────────┬─────────┬─────────┐ │ │
│  │  │Dashboard │  Jobs    │ Candidat │Interview│ Profil  │ │ │
│  │  └──────────┴──────────┴──────────┴─────────┴─────────┘ │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
           │
           ├─→ JobPanel → JobService → ApiClient → HttpClient
           │
           ├─→ ApplicationPanel → ApplicationService → ApiClient → HttpClient
           │
           ├─→ InterviewPanel → InterviewService → ApiClient → HttpClient
           │
           └─→ ProfilePanel → AuthService → ApiClient → HttpClient

ApiClient (Singleton)
  │
  ├─ accessToken
  ├─ refreshToken
  └─ BASE_URL = "http://84.8.221.29:8001/api"

SessionManager (Singleton)
  │
  └─ currentUser: User

RememberMeManager
  │
  └─ ~/.hrdesktop/auth.properties
```

---

## 8. Checklist de présentation

- [ ] Expliquer l'architecture 3-couches : UI → Services → API
- [ ] Montrer comment un bouton déclenche une action complète
- [ ] Expliquer le pattern Singleton (ApiClient, SessionManager)
- [ ] Montrer la conversion JSON ↔ Objets (Gson)
- [ ] Expliquer la gestion des erreurs (statusCode, exceptions, JOptionPane)
- [ ] Parler de la sauvegarde locale (RememberMeManager)
- [ ] Mentionner les technos : Java 11+, HttpClient, Gson, Swing
- [ ] Montrer le flux d'authentification
- [ ] Expliquer les statuts HTTP (200, 201, 204, 401, 404)
- [ ] Montrer les endpoints REST utilisés
