package co.obraplan.agents.ai;

import co.obraplan.HttpError;
import co.obraplan.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

public final class GeminiClient implements ModelClient {
    private final String key;
    private final String model;
    private final HttpClient http =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public GeminiClient() {
        key = System.getenv().getOrDefault("GEMINI_API_KEY", "").trim();
        model = System.getenv().getOrDefault("OBRAPLAN_GEMINI_MODEL", "gemini-3.5-flash-lite");
        if (!Set.of("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash")
                .contains(model)) {
            throw new IllegalArgumentException("Modelo fuera del catálogo gratuito verificado.");
        }
    }

    public boolean configured() {
        return !key.isBlank();
    }

    public String model() {
        return model;
    }

    public Map<String, Object> generate(Map<String, Object> request) {
        if (!configured()) {
            throw new HttpError(
                    503, "Configura Gemini con scripts/configure_ai.ps1 y reinicia ObraPlan.");
        }
        try {
            HttpRequest call =
                    HttpRequest.newBuilder(
                                    URI.create(
                                            "https://generativelanguage.googleapis.com/v1beta/models/"
                                                    + model
                                                    + ":generateContent"))
                            .timeout(Duration.ofSeconds(25))
                            .header("Content-Type", "application/json")
                            .header("x-goog-api-key", key)
                            .POST(HttpRequest.BodyPublishers.ofString(Json.stringify(request)))
                            .build();
            HttpResponse<String> result = http.send(call, HttpResponse.BodyHandlers.ofString());
            switch (result.statusCode()) {
                case 200:
                    return Json.object(Json.parse(result.body()));
                case 429:
                    throw new HttpError(
                            429,
                            "Gemini agotó su cuota o límite temporal. No se reintentó ni se cambió"
                                + " a un servicio de pago.");
                case 400, 404:
                    throw new HttpError(
                            502,
                            "Gemini rechazó el modelo o la solicitud. Revisa el modelo disponible"
                                + " en AI Studio.");
                case 401, 403:
                    throw new HttpError(
                            503,
                            "Gemini rechazó la clave o sus permisos. Revisa la configuración"
                                + " local.");
                default:
                    throw new HttpError(
                            502, "Gemini no está disponible. Tus registros permanecen guardados.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpError(503, "Consulta de IA interrumpida.");
        } catch (IOException | IllegalArgumentException e) {
            throw new HttpError(502, "No se pudo conectar o interpretar la respuesta de Gemini.");
        }
    }
}
