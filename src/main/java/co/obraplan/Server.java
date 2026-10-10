package co.obraplan;

import co.obraplan.agents.KnowledgeAgent;
import co.obraplan.agents.ai.AgentTeam;
import co.obraplan.agents.ai.GeminiClient;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

public final class Server {
    private static final int BODY_LIMIT = 1_000_000;
    private final AppService service;
    private final Path staticDir;
    private final AgentTeam team = new AgentTeam(new GeminiClient());

    private Server(AppService service, Path staticDir) {
        this.service = service;
        this.staticDir = staticDir;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("OBRAPLAN_PORT", "8080"));
        Path root = Path.of(System.getProperty("user.dir"));
        Path data =
                Path.of(
                        System.getenv()
                                .getOrDefault(
                                        "OBRAPLAN_DATA",
                                        root.resolve("data/obraplan.json").toString()));
        Path stat =
                Path.of(
                        System.getenv()
                                .getOrDefault(
                                        "OBRAPLAN_STATIC",
                                        root.resolve("src/main/resources/static").toString()));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        String vaultPath = System.getenv().getOrDefault("OBRAPLAN_VAULT", "");
        KnowledgeAgent knowledge =
                new KnowledgeAgent(root, vaultPath.isBlank() ? null : Path.of(vaultPath));
        Server app = new Server(new AppService(new Database(data), knowledge), stat);
        server.createContext("/", app::handle);
        server.setExecutor(
                Executors.newFixedThreadPool(
                        Math.max(4, Runtime.getRuntime().availableProcessors())));
        server.start();
        System.out.println(
                "ObraPlan disponible en http://127.0.0.1:"
                        + port
                        + "  (datos: "
                        + data.toAbsolutePath()
                        + ")");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    private void handle(HttpExchange x) throws IOException {
        addHeaders(x);
        String path = x.getRequestURI().getPath();
        try {
            if (path.startsWith("/api/")) {
                api(x);
                return;
            }
            if (path.equals("/") || path.equals("/index.html")) {
                sendFile(x, staticDir.resolve("index.html"));
                return;
            }
            if (path.startsWith("/assets/")) {
                sendFile(x, staticDir.resolve(path.substring("/assets/".length())).normalize());
                return;
            }
            sendText(x, 404, "Not found");
        } catch (HttpError e) {
            sendJson(x, e.status, Json.map("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            sendJson(x, 400, Json.map("error", "Solicitud no válida."));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(
                    x,
                    500,
                    Json.map(
                            "error",
                            "Ocurrió un error interno. Revisa los registros del servidor."));
        }
    }

    private void api(HttpExchange x) throws IOException {
        String method = x.getRequestMethod().toUpperCase(Locale.ROOT),
                p = x.getRequestURI().getPath();
        Map<String, Object> b = body(x);
        String token = cookie(x, "OPSESSION");
        if (p.equals("/api/health") && method.equals("GET")) {
            sendJson(x, 200, Json.map("status", "ok", "app", "ObraPlan"));
            return;
        }
        if (p.equals("/api/auth/login") && method.equals("POST")) {
            AppService.Login l = service.login(str(b, "email"), str(b, "password"));
            session(x, l.token());
            sendJson(x, 200, Json.map("user", l.user()));
            return;
        }
        if (p.equals("/api/auth/register") && method.equals("POST")) {
            AppService.Login l =
                    service.register(str(b, "name"), str(b, "email"), str(b, "password"));
            session(x, l.token());
            sendJson(x, 201, Json.map("user", l.user()));
            return;
        }
        if (p.equals("/api/auth/logout") && method.equals("POST")) {
            service.logout(token);
            x.getResponseHeaders()
                    .add("Set-Cookie", "OPSESSION=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
            sendJson(x, 200, Json.map("ok", true));
            return;
        }
        if (p.equals("/api/auth/me") && method.equals("GET")) {
            sendJson(x, 200, Json.map("user", service.current(token)));
            return;
        }
        Map<String, Object> user = service.current(token);
        if (p.equals("/api/assistant/status") && method.equals("GET")) {
            sendJson(x, 200, team.status());
            return;
        }
        String[] a = p.substring(5).split("/");
        if (a.length == 1 && a[0].equals("works")) {
            if (method.equals("GET")) {
                sendJson(x, 200, Json.map("works", service.works(user)));
                return;
            }
            if (method.equals("POST")) {
                sendJson(x, 201, Json.map("work", service.createWork(user, b)));
                return;
            }
        }
        if (a.length >= 2 && a[0].equals("works")) {
            String wid = decode(a[1]);
            if (a.length == 3 && a[2].equals("assistant") && method.equals("POST")) {
                var tools = new WorkAgentTools(service, user, wid);
                sendJson(x, 200, team.ask(str(user, "id"), wid, str(b, "question"), tools));
                return;
            }
            if (a.length == 4
                    && a[2].equals("assistant")
                    && a[3].equals("memory")
                    && method.equals("POST")) {
                var tools = new WorkAgentTools(service, user, wid);
                sendJson(x, 200, team.remember(str(user, "id"), wid, str(b, "runId"), tools));
                return;
            }
            if (a.length == 3 && a[2].equals("knowledge") && method.equals("GET")) {
                String query = x.getRequestURI().getRawQuery();
                String question = "";
                if (query != null) {
                    for (String pair : query.split("&")) {
                        String[] parts = pair.split("=", 2);
                        if (parts.length == 2 && parts[0].equals("q")) question = decode(parts[1]);
                    }
                }
                sendJson(x, 200, service.knowledge(user, wid, question));
                return;
            }
            if (a.length == 4
                    && a[2].equals("knowledge")
                    && a[3].equals("sync")
                    && method.equals("POST")) {
                sendJson(x, 200, service.syncKnowledge(user, wid));
                return;
            }
            if (a.length == 3 && a[2].equals("dashboard") && method.equals("GET")) {
                sendJson(x, 200, service.dashboard(user, wid));
                return;
            }
            if (a.length == 2 && method.equals("PATCH")) {
                sendJson(x, 200, Json.map("work", service.updateWork(user, wid, b)));
                return;
            }
            if (a.length == 3 && a[2].equals("materials") && method.equals("POST")) {
                sendJson(x, 201, Json.map("material", service.addMaterial(user, wid, b)));
                return;
            }
            if (a.length == 4 && a[2].equals("materials")) {
                String mid = decode(a[3]);
                if (method.equals("PATCH")) {
                    sendJson(
                            x,
                            200,
                            Json.map("material", service.updateMaterial(user, wid, mid, b)));
                    return;
                }
            }
            if (a.length == 5
                    && a[2].equals("materials")
                    && a[4].equals("status")
                    && method.equals("PATCH")) {
                sendJson(
                        x,
                        200,
                        Json.map(
                                "material",
                                service.materialStatus(user, wid, decode(a[3]), str(b, "status"))));
                return;
            }
            if (a.length == 3 && a[2].equals("suppliers")) {
                if (method.equals("GET")) {
                    sendJson(x, 200, Json.map("suppliers", service.supplierView(user, wid)));
                    return;
                }
                if (method.equals("POST")) {
                    sendJson(x, 201, Json.map("supplier", service.addSupplier(user, wid, b)));
                    return;
                }
            }
            if (a.length == 4 && a[2].equals("suppliers")) {
                String sid = decode(a[3]);
                if (method.equals("PATCH")) {
                    sendJson(
                            x,
                            200,
                            Json.map("supplier", service.updateSupplier(user, wid, sid, b)));
                    return;
                }
                if (method.equals("DELETE")) {
                    service.deleteSupplier(user, wid, sid);
                    sendJson(x, 200, Json.map("ok", true));
                    return;
                }
            }
            if (a.length == 3 && a[2].equals("quotes")) {
                if (method.equals("POST")) {
                    sendJson(x, 201, Json.map("quote", service.addQuote(user, wid, b)));
                    return;
                }
                if (method.equals("GET")) {
                    sendJson(
                            x, 200, Json.map("quotes", service.dashboard(user, wid).get("quotes")));
                    return;
                }
            }
            if (a.length == 5
                    && a[2].equals("quotes")
                    && a[4].equals("status")
                    && method.equals("PATCH")) {
                sendJson(
                        x,
                        200,
                        Json.map(
                                "quote",
                                service.quoteStatus(user, wid, decode(a[3]), str(b, "status"))));
                return;
            }
        }
        sendJson(x, 404, Json.map("error", "Ruta no encontrada."));
    }

    private Map<String, Object> body(HttpExchange x) throws IOException {
        if (!Set.of("POST", "PATCH", "PUT").contains(x.getRequestMethod().toUpperCase(Locale.ROOT)))
            return new HashMap<>();
        byte[] bytes = x.getRequestBody().readNBytes(BODY_LIMIT + 1);
        if (bytes.length > BODY_LIMIT)
            throw new HttpError(413, "La solicitud es demasiado grande.");
        if (bytes.length == 0) return new HashMap<>();
        Object v = Json.parse(new String(bytes, StandardCharsets.UTF_8));
        if (!(v instanceof Map<?, ?>)) throw new HttpError(400, "Se esperaba un objeto JSON.");
        return Json.object(v);
    }

    private void sendFile(HttpExchange x, Path file) throws IOException {
        Path base = staticDir.toAbsolutePath().normalize();
        Path resolved = file.toAbsolutePath().normalize();
        if (!resolved.startsWith(base) || !Files.isRegularFile(resolved)) {
            sendText(x, 404, "Not found");
            return;
        }
        String type =
                resolved.toString().endsWith(".css")
                        ? "text/css; charset=utf-8"
                        : resolved.toString().endsWith(".js")
                                ? "text/javascript; charset=utf-8"
                                : "text/html; charset=utf-8";
        x.getResponseHeaders().set("Content-Type", type);
        byte[] bytes = Files.readAllBytes(resolved);
        x.sendResponseHeaders(200, bytes.length);
        try (OutputStream o = x.getResponseBody()) {
            o.write(bytes);
        }
    }

    private void sendJson(HttpExchange x, int status, Object o) throws IOException {
        byte[] bytes = Json.stringify(o).getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        x.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = x.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void sendText(HttpExchange x, int status, String text) throws IOException {
        byte[] b = text.getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        x.sendResponseHeaders(status, b.length);
        try (OutputStream o = x.getResponseBody()) {
            o.write(b);
        }
    }

    private void addHeaders(HttpExchange x) {
        x.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        x.getResponseHeaders().set("X-Frame-Options", "DENY");
        x.getResponseHeaders().set("Referrer-Policy", "strict-origin-when-cross-origin");
        x.getResponseHeaders().set("Cache-Control", "no-store");
    }

    private void session(HttpExchange x, String token) {
        x.getResponseHeaders()
                .add("Set-Cookie", "OPSESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax");
    }

    private static String cookie(HttpExchange x, String name) {
        String c = x.getRequestHeaders().getFirst("Cookie");
        if (c == null) return null;
        for (String p : c.split(";")) {
            String[] kv = p.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) return kv[1];
        }
        return null;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? "" : v.toString();
    }
}
