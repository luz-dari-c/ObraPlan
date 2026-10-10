package co.obraplan.agents;

import static co.obraplan.agents.AgentData.date;

import co.obraplan.Json;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Orchestrator {
    private final LogisticsAgent logistics = new LogisticsAgent();
    private final CostAgent costs = new CostAgent();
    private final TrackingAgent tracking = new TrackingAgent();

    public Map<String, Object> brief(
            List<Map<String, Object>> materials,
            List<Map<String, Object>> suppliers,
            List<Map<String, Object>> quotes) {
        List<Insight> insights = new ArrayList<>();
        insights.addAll(logistics.plan(materials));
        insights.addAll(tracking.inspect(quotes));
        Object next =
                materials.stream()
                        .filter(material -> !"Recibido".equals(material.get("status")))
                        .map(material -> date(material.get("neededDate")))
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .map(LocalDate::toString)
                        .orElse(null);
        return Json.map(
                "metrics",
                        Json.map(
                                "toQuote",
                                        materials.stream()
                                                .filter(m -> "Por cotizar".equals(m.get("status")))
                                                .count(),
                                "quotes", quotes.size(),
                                "activeOrders",
                                        quotes.stream()
                                                .filter(
                                                        q ->
                                                                List.of(
                                                                                "Pedido realizado",
                                                                                "En tránsito")
                                                                        .contains(q.get("status")))
                                                .count(),
                                "nextCriticalDate", next),
                "estimate", costs.estimate(materials, quotes),
                "insights",
                        insights.stream()
                                .map(
                                        i ->
                                                Json.map(
                                                        "agent",
                                                        i.agent(),
                                                        "title",
                                                        i.title(),
                                                        "detail",
                                                        i.detail(),
                                                        "severity",
                                                        i.severity(),
                                                        "materialId",
                                                        i.materialId(),
                                                        "source",
                                                        i.source()))
                                .toList(),
                "agents",
                        List.of(
                                role(
                                        "Orquestador",
                                        "Coordina el análisis y reúne resultados.",
                                        "OP-Agente-Orquestador"),
                                role(
                                        "Logística",
                                        "Calcula fechas sugeridas según necesidad y plazo.",
                                        "OP-Agente-Logistica"),
                                role(
                                        "Costos",
                                        "Suma precios manuales y señala datos faltantes.",
                                        "OP-Agente-Costos"),
                                role(
                                        "Seguimiento",
                                        "Revisa fechas de pedidos en curso.",
                                        "OP-Agente-Seguimiento"),
                                role(
                                        "Conocimiento",
                                        "Consulta relaciones y actualiza el grafo al guardar"
                                                + " cambios.",
                                        "OP-Agente-Conocimiento")));
    }

    private Map<String, Object> role(String name, String role, String source) {
        return Json.map("name", name, "role", role, "source", source, "mode", "Reglas locales");
    }
}
