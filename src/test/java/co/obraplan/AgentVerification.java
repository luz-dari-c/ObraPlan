package co.obraplan;

import co.obraplan.agents.KnowledgeAgent;
import co.obraplan.agents.ai.*;

import java.nio.file.*;
import java.util.*;

/** Scripted provider validates orchestration without spending quota or claiming real LLM output. */
public final class AgentVerification {
    private static final List<String> checks = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Path temp = root.resolve("output/verification/ai-" + UUID.randomUUID());
        Files.createDirectories(temp.resolve("knowledge"));
        try (var files = Files.list(root.resolve("knowledge"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                Files.copy(file, temp.resolve("knowledge").resolve(file.getFileName()));
            }
        }
        Path vault = temp.resolve("vault");
        Files.createDirectories(vault.resolve(".obsidian"));
        KnowledgeAgent knowledge = new KnowledgeAgent(temp, vault);
        AppService service = new AppService(new Database(temp.resolve("data.json")), knowledge);
        var owner = service.login("demo@obraplan.co", "ObraPlan2026!").user();
        var outsider =
                service.register("Otro usuario", "otro@example.test", "OtraClave2026!").user();
        expect(404, () -> new WorkAgentTools(service, outsider, "w-demo"));
        check("Herramientas rechazan obra ajena antes de ejecutar IA", true);
        WorkAgentTools tools = new WorkAgentTools(service, owner, "w-demo");
        String projection =
                Json.stringify(
                        tools.execute("leer_cotizaciones", Json.map("materialId", "m-demo-1")));
        check(
                "Proyección excluye contactos y credenciales",
                !projection.contains("phone")
                        && !projection.contains("email")
                        && !projection.contains("passwordHash"));
        var costs = tools.execute("calcular_costos", Map.of());
        check(
                "Costos conserva faltantes y totales del motor local",
                costs.get("knownSubtotal")
                        .equals(
                                AgentTeam.object(
                                                AgentTeam.object(
                                                                service.dashboard(owner, "w-demo")
                                                                        .get("analysis"))
                                                        .get("estimate"))
                                        .get("knownSubtotal")));
        var graph = tools.execute("buscar_conocimiento", Json.map("question", "costos"));
        check(
                "Conocimiento recupera fuentes y relaciones reales",
                !AgentTeam.maps(graph.get("sources")).isEmpty()
                        && !AgentTeam.maps(graph.get("relations")).isEmpty());

        Scripted client =
                new Scripted(
                        List.of(
                                calls(
                                        "consultar_logistica",
                                        "consultar_costos",
                                        "consultar_conocimiento"),
                                calls("evaluar_logistica"),
                                text("Logística consultada [OP-Regla-Logistica]."),
                                calls("calcular_costos"),
                                text("Costo parcial [OP-Regla-Costos]."),
                                calls("buscar_conocimiento"),
                                text("Reglas consultadas [OP-Regla-Costos]."),
                                text(
                                        "Propuesta con logística, costos y conocimiento"
                                                + " [OP-Regla-Costos].")));
        AgentTeam team = new AgentTeam(client);
        var result = team.ask(owner.get("id").toString(), "w-demo", "Analiza la obra", tools);
        check(
                "Coordinador ejecuta los tres especialistas con sus herramientas",
                client.count == 8
                        && AgentTeam.maps(result.get("trace")).stream()
                                        .filter(t -> t.get("event").equals("tool"))
                                        .count()
                                == 6);
        check(
                "Firmas del proveedor se conservan al devolver herramientas",
                client.requests.stream()
                        .skip(1)
                        .anyMatch(
                                r -> Json.stringify(r.get("contents")).contains("test-signature")));
        check(
                "Uso de tokens proviene de metadata del proveedor",
                AgentTeam.object(result.get("usage")).get("totalTokenCount").equals(80));
        check(
                "Fuentes corresponden a herramientas consultadas",
                AgentTeam.maps(result.get("sources")).stream()
                        .anyMatch(s -> s.get("id").equals("OP-Regla-Costos")));
        var cached = team.ask(owner.get("id").toString(), "w-demo", "Analiza la obra", tools);
        check(
                "Consulta repetida reutiliza resultado sin llamadas",
                cached.get("cached").equals(true)
                        && cached.get("callsThisRequest").equals(0)
                        && client.count == 8);
        expect(
                404,
                () ->
                        team.remember(
                                outsider.get("id").toString(),
                                "w-demo",
                                result.get("runId").toString(),
                                tools));
        check("Otro usuario no puede conservar una ejecución ajena", true);
        var saved =
                team.remember(
                        owner.get("id").toString(),
                        "w-demo",
                        result.get("runId").toString(),
                        tools);
        check(
                "Confirmación conserva propuesta y publica nota en vault temporal",
                saved.get("published").equals(true)
                        && knowledge.query("w-demo", "Propuesta").get("nodeCount")
                                instanceof Number);
        String savedId = saved.get("id").toString();
        check(
                "Referencias breves conservan su correspondencia en la nota",
                Files.readString(
                                temp.resolve("output/knowledge/analyses/w-demo/" + savedId + ".md"))
                        .contains("(F1)"));
        var ownGraph = knowledge.query("w-demo", "");
        check(
                "Análisis queda conectado a obra, coordinador y fuentes",
                AgentTeam.maps(ownGraph.get("edges")).stream()
                        .anyMatch(
                                e ->
                                        e.get("source").equals(savedId)
                                                && e.get("target").equals("OP-IA-Coordinador")));
        check(
                "Análisis no aparece en el grafo de otra obra",
                AgentTeam.maps(knowledge.query("otra", "").get("nodes")).stream()
                        .noneMatch(n -> n.get("id").equals(savedId)));
        expect(
                409,
                () ->
                        team.remember(
                                owner.get("id").toString(),
                                "w-demo",
                                result.get("runId").toString(),
                                tools));
        check("Cambio de memoria invalida análisis anterior", true);
        expect(429, () -> team.ask(owner.get("id").toString(), "w-demo", "Analiza la obra", tools));
        check("Cambio de datos invalida caché y aplica límite temporal", true);

        AgentTeam unavailable =
                new AgentTeam(
                        new ModelClient() {
                            public boolean configured() {
                                return false;
                            }

                            public String model() {
                                return "test";
                            }

                            public Map<String, Object> generate(Map<String, Object> r) {
                                throw new AssertionError();
                            }
                        });
        expect(503, () -> unavailable.ask("u", "w", "consulta", tools));
        check("Sin clave informa pendiente sin fabricar respuesta IA", true);
        AgentTeam exhausted =
                new AgentTeam(
                        new ModelClient() {
                            public boolean configured() {
                                return true;
                            }

                            public String model() {
                                return "test";
                            }

                            public Map<String, Object> generate(Map<String, Object> r) {
                                throw new HttpError(429, "cuota");
                            }
                        });
        expect(429, () -> exhausted.ask("u", "w", "consulta", tools));
        check("Cuota agotada se propaga sin reintento ni proveedor alternativo", true);
        var loopClient =
                new ModelClient() {
                    int count;

                    public boolean configured() {
                        return true;
                    }

                    public String model() {
                        return "test";
                    }

                    public Map<String, Object> generate(Map<String, Object> r) {
                        count++;
                        return Json.stringify(r.get("systemInstruction"))
                                        .contains("Tu rol: coordinador")
                                ? calls("consultar_costos")
                                : calls("calcular_costos");
                    }
                };
        var partial = new AgentTeam(loopClient).ask("u", "w", "consulta", tools);
        check(
                "Bucle de herramientas termina con resultado incompleto",
                partial.get("complete").equals(false) && loopClient.count <= 10);
        AgentTeam rejecting =
                new AgentTeam(
                        new Scripted(
                                List.of(
                                        calls("eliminar_pedidos"),
                                        calls("consultar_costos"),
                                        calls("calcular_costos"),
                                        text("Costo"),
                                        text("Resultado"))));
        var safe = rejecting.ask("u", "w", "consulta", tools);
        check("Herramienta no autorizada no ejecuta acciones", safe.get("complete").equals(true));
        check(
                "Fuentes conservan ID canónico y referencia corta única",
                AgentTeam.maps(result.get("sources")).stream()
                        .allMatch(s -> s.get("ref").toString().matches("F[0-9]+")));
        var lastRequest = client.requests.getLast();
        check(
                "Síntesis final tiene espacio reservado y no vuelve a delegar después de los tres"
                        + " agentes",
                ((Number)
                                                AgentTeam.object(
                                                                lastRequest.get("generationConfig"))
                                                        .get("maxOutputTokens"))
                                        .intValue()
                                == 1280
                        && Json.stringify(lastRequest.get("toolConfig")).contains("NONE"));
        var truncated = text("Respuesta truncada");
        AgentTeam.maps(truncated.get("candidates")).getFirst().put("finishReason", "MAX_TOKENS");
        var truncatedTeam =
                new AgentTeam(
                        new Scripted(
                                List.of(
                                        calls("consultar_costos"),
                                        calls("calcular_costos"),
                                        text("Subtotal parcial [F1]."),
                                        truncated)));
        var incomplete = truncatedTeam.ask("u", "w", "consulta", tools);
        check(
                "Respuesta truncada distingue extensión de límite de llamadas",
                incomplete.get("complete").equals(false)
                        && incomplete.get("stopReason").equals("output")
                        && incomplete.get("callsThisRequest").equals(4));
        expect(
                409,
                () -> truncatedTeam.remember("u", "w", incomplete.get("runId").toString(), tools));
        check("Respuesta truncada no puede conservarse como completa", true);
        Files.writeString(
                root.resolve("output/verification/ai-report.json"),
                Json.stringify(
                        Json.map(
                                "passed",
                                checks.size(),
                                "checks",
                                checks,
                                "provider",
                                "Simulado exclusivamente en pruebas; no hubo llamadas a Gemini",
                                "realProviderVerified",
                                false)));
        System.out.println(checks.size() + " verificaciones de IA correctas (proveedor simulado).");
    }

