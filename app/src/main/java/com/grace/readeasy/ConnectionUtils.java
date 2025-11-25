package com.grace.readeasy;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionUtils {
    private static final String TAG = "ConnectionUtils";
    public interface WebsiteContentCallback {
        void onSuccess(String content);
        void onError(String errorMessage);
    }

    public static void fetchWebsiteContent(String urlString, WebsiteContentCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // 15 seconds
                connection.setReadTimeout(10000);    // 10 seconds

                // Set request headers to mimic a browser
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    // Parse HTML and extract meaningful text
                    String parsedContent = parseHtmlContent(content.toString());
                    callback.onSuccess(parsedContent);

                } else {
                    callback.onError("HTTP Error: " + responseCode);
                }

            } catch (IOException e) {
                Log.e(TAG, "Error fetching website: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }).start();
    }

    private static String parseHtmlContent(String htmlContent) {
        // Remove HTML tags and extract text content
        String textOnly = htmlContent
                .replaceAll("<script[^>]*>.*?</script>", "")  // Remove scripts
                .replaceAll("<style[^>]*>.*?</style>", "")    // Remove styles
                .replaceAll("<[^>]*>", " ")                   // Remove all HTML tags
                .replaceAll("&nbsp;", " ")                    // Replace HTML entities
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s+", " ")                      // Collapse multiple spaces
                .trim();

        // Extract first 1000 characters or meaningful content
        return extractRelevantContent(textOnly);
    }

    private static String extractRelevantContent(String fullText) {
        // Look for dyslexia-related content
        String[] keywords = {"dyslexia", "reading", "learning", "disorder", "difficulty"};
        String lowerText = fullText.toLowerCase();

        // Find the most relevant section
        for (String keyword : keywords) {
            int index = lowerText.indexOf(keyword);
            if (index != -1) {
                // Extract content around the keyword
                int start = Math.max(0, index - 200);
                int end = Math.min(fullText.length(), index + 800);
                return fullText.substring(start, end).trim();
            }
        }

        // Fallback: return first 1000 characters
        return fullText.length() > 1000 ? fullText.substring(0, 1000) + "..." : fullText;
    }
}

