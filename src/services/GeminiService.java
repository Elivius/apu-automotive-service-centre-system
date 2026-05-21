package services;

import models.Appointment;
import models.User;
import models.Technician;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Central service class for all Gemini AI interactions.
 * Uses Java 11's HttpClient to call the Gemini REST API with fallback support.
 */
public class GeminiService {

    /**
     * Helper to escape special characters for JSON payloads.
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Parses the response from Gemini to extract text.
     */
    private static String parseGeminiResponse(String json) {
        if (json == null) return "Error: Empty response from AI.";
        
        int textIndex = json.indexOf("\"text\"");
        if (textIndex == -1) {
            return "Error: Could not extract response from AI. Response was:\n" + json;
        }
        
        int colonIndex = json.indexOf(":", textIndex);
        if (colonIndex == -1) return "Error: Invalid response format.";
        
        int openQuote = json.indexOf("\"", colonIndex);
        if (openQuote == -1) return "Error: Invalid response format.";
        
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = openQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    default: sb.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Simple, zero-dependency helper to convert basic Markdown to styled HTML for Swing components.
     */
    public static String markdownToHtml(String markdown) {
        if (markdown == null) return "";

        // 1. Escape basic HTML tags in raw text to avoid rendering bugs
        String escaped = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");

        // 2. Process line by line to prevent cross-matching
        String[] lines = escaped.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("###")) {
                String content = trimmed.substring(3).trim();
                content = replaceBold(content);
                sb.append("<h3 style='color: #10B981; font-family: SansSerif; margin: 12px 0 4px 0;'>").append(content).append("</h3>");
            } else if (trimmed.startsWith("##")) {
                String content = trimmed.substring(2).trim();
                content = replaceBold(content);
                sb.append("<h2 style='color: #38BDF8; font-family: SansSerif; margin: 16px 0 6px 0;'>").append(content).append("</h2>");
            } else if (trimmed.startsWith("#")) {
                String content = trimmed.substring(1).trim();
                content = replaceBold(content);
                sb.append("<h1 style='color: #7C6BFF; font-family: SansSerif; margin: 20px 0 8px 0;'>").append(content).append("</h1>");
            } else if (trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]")) {
                String content = trimmed.substring(5).trim();
                content = replaceBold(content);
                sb.append("<div style='margin-left: 15px; text-indent: -10px; margin-bottom: 3px;'>☐ ").append(content).append("</div>");
            } else if (trimmed.startsWith("- [x]") || trimmed.startsWith("* [x]") || trimmed.startsWith("- [X]") || trimmed.startsWith("* [X]")) {
                String content = trimmed.substring(5).trim();
                content = replaceBold(content);
                sb.append("<div style='margin-left: 15px; text-indent: -10px; margin-bottom: 3px;'>☑ ").append(content).append("</div>");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.equals("-") || trimmed.equals("*")) {
                String content = trimmed.substring(1).trim();
                content = replaceBold(content);
                sb.append("<div style='margin-left: 15px; text-indent: -10px; margin-bottom: 3px;'>• ").append(content).append("</div>");
            } else if (trimmed.isEmpty()) {
                sb.append("<br/>");
            } else {
                String content = replaceBold(line);
                sb.append(content).append("<br/>");
            }
        }

        // 3. Wrap in a styled body block matching UITheme colors
        return "<html><body style='font-family: SansSerif; font-size: 11px; color: #EAEAEA; margin: 8px;'>"
                + sb
                + "</body></html>";
    }

    private static String replaceBold(String text) {
        return text.replaceAll("\\*\\*\\s*(.*?)\\s*\\*\\*", "<b>$1</b>");
    }

    /**
     * Centralized private method to make calls to Gemini API with robust retries and model fallbacks.
     */
    private static String callGeminiAPI(String prompt) {
        String apiKey = GeminiConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: Gemini API Key is not configured. Please set the key in the data/gemini_api_key.txt file first.";
        }

        // Try user preferred model first, fall back to stable fast models if needed
        String[] models = {"gemini-3.1-flash-lite", "gemini-3.5-flash", "gemini-2.5-flash-lite"};
        String lastError = "";

        for (String model : models) {
            try {
                String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
                String escapedPrompt = escapeJson(prompt);
                String payload = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlStr))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .timeout(Duration.ofSeconds(20))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    return parseGeminiResponse(response.body());
                } else {
                    lastError = "HTTP " + statusCode + ": " + response.body();
                }
            } catch (Exception e) {
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
        }
        return "Error calling Gemini API (" + lastError + "). Please verify your internet connection and API key configuration.";
    }

    /**
     * Analyzes customer symptoms to recommend a service type and possible issues.
     */
    public static String analyzeSymptoms(String customerComments) {
        if (customerComments == null || customerComments.trim().isEmpty()) {
            return "Please provide comments detailing the car's symptoms before request diagnosis.";
        }

        String prompt = "You are Kelwin, an expert car mechanic/advisor at an APU automotive service centre.\n" +
                "Analyze the following customer's description of their car issue:\n" +
                "\"" + customerComments + "\"\n\n" +
                "Please provide a response in the following format:\n" +
                "### Possible Issues\n" +
                "[List 2-3 potential causes in simple, easy-to-understand terms]\n\n" +
                "### Recommended Service Type\n" +
                "[Specify exactly \"Normal\" or \"Major\" service and explain why]\n\n" +
                "### Safety Advisory\n" +
                "[Give a short advice on safety, e.g., if it's safe to drive, or if they should stop immediately]";

        return callGeminiAPI(prompt);
    }

    /**
     * Generates a diagnostic checklist for the technician based on symptoms and service type.
     */
    public static String generateDiagnosticChecklist(String serviceType, String customerComments) {
        String comments = (customerComments == null || customerComments.trim().isEmpty()) ? "None provided" : customerComments;
        
        String prompt = "You are Kelwin, an expert automotive technician at APU.\n" +
                "You are tasked with diagnosing/servicing a vehicle.\n" +
                "Service Type: " + serviceType + "\n" +
                "Customer Comments: " + comments + "\n\n" +
                "Please generate a step-by-step diagnostic and service checklist for the technician.\n" +
                "Make it highly professional, structured, and tailored to the symptoms.\n" +
                "Format with markdown checkboxes like:\n" +
                "- [ ] Inspect...\n" +
                "- [ ] Test...";

        return callGeminiAPI(prompt);
    }

    /**
     * Polishes the technician's raw feedback to make it look professional and customer-friendly.
     */
    public static String polishFeedback(String rawFeedback) {
        if (rawFeedback == null || rawFeedback.trim().isEmpty()) {
            return "Please provide feedback text before attempting to polish.";
        }

        String prompt = "You are Kelwin, a professional service manager at an automotive service centre.\n" +
                "A technician has written the following raw feedback for a customer's car service:\n" +
                "\"" + rawFeedback + "\"\n\n" +
                "Please polish this feedback to make it sound highly professional, polite, grammatically correct, and clear for the customer, while retaining all technical facts. Keep it concise, within a single paragraph if possible.";

        return callGeminiAPI(prompt);
    }

    /**
     * Match the best technician for a job based on customer comments and available tech list.
     */
    public static String matchTechnician(String customerComments, List<Technician> technicians) {
        String comments = (customerComments == null || customerComments.trim().isEmpty()) ? "General service / no comments" : customerComments;
        
        StringBuilder techList = new StringBuilder();
        for (Technician t : technicians) {
            techList.append("- ID: ").append(t.getUserId())
                    .append(", Name: ").append(t.getName())
                    .append(", Specialization: ").append(t.getSpecialization())
                    .append("\n");
        }

        String prompt = "You are Kelwin, a service coordinator at an automotive service centre.\n" +
                "An appointment has the following customer comments:\n" +
                "\"" + comments + "\"\n\n" +
                "We have the following technicians available:\n" +
                techList + "\n" +
                "Match the best technician for the job. Give their ID, Name, and a clear, brief explanation of why they are the best fit. If multiple technicians are suitable or specialized, explain your reasoning.";

        return callGeminiAPI(prompt);
    }

    /**
     * Analyzes feedback sentiment across appointments to provide insights to the manager.
     */
    public static String analyzeSentiment(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return "No appointments found to analyze sentiment.";
        }

        StringBuilder data = new StringBuilder();
        int count = 0;
        for (Appointment apt : appointments) {
            String c = apt.getComments() != null ? apt.getComments() : "";
            String f = apt.getFeedback() != null ? apt.getFeedback() : "";
            String r = apt.getServiceReview() != null ? apt.getServiceReview() : "";
            if (!c.isEmpty() || !f.isEmpty() || !r.isEmpty()) {
                count++;
                data.append("Appointment ID: ").append(apt.getAppointmentId()).append("\n")
                    .append("  Customer Comments: ").append(c).append("\n")
                    .append("  Tech Feedback: ").append(f).append("\n")
                    .append("  Customer Review: ").append(r).append("\n\n");
                if (count >= 1000) { // Limit input to avoid token overflow
                    break;
                }
            }
        }

        if (count == 0) {
            return "No feedback or reviews are currently available for sentiment analysis.";
        }

        String prompt = "You are Kelwin, a senior business analyst for an automotive service centre.\n" +
                "Analyze the following list of customer appointments, their comments, feedback, and reviews:\n\n" +
                data + "\n" +
                "Please provide a summary report with the following format:\n" +
                "### Overall Sentiment\n" +
                "[Provide a summary of the sentiment: Positive/Neutral/Negative percentage or analysis]\n\n" +
                "### Key Themes\n" +
                "[List main praise points and common complaints]\n\n" +
                "### Actionable Insights\n" +
                "[Provide 3 concrete, realistic suggestions for improving service quality or customer satisfaction]";

        return callGeminiAPI(prompt);
    }
}
