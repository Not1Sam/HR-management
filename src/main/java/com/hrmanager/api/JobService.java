package com.hrmanager.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrmanager.model.Job;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JobService {
    private static final Gson gson = new Gson();
    private final ApiClient client;

    public JobService() {
        this.client = ApiClient.getInstance();
    }

    private List<Job> analyserReponse(String corpsReponse) {
        List<Job> listeOffres = new ArrayList<>();
        try {
            JsonObject json = JsonParser.parseString(corpsReponse).getAsJsonObject();
            JsonArray tableau = json.getAsJsonArray("results");
            if (tableau != null) {
                for (int i = 0; i < tableau.size(); i++) {
                    listeOffres.add(gson.fromJson(tableau.get(i), Job.class));
                }
            }
        } catch (Exception e) {
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

    public List<Job> recupererOffres() throws Exception {
        long horodatage = System.currentTimeMillis();
        HttpRequest requete = client.request("/jobs/?t=" + horodatage).GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation des offres");
        }
        return analyserReponse(reponse.body());
    }

    public Job recupererOffre(int id) throws Exception {
        HttpRequest requete = client.request("/jobs/" + id + "/").GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation de l'offre");
        }
        return gson.fromJson(reponse.body(), Job.class);
    }

    public Job creerOffre(Job offre) throws Exception {
        JsonObject donnees = new JsonObject();
        donnees.addProperty("title", offre.getTitle());
        donnees.addProperty("department", offre.getDepartment());
        donnees.addProperty("location", offre.getLocation());
        donnees.addProperty("type", offre.getType());
        donnees.addProperty("experienceLevel", offre.getExperienceLevel());
        donnees.addProperty("status", offre.getStatus() != null ? offre.getStatus() : "ACTIVE");
        donnees.addProperty("description", offre.getDescription());
        
        if (offre.getSalary() != null && !offre.getSalary().trim().isEmpty()) {
            donnees.addProperty("salary", offre.getSalary());
        }
        if (offre.getDeadline() != null && !offre.getDeadline().trim().isEmpty()) {
            donnees.addProperty("deadline", offre.getDeadline());
        }
        
        HttpRequest requete = client.request("/jobs/")
                .POST(HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 201) {
            throw new Exception("Erreur lors de la creation: " + reponse.body());
        }
        return gson.fromJson(reponse.body(), Job.class);
    }

    public Job modifierOffre(int id, Job offre) throws Exception {
        JsonObject donnees = new JsonObject();
        donnees.addProperty("title", offre.getTitle());
        donnees.addProperty("department", offre.getDepartment());
        donnees.addProperty("location", offre.getLocation());
        donnees.addProperty("type", offre.getType());
        donnees.addProperty("experienceLevel", offre.getExperienceLevel());
        donnees.addProperty("status", offre.getStatus() != null ? offre.getStatus() : "ACTIVE");
        
        if (offre.getSalary() != null && !offre.getSalary().trim().isEmpty()) {
            donnees.addProperty("salary", offre.getSalary());
        }
        if (offre.getDescription() != null && !offre.getDescription().isEmpty()) {
            donnees.addProperty("description", offre.getDescription());
        }
        if (offre.getDeadline() != null && !offre.getDeadline().trim().isEmpty()) {
            donnees.addProperty("deadline", offre.getDeadline());
        }
        
        HttpRequest requete = client.request("/jobs/" + id + "/")
                .method("PUT", HttpRequest.BodyPublishers.ofString(donnees.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la mise a jour (" + reponse.statusCode() + "): " + reponse.body());
        }
        return gson.fromJson(reponse.body(), Job.class);
    }

    public void supprimerOffre(int id) throws Exception {
        HttpRequest requete = client.request("/jobs/" + id + "/").DELETE().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 204 && reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la suppression");
        }
    }

    public Job fermerOffre(int id) throws Exception {
        HttpRequest requete = client.request("/jobs/" + id + "/close/")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la fermeture de l'offre");
        }
        return gson.fromJson(reponse.body(), Job.class);
    }
}