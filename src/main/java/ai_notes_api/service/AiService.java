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

    public String getSummaryFromAi(String noteContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Yapay zekaya verilen talimat
            String prompt = "Lütfen şu notu en fazla 2 cümle ile özetle: " + noteContent;
            String requestBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            // Yapay zekaya soruyu gönderip cevabı alma kısmı
            String response = restTemplate.postForObject(apiUrl + apiKey, request, String.class);

            // Gelen  paketin içinden sadece 'özet metnini' yapiyo
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            // Hatayı arka planda bizim görebilmemiz için ekrana yazdırıyoruz:
            System.out.println("YAPAY ZEKA BAĞLANTI HATASI: " + e.getMessage());
            e.printStackTrace();

            return "Özet oluşturulamadı, sistem meşgul.";
        }
    }
}