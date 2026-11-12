package si.um.feri.riris.backendfm.engine.activity;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class OpenAiActivityTextExtractorTests {
    private final JsonMapper json=JsonMapper.builder().build();
    @Test void validatesProviderEnvelopeAndRequestWithoutLeakingCredentials() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        var payload=new java.util.concurrent.atomic.AtomicReference<>("{\"title\":\"Študij\",\"date\":\"2026-11-12\",\"startTime\":\"09:00\",\"endTime\":\"11:00\",\"categoryCode\":\"STUDY\",\"placeName\":null}");
        var status=new java.util.concurrent.atomic.AtomicInteger(200);
        server.createContext("/responses",exchange -> {
            var body=json.readTree(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            assertThat(body.path("store").asBoolean()).isFalse();
            assertThat(body.path("instructions").asText()).contains("2026-11-12","Europe/Ljubljana");
            assertThat(body.path("text").path("format").path("strict").asBoolean()).isTrue();
            String response=json.writeValueAsString(Map.of("status","completed","output",List.of(Map.of("content",List.of(Map.of("type","output_text","text",payload.get()))))));
            byte[] bytes=response.getBytes(StandardCharsets.UTF_8); exchange.sendResponseHeaders(status.get(),bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        try {
            var extractor=new OpenAiActivityTextExtractor("test-key","test-model","http://127.0.0.1:"+server.getAddress().getPort()+"/responses");
            assertThat(extractor.extract("Danes študij",LocalDate.of(2026,11,12),ZoneId.of("Europe/Ljubljana"),List.of()).startTime()).isEqualTo("09:00");
            payload.set("not json");
            assertThatThrownBy(() -> extractor.extract("Opis",LocalDate.of(2026,11,12),ZoneId.of("Europe/Ljubljana"),List.of()))
                    .isInstanceOfSatisfying(ResponseStatusException.class,e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
            payload.set("{\"title\":42}");
            assertThatThrownBy(() -> extractor.extract("Opis",LocalDate.of(2026,11,12),ZoneId.of("Europe/Ljubljana"),List.of())).isInstanceOf(ResponseStatusException.class);
            status.set(429);
            assertThatThrownBy(() -> extractor.extract("Opis",LocalDate.of(2026,11,12),ZoneId.of("Europe/Ljubljana"),List.of()))
                    .isInstanceOfSatisfying(ResponseStatusException.class,e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
        } finally { server.stop(0); }
    }
    @Test void missingConfigurationAndUnavailableServiceHaveRecognizableErrors() {
        assertThatThrownBy(() -> new OpenAiActivityTextExtractor("","", "https://api.openai.com/v1/responses")
                .extract("Opis",LocalDate.now(),ZoneId.of("Europe/Ljubljana"),List.of()))
                .isInstanceOfSatisfying(ResponseStatusException.class,e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
        assertThatThrownBy(() -> new OpenAiActivityTextExtractor("test","test", "http://127.0.0.1:1/responses")
                .extract("Opis",LocalDate.now(),ZoneId.of("Europe/Ljubljana"),List.of()))
                .isInstanceOfSatisfying(ResponseStatusException.class,e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
    }
}
