package com.hrmanager.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrmanager.model.User;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AuthService {
    private static final Gson gson = new Gson();
    private final ApiClient client;

    public AuthService() {
        this.client = ApiClient.getInstance();
    }

    public ResultatAuth connexion(String email, String motDePasse) throws Exception {
        JsonObject donnees = new JsonObject();
        donnees.addProperty("email", email);
        donnees.addProperty("password", motDePasse);

        HttpRequest requete = client.request("/auth/login/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() == 404) {
            throw new Exception("Email non enregistre. Veuillez creer un compte.");
        }
        if (reponse.statusCode() == 401) {
            throw new Exception("Mot de passe incorrect.");
        }
        if (reponse.statusCode() != 200) {
            JsonObject erreur = JsonParser.parseString(reponse.body()).getAsJsonObject();
            throw new Exception(erreur.has("detail") ? erreur.get("detail").getAsString() : "Erreur de connexion");
        }

        JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
        String tokenAcces = json.get("access").getAsString();
        String tokenRefresh = json.get("refresh").getAsString();
        User utilisateur = gson.fromJson(json.get("user"), User.class);

        if (!"HR".equalsIgnoreCase(utilisateur.getRole())) {
            throw new Exception("Acces refuse. Cette application est reservee au personnel RH.");
        }

        client.setTokens(tokenAcces, tokenRefresh);
        return new ResultatAuth(utilisateur, tokenAcces, tokenRefresh);
    }

    public void changerMotDePasse(String nouveauMotDePasse) throws Exception {
        JsonObject donnees = new JsonObject();
        donnees.addProperty("password", nouveauMotDePasse);

        HttpRequest requete = client.request("/users/change-password/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() != 200) {
            JsonObject erreur = JsonParser.parseString(reponse.body()).getAsJsonObject();
            throw new Exception(erreur.has("password") ? erreur.get("password").getAsString() : "Erreur de changement de mot de passe");
        }
    }

    public User recupererUtilisateur() throws Exception {
        HttpRequest requete = client.request("/users/me/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation du profil");
        }
        return gson.fromJson(reponse.body(), User.class);
    }

    public boolean rafraichirToken() {
        try {
            JsonObject donnees = new JsonObject();
            donnees.addProperty("refresh", client.getRefreshToken());

            HttpRequest requete = client.request("/auth/token/refresh/")
                    .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
            if (reponse.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
                String nouveauToken = json.get("access").getAsString();
                client.setTokens(nouveauToken, client.getRefreshToken());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deconnexion() {
        client.clearTokens();
    }

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