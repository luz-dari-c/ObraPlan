package co.obraplan.agents.ai;

import co.obraplan.Json;

import java.util.*;

/** Construcción explícita de la ventana de contexto; no contiene claves. */
public final class SolicitudModelo {
    private SolicitudModelo() {}

    public static Map<String, Object> crearSolicitudModelo(
            List<Object> mensajes,
            String instrucciones,
            List<Object> herramientas,
            int maximoTokens) {
        Map<String, Object> configuracion =
                Json.map(
                        "maxOutputTokens",
                        maximoTokens,
                        "temperature",
                        0.2,
                        "thinkingConfig",
                        Json.map("thinkingLevel", "MINIMAL"));
        return Json.map(
                "systemInstruction",
                Json.map("parts", List.of(Json.map("text", instrucciones))),
                "contents",
                mensajes,
                "generationConfig",
                configuracion,
                "tools",
                List.of(Json.map("functionDeclarations", herramientas)));
    }
}
