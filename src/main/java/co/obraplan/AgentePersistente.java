package co.obraplan;

import co.obraplan.agents.ai.AgentTeam;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Ciclo real: disparador -> memoria -> modelo/herramientas -> memoria durable. */
final class AgentePersistente implements AutoCloseable {
    private final Database datos;
    private final AppService servicio;
    private final AgentTeam equipo;
    private final MemoriaAgente memoria;
    private final ScheduledExecutorService reloj = Executors.newSingleThreadScheduledExecutor();

    AgentePersistente(Database datos, AppService servicio, AgentTeam equipo) {
        this.datos = datos;
        this.servicio = servicio;
        this.equipo = equipo;
        this.memoria = new MemoriaAgente(datos);
    }

    void iniciar() {
        reloj.scheduleWithFixedDelay(
                () -> {
                    try {
                        procesarPendiente();
                    } catch (RuntimeException error) {
                        System.err.println(
                                "Revisión del agente pendiente; consulta el historial autorizado.");
                    }
                },
                5,
                15,
                TimeUnit.SECONDS);
    }

    Map<String, Object> ejecutar(
            Map<String, Object> usuario, String obra, String pregunta, String disparador) {
        var herramientas = new WorkAgentTools(servicio, usuario, obra);
        var resultado = equipo.ask(usuario.get("id").toString(), obra, pregunta, herramientas);
        memoria.guardarMemoria(usuario.get("id").toString(), obra, disparador, resultado);
        resultado = new LinkedHashMap<>(resultado);
        resultado.put(
                "ciclo",
                Json.map(
                        "disparador",
                        disparador,
                        "memoriasRecuperadas",
                        ((List<?>) herramientas.recuperarMemoria().get("recuerdos")).size(),
                        "modelo",
                        resultado.get("model"),
                        "memoriaGuardada",
                        resultado.get("complete"),
                        "pasos",
                        List.of(
                                "Disparador",
                                "recuperarMemoria",
                                "Construir contexto",
                                "crearSolicitudModelo",
                                "llamarApi",
                                "guardarMemoria")));
        return resultado;
    }

    Map<String, Object> estado(Map<String, Object> usuario, String obra) {
        servicio.dashboard(usuario, obra);
        var configuracion =
                datos.readWorks().stream()
                        .filter(w -> obra.equals(w.get("id")))
                        .findFirst()
                        .orElseThrow();
        var trabajos =
                datos.readList("agentJobs").stream()
                        .filter(
                                t ->
                                        obra.equals(t.get("workId"))
                                                && usuario.get("id").equals(t.get("ownerId")))
                        .sorted(
                                Comparator.comparing(
                                        t -> t.get("createdAt").toString(),
                                        Comparator.reverseOrder()))
                        .limit(12)
                        .toList();
        return Json.map(
                "enabled",
                Boolean.TRUE.equals(configuracion.get("agentEnabled")),
                "configured",
                equipo.status().get("configured"),
                "jobs",
                trabajos,
                "memory",
                memoria.recuperarMemoria(usuario.get("id").toString(), obra),
                "notice",
                "Máximo 3 eventos por obra y día. Los pendientes se recuperan al iniciar; una"
                        + " ejecución interrumpida no se repite automáticamente.");
    }

    void configurar(Map<String, Object> usuario, String obra, boolean activar) {
        servicio.dashboard(usuario, obra);
        datos.write(
                estado -> {
                    var registro =
                            DisparadoresAgente.lista(estado, "works").stream()
                                    .filter(w -> obra.equals(w.get("id")))
                                    .findFirst()
                                    .orElseThrow();
                    registro.put("agentEnabled", activar);
                    if (activar) DisparadoresAgente.encolar(estado, registro, "inicio_seguimiento");
                    else
                        for (var trabajo : DisparadoresAgente.lista(estado, "agentJobs")) {
                            if (obra.equals(trabajo.get("workId"))
                                    && "pendiente".equals(trabajo.get("status")))
                                trabajo.put("status", "cancelado");
                        }
                    return null;
                });
    }

    void procesarPendiente() {
        if (!Boolean.TRUE.equals(equipo.status().get("configured"))) return;
        if (datos.readList("agentJobs").stream()
                .noneMatch(t -> List.of("pendiente", "en_curso").contains(t.get("status")))) return;
        Map<String, Object> trabajo =
                datos.write(
                        estado -> {
                            var trabajos = DisparadoresAgente.lista(estado, "agentJobs");
                            for (var t : trabajos) {
                                if ("en_curso".equals(t.get("status"))
                                        && Instant.parse(t.get("leaseUntil").toString())
                                                .isBefore(Instant.now())) {
                                    t.put("status", "interrumpido");
                                    t.put(
                                            "error",
                                            "El proceso terminó sin confirmar resultado. Revisa"
                                                    + " antes de solicitar otro análisis.");
                                }
                            }
                            // No solapar workers entre instancias.
                            if (trabajos.stream().anyMatch(t -> "en_curso".equals(t.get("status"))))
                                return null;
                            for (var t : trabajos) {
                                if (!"pendiente".equals(t.get("status"))
                                        || Instant.parse(t.get("readyAt").toString())
                                                .isAfter(Instant.now())) continue;
                                t.put("status", "en_curso");
                                t.put("leaseUntil", Instant.now().plusSeconds(240).toString());
                                return new LinkedHashMap<>(t);
                            }
                            return null;
                        });
        if (trabajo == null) return;
        Map<String, Object> resultado = null;
        String error = "";
        try {
            resultado =
                    ejecutar(
                            Json.map("id", trabajo.get("ownerId")),
                            trabajo.get("workId").toString(),
                            "Se activó el seguimiento por "
                                    + trabajo.get("trigger")
                                    + ". Revisa únicamente logística y seguimiento: próximos"
                                    + " pendientes y siguiente paso manual. Consulta datos actuales"
                                    + " y distingue los recuerdos históricos.",
                            trabajo.get("trigger").toString());
        } catch (RuntimeException fallo) {
            error =
                    fallo instanceof HttpError
                            ? fallo.getMessage()
                            : "No se pudo completar la ejecución. Los datos permanecen guardados.";
        }
        Map<String, Object> finalResultado = resultado;
        String finalError = error;
        datos.write(
                estado -> {
                    var t =
                            DisparadoresAgente.lista(estado, "agentJobs").stream()
                                    .filter(j -> trabajo.get("id").equals(j.get("id")))
                                    .findFirst()
                                    .orElseThrow();
                    t.put(
                            "status",
                            finalResultado == null
                                    ? "error"
                                    : Boolean.TRUE.equals(finalResultado.get("complete"))
                                            ? "completado"
                                            : "incompleto");
                    t.put("finishedAt", Instant.now().toString());
                    if (finalResultado != null) t.put("result", finalResultado);
                    else t.put("error", finalError);
                    return null;
                });
    }

    public void close() {
        reloj.shutdownNow();
    }
}
