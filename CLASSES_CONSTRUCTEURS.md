# Fondamentaux Java du projet HR Management

## Rappel : Les 4 piliers des classes Java

```
public class MaClasse {
    // 1. ATTRIBUTS (variables de classe)
    private String nom;
    private int age;
    
    // 2. CONSTRUCTEUR (crée l'objet)
    public MaClasse(String nom, int age) {
        this.nom = nom;
        this.age = age;
    }
    
    // 3. MÉTHODES (comportements)
    public void afficherInfo() {
        System.out.println(nom + " a " + age + " ans");
    }
    
    // 4. GETTERS/SETTERS (accès contrôlé aux attributs)
    public String getNom() {
        return this.nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
}
```

---

## 1. Classe `User.java` - Modèle utilisateur

### Structure

```java
public class User {
    // ==================== ATTRIBUTS ====================
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String avatar;
    private String avatarUrl;
    private String phone;
    private String location;
    private String department;
    private String linkedin;
    private String github;
    private List<String> skills;
    private String availability;
    private String bio;
    private boolean isActive;
    private boolean mustChangePassword;
    
    // ==================== CONSTRUCTEUR ====================
    // Implicite (créé automatiquement par Java si on ne le définit pas)
    // Utilisé comme: User user = new User();
    
    // ==================== GETTERS ====================
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getFullName() {
        return firstName + " " + lastName;
    }
    public boolean isMustChangePassword() { return mustChangePassword; }
    
    // ==================== SETTERS ====================
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setMustChangePassword(boolean mustChangePassword) { 
        this.mustChangePassword = mustChangePassword; 
    }
}
```

### Comment elle est utilisée

```java
// 1. Création d'une instance de User
User user = new User();  // ← Constructeur implicite

// 2. Utilisation des setters pour remplir les attributs
user.setId(1);
user.setEmail("alice@example.com");
user.setFirstName("Alice");
user.setLastName("Dupont");
user.setRole("HR");

// 3. Utilisation des getters pour accéder aux attributs
System.out.println(user.getFullName());  // "Alice Dupont"
System.out.println(user.getRole());      // "HR"

// 4. Utilisation dans une condition
if ("HR".equalsIgnoreCase(user.getRole()) && !user.isMustChangePassword()) {
    // Connecter l'utilisateur
}
```

### Utilisation réelle dans le projet

```java
// Dans AuthService.java, ligne 35-40
User utilisateur = gson.fromJson(json.get("user"), User.class);
// ↑ Gson crée une instance User automatiquement à partir du JSON

if (!"HR".equalsIgnoreCase(utilisateur.getRole())) {
    // ↑ Appelle le getter getRole()
    throw new Exception("Acces refuse.");
}

// Dans App.java, ligne 37
SessionManager.getInstance().login(utilisateur);
// ↑ Passe l'instance User au SessionManager
```

---

## 2. Classe `Job.java` - Offre d'emploi

### Structure

```java
public class Job {
    // ==================== ATTRIBUTS ====================
    private int id;
    private String title;
    private String department;
    private String location;
    private String type;           // "CDI", "CDD", "Stage"
    private String experienceLevel; // "JUNIOR", "SENIOR"
    private String salary;
    private String status;         // "ACTIVE", "CLOSED"
    private int views;
    private String postedAt;
    private String deadline;
    private String description;
    private List<String> missions;
    private List<String> profile;
    private List<String> advantages;
    private List<String> skills;
    private int postedBy;
    private int applicationsCount;
    
    // ==================== CONSTRUCTEUR ====================
    public Job() {}  // ← Constructeur vide explicite
    
    // ==================== GETTERS ====================
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getStatus() { return status; }
    public int getApplicationsCount() { return applicationsCount; }
    
    // ==================== SETTERS ====================
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDepartment(String department) { this.department = department; }
    public void setStatus(String status) { this.status = status; }
}
```

### Comment elle est utilisée

