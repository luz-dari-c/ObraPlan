package co.obraplan;

import java.time.Instant;
import java.util.*;

/** Outbox en la misma transacción que el material/pedido. Sin broker externo. */
final class DisparadoresAgente {
    private DisparadoresAgente() {}

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> lista(Map<String, Object> estado, String clave) {
        return (List<Map<String, Object>>) estado.computeIfAbsent(clave, k -> new ArrayList<>());
    }

    static void detectarCambios(Map<String, Object> anterior, Map<String, Object> siguiente) {
        for (var obra : lista(siguiente, "works")) {
            if (!Boolean.TRUE.equals(obra.get("agentEnabled"))) continue;
            String id = obra.get("id").toString();
            for (String coleccion : List.of("materials", "quotes")) {
                var antes = registros(anterior, coleccion, id);
                var despues = registros(siguiente, coleccion, id);
                if (!Json.stringify(antes).equals(Json.stringify(despues))) {
                    encolar(
                            siguiente,
                            obra,
                            coleccion.equals("materials")
                                    ? "material_guardado"
                                    : "pedido_o_cotizacion_actualizado");
                    break;
                }
            }
        }
    }

    private static List<?> registros(Map<String, Object> estado, String coleccion, String obra) {
        return ((List<?>) estado.getOrDefault(coleccion, List.of()))
                .stream().filter(r -> obra.equals(Json.object(r).get("workId"))).toList();
    }

    static void encolar(Map<String, Object> estado, Map<String, Object> obra, String motivo) {
        var trabajos = lista(estado, "agentJobs");
        String idObra = obra.get("id").toString();
        var pendiente =
                trabajos.stream()
                        .filter(
                                t ->
                                        idObra.equals(t.get("workId"))
                                                && "pendiente".equals(t.get("status")))
                        .findFirst();
        if (pendiente.isPresent()) {
            pendiente.get().put("trigger", motivo);
            return; // Agrupar cambios sin retrasar indefinidamente el primer evento.
        }
        String dia = java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota")).toString();
        if (trabajos.stream()
                        .filter(t -> idObra.equals(t.get("workId")) && dia.equals(t.get("day")))
                        .count()
                >= 3) return;
        trabajos.add(
                Json.map(
                        "id",
                        UUID.randomUUID().toString(),
                        "workId",
                        idObra,
                        "ownerId",
                        obra.get("ownerId"),
                        "trigger",
                        motivo,
                        "status",
                        "pendiente",
                        "day",
                        dia,
                        "createdAt",
                        Instant.now().toString(),
                        "readyAt",
                        Instant.now().plusSeconds(20).toString()));
    }
}
