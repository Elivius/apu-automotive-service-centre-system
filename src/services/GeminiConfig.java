package services;

import utils.FileHandler;
import java.util.List;

/**
 * Configuration manager for Google Gemini AI API key.
 * Stores and reads the API key from data/gemini_api_key.txt using FileHandler.
 */
public class GeminiConfig {

    /**
     * Gets the stored API key from data/gemini_api_key.txt using FileHandler.
     *
     * @return the API key or an empty string if not configured.
     */
    static synchronized String getApiKey() {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.GEMINI_API_KEY_FILE);
        if (lines != null && !lines.isEmpty()) {
            return lines.get(0).trim();
        }
        return "";
    }

    /**
     * Checks whether a non-empty API key is configured.
     *
     * @return true if an API key is set
     */
    public static boolean isConfigured() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }
}