```java
// 1. Création d'une instance vide
Job job = new Job();  // ← Appelle le constructeur vide public Job() {}

// 2. Remplissage des attributs via setters
job.setTitle("Développeur Java");
job.setDepartment("Informatique");
job.setLocation("Paris");
job.setType("CDI");
job.setExperienceLevel("SENIOR");
job.setStatus("ACTIVE");

// 3. Utilisation des getters
System.out.println(job.getTitle());  // "Développeur Java"

// 4. Envoi au serveur
jobService.creerOffre(job);
```

### Utilisation réelle dans le projet

```java
// Dans JobService.java, ligne 55-63
List<Job> analyserReponse(String corpsReponse) {
    List<Job> listeOffres = new ArrayList<>();
    JsonArray tableau = json.getAsJsonArray("results");
    
    for (int i = 0; i < tableau.size(); i++) {
        // ↓ Gson crée une instance Job et remplit ses attributs
        listeOffres.add(gson.fromJson(tableau.get(i), Job.class));
    }
    return listeOffres;
}

// Dans JobPanel.java, ligne 107-117
Job creerOffre(Job offre) throws Exception {
    JsonObject donnees = new JsonObject();
    donnees.addProperty("title", offre.getTitle());       // ↓ Getter
    donnees.addProperty("department", offre.getDepartment()); // ↓ Getter
    // ...
}
```

---

## 3. Classe `ApiClient.java` - Singleton

### Structure (attention : Singleton !)

```java
public class ApiClient {
    // ==================== ATTRIBUTS DE CLASSE (STATIQUES) ====================
    public static final String BASE_URL = "http://84.8.221.29:8001/api";
    private static final ApiClient instance = new ApiClient();  // ← Une seule instance
    
    // ==================== ATTRIBUTS D'INSTANCE ====================
    private final HttpClient client;      // Client HTTP
    private String accessToken;           // Token JWT
    private String refreshToken;          // Token refresh
    
    // ==================== CONSTRUCTEUR ====================
    private ApiClient() {  // ← Privé ! Pas d'instantiation directe
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    // ==================== MÉTHODE STATIQUE POUR ACCÉDER À L'INSTANCE ====================
    public static ApiClient getInstance() {
        return instance;  // ← Toujours la même instance
    }
    
    // ==================== GETTERS ====================
    public HttpClient getClient() { return client; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getBaseUrl() { return BASE_URL; }
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isEmpty();
    }
    
    // ==================== SETTERS ====================
    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
    }
    
    // ==================== MÉTHODES ====================
    public HttpRequest.Builder request(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint));
        if (isAuthenticated()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        builder.header("Content-Type", "application/json");
        return builder;
    }
}
```

### Différence : Singleton vs Classe normale

```java
// Classe NORMALE
public class Voiture {
    public Voiture() {}
}

// Utilisation normale
Voiture v1 = new Voiture();
Voiture v2 = new Voiture();
// v1 et v2 sont DEUX instances différentes

// ──────────────────────────────

// Classe SINGLETON (ApiClient)
public class ApiClient {
    private static final ApiClient instance = new ApiClient();
    private ApiClient() {}  // Constructeur privé
    public static ApiClient getInstance() { return instance; }
}

// Utilisation Singleton
ApiClient c1 = ApiClient.getInstance();
ApiClient c2 = ApiClient.getInstance();
// c1 et c2 pointent vers LA MÊME instance !
// c1 == c2  ← true
```

### Avantage

```java
// Avec Singleton, les tokens sont partagés partout
ApiClient.getInstance().setTokens(access, refresh);

// N'importe où dans le code
if (ApiClient.getInstance().isAuthenticated()) {
    // Tous accèdent aux mêmes tokens
}
```

---

## 4. Classe `SessionManager.java` - Singleton utilisateur

```java
public class SessionManager {
    // ==================== ATTRIBUTS ====================
    private static SessionManager instance;  // Une seule instance (lazy initialization)
    private User utilisateurActuel;
    private boolean estConnecte = false;
    
    // ==================== CONSTRUCTEUR ====================
    private SessionManager() {}  // ← Privé, pas d'instantiation directe
    
    // ==================== MÉTHODE POUR ACCÉDER À L'INSTANCE ====================
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();  // ← Créé à la première utilisation
        }
        return instance;
    }
    
    // ==================== MÉTHODES ====================
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
}
```

### Utilisation réelle

