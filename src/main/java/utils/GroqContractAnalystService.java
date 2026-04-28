package utils;

import entities.ContractAnalysisResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroqContractAnalystService {

    private static final String SYSTEM_PROMPT =
        "You are an expert AI business analyst specialising in B2B contract risk assessment " +
        "for creative-industry collaboration agreements. " +
        "Analyse the provided collaboration contract proposal data and return ONLY a valid " +
        "JSON object with exactly these four fields — no prose, no markdown code fences:\n" +
        "  \"clarity_score\"              : integer 0-100  (how clear and unambiguous the language is)\n" +
        "  \"budget_realism_score\"       : integer 0-100  (how realistic the budget is relative to scope and timeline)\n" +
        "  \"timeline_feasibility_score\" : integer 0-100  (how achievable the stated timeline is)\n" +
        "  \"flags\"                      : array of strings  (concise issues found, e.g. " +
        "\"Vague payment terms\", \"Budget too low for scope\", \"Contradictory cancellation clauses\", " +
        "\"Insufficient timeline\". Empty array [] if none.)\n" +
        "Return ONLY the raw JSON object — nothing else.";

    /**
     * Performs the analysis.  Safe to call from a background thread.
     *
     * @param title               Contract / request title
     * @param description         Terms / description text
     * @param amount              Budget/amount as a string
     * @param paymentSchedule     Payment schedule text
     * @param startDate           Start date as a formatted string
     * @param endDate             End date as a formatted string
     * @param confidentiality     Confidentiality clause text (may be null)
     * @param cancellationTerms   Cancellation terms text (may be null)
     * @return  A {@link ContractAnalysisResult} — always non-null. Check {@code isSuccess()}.
     */
    public static ContractAnalysisResult analyse(String title,
                                                  String description,
                                                  String amount,
                                                  String paymentSchedule,
                                                  String startDate,
                                                  String endDate,
                                                  String confidentiality,
                                                  String cancellationTerms) {
        try {
            // ── Build the user message ──────────────────────────────────────────────
            String userMessage = "CONTRACT TITLE: "         + nvl(title)              + "\n" +
                                 "TERMS / DESCRIPTION: "   + nvl(description)        + "\n" +
                                 "BUDGET / AMOUNT: "        + nvl(amount)             + "\n" +
                                 "PAYMENT SCHEDULE: "       + nvl(paymentSchedule)    + "\n" +
                                 "START DATE: "             + nvl(startDate)          + "\n" +
                                 "END DATE: "               + nvl(endDate)            + "\n" +
                                 "CONFIDENTIALITY CLAUSE: " + nvl(confidentiality)    + "\n" +
                                 "CANCELLATION TERMS: "     + nvl(cancellationTerms);

            // ── Escape for inline JSON string
            String safeSystem = escapeJson(SYSTEM_PROMPT);
            String safeUser   = escapeJson(userMessage);

            String jsonBody = "{"
                + "\"model\": \"" + GroqConfig.ANALYST_MODEL + "\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"" + safeSystem + "\"},"
                + "  {\"role\": \"user\",   \"content\": \"" + safeUser   + "\"}"
                + "],"
                + "\"temperature\": 0.1"   // Low temp → deterministic JSON output
                + "}";

            // ── HTTP call
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GroqConfig.API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GroqConfig.API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[GroqAnalyst] HTTP " + response.statusCode() + ": " + response.body());
                return new ContractAnalysisResult("API error " + response.statusCode());
            }

            // ── Extract the assistant's content from the chat completion JSON
            String body    = response.body();
            String content = extractContent(body);
            if (content == null || content.isBlank()) {
                return new ContractAnalysisResult("Empty response from API.");
            }

            // Strip markdown code fences if the model added them despite instructions
            content = content.replaceAll("(?s)```(?:json)?\\s*", "").replaceAll("```", "").trim();

            // ── Parse the structured JSON fields
            int clarity    = parseIntField(content, "clarity_score");
            int budget     = parseIntField(content, "budget_realism_score");
            int timeline   = parseIntField(content, "timeline_feasibility_score");
            List<String> flags = parseFlagsField(content);

            return new ContractAnalysisResult(clarity, budget, timeline, flags);

        } catch (Exception e) {
            e.printStackTrace();
            return new ContractAnalysisResult("Analysis failed: " + e.getMessage());
        }
    }

    // ── Parsing helpers

    /** Extracts choices[0].message.content from a Groq/OpenAI chat completion JSON. */
    private static String extractContent(String responseBody) {
        // Match the first "content": "..." value (handles escaped chars)
        Pattern p = Pattern.compile("\"content\":\\s*\"(.*?)\"(?=\\s*[,}])", Pattern.DOTALL);
        Matcher m = p.matcher(responseBody);
        if (m.find()) {
            return m.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\t", "\t");
        }
        return null;
    }

    /** Extracts an integer value for a named JSON field. Returns 50 as a safe fallback. */
    private static int parseIntField(String json, String fieldName) {
        Pattern p = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d{1,3})");
        Matcher m = p.matcher(json);
        if (m.find()) {
            int v = Integer.parseInt(m.group(1));
            return Math.min(100, Math.max(0, v)); // clamp 0-100
        }
        return 50; // neutral fallback
    }

    /** Extracts the string items from a JSON array field named "flags". */
    private static List<String> parseFlagsField(String json) {
        List<String> flags = new ArrayList<>();
        // Find the flags array: "flags": [ ... ]
        Pattern arrayPat = Pattern.compile("\"flags\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
        Matcher arrayMat = arrayPat.matcher(json);
        if (arrayMat.find()) {
            String arrayContent = arrayMat.group(1).trim();
            if (!arrayContent.isEmpty()) {
                // Extract each quoted string item
                Pattern itemPat = Pattern.compile("\"(.*?)\"");
                Matcher itemMat = itemPat.matcher(arrayContent);
                while (itemMat.find()) {
                    String flag = itemMat.group(1).trim();
                    if (!flag.isEmpty()) flags.add(flag);
                }
            }
        }
        return flags;
    }

    // String utilities

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "Not specified" : s.trim();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }
}
