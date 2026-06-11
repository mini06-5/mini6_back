package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.AiRecommendation;
import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.repository.AiRecommendationRepository;
import com.aivle.bookapp.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRecommendationService implements ApplicationRunner {
    private final BookRepository bookRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    @Value("${openai.api.key}")
    private String apiKey;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🔍 [서버 기동 시스템] DB에 이달의 AI 추천 데이터가 존재하는지 검사합니다...");

        AiRecommendation rec = aiRecommendationRepository.findById(1L).orElse(null);

        if (rec == null || rec.getRecommendedBook() == null) {
            System.out.println("⚠️ [안전장치 발동] 추천 데이터가 비어있습니다! 즉시 OpenAI를 호출하여 데이터 생성을 시작합니다.");
            updateAiRecommendation();
        } else {
            System.out.println("✅ [검사 완료] 이번 달 추천 데이터가 이미 DB에 안전하게 캐싱되어 있습니다.");
        }
    }

    public Map<String, Object> getCachedRecommendation() {
        AiRecommendation rec = aiRecommendationRepository.findById(1L).orElse(null);
        if (rec == null || rec.getRecommendedBook() == null) {
            return Collections.emptyMap();
        }

        Book book = rec.getRecommendedBook();

        Map<String, Object> result = new HashMap<>();
        result.put("id", book.getId());
        result.put("title", book.getTitle());
        result.put("content", book.getContent());
        result.put("author", book.getAuthor());
        result.put("publisher", book.getPublisher());
        result.put("coverImageUrl", book.getCoverImageUrl());
        result.put("reason", rec.getReason());

        return result;
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    @Transactional
    public void updateAiRecommendation() {

        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            return;
        }

        // 데이터 가공
        List<Map<String, Object>> simplifiedBooks = books.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("title", b.getTitle());
            map.put("content", b.getContent());
            return map;
        }).collect(Collectors.toList());

        int currentMonth = LocalDate.now().getMonthValue();

        String prompt = String.format("""
                이번달은 %d달이야
                다음은 우리 도서관의 책 목록이야:
                %s
                
                'reason' 문장은 책방의 다정한 북 큐레이터나 사서처럼 감성적이고 
                친절한 존댓말 말투(~해보세요, ~를 추천해 드립니다! 등)로 한 두문장으로 정성스럽게 작성해줘.
                이 중에서 이번 %d월의 계절감이나 분위기와 가장 잘 어울리는 추천작을 하나 골라줘.
                결과는 반드시 아래와 같은 순수 JSON 형태로만 응답해. 백틱(```)이나 다른 설명은 절대 넣지 마.
                추천 이유는 
                {"recommendedId": 숫자, "reason": "추천 이유"}
                """, currentMonth, simplifiedBooks.toString(), currentMonth);

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            System.out.println("💬 [통신] OpenAI API 호출 중...");

            Map<String, Object> responseMap = restClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String aiJsonContent = (String) message.get("content");

            Long recommendedBookId = null;
            String reason = "";

            String cleanJson = aiJsonContent.trim().replace("{", "").replace("}", "");

            String[] parts = cleanJson.split(",\\s*\"reason\"\\s*:\\s*");
            if (parts.length >= 2) {
                String idPart = parts[0].replaceAll("[^0-9]", "");
                recommendedBookId = Long.parseLong(idPart);

                reason = parts[1].trim();
                if (reason.startsWith("\"") && reason.endsWith("\"")) {
                    reason = reason.substring(1, reason.length() - 1);
                }
            }
            Book targetBook = bookRepository.findById(recommendedBookId)
                    .orElseThrow(() -> new RuntimeException("AI가 추천한 도서가 DB에 없습니다. ID: "));

            if (reason.isEmpty()) reason = currentMonth + "월에 어울리는 추천 도서입니다.";

            AiRecommendation newRec = AiRecommendation.builder()
                    .id(1L)
                    .recommendedBook(targetBook)
                    .reason(reason)
                    .build();

            aiRecommendationRepository.save(newRec);


        } catch (Exception e) {
            throw new RuntimeException("AI 추천 서비스 실패", e);
        }
    }
}