```java
// Dans App.java, ligne 37-38
SessionManager.getInstance().login(utilisateur);
new MainFrame(SessionManager.getInstance()).setVisible(true);

// Dans MainFrame.java, ligne 21
User currentUser = session.getCurrentUser();

// N'importe où dans l'app
User user = SessionManager.getInstance().getCurrentUser();
if (user != null) {
    System.out.println(user.getFullName());
}
```

---

## 5. Classe `AuthService.java` - Service avec méthodes statiques

```java
public class AuthService {
    // ==================== ATTRIBUTS DE CLASSE (STATIQUES) ====================
    private static final Gson gson = new Gson();  // Une seule instance Gson pour toute la classe
    
    // ==================== ATTRIBUTS D'INSTANCE ====================
    private final ApiClient client;  // Référence au singleton ApiClient
    
    // ==================== CONSTRUCTEUR ====================
    public AuthService() {
        this.client = ApiClient.getInstance();  // ← Récupère le singleton
    }
    
    // ==================== MÉTHODES ====================
    public ResultatAuth connexion(String email, String motDePasse) throws Exception {
        JsonObject donnees = new JsonObject();
        donnees.addProperty("email", email);
        donnees.addProperty("password", motDePasse);
        
        HttpRequest requete = client.request("/auth/login/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();
        
        HttpResponse<String> reponse = client.getClient().send(requete, 
                                                              HttpResponse.BodyHandlers.ofString());
        
        if (reponse.statusCode() == 404) {
            throw new Exception("Email non enregistre.");
        }
        if (reponse.statusCode() == 401) {
            throw new Exception("Mot de passe incorrect.");
        }
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur de connexion");
        }
        
        JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
        String tokenAcces = json.get("access").getAsString();
        String tokenRefresh = json.get("refresh").getAsString();
        User utilisateur = gson.fromJson(json.get("user"), User.class);
        
        if (!"HR".equalsIgnoreCase(utilisateur.getRole())) {
            throw new Exception("Acces refuse. Reservé au personnel RH.");
        }
        
        client.setTokens(tokenAcces, tokenRefresh);
        return new ResultatAuth(utilisateur, tokenAcces, tokenRefresh);
    }
    
    public User recupererUtilisateur() throws Exception {
        HttpRequest requete = client.request("/users/me/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, 
                                                              HttpResponse.BodyHandlers.ofString());
        
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation du profil");
        }
        
        return gson.fromJson(reponse.body(), User.class);
    }
    
    // ==================== CLASSE INTERNE ====================
    public static class ResultatAuth {
        private final User utilisateur;
        private final String tokenAcces;
        private final String tokenRefresh;
        
        public ResultatAuth(User utilisateur, String tokenAcces, String tokenRefresh) {
            this.utilisateur = utilisateur;
            this.tokenAcces = tokenAcces;
            this.tokenRefresh = tokenRefresh;
        }
        
        public User getUtilisateur() { return utilisateur; }
        public String getTokenAcces() { return tokenAcces; }
        public String getTokenRefresh() { return tokenRefresh; }
    }
}
```

### Utilisation réelle

```java
// Dans App.java, ligne 34-36
AuthService serviceAuth = new AuthService();  // ← Crée une instance
User utilisateur = serviceAuth.recupererUtilisateur();

// Dans LoginFrame.java, ligne 131-132
AuthService serviceAuth = new AuthService();
AuthService.ResultatAuth resultat = serviceAuth.connexion(email, motDePasse);
// ↑ Retourne un objet ResultatAuth (classe interne)

// Accès à la classe interne
User user = resultat.getUtilisateur();
String token = resultat.getTokenAcces();
```

---

## 6. Classe `JobPanel.java` - Composant Swing avec constructeur

