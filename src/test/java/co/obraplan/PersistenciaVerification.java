package co.obraplan;

import co.obraplan.agents.KnowledgeAgent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Comprueba SQL real, rollback, reinicio, exclusión mutua y sesión durable. */
public final class PersistenciaVerification {
    public static void main(String[] args) throws Exception {
        String url = Objects.requireNonNull(System.getenv("DATABASE_URL"));
        Database datos = new Database(new AlmacenPostgres(url));
        KnowledgeAgent memoria = new KnowledgeAgent(Path.of(args[0]), null);
        AppService servicio = new AppService(datos, memoria);
        String correo = "persistencia-" + UUID.randomUUID() + "@example.test";
        var acceso = servicio.register("Prueba persistencia", correo, "SoloPruebas2026!");
        AppService reiniciado = new AppService(new Database(new AlmacenPostgres(url)), memoria);
        comprobar(reiniciado.current(acceso.token()).get("email").equals(correo), "sesión tras reiniciar");
        String copia = Json.stringify(datos.read());
        try {
            datos.write(estado -> { estado.put("works", List.of(Json.map("id", "fallo"))); throw new IllegalArgumentException("fallo simulado"); });
        } catch (IllegalArgumentException esperado) { }
        comprobar(Json.stringify(datos.read()).equals(copia), "rollback");
        try (var ejecutor = Executors.newFixedThreadPool(4)) {
            List<Future<?>> tareas = new ArrayList<>();
            for (int i = 0; i < 12; i++) tareas.add(ejecutor.submit(() -> new Database(new AlmacenPostgres(url)).write(estado -> {
                @SuppressWarnings("unchecked") var actividades = (List<Object>) estado.get("activities");
                actividades.add(Json.map("id", UUID.randomUUID().toString(), "message", "prueba")); return null;
            })));
            for (var tarea : tareas) tarea.get();
        }
        comprobar(datos.readActivities().size() == 12, "doce escrituras concurrentes sin pérdidas");
        reiniciado.logout(acceso.token());
        try { servicio.current(acceso.token()); throw new AssertionError("Logout no revocó la sesión"); }
        catch (HttpError esperado) { comprobar(esperado.status == 401, "revocación compartida"); }
        System.out.println("PostgreSQL: reinicio, rollback, concurrencia y revocación correctos.");
    }
    private static void comprobar(boolean resultado, String nombre) {
        if (!resultado) throw new AssertionError(nombre);
    }
}
