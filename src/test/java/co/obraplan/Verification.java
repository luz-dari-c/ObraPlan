package co.obraplan;

import co.obraplan.agents.*;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Comprobación reproducible sin JUnit, con base y vault temporales. */
public final class Verification {
    private static final List<String> checks = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath();
        Path temp = root.resolve("output/verification/run-" + UUID.randomUUID());
        Files.createDirectories(temp.resolve("knowledge"));
        try (var files = Files.list(root.resolve("knowledge"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                Files.copy(file, temp.resolve("knowledge").resolve(file.getFileName()));
            }
        }
        Path vault = temp.resolve("vault");
        Files.createDirectories(vault.resolve(".obsidian"));
        Files.createDirectories(vault.resolve("Conocimiento"));
        Files.writeString(
                vault.resolve("Conocimiento/MemoriaCompartida.md"),
                "# Memoria compartida\n"
                    + "Referencia universalcuadrante.\n"
                    + "- complementa:: [[OP-Proyecto]]\n");
        Files.createDirectories(temp.resolve("config"));
        Files.writeString(
                temp.resolve("config/integrations.json"),
                "{\"sharedKnowledgeFiles\":[\"Conocimiento/MemoriaCompartida.md\"]}");
        Path data = temp.resolve("data/obraplan.json");
        Database db = new Database(data);
        KnowledgeAgent knowledge = new KnowledgeAgent(temp, vault);
        AppService service = new AppService(db, knowledge);
        var sharedQuery = knowledge.query("w-demo", "universalcuadrante");
        check(
                "Consulta notas compartidas del vault",
                ((List<?>) sharedQuery.get("sources"))
                        .stream()
                                .map(Json::object)
                                .anyMatch(note -> note.get("id").equals("MemoriaCompartida")));
        Files.writeString(
                vault.resolve("Conocimiento/MemoriaCompartida.md"),
                "# Memoria compartida\n"
                    + "Referencia universalactualizada.\n"
                    + "- complementa:: [[OP-Proyecto]]\n");
        check(
                "Consulta refleja edición en Obsidian",
                !((List<?>) knowledge.query("w-demo", "universalactualizada").get("sources"))
                        .isEmpty());
        var user = service.login("demo@obraplan.co", "ObraPlan2026!").user();
        check("Inicio de sesión demo", user.get("id").equals("u-demo"));
        expect(
                "Credenciales incorrectas",
                401,
                () -> service.login("demo@obraplan.co", "incorrecta"));
        expect("Acceso sin sesión", 401, () -> service.current(null));
        var otherLogin = service.register("Otro usuario", "otro@example.test", "ClavePrueba2026!");
        expect(
                "Correo duplicado",
                409,
                () -> service.register("Otro usuario", "otro@example.test", "ClavePrueba2026!"));
        expect("Obra ajena inaccesible", 404, () -> service.dashboard(otherLogin.user(), "w-demo"));
        expect(
                "Grafo de obra ajena inaccesible",
                404,
                () -> service.knowledge(otherLogin.user(), "w-demo", "precio"));
        var work =
                service.createWork(
                        user,
                        Json.map(
                                "name",
                                "Obra de prueba",
                                "address",
                                "Sitio ficticio",
                                "city",
                                "Ciudad demo",
                                "startDate",
                                LocalDate.now().toString()));
        String wid = work.get("id").toString();
        var material =
                service.addMaterial(
                        user,
                        wid,
                        Json.map(
                                "name",
                                "Cemento prueba",
                                "stage",
                                "Cimentación",
                                "unit",
                                "bultos",
                                "quantity",
                                10,
                                "neededDate",
                                LocalDate.now().plusDays(1).toString(),
                                "leadDays",
                                3));
        String mid = material.get("id").toString();
        var supplier =
                service.addSupplier(
                        user,
                        wid,
                        Json.map(
                                "name", "Proveedor prueba", "phone", "000", "city", "Ciudad demo"));
        String sid = supplier.get("id").toString();
        var estimate = estimate(service, user, wid);
        check(
                "Sin cotizar es nulo",
                ((List<?>) estimate.get("lines"))
                        .stream().map(Json::object).allMatch(line -> line.get("subtotal") == null));
        check(
                "Subtotal parcial con faltante",
                estimate.get("missingPriceCount").equals(1)
                        && estimate.get("isComplete").equals(false));
        expect(
                "Cantidad negativa rechazada",
                400,
                () ->
                        service.addMaterial(
                                user,
                                wid,
                                Json.map(
                                        "name",
                                        "Inválido",
                                        "stage",
                                        "Estructura",
                                        "unit",
                                        "kg",
                                        "quantity",
                                        -1,
                                        "neededDate",
                                        LocalDate.now().toString())));
        expect(
                "Edición inválida rechazada",
                400,
                () ->
                        service.updateMaterial(
                                user,
                                wid,
                                mid,
                                Json.map("name", "Nombre que no debe guardarse", "quantity", -1)));
        expect(
                "Nombre vacío en edición rechazado",
                400,
                () -> service.updateMaterial(user, wid, mid, Json.map("name", "   ")));
        var current =
                db.readMaterials().stream()
                        .filter(m -> m.get("id").equals(mid))
                        .findFirst()
                        .orElseThrow();
        check(
                "Edición fallida conserva nombre y cantidad",
                current.get("name").equals("Cemento prueba")
                        && ((Number) current.get("quantity")).doubleValue() == 10);
        var quote =
                service.addQuote(
                        user,
                        wid,
                        Json.map(
                                "materialId",
                                mid,
                                "supplierId",
                                sid,
                                "quantity",
                                10,
                                "unitPrice",
                                40000,
                                "quotedAt",
                                LocalDate.now().toString(),
                                "expectedDate",
                                LocalDate.now().minusDays(1).toString()));
        String qid = quote.get("id").toString();
        check(
                "10 × 40000 = 400000",
                ((Number) estimate(service, user, wid).get("knownSubtotal")).doubleValue()
                        == 400000);
        check(
                "Cotización no pedida sin aviso de entrega",
                new TrackingAgent().inspect(List.of(quote)).isEmpty());
        service.quoteStatus(user, wid, qid, "Pedido realizado");
        var ordered =
                db.readQuotes().stream()
                        .filter(q -> q.get("id").equals(qid))
                        .findFirst()
                        .orElseThrow();
        check(
                "Pedido activo vencido produce aviso",
                new TrackingAgent().inspect(List.of(ordered)).size() == 1);
        check(
                "Logística advierte gestión vencida",
                new LogisticsAgent().plan(List.of(material)).size() == 1);
        service.quoteStatus(user, wid, qid, "Cancelado");
        check(
                "Cancelado excluido de costos",
                ((Number) estimate(service, user, wid).get("knownSubtotal")).doubleValue() == 0);
        service.quoteStatus(user, wid, qid, "Rechazada");
        check(
                "Rechazada excluida de costos",
                estimate(service, user, wid).get("missingPriceCount").equals(1));
        service.quoteStatus(user, wid, qid, "En tránsito");
        service.quoteStatus(user, wid, qid, "Recibido");
        var received =
                db.readMaterials().stream()
                        .filter(m -> m.get("id").equals(mid))
                        .findFirst()
                        .orElseThrow();
        check("Recepción actualiza material", received.get("status").equals("Recibido"));
        check(
                "Recibido no alerta logística",
                new LogisticsAgent().plan(List.of(received)).isEmpty());
        expect(
                "Proveedor con cotización no se elimina",
                409,
                () -> service.deleteSupplier(user, wid, sid));
        expect(
                "Material de otra obra no se cotiza",
                404,
                () ->
                        service.addQuote(
                                user,
                                wid,
                                Json.map(
                                        "materialId",
                                        "m-cemento",
                                        "supplierId",
                                        sid,
                                        "quantity",
                                        1,
                                        "unitPrice",
                                        1,
                                        "quotedAt",
                                        LocalDate.now().toString())));
        var graph = service.knowledge(user, wid, "costo sin cotización");
        check("Consulta con fuentes", !((List<?>) graph.get("sources")).isEmpty());
        check(
                "Grafo con registros de obra",
                ((List<?>) graph.get("nodes"))
                        .stream()
                                .map(Json::object)
                                .anyMatch(n -> n.get("id").equals("OP-Material-" + mid)));
        check(
                "Grafo oculta registros de otra obra",
                ((List<?>) graph.get("nodes"))
                        .stream()
                                .map(Json::object)
                                .noneMatch(n -> n.get("id").equals("OP-Material-m-cemento")));
        check(
                "Exportación automática de material",
                Files.isRegularFile(
                        vault.resolve(
                                "Proyectos/ObraPlan/Obras/"
                                        + wid
                                        + "/OP-Material-"
                                        + mid
                                        + ".md")));
        String graphText = Json.stringify(graph);
        check(
                "Grafo sin hashes/sesiones",
                !graphText.contains("passwordHash") && !graphText.contains("OPSESSION"));
        var recovered = new Database(data);
        check(
                "Datos recuperados tras reabrir base",
                recovered.readMaterials().stream()
                        .anyMatch(
                                m ->
                                        m.get("id").equals(mid)
                                                && m.get("status").equals("Recibido")));
        KnowledgeAgent failed = new KnowledgeAgent(temp, temp.resolve("vault-ausente"));
        var isolatedService = new AppService(recovered, failed);
        isolatedService.updateWork(user, wid, Json.map("stage", "Acabados"));
        check("Fallo del vault visible", failed.status().get("ok").equals(false));
        check(
                "Fallo del vault no pierde guardado",
                new Database(data)
                        .readWorks().stream()
                                .anyMatch(
                                        w ->
                                                w.get("id").equals(wid)
                                                        && w.get("stage").equals("Acabados")));
        service.logout(otherLogin.token());
        expect("Sesión cerrada inaccesible", 401, () -> service.current(otherLogin.token()));
        Files.createDirectories(root.resolve("output/verification"));
        Files.writeString(
                root.resolve("output/verification/java-report.json"),
                Json.stringify(
                        Json.map(
                                "passed",
                                checks.size(),
                                "checks",
                                checks,
                                "temporaryRoot",
                                temp.toString())));
        System.out.println("Verificación Java: " + checks.size() + " comprobaciones correctas.");
    }

    private static Map<String, Object> estimate(
            AppService service, Map<String, Object> user, String work) {
        return Json.object(
                Json.object(service.dashboard(user, work).get("analysis")).get("estimate"));
    }

    private static void check(String name, boolean condition) {
        if (!condition) throw new AssertionError(name);
        checks.add(name);
    }

    private static void expect(String name, int code, Runnable action) {
        try {
            action.run();
            throw new AssertionError(name + ": debía rechazarse");
        } catch (HttpError error) {
            check(name, error.status == code);
        }
    }
}
