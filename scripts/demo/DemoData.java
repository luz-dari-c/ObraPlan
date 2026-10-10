package co.obraplan;

import co.obraplan.agents.KnowledgeAgent;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Carga local explícita de un escenario ficticio; usa los mismos casos de uso de la aplicación. */
public final class DemoData {
    private static final String SCENARIO = "video-v1";
    private static final String NAME = "Demostración para video";

    public static void main(String[] args) throws Exception {
        if (args.length < 2)
            throw new IllegalArgumentException(
                    "Indica raíz y al menos un ID de usuario existente.");
        Path root = Path.of(args[0]).toAbsolutePath();
        Database db = new Database(root.resolve("data/obraplan.json"));
        Map<String, Object> before = db.read();
        String configuredVault = System.getenv().getOrDefault("OBRAPLAN_VAULT", "");
        KnowledgeAgent knowledge =
                new KnowledgeAgent(
                        root, configuredVault.isBlank() ? null : Path.of(configuredVault));
        AppService service = new AppService(db, knowledge);
        List<Map<String, Object>> reports = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String userId = args[i];
            var user =
                    db.readUsers().stream()
                            .filter(item -> userId.equals(item.get("id")))
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Usuario local inexistente: " + userId));
            reports.add(populate(service, db, user));
        }
        Map<String, Object> after = db.read();
        for (String collection :
                List.of("users", "works", "materials", "suppliers", "quotes", "activities")) {
            for (var original : rows(before, collection)) {
                boolean unchanged = rows(after, collection).stream().anyMatch(original::equals);
                if (!unchanged)
                    throw new IllegalStateException("Cambió un registro previo de " + collection);
            }
        }
        Path report = root.resolve("output/demo/report.json");
        Files.createDirectories(report.getParent());
        Files.writeString(
                report,
                Json.stringify(Json.map("existingRecordsPreserved", true, "scenarios", reports)));
        System.out.println(
                Json.stringify(Json.map("existingRecordsPreserved", true, "scenarios", reports)));
    }

    private static Map<String, Object> populate(
            AppService service, Database db, Map<String, Object> user) {
        var work =
                service.works(user).stream()
                        .filter(item -> SCENARIO.equals(item.get("demoScenario")))
                        .findFirst()
                        .orElse(null);
        if (work == null) {
            work =
                    service.createWork(
                            user,
                            Json.map(
                                    "name",
                                    NAME,
                                    "address",
                                    "Sector ficticio para presentación",
                                    "city",
                                    "Medellín, Antioquia",
                                    "startDate",
                                    LocalDate.now().minusDays(15).toString(),
                                    "stage",
                                    "Cimentación",
                                    "description",
                                    "Datos ficticios preparados para explicar la aplicación."));
        }
        String wid = work.get("id").toString();
        if (!Boolean.TRUE.equals(work.get("demoComplete"))) {
            seed(service, db, user, wid);
            db.write(
                    state -> {
                        for (String collection :
                                List.of("works", "materials", "suppliers", "quotes")) {
                            for (var item : rows(state, collection)) {
                                if (wid.equals(item.get("workId"))
                                        || (collection.equals("works")
                                                && wid.equals(item.get("id")))) {
                                    item.put("demo", true);
                                    if (collection.equals("works")) {
                                        item.put("demoScenario", SCENARIO);
                                        item.put("demoComplete", true);
                                    }
                                }
                            }
                        }
                        return null;
                    });
        }
        var dashboard = service.dashboard(user, wid);
        var analysis = Json.object(dashboard.get("analysis"));
        var estimate = Json.object(analysis.get("estimate"));
        if (((Number) estimate.get("knownSubtotal")).doubleValue() != 2152000
                || ((Number) estimate.get("missingPriceCount")).intValue() != 4) {
            throw new IllegalStateException(
                    "El escenario no tiene los costos/faltantes esperados.");
        }
        return Json.map(
                "userName",
                user.get("name"),
                "workId",
                wid,
                "workName",
                NAME,
                "materials",
                ((List<?>) dashboard.get("materials")).size(),
                "suppliers",
                ((List<?>) dashboard.get("suppliers")).size(),
                "quotes",
                ((List<?>) dashboard.get("quotes")).size(),
                "analysis",
                analysis);
    }

    private static void seed(
            AppService service, Database db, Map<String, Object> user, String wid) {
        var center = supplier(service, user, wid, "Ferretería Demo Centro", "000 000 0001");
        var north = supplier(service, user, wid, "Materiales Demo Norte", "000 000 0002");
        var structure = supplier(service, user, wid, "Depósito Demo Estructuras", "000 000 0003");
        var finishing = supplier(service, user, wid, "Acabados Demo Hogar", "000 000 0004");
        var cement =
                material(service, user, wid, "Cemento gris", "Cimentación", 10, "bultos", 1, 3);
        var sand = material(service, user, wid, "Arena lavada", "Cimentación", 3, "m³", 2, 3);
        var rebar =
                material(
                        service, user, wid, "Varilla corrugada 1/2", "Estructura", 120, "kg", 2, 4);
        var blocks =
                material(
                        service,
                        user,
                        wid,
                        "Bloque estructural",
                        "Mampostería",
                        200,
                        "unidades",
                        8,
                        2);
        material(service, user, wid, "Ladrillo cerámico", "Mampostería", 300, "unidades", 9, 3);
        material(service, user, wid, "Pintura blanca 20 litros", "Acabados", 4, "cuñetes", 21, 2);
        var ceramics =
                material(service, user, wid, "Cerámica para piso", "Acabados", 36, "m²", 18, 5);
        var pipes = material(service, user, wid, "Tubería PVC", "Estructura", 20, "unidades", 4, 2);
        var wire = material(service, user, wid, "Alambre negro", "Estructura", 10, "kg", 5, 1);
        material(service, user, wid, "Malla electrosoldada", "Estructura", 6, "unidades", 2, 3);
        quote(service, user, wid, cement, center, 40000, 3, 2, "Pendiente de decisión");
        quote(service, user, wid, cement, north, 38500, 4, 4, "Rechazada");
        quote(service, user, wid, sand, north, 78000, 3, -1, "En tránsito");
        quote(service, user, wid, rebar, structure, 5800, 4, 1, "Pedido realizado");
        quote(service, user, wid, blocks, structure, 2800, 2, -2, "Recibido");
        quote(service, user, wid, ceramics, finishing, 45000, 5, 6, "Cancelado");
        quote(service, user, wid, pipes, center, 8500, 2, 2, "Cotización recibida");
        quote(service, user, wid, wire, structure, 9200, 1, 1, "Pendiente de decisión");
        service.materialStatus(user, wid, ceramics.get("id").toString(), "Por cotizar");
    }

    private static Map<String, Object> supplier(
            AppService service, Map<String, Object> user, String wid, String name, String phone) {
        return service.addSupplier(
                user,
                wid,
                Json.map(
                        "name",
                        name,
                        "phone",
                        phone,
                        "city",
                        "Medellín, Antioquia",
                        "address",
                        "Dirección ficticia",
                        "notes",
                        "Proveedor ficticio para video; no contactar."));
    }

    private static Map<String, Object> material(
            AppService service,
            Map<String, Object> user,
            String wid,
            String name,
            String stage,
            int quantity,
            String unit,
            int neededIn,
            int lead) {
        return service.addMaterial(
                user,
                wid,
                Json.map(
                        "name",
                        name,
                        "stage",
                        stage,
                        "quantity",
                        quantity,
                        "unit",
                        unit,
                        "neededDate",
                        LocalDate.now().plusDays(neededIn).toString(),
                        "leadDays",
                        lead,
                        "notes",
                        "Registro ficticio preparado para la explicación del video."));
    }

    private static void quote(
            AppService service,
            Map<String, Object> user,
            String wid,
            Map<String, Object> material,
            Map<String, Object> supplier,
            int price,
            int lead,
            int expectedIn,
            String status) {
        var quote =
                service.addQuote(
                        user,
                        wid,
                        Json.map(
                                "materialId",
                                material.get("id"),
                                "supplierId",
                                supplier.get("id"),
                                "quantity",
                                material.get("quantity"),
                                "unitPrice",
                                price,
                                "quotedAt",
                                LocalDate.now().toString(),
                                "leadDays",
                                lead,
                                "expectedDate",
                                LocalDate.now().plusDays(expectedIn).toString(),
                                "notes",
                                "Precio y fechas ficticios para demostración."));
        if (!status.equals("Cotización recibida"))
            service.quoteStatus(user, wid, quote.get("id").toString(), status);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Map<String, Object> state, String collection) {
        return (List<Map<String, Object>>) (List<?>) state.get(collection);
    }
}
