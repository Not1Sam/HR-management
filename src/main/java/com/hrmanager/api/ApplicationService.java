package com.hrmanager.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrmanager.model.Application;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService {
    private static final Gson gson = new Gson();
    private final ApiClient client;

    public ApplicationService() {
        this.client = ApiClient.getInstance();
    }

    private List<Application> analyserCandidatures(String corpsReponse) {
        List<Application> listeCandidatures = new ArrayList<>();
        try {
            JsonObject json = JsonParser.parseString(corpsReponse).getAsJsonObject();
            JsonArray tableau = json.getAsJsonArray("results");
            if (tableau != null) {
                for (int i = 0; i < tableau.size(); i++) {
                    listeCandidatures.add(gson.fromJson(tableau.get(i), Application.class));
                }
            }
        } catch (Exception e) {
            try {
                JsonArray tableau = JsonParser.parseString(corpsReponse).getAsJsonArray();
                for (int i = 0; i < tableau.size(); i++) {
                    listeCandidatures.add(gson.fromJson(tableau.get(i), Application.class));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return listeCandidatures;
    }

    public List<Application> recupererCandidatures() throws Exception {
        HttpRequest requete = client.request("/applications/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation des candidatures");
        }

        return analyserCandidatures(reponse.body());
    }

    public Application recupererCandidature(int id) throws Exception {
        HttpRequest requete = client.request("/applications/" + id + "/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation de la candidature");
        }
        return gson.fromJson(reponse.body(), Application.class);
    }

    public Application creerCandidature(Application candidature) throws Exception {
        String donnees = gson.toJson(candidature);
        HttpRequest requete = client.request("/applications/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 201) {
            throw new Exception("Erreur lors de la creation de la candidature");
        }
        return gson.fromJson(reponse.body(), Application.class);
    }

    public Application modifierCandidature(int id, Application candidature) throws Exception {
        String donnees = gson.toJson(candidature);
        HttpRequest requete = client.request("/applications/" + id + "/")
                .method("PUT", HttpRequest.BodyPublishers.ofString(donnees, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la mise a jour de la candidature");
        }
        return gson.fromJson(reponse.body(), Application.class);
    }

    public void supprimerCandidature(int id) throws Exception {
        HttpRequest requete = client.request("/applications/" + id + "/").DELETE().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 204 && reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la suppression de la candidature");
        }
    }

    public Application changerStatut(int id, String statut, String notes) throws Exception {
        java.util.Map<String, String> donnees = new java.util.HashMap<>();
        donnees.put("status", statut);
        if (notes != null) {
            donnees.put("notes", notes);
        }
        String payload = gson.toJson(donnees);
        HttpRequest requete = client.request("/applications/" + id + "/status/")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la mise a jour du statut");
        }
        return gson.fromJson(reponse.body(), Application.class);
    }
}