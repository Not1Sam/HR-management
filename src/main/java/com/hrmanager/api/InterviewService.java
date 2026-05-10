package com.hrmanager.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrmanager.model.Interview;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InterviewService {
    private static final Gson gson = new Gson();
    private final ApiClient client;

    public InterviewService() {
        this.client = ApiClient.getInstance();
    }

    private List<Interview> analyserEntretiens(String corpsReponse) {
        List<Interview> listeEntretiens = new ArrayList<>();
        try {
            JsonObject json = JsonParser.parseString(corpsReponse).getAsJsonObject();
            JsonArray tableau = json.getAsJsonArray("results");
            if (tableau != null) {
                for (int i = 0; i < tableau.size(); i++) {
                    listeEntretiens.add(gson.fromJson(tableau.get(i), Interview.class));
                }
            }
        } catch (Exception e) {
            try {
                JsonArray tableau = JsonParser.parseString(corpsReponse).getAsJsonArray();
                for (int i = 0; i < tableau.size(); i++) {
                    listeEntretiens.add(gson.fromJson(tableau.get(i), Interview.class));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return listeEntretiens;
    }

    public List<Interview> recupererEntretiens() throws Exception {
        HttpRequest requete = client.request("/interviews/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());

        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation des entretiens");
        }

        return analyserEntretiens(reponse.body());
    }

    public Interview recupererEntretien(int id) throws Exception {
        HttpRequest requete = client.request("/interviews/" + id + "/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation de l'entretien");
        }
        return gson.fromJson(reponse.body(), Interview.class);
    }

    public Interview creerEntretien(Interview entretien) throws Exception {
        String donnees = gson.toJson(entretien);
        HttpRequest requete = client.request("/interviews/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 201) {
            throw new Exception("Erreur lors de la creation de l'entretien");
        }
        return gson.fromJson(reponse.body(), Interview.class);
    }

    public Interview modifierEntretien(int id, Interview entretien) throws Exception {
        String donnees = gson.toJson(entretien);
        HttpRequest requete = client.request("/interviews/" + id + "/")
                .method("PUT", HttpRequest.BodyPublishers.ofString(donnees, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la mise a jour de l'entretien");
        }
        return gson.fromJson(reponse.body(), Interview.class);
    }

    public void supprimerEntretien(int id) throws Exception {
        HttpRequest requete = client.request("/interviews/" + id + "/").DELETE().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 204 && reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la suppression de l'entretien");
        }
    }
}