package com.hrmanager.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class RememberMeManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".hrdesktop";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "auth.properties";
    
    public static void saveCredentials(String email, String accessToken, String refreshToken) {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            Properties props = new Properties();
            props.setProperty("email", email);
            props.setProperty("accessToken", accessToken);
            props.setProperty("refreshToken", refreshToken);
            
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
            if (!file.exists()) return null;
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            }
            
            String email = props.getProperty("email");
            String accessToken = props.getProperty("accessToken");
            String refreshToken = props.getProperty("refreshToken");
            
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
            Files.deleteIfExists(Paths.get(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static class Credentials {
        private final String email;
        private final String accessToken;
        private final String refreshToken;
        
        public Credentials(String email, String accessToken, String refreshToken) {
            this.email = email;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
        
        public String getEmail() { return email; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}