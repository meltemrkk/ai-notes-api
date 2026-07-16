package ai_notes_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private String cleanTextForJson(String text) {
        if (text == null) return "";

        String safeText = text.length() > 4000 ? text.substring(0, 4000) : text;

        // JSON formatını bozacak enter boşluklarını ve tırnak işaretlerini temizliyoruz
        return safeText.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    public String getSummaryFromAi(String noteContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // İstek atmadan önce metni temizle
            String cleanContent = cleanTextForJson(noteContent);
            String prompt = "Lütfen şu metni en fazla 2 cümle ile özetle: " + cleanContent;
            String requestBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(apiUrl + apiKey, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            System.out.println("YAPAY ZEKA ÖZET HATASI: " + e.getMessage());
            return "Özet oluşturulamadı, sistem meşgul.";
        }
    }

    public String getTitleFromAi(String noteContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // İstek atmadan önce metni temizle
            String cleanContent = cleanTextForJson(noteContent);
            String prompt = "Aşağıdaki metin için en fazla 3-4 kelimelik, konuyu özetleyen çarpıcı bir başlık üret. Sadece başlığı yaz: " + cleanContent;
            String requestBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(apiUrl + apiKey, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().trim();

        } catch (Exception e) {
            System.out.println("YAPAY ZEKA BAŞLIK HATASI: " + e.getMessage());
            return "İsimsiz Not";
        }
    }
}