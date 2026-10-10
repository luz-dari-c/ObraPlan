package co.obraplan.agents;

import co.obraplan.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class KnowledgeAgent {
    private static final Pattern LINK = Pattern.compile("\\[\\[([^]#|]+)(?:[#|][^]]*)?]]");
    private final Path knowledge;
    private final Path runtime;
    private final Path analyses;
    private final Path vault;
    private final Path integrations;
    private String lastSync = "";
    private String syncError = "";

    public KnowledgeAgent(Path projectRoot, Path vault) {
        this.knowledge = projectRoot.resolve("knowledge");
        this.runtime = projectRoot.resolve("output/knowledge/runtime");
        this.analyses = projectRoot.resolve("output/knowledge/analyses");
        this.integrations = projectRoot.resolve("config/integrations.json");
        this.vault =
                vault == null
                        ? null
                        : vault.toAbsolutePath().normalize().resolve("Proyectos/ObraPlan");
    }

    public synchronized void record(Map<String, Object> state) {
        try {
            Files.createDirectories(runtime);
            for (var work : maps(state, "works")) {
                String workId = safeId(work.get("id"));
                Path folder = runtime.resolve(workId);
                Files.createDirectories(folder);
                Set<String> expected = new HashSet<>();
                String workNode = "OP-Obra-" + workId;
                StringBuilder summary =
                        new StringBuilder(note(workNode, "obra", text(work.get("name"))));
                summary.append("Etapa: ")
                        .append(text(work.get("stage")))
                        .append("\n\n")
                        .append("- instancia_de:: [[OP-Entidad-Obra]]\n")
                        .append("- aplica:: [[OP-Regla-Costos]]\n")
                        .append("- aplica:: [[OP-Regla-Logistica]]\n")
                        .append("- pertenece_a:: [[OP-Proyecto]]\n");
                for (String collection : List.of("materials", "suppliers", "quotes")) {
                    for (var item : maps(state, collection)) {
                        if (!Objects.equals(item.get("workId"), work.get("id"))) continue;
                        String prefix =
                                switch (collection) {
                                    case "materials" -> "Material";
                                    case "suppliers" -> "Ferreteria";
                                    default -> "Cotizacion";
                                };
                        String node = "OP-" + prefix + "-" + safeId(item.get("id"));
                        String title =
                                collection.equals("quotes")
                                        ? "Cotización " + text(item.get("id"))
                                        : text(item.get("name"));
                        StringBuilder content =
                                new StringBuilder(
                                        note(node, prefix.toLowerCase(Locale.ROOT), title));
                        content.append("- pertenece_a:: [[")
                                .append(workNode)
                                .append("]]\n")
                                .append("- instancia_de:: [[OP-Entidad-")
                                .append(prefix)
                                .append("]]\n\n");
                        // Sólo campos necesarios para explicar adquisiciones; nunca usuarios,
                        // claves o contactos.
                        for (String key :
                                List.of(
                                        "stage",
                                        "quantity",
                                        "unit",
                                        "neededDate",
                                        "leadDays",
                                        "unitPrice",
                                        "quotedAt",
                                        "expectedDate",
                                        "status")) {
                            if (item.get(key) != null)
                                content.append(key)
                                        .append(": ")
                                        .append(text(item.get(key)))
                                        .append("\n\n");
                        }
                        if (collection.equals("materials"))
                            content.append(
                                    "- evaluado_por:: [[OP-Agente-Logistica]]\n"
                                            + "- evaluado_por:: [[OP-Agente-Costos]]\n");
                        if (collection.equals("quotes"))
                            content.append("- cotiza:: [[OP-Material-")
                                    .append(safeId(item.get("materialId")))
                                    .append("]]\n")
                                    .append("- ofertada_por:: [[OP-Ferreteria-")
                                    .append(safeId(item.get("supplierId")))
                                    .append("]]\n")
                                    .append("- evaluado_por:: [[OP-Agente-Seguimiento]]\n");
                        summary.append("- contiene:: [[").append(node).append("]]\n");
                        put(folder, node, content.toString());
                        expected.add(node + ".md");
                    }
                }
                put(folder, workNode, summary.toString());
                expected.add(workNode + ".md");
                // La carpeta local es derivada y exclusiva del agente.
                try (var files = Files.list(folder)) {
                    for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                        if (!expected.contains(file.getFileName().toString())) Files.delete(file);
                    }
                }
            }
            lastSync = Instant.now().toString();
            if (vault != null) {
                if (!Files.isDirectory(vault.getParent().getParent().resolve(".obsidian"))) {
                    throw new IOException("El vault configurado no existe o no contiene .obsidian");
                }
                try (var folders = Files.list(runtime)) {
                    for (Path folder : folders.filter(Files::isDirectory).toList()) {
                        mirror(folder, vault.resolve("Obras").resolve(folder.getFileName()));
                    }
                }
                mirror(knowledge, vault.resolve("Conocimiento"));
            }
            syncError = "";
        } catch (Exception error) {
            syncError = error.getClass().getSimpleName() + ": " + error.getMessage();
            System.err.println("Conocimiento pendiente de sincronizar: " + syncError);
        }
    }

    public synchronized Map<String, Object> status() {
        return Json.map(
                "mode",
                "Recuperación local con relaciones",
                "vaultConfigured",
                vault != null,
                "lastSync",
                lastSync,
                "ok",
                syncError.isEmpty(),
                "error",
                syncError,
                "automatic",
                "Después de cada cambio confirmado en la base de datos");
    }

    public synchronized Map<String, Object> query(String workId, String question) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        try {
            readNotes(knowledge, nodes);
            readNotes(runtime.resolve(safeId(workId)), nodes);
            readNotes(analyses.resolve(safeId(workId)), nodes);
            if (vault != null) {
                readNotes(vault.resolve("Aprendizajes"), nodes);
                if (Files.isRegularFile(integrations)) {
                    var settings =
                            Json.object(
                                    Json.parse(
                                            Files.readString(
                                                    integrations, StandardCharsets.UTF_8)));
                    Path vaultRoot = vault.getParent().getParent();
                    for (Object relative :
                            (List<?>) settings.getOrDefault("sharedKnowledgeFiles", List.of())) {
                        Path file = vaultRoot.resolve(relative.toString()).normalize();
                        if (!file.startsWith(vaultRoot))
                            throw new IOException("Referencia fuera del vault");
                        readNotes(file, nodes);
                    }
                }
            }
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo consultar el conocimiento", error);
        }
        Set<String> ids = new HashSet<>();
        nodes.forEach(node -> ids.add(node.get("id").toString()));
        List<Map<String, Object>> edges = new ArrayList<>();
        for (var node : nodes) {
            for (String line : node.get("content").toString().split("\n")) {
                var matcher = LINK.matcher(line);
                while (matcher.find()) {
                    String target = Path.of(matcher.group(1)).getFileName().toString();
                    String relation =
                            line.contains("::")
                                    ? line.substring(0, line.indexOf("::")).replace("-", "").trim()
                                    : "relaciona";
                    edges.add(
                            Json.map(
                                    "source",
                                    node.get("id"),
                                    "target",
                                    target,
                                    "relation",
                                    relation,
                                    "resolved",
                                    ids.contains(target)));
                }
            }
        }
        String clean = normalize(question == null ? "" : question);
        List<String> words =
                Arrays.stream(clean.split("\\W+"))
                        .filter(word -> word.length() > 2)
                        .distinct()
                        .toList();
        List<Map<String, Object>> sources =
                nodes.stream()
                        .filter(
                                node ->
                                        words.stream()
                                                .anyMatch(
                                                        word ->
                                                                normalize(
                                                                                node.get("content")
                                                                                        .toString())
                                                                        .contains(word)))
                        .sorted(
                                Comparator.comparingLong(
                                                (Map<String, Object> node) ->
                                                        words.stream()
                                                                .filter(
                                                                        word ->
                                                                                normalize(
                                                                                                node.get(
                                                                                                                "title")
                                                                                                        .toString())
                                                                                        .contains(
                                                                                                word))
                                                                .count())
                                        .reversed())
                        .limit(6)
                        .map(
                                node ->
                                        Json.map(
                                                "id",
                                                node.get("id"),
                                                "title",
                                                node.get("title"),
                                                "excerpt",
                                                excerpt(node.get("content").toString()),
                                                "path",
                                                node.get("path"),
                                                "obsidianUri",
                                                uri(node.get("id").toString())))
                        .toList();
        String answer =
                words.isEmpty()
                        ? "El grafo conecta problema, requisitos, entidades, agentes, reglas y"
                                + " registros de esta obra."
                        : sources.isEmpty()
                                ? "No encontré evidencia para esa consulta. Reformula con términos"
                                        + " del proyecto."
                                : "Encontré "
                                        + sources.size()
                                        + " notas relacionadas. Consulta sus extractos y"
                                        + " relaciones; no se generan hechos nuevos.";
        return Json.map(
                "answer",
                answer,
                "sources",
                sources,
                "revision",
                fingerprint(Json.stringify(nodes)),
                "status",
                status(),
                "nodes",
                nodes.stream()
                        .map(
                                node ->
                                        Json.map(
                                                "id",
                                                node.get("id"),
                                                "title",
                                                node.get("title"),
                                                "type",
                                                node.get("type"),
                                                "obsidianUri",
                                                uri(node.get("id").toString())))
                        .toList(),
                "edges",
                edges,
                "nodeCount",
                nodes.size(),
                "edgeCount",
                edges.size());
    }

    private String uri(String id) {
        if (vault == null) return "";
        String vaultName = vault.getParent().getParent().getFileName().toString();
        return "obsidian://open?vault="
                + java.net.URLEncoder.encode(vaultName, StandardCharsets.UTF_8)
                + "&file="
                + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    private static void readNotes(Path folder, List<Map<String, Object>> nodes) throws IOException {
        if (!Files.exists(folder)) return;
        try (Stream<Path> paths =
                Files.isRegularFile(folder) ? Stream.of(folder) : Files.walk(folder)) {
            for (Path file :
                    paths.filter(path -> path.toString().endsWith(".md")).sorted().toList()) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                String title =
                        content.lines()
                                .filter(line -> line.startsWith("# "))
                                .findFirst()
                                .orElse("# " + file.getFileName())
                                .substring(2);
                String type =
                        content.lines()
                                .filter(line -> line.startsWith("tipo:"))
                                .findFirst()
                                .orElse("tipo: conocimiento")
                                .substring(5)
                                .trim();
                String id = file.getFileName().toString().replaceFirst("\\.md$", "");
                nodes.add(
                        Json.map(
                                "id",
                                id,
                                "title",
                                title,
                                "type",
                                type,
                                "content",
                                content,
                                "path",
                                file.toAbsolutePath().toString()));
            }
        }
    }

    private static void mirror(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) return;
        Files.createDirectories(target);
        try (var paths = Files.list(source)) {
            for (Path file : paths.filter(path -> path.toString().endsWith(".md")).toList()) {
                put(
                        target,
                        file.getFileName().toString().replaceFirst("\\.md$", ""),
                        Files.readString(file, StandardCharsets.UTF_8));
            }
        }
    }

    private static void put(Path folder, String id, String content) throws IOException {
        Files.createDirectories(folder);
        Path file = folder.resolve(safeId(id) + ".md");
        if (Files.exists(file) && Files.readString(file, StandardCharsets.UTF_8).equals(content))
            return;
        Path temporary = folder.resolve(safeId(id) + ".md.tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try {
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String note(String id, String type, String title) {
        return "---\nid: "
                + id
                + "\ntipo: "
                + type
                + "\ntags: [obraplan, generado]\n---\n\n# "
                + title
                + "\n\n"
                + "Nota derivada de la base local. Se actualiza al guardar; no editar"
                + " manualmente.\n\n";
    }

    public synchronized Map<String, Object> saveAnalysis(
            String workId, Map<String, Object> result) {
        String node = "OP-Analisis-" + safeId(result.get("runId"));
        String text = result.get("answer").toString().replace("[[", "(").replace("]]", ")");
        StringBuilder note =
                new StringBuilder(note(node, "analisis-ia", "Análisis IA de adquisiciones"));
        note.append(
                        "Propuesta generada por IA y conservada por el usuario; no modifica"
                                + " pedidos.\n\n")
                .append("Modelo: ")
                .append(result.get("model"))
                .append("\n\n")
                .append("Fecha: ")
                .append(result.get("createdAt"))
                .append("\n\n")
                .append(text)
                .append("\n\n")
                .append("- pertenece_a:: [[OP-Obra-")
                .append(safeId(workId))
                .append("]]\n")
                .append("- generado_por:: [[OP-IA-Coordinador]]\n");
        for (var source : co.obraplan.agents.ai.AgentTeam.maps(result.get("sources"))) {
            String sourceId = source.get("id").toString();
            if (sourceId.matches("[A-Za-z0-9_-]+")) {
                note.append("- sustentado_en:: [[").append(sourceId).append("]]");
                String reference = text(source.get("ref"));
                if (reference.matches("F[0-9]+")) {
                    note.append(" (").append(reference).append(")");
                }
                note.append("\n");
            }
        }
        try {
            if (!Files.isDirectory(vault.getParent().getParent().resolve(".obsidian"))) {
                throw new IOException("Vault no disponible");
            }
            Path folder = analyses.resolve(safeId(workId));
            Files.createDirectories(folder);
            put(folder, node, note.toString());
            boolean published = false;
            if (vault != null) {
                try {
                    Path target = vault.resolve("Analisis").resolve(safeId(workId));
                    Files.createDirectories(target);
                    put(target, node, note.toString());
                    published = true;
                } catch (IOException e) {
                    return Json.map(
                            "saved",
                            true,
                            "published",
                            false,
                            "id",
                            node,
                            "message",
                            "Análisis conservado localmente; publicación en Obsidian pendiente.");
                }
            }
            return Json.map("saved", true, "published", published, "id", node);
        } catch (IOException e) {
            throw new co.obraplan.HttpError(503, "No se pudo conservar el análisis.");
        }
    }

    private static String fingerprint(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String excerpt(String content) {
        String result = content.replaceFirst("(?s)^---.*?---\\s*", "");
        return result.length() > 750 ? result.substring(0, 750) + "…" : result;
    }

    private static String text(Object value) {
        return AgentData.str(value).replaceAll("[\\r\\n\\[\\]#]", " ");
    }

    private static String safeId(Object value) {
        String result = AgentData.str(value);
        if (!result.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Identificador de nota inválido");
        return result;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static List<Map<String, Object>> maps(Map<String, Object> state, String key) {
        return ((List<?>) state.getOrDefault(key, List.of())).stream().map(Json::object).toList();
    }
}
