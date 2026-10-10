package co.obraplan;

import co.obraplan.agents.AgentData;
import co.obraplan.agents.KnowledgeAgent;
import co.obraplan.agents.Orchestrator;

import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class AppService {
    private final Database db;
    private final KnowledgeAgent knowledge;
    private final Orchestrator agents = new Orchestrator();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    AppService(Database db, KnowledgeAgent knowledge) {
        this.db = db;
        this.knowledge = knowledge;
        db.onCommit(knowledge::record);
        knowledge.record(db.read());
    }

    Map<String, Object> knowledge(Map<String, Object> user, String workId, String query) {
        work(user, workId);
        return knowledge.query(workId, query);
    }

    Map<String, Object> syncKnowledge(Map<String, Object> user, String workId) {
        work(user, workId);
        knowledge.record(db.read());
        return knowledge.query(workId, "");
    }

    record Login(String token, Map<String, Object> user) {}

    Map<String, Object> saveAnalysis(
            Map<String, Object> user, String workId, Map<String, Object> result) {
        work(user, workId);
        return knowledge.saveAnalysis(workId, result);
    }

    Login login(String email, String password) {
        String clean = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        Map<String, Object> user =
                db.readUsers().stream()
                        .filter(u -> clean.equals(AgentData.str(u.get("email"))))
                        .findFirst()
                        .orElseThrow(() -> new HttpError(401, "Correo o contraseña incorrectos."));
        if (!Password.verify(
                password == null ? "" : password, AgentData.str(user.get("passwordHash"))))
            throw new HttpError(401, "Correo o contraseña incorrectos.");
        String token = UUID.randomUUID().toString();
        sessions.put(token, AgentData.str(user.get("id")));
        return new Login(token, publicUser(user));
    }

    Login register(String name, String email, String password) {
        String e = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (name == null || name.trim().length() < 2)
            throw new HttpError(400, "Ingresa tu nombre.");
        if (!e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new HttpError(400, "Ingresa un correo válido.");
        if (password == null || password.length() < 10)
            throw new HttpError(400, "La contraseña debe tener al menos 10 caracteres.");
        Map<String, Object> user =
                db.write(
                        s -> {
                            List<Map<String, Object>> users = maps(s, "users");
                            if (users.stream().anyMatch(u -> e.equals(u.get("email"))))
                                throw new HttpError(409, "Ya existe una cuenta con ese correo.");
                            Map<String, Object> u =
                                    Json.map(
                                            "id",
                                            id(),
                                            "name",
                                            name.trim(),
                                            "email",
                                            e,
                                            "passwordHash",
                                            Password.hash(password));
                            users.add(u);
                            return u;
                        });
        String t = UUID.randomUUID().toString();
        sessions.put(t, AgentData.str(user.get("id")));
        return new Login(t, publicUser(user));
    }

    void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    Map<String, Object> current(String token) {
        if (token == null || token.isBlank())
            throw new HttpError(401, "Inicia sesión para continuar.");
        String uid = sessions.get(token);
        if (uid == null) throw new HttpError(401, "Inicia sesión para continuar.");
        return db.readUsers().stream()
                .filter(u -> uid.equals(u.get("id")))
                .findFirst()
                .map(this::publicUser)
                .orElseThrow(() -> new HttpError(401, "La sesión ya no es válida."));
    }

    private Map<String, Object> publicUser(Map<String, Object> u) {
        return Json.map("id", u.get("id"), "name", u.get("name"), "email", u.get("email"));
    }

    List<Map<String, Object>> works(Map<String, Object> user) {
        return db.readWorks().stream()
                .filter(w -> Objects.equals(w.get("ownerId"), user.get("id")))
                .toList();
    }

    Map<String, Object> createWork(Map<String, Object> u, Map<String, Object> b) {
        String name = required(b, "name", "Nombre de la obra"),
                address = required(b, "address", "Ubicación"),
                city = required(b, "city", "Ciudad y departamento"),
                start = required(b, "startDate", "Fecha de inicio");
        validDate(start, "Fecha de inicio");
        Map<String, Object> w =
                Json.map(
                        "id",
                        id(),
                        "ownerId",
                        u.get("id"),
                        "name",
                        name,
                        "address",
                        address,
                        "city",
                        city,
                        "startDate",
                        start,
                        "description",
                        str(b, "description"),
                        "stage",
                        empty(str(b, "stage")) ? "Cimentación" : str(b, "stage"),
                        "createdAt",
                        LocalDate.now().toString(),
                        "demo",
                        false);
        db.write(
                s -> {
                    maps(s, "works").add(w);
                    activity(s, w, "Obra creada: " + name);
                    return null;
                });
        return w;
    }

    Map<String, Object> updateWork(Map<String, Object> u, String wid, Map<String, Object> b) {
        work(u, wid);
        Map<String, Object> updated =
                db.write(
                        s -> {
                            Map<String, Object> w = find(maps(s, "works"), wid);
                            if (b.containsKey("stage"))
                                w.put("stage", required(b, "stage", "Etapa"));
                            if (b.containsKey("name")) w.put("name", required(b, "name", "Nombre"));
                            if (b.containsKey("address"))
                                w.put("address", required(b, "address", "Ubicación"));
                            if (b.containsKey("city")) w.put("city", required(b, "city", "Ciudad"));
                            if (b.containsKey("startDate")) {
                                validDate(required(b, "startDate", "Fecha"), "Fecha");
                                w.put("startDate", b.get("startDate"));
                            }
                            activity(s, w, "Información de la obra actualizada.");
                            return w;
                        });
        return updated;
    }

    Map<String, Object> dashboard(Map<String, Object> u, String wid) {
        Map<String, Object> w = work(u, wid);
        List<Map<String, Object>> m = filter(db.readMaterials(), wid),
                q = filter(db.readQuotes(), wid),
                s = filter(db.readSuppliers(), wid);
        return Json.map(
                "work",
                w,
                "materials",
                m,
                "suppliers",
                s,
                "quotes",
                q,
                "activity",
                activityFor(wid, 8),
                "analysis",
                agents.brief(m, s, q));
    }

    Map<String, Object> addMaterial(Map<String, Object> u, String wid, Map<String, Object> b) {
        Map<String, Object> w = work(u, wid);
        String name = required(b, "name", "Material"),
                stage = required(b, "stage", "Etapa"),
                unit = required(b, "unit", "Unidad"),
                date = required(b, "neededDate", "Fecha necesaria");
        double qty = positive(b, "quantity", "Cantidad");
        validDate(date, "Fecha necesaria");
        Integer lead = optionalInt(b.get("leadDays"), "Plazo en días");
        Map<String, Object> m =
                Json.map(
                        "id",
                        id(),
                        "workId",
                        wid,
                        "name",
                        name,
                        "category",
                        str(b, "category"),
                        "stage",
                        stage,
                        "quantity",
                        qty,
                        "unit",
                        unit,
                        "neededDate",
                        date,
                        "leadDays",
                        lead == null ? 0 : lead,
                        "notes",
                        str(b, "notes"),
                        "status",
                        "Por cotizar",
                        "createdAt",
                        LocalDate.now().toString());
        db.write(
                s -> {
                    maps(s, "materials").add(m);
                    activity(s, w, "Material agregado al plan: " + name + ".");
                    return null;
                });
        return m;
    }

    Map<String, Object> updateMaterial(
            Map<String, Object> u, String wid, String mid, Map<String, Object> b) {
        Map<String, Object> w = work(u, wid);
        material(wid, mid);
        return db.write(
                s -> {
                    Map<String, Object> m = find(maps(s, "materials"), mid);
                    for (String k : List.of("name", "stage", "unit"))
                        if (b.containsKey(k)) m.put(k, required(b, k, k));
                    for (String k : List.of("category", "notes"))
                        if (b.containsKey(k)) m.put(k, str(b, k).trim());
                    if (b.containsKey("quantity"))
                        m.put("quantity", positive(b, "quantity", "Cantidad"));
                    if (b.containsKey("neededDate")) {
                        validDate(required(b, "neededDate", "Fecha necesaria"), "Fecha necesaria");
                        m.put("neededDate", b.get("neededDate"));
                    }
                    if (b.containsKey("leadDays")) {
                        Integer n = optionalInt(b.get("leadDays"), "Plazo");
                        m.put("leadDays", n == null ? 0 : n);
                    }
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Material actualizado: " + m.get("name") + ".");
                    return m;
                });
    }

    Map<String, Object> materialStatus(
            Map<String, Object> u, String wid, String mid, String status) {
        work(u, wid);
        material(wid, mid);
        if (!Set.of("Por cotizar", "Cotizado", "Pedido", "Recibido").contains(status))
            throw new HttpError(400, "Estado de material no válido.");
        return db.write(
                s -> {
                    Map<String, Object> m = find(maps(s, "materials"), mid);
                    m.put("status", status);
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Estado de " + m.get("name") + " actualizado a " + status + ".");
                    return m;
                });
    }

    Map<String, Object> addSupplier(Map<String, Object> u, String wid, Map<String, Object> b) {
        Map<String, Object> w = work(u, wid);
        String name = required(b, "name", "Nombre"),
                phone = required(b, "phone", "Teléfono"),
                city = required(b, "city", "Ciudad y departamento"),
                web = str(b, "website");
        if (!web.isBlank()) {
            try {
                URI uri = URI.create(web);
                if (!Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null)
                    throw new Exception();
            } catch (Exception e) {
                throw new HttpError(
                        400, "El enlace debe comenzar con http:// o https:// y ser válido.");
            }
        }
        Map<String, Object> supplier =
                Json.map(
                        "id",
                        id(),
                        "workId",
                        wid,
                        "name",
                        name,
                        "phone",
                        phone,
                        "address",
                        str(b, "address"),
                        "city",
                        city,
                        "website",
                        web,
                        "notes",
                        str(b, "notes"),
                        "createdAt",
                        LocalDate.now().toString());
        db.write(
                s -> {
                    maps(s, "suppliers").add(supplier);
                    activity(s, w, "Ferretería agregada al directorio: " + name + ".");
                    return null;
                });
        return supplier;
    }

    Map<String, Object> updateSupplier(
            Map<String, Object> u, String wid, String sid, Map<String, Object> b) {
        work(u, wid);
        supplier(wid, sid);
        return db.write(
                s -> {
                    Map<String, Object> sup = find(maps(s, "suppliers"), sid);
                    for (String k : List.of("name", "phone", "city"))
                        if (b.containsKey(k)) sup.put(k, required(b, k, k));
                    for (String k : List.of("address", "notes"))
                        if (b.containsKey(k)) sup.put(k, str(b, k).trim());
                    if (b.containsKey("website")) {
                        String url = str(b, "website");
                        if (!url.isBlank())
                            try {
                                URI uri = URI.create(url);
                                if (!Set.of("http", "https").contains(uri.getScheme())
                                        || uri.getHost() == null) throw new Exception();
                            } catch (Exception e) {
                                throw new HttpError(400, "Enlace web no válido.");
                            }
                        sup.put("website", url);
                    }
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Ferretería actualizada: " + sup.get("name") + ".");
                    return sup;
                });
    }

    void deleteSupplier(Map<String, Object> u, String wid, String sid) {
        work(u, wid);
        Map<String, Object> sup = supplier(wid, sid);
        db.write(
                s -> {
                    if (maps(s, "quotes").stream()
                            .anyMatch(
                                    q ->
                                            Objects.equals(q.get("workId"), wid)
                                                    && Objects.equals(q.get("supplierId"), sid)))
                        throw new HttpError(
                                409,
                                "No se puede eliminar: hay cotizaciones vinculadas. Puedes editar"
                                        + " sus datos.");
                    Map<String, Object> curr = find(maps(s, "suppliers"), sid);
                    maps(s, "suppliers").remove(curr);
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Ferretería eliminada: " + sup.get("name") + ".");
                    return null;
                });
    }

    Map<String, Object> addQuote(Map<String, Object> u, String wid, Map<String, Object> b) {
        Map<String, Object> w = work(u, wid);
        String mid = required(b, "materialId", "Material"),
                sid = required(b, "supplierId", "Ferretería"),
                quoted = required(b, "quotedAt", "Fecha de cotización");
        Map<String, Object> m = material(wid, mid), sup = supplier(wid, sid);
        double qty = positive(b, "quantity", "Cantidad");
        double price = nonnegative(b, "unitPrice", "Precio unitario");
        validDate(quoted, "Fecha de cotización");
        Integer lead = optionalInt(b.get("leadDays"), "Plazo");
        String expected = str(b, "expectedDate");
        if (!expected.isBlank()) validDate(expected, "Fecha estimada");
        Map<String, Object> q =
                Json.map(
                        "id",
                        id(),
                        "workId",
                        wid,
                        "materialId",
                        mid,
                        "supplierId",
                        sid,
                        "quantity",
                        qty,
                        "unitPrice",
                        price,
                        "quotedAt",
                        quoted,
                        "leadDays",
                        lead,
                        "expectedDate",
                        expected.isBlank()
                                ? (lead == null
                                        ? null
                                        : LocalDate.parse(quoted).plusDays(lead).toString())
                                : expected,
                        "status",
                        "Cotización recibida",
                        "notes",
                        str(b, "notes"),
                        "createdAt",
                        LocalDate.now().toString());
        db.write(
                s -> {
                    maps(s, "quotes").add(q);
                    Map<String, Object> curr = find(maps(s, "materials"), mid);
                    curr.put("status", "Cotizado");
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Cotización registrada: "
                                    + m.get("name")
                                    + " · "
                                    + sup.get("name")
                                    + ".");
                    return null;
                });
        return q;
    }

    Map<String, Object> quoteStatus(Map<String, Object> u, String wid, String qid, String status) {
        work(u, wid);
        quote(wid, qid);
        if (!Set.of(
                        "Pendiente de decisión",
                        "Pedido realizado",
                        "En tránsito",
                        "Recibido",
                        "Cancelado",
                        "Rechazada")
                .contains(status)) throw new HttpError(400, "Estado de cotización no válido.");
        return db.write(
                s -> {
                    Map<String, Object> q = find(maps(s, "quotes"), qid);
                    q.put("status", status);
                    Map<String, Object> m =
                            find(maps(s, "materials"), AgentData.str(q.get("materialId")));
                    if (status.equals("Pedido realizado") || status.equals("En tránsito"))
                        m.put("status", "Pedido");
                    if (status.equals("Recibido")) m.put("status", "Recibido");
                    activity(
                            s,
                            find(maps(s, "works"), wid),
                            "Estado de cotización para "
                                    + m.get("name")
                                    + " actualizado a "
                                    + status
                                    + ".");
                    return q;
                });
    }

    List<Map<String, Object>> supplierView(Map<String, Object> u, String wid) {
        work(u, wid);
        Map<String, Object> state = db.read();
        List<Map<String, Object>> out = new ArrayList<>();
        for (var sup : filter(maps(state, "suppliers"), wid)) {
            Map<String, Object> x = new LinkedHashMap<>(sup);
            x.put(
                    "quoteCount",
                    filter(maps(state, "quotes"), wid).stream()
                            .filter(q -> Objects.equals(q.get("supplierId"), sup.get("id")))
                            .count());
            out.add(x);
        }
        return out;
    }

    private Map<String, Object> work(Map<String, Object> u, String id) {
        return db.readWorks().stream()
                .filter(
                        w ->
                                Objects.equals(w.get("id"), id)
                                        && Objects.equals(w.get("ownerId"), u.get("id")))
                .findFirst()
                .orElseThrow(() -> new HttpError(404, "No se encontró la obra."));
    }

    private Map<String, Object> material(String wid, String id) {
        return db.readMaterials().stream()
                .filter(
                        x ->
                                Objects.equals(x.get("workId"), wid)
                                        && Objects.equals(x.get("id"), id))
                .findFirst()
                .orElseThrow(() -> new HttpError(404, "No se encontró el material."));
    }

    private Map<String, Object> supplier(String wid, String id) {
        return db.readSuppliers().stream()
                .filter(
                        x ->
                                Objects.equals(x.get("workId"), wid)
                                        && Objects.equals(x.get("id"), id))
                .findFirst()
                .orElseThrow(() -> new HttpError(404, "No se encontró la ferretería."));
    }

    private Map<String, Object> quote(String wid, String id) {
        return db.readQuotes().stream()
                .filter(
                        x ->
                                Objects.equals(x.get("workId"), wid)
                                        && Objects.equals(x.get("id"), id))
                .findFirst()
                .orElseThrow(() -> new HttpError(404, "No se encontró la cotización."));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Map<String, Object> s, String key) {
        return (List<Map<String, Object>>) (List<?>) s.get(key);
    }

    private static Map<String, Object> find(List<Map<String, Object>> a, String id) {
        return a.stream()
                .filter(x -> Objects.equals(x.get("id"), id))
                .findFirst()
                .orElseThrow(() -> new HttpError(404, "El registro ya no existe."));
    }

    private static List<Map<String, Object>> filter(List<Map<String, Object>> a, String wid) {
        return a.stream().filter(x -> Objects.equals(x.get("workId"), wid)).toList();
    }

    private static String required(Map<String, Object> b, String key, String label) {
        String v = str(b, key).trim();
        if (v.isEmpty()) throw new HttpError(400, "El campo " + label + " es obligatorio.");
        return v;
    }

    private static String str(Map<String, Object> b, String k) {
        return b.get(k) == null ? "" : b.get(k).toString();
    }

    private static boolean empty(String x) {
        return x == null || x.isBlank();
    }

    private static double positive(Map<String, Object> b, String k, String label) {
        double n = nonnegative(b, k, label);
        if (n <= 0) throw new HttpError(400, label + " debe ser mayor que cero.");
        return n;
    }

    private static double nonnegative(Map<String, Object> b, String k, String label) {
        try {
            double n = Double.parseDouble(required(b, k, label));
            if (!Double.isFinite(n) || n < 0) throw new Exception();
            return n;
        } catch (HttpError e) {
            throw e;
        } catch (Exception e) {
            throw new HttpError(400, label + " debe ser un número válido no negativo.");
        }
    }

    private static Integer optionalInt(Object o, String label) {
        if (o == null || o.toString().isBlank()) return null;
        try {
            int n = Integer.parseInt(o.toString());
            if (n < 0) throw new Exception();
            return n;
        } catch (Exception e) {
            throw new HttpError(400, label + " debe ser un entero no negativo.");
        }
    }

    private static void validDate(String s, String label) {
        try {
            LocalDate.parse(s);
        } catch (Exception e) {
            throw new HttpError(400, label + " debe tener una fecha válida (AAAA-MM-DD).");
        }
    }

    private static String id() {
        return UUID.randomUUID().toString();
    }

    private void activity(Map<String, Object> s, Map<String, Object> w, String message) {
        maps(s, "activities")
                .add(
                        0,
                        Json.map(
                                "id",
                                id(),
                                "workId",
                                w.get("id"),
                                "message",
                                message,
                                "createdAt",
                                LocalDateTime.now().toString()));
    }

    private List<Map<String, Object>> activityFor(String wid, int limit) {
        return db.readActivities().stream()
                .filter(a -> Objects.equals(a.get("workId"), wid))
                .limit(limit)
                .toList();
    }
}
