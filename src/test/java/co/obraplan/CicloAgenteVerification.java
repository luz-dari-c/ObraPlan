package co.obraplan;

import co.obraplan.agents.KnowledgeAgent;
import co.obraplan.agents.ai.*;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class CicloAgenteVerification {
    private static final List<String> comprobaciones = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path raiz = Path.of(args[0]);
        Path temporal = raiz.resolve("output/verification/ciclo-" + UUID.randomUUID());
        Files.createDirectories(temporal);
        Path archivo = temporal.resolve("datos.json");
        Database datos = abrir(archivo);
        if (System.getenv("OBRAPLAN_TEST_SQL") != null) {
            datos.write(
                    estado -> {
                        estado.putAll(new Database(archivo).read());
                        return null;
                    });
        }
        AppService servicio = new AppService(datos, new KnowledgeAgent(temporal, null));
        Map<String, Object> usuario = Json.map("id", "u-demo");
        servicio.addMaterial(usuario, "w-demo", material("Sin activar"));
        comprobar("Sin consentimiento no hay eventos", datos.readList("agentJobs").isEmpty());
        var cliente = simulado();
        try (var agente = new AgentePersistente(datos, servicio, new AgentTeam(cliente))) {
            agente.configurar(usuario, "w-demo", true);
            servicio.addMaterial(usuario, "w-demo", material("Cambio uno"));
            servicio.addMaterial(usuario, "w-demo", material("Cambio dos"));
            comprobar(
                    "Tres disparos agrupados en un pendiente durable",
                    datos.readList("agentJobs").size() == 1);
            datos.write(
                    estado -> {
                        DisparadoresAgente.lista(estado, "agentJobs")
                                .getFirst()
                                .put("readyAt", Instant.EPOCH.toString());
                        return null;
                    });
        }
        Database recuperada = abrir(archivo);
        AppService reiniciado = new AppService(recuperada, new KnowledgeAgent(temporal, null));
        try (var agente = new AgentePersistente(recuperada, reiniciado, new AgentTeam(cliente))) {
            try (var segundoWorker =
                    new AgentePersistente(recuperada, reiniciado, new AgentTeam(cliente))) {
                java.util.concurrent.CompletableFuture.allOf(
                                java.util.concurrent.CompletableFuture.runAsync(
                                        agente::procesarPendiente),
                                java.util.concurrent.CompletableFuture.runAsync(
                                        segundoWorker::procesarPendiente))
                        .join();
            }
            comprobar(
                    "Pendiente continúa tras reiniciar y llama al modelo",
                    cliente.count == 4
                            && "completado"
                                    .equals(
                                            recuperada
                                                    .readList("agentJobs")
                                                    .getFirst()
                                                    .get("status")));
            agente.procesarPendiente();
            comprobar("Evento completado no se repite", cliente.count == 4);
        }
        var memoria = new MemoriaAgente(abrir(archivo));
        comprobar(
                "Memoria recuperable tras reinicio",
                ((List<?>) memoria.recuperarMemoria("u-demo", "w-demo").get("recuerdos")).size()
                        == 1);
        comprobar(
                "Memoria aislada por propietario",
                ((List<?>) memoria.recuperarMemoria("otro", "w-demo").get("recuerdos")).isEmpty());
        var segunda = simulado();
        try (var agente = new AgentePersistente(recuperada, reiniciado, new AgentTeam(segunda))) {
            var resultado = agente.ejecutar(usuario, "w-demo", "Revisa costos", "consulta_usuario");
            comprobar(
                    "El JSON enviado contiene memoria real y configuración",
                    Json.stringify(segunda.requests.getFirst()).contains("Resultado simulado")
                            && segunda.requests.getFirst().containsKey("generationConfig"));
            comprobar(
                    "Respuesta muestra ciclo y recuerdo recuperado",
                    ((Number) Json.object(resultado.get("ciclo")).get("memoriasRecuperadas"))
                                    .intValue()
                            == 1);
            try {
                agente.estado(Json.map("id", "otro"), "w-demo");
                throw new AssertionError("Acceso cruzado");
            } catch (HttpError esperado) {
                comprobar("Historial requiere propietario", esperado.status == 404);
            }
            agente.configurar(usuario, "w-demo", true);
            agente.configurar(usuario, "w-demo", false);
            comprobar(
                    "Desactivar cancela pendientes",
                    recuperada.readList("agentJobs").stream()
                            .noneMatch(t -> "pendiente".equals(t.get("status"))));
        }
        recuperada.write(
                estado -> {
                    DisparadoresAgente.lista(estado, "agentJobs")
                            .add(
                                    Json.map(
                                            "id",
                                            "interrumpido-test",
                                            "ownerId",
                                            "u-demo",
                                            "workId",
                                            "w-demo",
                                            "createdAt",
                                            Instant.now().toString(),
                                            "status",
                                            "en_curso",
                                            "leaseUntil",
                                            Instant.EPOCH.toString()));
                    return null;
                });
        var noReintentar = simulado();
        try (var agente =
                new AgentePersistente(recuperada, reiniciado, new AgentTeam(noReintentar))) {
            agente.procesarPendiente();
            comprobar(
                    "Reserva vencida se marca interrumpida sin repetir llamada",
                    noReintentar.count == 0
                            && recuperada.readList("agentJobs").stream()
                                    .anyMatch(t -> "interrumpido".equals(t.get("status"))));
            for (int n = 0; n < 5; n++) {
                agente.configurar(usuario, "w-demo", true);
                agente.configurar(usuario, "w-demo", false);
            }
            comprobar(
                    "Alternar activación no reinicia el límite diario",
                    recuperada.readList("agentJobs").stream()
                                    .filter(t -> t.containsKey("day"))
                                    .count()
                            == 3);
        }
        Files.writeString(
                temporal.resolve("report.json"),
                Json.stringify(
                        Json.map(
                                "checks",
                                comprobaciones,
                                "provider",
                                "Simulado, sin llamadas externas")));
        System.out.println(
                comprobaciones.size() + " comprobaciones de ciclo persistente correctas.");
    }

    private static Database abrir(Path archivo) {
        if (System.getenv("OBRAPLAN_TEST_SQL") == null) return new Database(archivo);
        String url = System.getenv("DATABASE_URL");
        if (url == null
                || !url.matches(
                        "postgresql://obraplan:test@127\\.0\\.0\\.1:[0-9]+/ciclo_verificacion_[a-z0-9]+"))
            throw new IllegalStateException(
                    "La prueba SQL requiere una base local aislada con prefijo"
                            + " ciclo_verificacion_.");
        return new Database(new AlmacenPostgres(url));
    }

    private static Map<String, Object> material(String nombre) {
        return Json.map(
                "name",
                nombre,
                "stage",
                "Cimentación",
                "quantity",
                1,
                "unit",
                "bulto",
                "neededDate",
                "2026-12-01",
                "leadDays",
                3);
    }

    private static AgentVerification.Scripted simulado() {
        return new AgentVerification.Scripted(
                List.of(
                        AgentVerification.calls("consultar_costos"),
                        AgentVerification.calls("calcular_costos"),
                        AgentVerification.text("Subtotal simulado"),
                        AgentVerification.text("Resultado simulado")));
    }

    private static void comprobar(String nombre, boolean correcto) {
        if (!correcto) throw new AssertionError(nombre);
        comprobaciones.add(nombre);
    }
}
