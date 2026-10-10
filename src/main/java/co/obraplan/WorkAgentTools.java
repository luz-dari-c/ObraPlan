package co.obraplan;

import co.obraplan.agents.ai.AgentTeam;
import co.obraplan.agents.ai.AgentTools;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDate;
import java.util.*;

final class WorkAgentTools implements AgentTools {
    private final AppService service;
    private final Map<String, Object> user;
    private final String workId;

    WorkAgentTools(AppService service, Map<String, Object> user, String workId) {
        this.service = service;
        this.user = user;
        this.workId = workId;
        service.dashboard(user, workId);
    }

    public String revision(String question) {
        String value =
                Json.stringify(service.dashboard(user, workId))
                        + service.knowledge(user, workId, "").get("revision")
                        + LocalDate.now();
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> execute(String name, Map<String, Object> args) {
        Map<String, Object> dashboard = service.dashboard(user, workId);
        int offset = 0;
        if (args.get("offset") instanceof Number n) {
            if (n.doubleValue() != n.intValue() || n.intValue() < 0) {
                return Json.map("error", "offset debe ser un entero no negativo.");
            }
            offset = n.intValue();
        }
        List<Map<String, Object>> materials = AgentTeam.maps(dashboard.get("materials"));
        List<Map<String, Object>> quotes = AgentTeam.maps(dashboard.get("quotes"));
        Map<String, Object> analysis = AgentTeam.object(dashboard.get("analysis"));
        return switch (name) {
            case "leer_materiales" ->
                    page(
                            materials,
                            offset,
                            "OP-Material-",
                            List.of(
                                    "id",
                                    "name",
                                    "stage",
                                    "quantity",
                                    "unit",
                                    "neededDate",
                                    "leadDays",
                                    "status"));
            case "leer_pedidos" ->
                    page(
                            quotes.stream()
                                    .filter(
                                            q ->
                                                    List.of("Pedido realizado", "En tránsito")
                                                            .contains(q.get("status")))
                                    .toList(),
                            offset,
                            "OP-Cotizacion-",
                            List.of("id", "materialId", "supplierId", "expectedDate", "status"));
            case "leer_cotizaciones" ->
                    page(
                            quotes.stream()
                                    .filter(
                                            q ->
                                                    Objects.equals(
                                                            q.get("materialId"),
                                                            args.get("materialId")))
                                    .toList(),
                            offset,
                            "OP-Cotizacion-",
                            List.of(
                                    "id",
                                    "materialId",
                                    "supplierId",
                                    "quantity",
                                    "unitPrice",
                                    "status",
                                    "quotedAt"));
            case "calcular_costos" -> cost(AgentTeam.object(analysis.get("estimate")), offset);
            case "evaluar_logistica" -> {
                List<Map<String, Object>> insights = AgentTeam.maps(analysis.get("insights"));
                List<Map<String, Object>> selected = insights.stream().limit(12).toList();
                yield Json.map(
                        "alerts",
                        selected,
                        "total",
                        insights.size(),
                        "truncated",
                        insights.size() > 12,
                        "sources",
                        List.of(
                                source("OP-Regla-Logistica", "Regla logística"),
                                source("OP-Regla-Seguimiento", "Regla de seguimiento")));
            }
            case "buscar_conocimiento" -> knowledge(Objects.toString(args.get("question"), ""));
            default -> Json.map("error", "Herramienta desconocida.");
        };
    }

    private Map<String, Object> cost(Map<String, Object> estimate, int offset) {
        Map<String, Object> result = new LinkedHashMap<>(estimate);
        List<Map<String, Object>> all = AgentTeam.maps(estimate.get("lines"));
        List<Map<String, Object>> lines = all.stream().skip(offset).limit(12).toList();
        result.put("lines", lines);
        result.put(
                "nextOffset",
                (long) offset + lines.size() < all.size() ? offset + lines.size() : null);
        result.put("totalLines", all.size());
        List<Object> sources = new ArrayList<>();
        sources.add(source("OP-Regla-Costos", "Cálculo de costos"));
        lines.forEach(
                line ->
                        sources.add(
                                source(
                                        "OP-Material-" + line.get("materialId"),
                                        Objects.toString(line.get("material")))));
        result.put("sources", sources);
        return result;
    }

    private Map<String, Object> page(
            List<Map<String, Object>> all, int offset, String prefix, List<String> fields) {
        List<Map<String, Object>> items =
                all.stream().skip(offset).limit(12).map(item -> project(item, fields)).toList();
        return Json.map(
                "items",
                items,
                "total",
                all.size(),
                "nextOffset",
                (long) offset + items.size() < all.size() ? offset + items.size() : null,
                "sources",
                items.stream()
                        .map(
                                item ->
                                        source(
                                                prefix + item.get("id"),
                                                Objects.toString(
                                                        item.getOrDefault("name", item.get("id")))))
                        .toList());
    }

    private Map<String, Object> knowledge(String question) {
        Map<String, Object> graph =
                service.knowledge(user, workId, AgentTeam.bounded(question, 600));
        List<Map<String, Object>> sources =
                AgentTeam.maps(graph.get("sources")).stream()
                        .map(
                                s ->
                                        Json.map(
                                                "id",
                                                s.get("id"),
                                                "title",
                                                s.get("title"),
                                                "excerpt",
                                                AgentTeam.bounded(
                                                        Objects.toString(s.get("excerpt")), 1200)))
                        .toList();
        Set<String> ids = new HashSet<>();
        sources.forEach(s -> ids.add(s.get("id").toString()));
        List<Map<String, Object>> edges =
                AgentTeam.maps(graph.get("edges")).stream()
                        .filter(e -> ids.contains(e.get("source")) || ids.contains(e.get("target")))
                        .filter(e -> Boolean.TRUE.equals(e.get("resolved")))
                        .limit(20)
                        .toList();
        Set<String> neighbors = new LinkedHashSet<>();
        edges.forEach(
                e -> {
                    neighbors.add(e.get("source").toString());
                    neighbors.add(e.get("target").toString());
                });
        return Json.map(
                "sources",
                sources,
                "relations",
                edges,
                "neighbors",
                AgentTeam.maps(graph.get("nodes")).stream()
                        .filter(n -> neighbors.contains(n.get("id")))
                        .map(n -> project(n, List.of("id", "title", "type")))
                        .toList(),
                "message",
                graph.get("answer"));
    }

    private Map<String, Object> project(Map<String, Object> item, List<String> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) {
            Object value = item.get(field);
            result.put(field, value instanceof String s ? AgentTeam.bounded(s, 240) : value);
        }
        return result;
    }

    private Map<String, Object> source(String id, String title) {
        return Json.map("id", id, "title", title);
    }

    public Map<String, Object> save(Map<String, Object> result) {
        return service.saveAnalysis(user, workId, result);
    }
}
