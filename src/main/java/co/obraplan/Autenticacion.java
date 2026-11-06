package co.obraplan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Sesiones persistentes de doce horas. Solo se guarda el hash del token. */
final class Autenticacion {
    private final Database datos;

    Autenticacion(Database datos) {
        this.datos = datos;
    }

    String crear(String usuario) {
        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        datos.write(
                estado -> {
                    var sesiones = sesiones(estado);
                    sesiones.removeIf(
                            sesion ->
                                    Instant.parse(sesion.get("expiresAt").toString())
                                            .isBefore(Instant.now()));
                    sesiones.add(
                            Json.map(
                                    "id",
                                    resumen(token),
                                    "userId",
                                    usuario,
                                    "expiresAt",
                                    Instant.now().plus(12, ChronoUnit.HOURS).toString()));
                    return null;
                });
        return token;
    }

    String usuario(String token) {
        if (token == null || token.length() > 100) return null;
        return datos.readList("sessions").stream()
                .filter(s -> Objects.equals(s.get("id"), resumen(token)))
                .filter(s -> Instant.parse(s.get("expiresAt").toString()).isAfter(Instant.now()))
                .map(s -> s.get("userId").toString())
                .findFirst()
                .orElse(null);
    }

    void cerrar(String token) {
        if (token == null) return;
        datos.write(
                estado -> {
                    sesiones(estado).removeIf(s -> Objects.equals(s.get("id"), resumen(token)));
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> sesiones(Map<String, Object> estado) {
        return (List<Map<String, Object>>)
                estado.computeIfAbsent("sessions", k -> new ArrayList<>());
    }

    private String resumen(String token) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
