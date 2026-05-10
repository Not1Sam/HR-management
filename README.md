# Application de Gestion RH

Application de bureau Swing pour le personnel RH, permettant de gerer les offres d'emploi, les candidatures et les entretiens.

## Technologies utilisees

- **Java** (JDK 17+) - Langage de programmation
- **Swing** - Bibliotheque graphique standard Java pour l'interface utilisateur
- **Maven** - Outil de gestion de projet et de dependances
- **Gson** - Bibliotheque pour parser et serialiser les donnees JSON
- **API REST** - Communication avec le backend via HTTP

## Architecture

```
src/main/java/com/hrmanager/
    App.java                    # Point d'entree de l'application
    api/
        ApiClient.java          # Client HTTP singleton avec authentification
        AuthService.java        # Connexion, deconnexion, changement de mot de passe
        JobService.java         # CRUD des offres d'emploi
        ApplicationService.java # CRUD des candidatures + changement de statut
        InterviewService.java   # CRUD des entretiens + planification
        StatsService.java       # Statistiques du tableau de bord
    model/
        User.java, Job.java, Application.java, Interview.java
    ui/
        LoginFrame.java         # Ecran de connexion
        MainFrame.java          # Fenetre principale avec onglets
        JobPanel.java           # Gestion des offres
        ApplicationPanel.java  # Gestion des candidatures
        InterviewPanel.java     # Gestion des entretiens
        ProfilePanel.java       # Modification du profil
    util/
        SessionManager.java     # Gestion de la session utilisateur
        RememberMeManager.java  # Sauvegarde des identifiants
        ThemeManager.java       # Gestion des couleurs
```

## Compilation et execution

### Methode Rapide (Scripts)

Des scripts sont fournis pour lancer l'application en un clic :
- **Linux / macOS :** `./run.sh`
- **Windows :** Double-cliquez sur `run.bat`

> **Note sur l'historique :** Suite à un problème technique lors de la fusion de nos branches de travail, nous avons dû réinitialiser l'historique du dépôt pour garantir la stabilité de la version finale.

### Avec Maven (Recommande)

```bash
# Se placer dans le dossier du projet
cd HR-management

# Compiler et executer
mvn compile exec:java
```

### Compilation manuelle

Si vous preferez compiler manuellement sans Maven :

**Linux / macOS :**

```bash
# Trouver la bibliotheque GSON
GSON=$(find ~/.m2/repository/com/google/code/gson -name 'gson*.jar' | head -1)

# Compiler tous les fichiers sources
find src/main/java/com/hrmanager -name "*.java" | xargs javac -d target/classes -cp "lib/*:$GSON"

# Executer l'application
java -cp "target/classes:lib/*:$GSON" com.hrmanager.App
```

**Windows (PowerShell) :**

```powershell
# Se placer dans le dossier du projet
cd C:\chemin\vers\HR-management

# Trouver le chemin vers GSON
$GSON = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository\com\google\code\gson" -Recurse -Filter "gson*.jar" | Select-Object -First 1 -ExpandProperty FullName

# Compiler
javac -cp "lib/*;$GSON" -d target/classes src/main/java/com/hrmanager/*.java src/main/java/com/hrmanager/api/*.java src/main/java/com/hrmanager/model/*.java src/main/java/com/hrmanager/ui/*.java src/main/java/com/hrmanager/ui/panels/*.java src/main/java/com/hrmanager/util/*.java

# Executer
java -cp "target/classes;lib/*;$GSON" com.hrmanager.App
```

**Windows (cmd) :**

```cmd
cd C:\chemin\vers\HR-management
set GSON=C:\Users\VOTRE_USER\.m2\repository\com\google\code\gson\gson-2.11.0.jar
javac -cp "lib/*;%GSON%" -d target\classes src\main\java\com\hrmanager\*.java src\main\java\com\hrmanager\api\*.java src\main\java\com\hrmanager\model\*.java src\main\java\com\hrmanager\ui\*.java src\main\java\com\hrmanager\ui\panels\*.java src\main\java\com\hrmanager\util\*.java
java -cp "target\classes;lib/*;%GSON%" com.hrmanager.App
```

## Connexion a l'API

L'application se connecte au backend Django REST a l'adresse :

```
http://84.8.221.29:8001/api
```

Assurez-vous que le serveur backend est en cours d'execution.

## Fonctionnalites

### Authentification
- Connexion par email et mot de passe
- Option "Se souvenir de moi" pour reconnecter automatiquement
- Changement de mot de passe obligatoire pour les nouveaux utilisateurs
- Acces reserve au personnel RH

### Tableau de bord
- Vue d'ensemble des statistiques
- Nombre d'offres actives
- Nombre de candidatures
- Entretiens de la semaine
- Candidats acceptes ce mois

### Gestion des offres d'emploi
- Creation, modification et suppression d'offres
- Filtre par statut (Active, Fermee)
- Consultation des candidatures par offre

### Gestion des candidatures
- Recherche par candidat ou poste
- Filtrage par statut (Envoyee, En cours, Preselectionnee, Entretien, Acceptee, Refusee)
- Consultation du detail et des documents
- Changement de statut avec notes

### Gestion des entretiens
- Planification d'entretiens pour les candidats preselectionnes
- Types : Presentiel, Visioconference, Telephone
- Modification du statut (Planifie, Confirme, Annule, Termine)

### Profil
- Modification des informations personnelles
- Mise a jour du mot de passe

## Structure de l'API

L'application utilise les endpoints suivants :

| Methode | Endpoint                    | Description                |
|---------|-----------------------------|----------------------------|
| POST    | /api/auth/login/            | Connexion                  |
| GET     | /api/users/me/              | Informations utilisateur   |
| POST    | /api/users/change-password/ | Changement de mot de passe |
| GET/POST| /api/jobs/                  | Liste/Creation offres      |
| GET/PUT | /api/jobs/{id}/             | Detail/Maj offre           |
| PATCH   | /api/jobs/{id}/close/       | Fermer une offre           |
| GET     | /api/applications/         | Liste candidatures         |
| PATCH   | /api/applications/{id}/status/ | Changer statut          |
| GET/POST| /api/interviews/           | Liste/Creation entretiens  |
| PATCH   | /api/interviews/{id}/status/| Changer statut entretien  |
| GET     | /api/stats/                | Statistiques               |

## Requirements

- Java JDK 17 ou superieur
- Maven 3.6+ (pour la methode recommandee)
- Acces au serveur backend