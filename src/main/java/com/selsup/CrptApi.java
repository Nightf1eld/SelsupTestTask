package com.selsup;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class CrptApi {

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private static final AtomicInteger requestCounter = new AtomicInteger();
    private Date leftTime = new Date();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    @SneakyThrows
    public synchronized void createDocument(Document doc, String sign) {

        long currentTime = System.currentTimeMillis();
        long passedTime = currentTime - leftTime.getTime();

        if (passedTime >= timeUnit.toMillis(1)) {
            requestCounter.set(0);
            leftTime = new Date(currentTime);
        }

        while (requestCounter.get() >= getRequestLimit()) {
            wait(getTimeUnit().toMillis(1) - passedTime);
            currentTime = System.currentTimeMillis();
            passedTime = currentTime - leftTime.getTime();

            if (passedTime >= timeUnit.toMillis(1)) {
                requestCounter.set(0);
                leftTime = new Date(currentTime);
            }
        }

        String jsonString = new ObjectMapper().writeValueAsString(doc);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .headers("Content-Type", "application/json", "Signature", sign)
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        client.send(request, HttpResponse.BodyHandlers.discarding());

        requestCounter.incrementAndGet();
    }

    @Getter
    @Setter
    @Builder
    static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;
    }

    @Getter
    @Setter
    @Builder
    static class Description {
        private String participantInn;
    }

    @Getter
    @Setter
    @Builder
    static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        CrptApi.Document document = Document.builder()
                .description(Description.builder().participantInn("string").build())
                .doc_id("string")
                .doc_status("string")
                .doc_type("LP_INTRODUCE_GOODS")
                .importRequest(true)
                .owner_inn("owner_inn")
                .participant_inn("string")
                .producer_inn("string")
                .production_date("2020-01-23")
                .production_type("string")
                .products(Arrays.asList(
                                Product.builder()
                                        .certificate_document("string")
                                        .certificate_document_date("2020-01-23")
                                        .certificate_document_number("string")
                                        .owner_inn("string")
                                        .producer_inn("string")
                                        .production_date("string")
                                        .tnved_code("string")
                                        .uit_code("string")
                                        .uitu_code("string")
                                        .build()
                        )
                )
                .reg_date("2020-01-23")
                .reg_number("string")
                .build();

                crptApi.createDocument(document, "signature");
    }
}