```java
public class JobPanel extends JPanel {
    // ==================== ATTRIBUTS ====================
    private final SessionManager session;          // Référence au Singleton
    private final JobService jobService;            // Service métier
    private JTable jobTable;                        // Composant Swing
    private DefaultTableModel tableModel;           // Modèle du tableau
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnClose;
    
    // ==================== CONSTRUCTEUR ====================
    public JobPanel(SessionManager session) {
        this.session = session;              // ← Paramètre du constructeur
        this.jobService = new JobService();  // ← Crée une nouvelle instance
        initializeComponents();              // ← Appelle méthode d'initialisation
        setupLayout();                       // ← Appelle méthode de layout
        loadJobs();                          // ← Charge les données au démarrage
    }
    
    // ==================== MÉTHODES D'INITIALISATION ====================
    private void initializeComponents() {
        String[] columnNames = {"ID", "Titre", "Departement", ...};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { 
                return false; 
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnAdd = new JButton("Ajouter");
        btnAdd.addActionListener(e -> addJob());  // ← Lambda, appelle la méthode
        
        btnEdit = new JButton("Modifier");
        btnEdit.addActionListener(e -> editJob());
        
        btnDelete = new JButton("Supprimer");
        btnDelete.addActionListener(e -> deleteJob());
        
        btnRefresh = new JButton("Actualiser");
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
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // ==================== MÉTHODES MÉTIER ====================
    private void loadJobs() {
        tableModel.setRowCount(0);
        try {
            List<Job> jobs = jobService.recupererOffres();
            for (Job job : jobs) {
                tableModel.addRow(new Object[]{
                    job.getId(), 
                    job.getTitle(), 
                    job.getDepartment(),
                    ...
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }
    
    private void addJob() {
        JobDialog dialog = new JobDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        if (dialog.showDialog()) {
            try {
                jobService.creerOffre(dialog.getJob());
                loadJobs();
                JOptionPane.showMessageDialog(this, "Offre creee");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage());
            }
        }
    }
}
```

### Utilisation réelle

```java
// Dans MainFrame.java, ligne 30
tabbedPane.addTab("Offres d'emploi", new JobPanel(session));
// ↑ Crée une instance de JobPanel, passe SessionManager au constructeur
```

---

## 7. Classe `LoginFrame.java` - JFrame avec ActionListener

```java
public class LoginFrame extends JFrame {
    // ==================== ATTRIBUTS ====================
    private JTextField champEmail;
    private JPasswordField champMotDePasse;
    private JCheckBox caseSeSouvenirDeMoi;
    private JButton boutonConnexion;
    private JLabel labelErreur;
    private User utilisateurActuel;
    
    // ==================== CONSTRUCTEUR ====================
    public LoginFrame() {
        setTitle("Portail RH - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Crée le UI
        createUI();
        verifierSeSouvenirDeMoi();
    }
    
    private void createUI() {
        // ... création des composants ...
        
        boutonConnexion = new JButton("Se connecter");
        boutonConnexion.addActionListener(new ActionConnexion());  
        // ↑ Classe interne ActionListener
    }
    
    // ==================== CLASSE INTERNE (ActionListener) ====================
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
                    // Afficher écran changement mot de passe
                } else {
                    if (caseSeSouvenirDeMoi.isSelected()) {
                        RememberMeManager.saveCredentials(email, 
                                                         resultat.getTokenAcces(), 
                                                         resultat.getTokenRefresh());
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
}
```

---

## 8. Résumé des constructeurs du projet

| Classe | Type | Constructeur | Utilisation |
|--------|------|--------------|-------------|
| `User` | POJO | Implicite () | `new User()` |
| `Job` | POJO | Explicite () | `new Job()` |
| `Application` | POJO | Implicite () | `new Application()` |
| `Interview` | POJO | Implicite () | `new Interview()` |
| `ApiClient` | Singleton | Privé | `ApiClient.getInstance()` |
| `SessionManager` | Singleton | Privé | `SessionManager.getInstance()` |
| `AuthService` | Service | Public, prend rien | `new AuthService()` |
| `JobService` | Service | Public, prend rien | `new JobService()` |
| `JobPanel` | Composant UI | Public, prend SessionManager | `new JobPanel(session)` |
| `LoginFrame` | Fenêtre UI | Public, prend rien | `new LoginFrame()` |

---

## 9. Différences clés

### POJOs (User, Job, Application, Interview)

