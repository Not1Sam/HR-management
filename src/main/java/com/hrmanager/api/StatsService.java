package com.hrmanager.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class StatsService {
    private static final Gson gson = new Gson();
    private final ApiClient client;

    public StatsService() {
        this.client = ApiClient.getInstance();
    }

    public StatistiquesTableauBord recupererStatistiques() throws Exception {
        long horodatage = System.currentTimeMillis();
        HttpRequest requete = client.request("/stats/?t=" + horodatage).GET().build();
        HttpResponse<String> reponse = client.getClient().send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new Exception("Erreur lors de la recuperation des statistiques");
        }
        JsonObject json = JsonParser.parseString(reponse.body()).getAsJsonObject();
        StatistiquesTableauBord stats = new StatistiquesTableauBord();
        if (json.has("overview")) {
            stats.resume = gson.fromJson(json.get("overview"), ResumeStatistiques.class);
        }
        if (json.has("applicationsByMonth")) {
            JsonArray tableau = json.getAsJsonArray("applicationsByMonth");
            stats.candidaturesParMois = new ArrayList<>();
            for (int i = 0; i < tableau.size(); i++) {
                stats.candidaturesParMois.add(gson.fromJson(tableau.get(i), CandidatureMensuelle.class));
            }
        }
        if (json.has("applicationsByStatus")) {
            JsonArray tableau = json.getAsJsonArray("applicationsByStatus");
            stats.candidaturesParStatut = new ArrayList<>();
            for (int i = 0; i < tableau.size(); i++) {
                stats.candidaturesParStatut.add(gson.fromJson(tableau.get(i), CandidatureStatut.class));
            }
        }
        if (json.has("applicationsByJob")) {
            JsonArray tableau = json.getAsJsonArray("applicationsByJob");
            stats.candidaturesParPoste = new ArrayList<>();
            for (int i = 0; i < tableau.size(); i++) {
                stats.candidaturesParPoste.add(gson.fromJson(tableau.get(i), TopPoste.class));
            }
        }
        return stats;
    }

    public static class StatistiquesTableauBord {
        public ResumeStatistiques resume;
        public List<CandidatureMensuelle> candidaturesParMois;
        public List<CandidatureStatut> candidaturesParStatut;
        public List<TopPoste> candidaturesParPoste;
    }

    public static class ResumeStatistiques {
        public int totalJobs;
        public int activeJobs;
        public int totalApplications;
        public int pendingReview;
        public int interviewsThisWeek;
        public int acceptedThisMonth;
        public int rejectedThisMonth;
        public String conversionRate;
    }

    public static class CandidatureMensuelle {
        public String month;
        public int count;
    }

    public static class CandidatureStatut {
        public String name;
        public int value;
        public String status_key;
    }

    public static class TopPoste {
        public String job;
        public int count;
    }
}