    static void check(String label, boolean condition) {
        if (!condition) throw new AssertionError(label);
        checks.add(label);
    }

    static void expect(int status, Runnable task) {
        try {
            task.run();
            throw new AssertionError("Se esperaba HTTP " + status);
        } catch (HttpError e) {
            if (e.status != status) throw new AssertionError("HTTP " + e.status);
        }
    }

    static Map<String, Object> calls(String... names) {
        List<Object> parts = new ArrayList<>();
        for (String name : names)
            parts.add(
                    Json.map(
                            "thoughtSignature",
                            "test-signature",
                            "functionCall",
                            Json.map(
                                    "id",
                                    "call-" + name,
                                    "name",
                                    name,
                                    "args",
                                    Json.map("question", "costos logística", "offset", 0))));
        return response(parts);
    }

    static Map<String, Object> text(String text) {
        return response(List.of(Json.map("text", text)));
    }

    static Map<String, Object> response(List<Object> parts) {
        return Json.map(
                "candidates",
                List.of(
                        Json.map(
                                "finishReason",
                                "STOP",
                                "content",
                                Json.map("role", "model", "parts", parts))),
                "usageMetadata",
                Json.map("totalTokenCount", 10));
    }

    static final class Scripted implements ModelClient {
        final Queue<Map<String, Object>> replies;
        final List<Map<String, Object>> requests = new ArrayList<>();
        int count;

        Scripted(List<Map<String, Object>> replies) {
            this.replies = new ArrayDeque<>(replies);
        }

        public boolean configured() {
            return true;
        }

        public String model() {
            return "scripted-test-provider";
        }

        public Map<String, Object> generate(Map<String, Object> request) {
            count++;
            requests.add(Json.object(Json.parse(Json.stringify(request))));
            return Objects.requireNonNull(replies.poll(), "Falta respuesta simulada");
        }
    }
}
