package com.helpinghand.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * AI Chatbot + Issue-Summarizer Servlet
 *
 * Endpoint 1: POST /api/chatbot
 *   Request body:  { "message": "user question here" }
 *   Response body: { "reply": "AI generated answer" }
 *
 * Endpoint 2: POST /api/summarize-issue
 *   Request body:  { "issue": "customer's raw diagnosis / issue text" }
 *   Response body: { "summary": "short, clean summary for the technician" }
 *
 * Uses Anthropic's Claude API (https://api.anthropic.com/v1/messages).
 * Get an API key from https://console.anthropic.com and set it below
 * or as an environment variable ANTHROPIC_API_KEY (recommended).
 */
@WebServlet({"/api/chatbot", "/api/summarize-issue"})
public class ChatbotServlet extends HttpServlet {

    // Prefer reading from environment variable so the key is never committed to GitHub
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    // Context given to the model so it answers only about Helping Hand's services.
    private static final String SYSTEM_PROMPT =
        "You are the AI assistant for 'Helping Hand', a home services booking " +
        "platform (electricians, plumbers, cleaning, appliance repair, etc). " +
        "Answer customer questions about services, pricing, booking process, and " +
        "timings helpfully and briefly. If you don't know a specific detail " +
        "(like an exact price), tell the user to check the Services page or " +
        "contact support. Keep answers short (2-4 sentences), friendly, and in " +
        "the same language the user writes in (Hindi or English).";

    // Context given to the model when summarizing a customer's issue description
    // for the technician who will visit. The technician needs a quick, scannable
    // summary — not a conversation.
    private static final String SUMMARY_SYSTEM_PROMPT =
        "You are helping a home-services company turn a customer's raw problem " +
        "description into a short, clear summary for the technician who will " +
        "visit. The customer may write in Hindi, English, or Hinglish, in a " +
        "messy or rambling way. Read it and produce ONLY a concise technician-" +
        "ready summary in ENGLISH, using this exact structure and nothing else:\n" +
        "Issue: <one short line naming the core problem>\n" +
        "Details: <1-2 short lines with symptoms, when it started, and any " +
        "relevant specifics the customer mentioned (sounds, smells, error codes, " +
        "how long it's been happening, etc.)\n" +
        "Bring: <likely part/tool to carry, only if it can be reasonably inferred; " +
        "otherwise omit this line entirely>\n" +
        "Do not add greetings, explanations, apologies, or any text outside this " +
        "structure. Do not invent details the customer did not mention. If the " +
        "text is too vague to summarize meaningfully, just output: " +
        "\"Issue: <best guess at category>\\nDetails: Customer description too " +
        "vague — please ask for more details on arrival.\"";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (API_KEY == null || API_KEY.isBlank()) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Server missing ANTHROPIC_API_KEY\"}");
            return;
        }

        // Read the raw JSON body once; which field we look for depends on the endpoint.
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        boolean isSummaryRequest = "/api/summarize-issue".equals(req.getServletPath());

        if (isSummaryRequest) {
            handleSummarizeIssue(sb.toString(), resp);
        } else {
            handleChat(sb.toString(), resp);
        }
    }

    private void handleChat(String rawBody, HttpServletResponse resp) throws IOException {
        String userMessage;
        try {
            JsonObject body = JsonParser.parseString(rawBody).getAsJsonObject();
            userMessage = body.get("message").getAsString();
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid request. Expected { message: string }\"}");
            return;
        }

        if (userMessage == null || userMessage.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"message cannot be empty\"}");
            return;
        }

        try {
            String reply = callClaude(userMessage, SYSTEM_PROMPT, 300);
            JsonObject out = new JsonObject();
            out.addProperty("reply", reply);
            resp.getWriter().write(out.toString());
        } catch (Exception e) {
            resp.setStatus(502);
            resp.getWriter().write("{\"error\":\"Chatbot service unavailable, try again.\"}");
            e.printStackTrace();
        }
    }

    // Turns a customer's raw diagnosis / issue text into a short, clean
    // summary that the technician can quickly scan before/at the visit.
    private void handleSummarizeIssue(String rawBody, HttpServletResponse resp) throws IOException {
        String issueText;
        try {
            JsonObject body = JsonParser.parseString(rawBody).getAsJsonObject();
            issueText = body.get("issue").getAsString();
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid request. Expected { issue: string }\"}");
            return;
        }

        if (issueText == null || issueText.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"issue cannot be empty\"}");
            return;
        }

        try {
            String summary = callClaude(issueText, SUMMARY_SYSTEM_PROMPT, 200);
            JsonObject out = new JsonObject();
            out.addProperty("summary", summary);
            resp.getWriter().write(out.toString());
        } catch (Exception e) {
            resp.setStatus(502);
            resp.getWriter().write("{\"error\":\"Summarizer unavailable, try again.\"}");
            e.printStackTrace();
        }
    }

    private String callClaude(String userMessage, String systemPrompt, int maxTokens)
            throws IOException, InterruptedException {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL);
        payload.addProperty("max_tokens", maxTokens);
        payload.addProperty("system", systemPrompt);

        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        payload.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Claude API error: " + response.statusCode() + " " + response.body());
        }

        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray content = responseJson.getAsJsonArray("content");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < content.size(); i++) {
            JsonObject block = content.get(i).getAsJsonObject();
            if ("text".equals(block.get("type").getAsString())) {
                text.append(block.get("text").getAsString());
            }
        }
        return text.toString().trim();
    }
}