```java
public class User {
    private String nom;
    private String email;
    
    // Constructeur implicite OR explicite vide
    public User() {}
    
    // Getters/Setters
}

// Utilisation
User user = new User();
user.setNom("Alice");
System.out.println(user.getNom());
```

**→ On crée autant d'instances qu'on veut**

### Services (AuthService, JobService)

```java
public class JobService {
    private final ApiClient client;
    
    public JobService() {
        this.client = ApiClient.getInstance();  // ← Récupère le Singleton
    }
    
    public List<Job> recupererOffres() { ... }
}

// Utilisation
JobService svc1 = new JobService();
JobService svc2 = new JobService();
// Deux instances, mais elles partagent ApiClient (le singleton)
```

**→ Services peuvent être instanciés plusieurs fois, mais accèdent toujours à la même ApiClient**

### Singletons (ApiClient, SessionManager)

```java
public class ApiClient {
    private static final ApiClient instance = new ApiClient();
    private ApiClient() {}  // Privé
    
    public static ApiClient getInstance() {
        return instance;  // Toujours la même
    }
}

// Utilisation
ApiClient c1 = ApiClient.getInstance();
ApiClient c2 = ApiClient.getInstance();
// c1 == c2  ← TRUE
```

**→ Une seule instance partagée partout**

### Composants Swing (JobPanel, LoginFrame)

```java
public class JobPanel extends JPanel {
    private final SessionManager session;
    
    public JobPanel(SessionManager session) {
        this.session = session;  // ← Injecté par le constructeur
        // Initialisation du UI
    }
}

// Utilisation
SessionManager session = SessionManager.getInstance();
JobPanel panel = new JobPanel(session);  // ← Passe le Singleton
```

**→ Constructeur avec dépendances (Dependency Injection)**

---

## 10. Arbre de création des objets

```
App.main()
  │
  ├─ LoginFrame()                    ← Constructeur public, crée la fenêtre
  │   │
  │   └─ ActionConnexion implements ActionListener
  │       │
  │       └─ AuthService()           ← Crée une instance service
  │           │
  │           └─ ApiClient.getInstance()  ← Récupère le Singleton
  │               │
  │               └─ HttpClient      ← Créé une seule fois dans ApiClient
  │
  │
  ├─ SessionManager.getInstance()    ← Récupère le Singleton
  │   │
  │   └─ login(User)                 ← Sauvegarde l'utilisateur
  │
  │
  └─ MainFrame(SessionManager)       ← Constructeur, passe le Singleton
      │
      ├─ new JobPanel(session)       ← Constructeur, passe SessionManager
      │   │
      │   ├─ new JobService()        ← Crée une instance service
      │   │
      │   └─ JobPanel.loadJobs()     ← Charge les données
      │
      ├─ new ApplicationPanel(session)
      │
      ├─ new InterviewPanel(session)
      │
      └─ new ProfilePanel(session)
```

---

## 11. Question-type à la prof

**"Prof, pourquoi ApiClient est un Singleton et pas un service normal?"**

→ Répondre :
- Parce qu'ApiClient doit être unique pour partager les tokens JWT
- Si on créait plusieurs ApiClient(), chacun aurait ses propres tokens
- Avec Singleton, tous les services utilisent **la même ApiClient** → **les mêmes tokens**
- Code : `ApiClient c1 = new ApiClient(); ApiClient c2 = new ApiClient(); // MAUVAIS`
- Bon : `ApiClient c1 = ApiClient.getInstance(); ApiClient c2 = ApiClient.getInstance(); // c1 == c2`

---

## 12. Hiérarchie de classes (UML simplifié)

```
JFrame
  └─ LoginFrame
      └─ JPanel
          ├─ JobPanel
          ├─ ApplicationPanel
          ├─ InterviewPanel
          └─ ProfilePanel

JPanel
  └─ DashboardPanel

JDialog
  └─ JobDialog

Object
  ├─ User              ← POJO
  ├─ Job               ← POJO
  ├─ Application       ← POJO
  ├─ Interview         ← POJO
  ├─ ApiClient         ← Singleton
  ├─ SessionManager    ← Singleton
  ├─ AuthService       ← Service
  ├─ JobService        ← Service
  └─ RememberMeManager ← Utilitaire
```
