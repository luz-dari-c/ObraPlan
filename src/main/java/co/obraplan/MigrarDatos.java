package co.obraplan;

import java.nio.file.*;
import java.util.*;

/** Migración explícita: jamás sobrescribe una base PostgreSQL con registros. */
public final class MigrarDatos {
    public static void main(String[] argumentos) throws Exception {
        if (argumentos.length != 1)
            throw new IllegalArgumentException("Indica el archivo JSON de origen.");
        Map<String, Object> origen =
                Json.object(Json.parse(Files.readString(Path.of(argumentos[0]))));
        AlmacenPostgres destino =
                new AlmacenPostgres(
                        Objects.requireNonNull(
                                System.getenv("DATABASE_URL"), "Falta DATABASE_URL"));
        destino.modificar(
                estado -> {
                    if (estado.values().stream()
                            .anyMatch(valor -> valor instanceof List<?> lista && !lista.isEmpty()))
                        throw new IllegalStateException(
                                "Destino no vacío: migración cancelada sin reemplazar datos.");
                    origen.forEach(
                            (clave, valor) -> {
                                if (!clave.equals("sessions")) estado.put(clave, valor);
                            });
                    return null;
                });
        Map<String, Object> recuperado = destino.leer();
        for (String coleccion :
                List.of("users", "works", "materials", "suppliers", "quotes", "activities")) {
            var antes = (List<?>) origen.getOrDefault(coleccion, List.of());
            var despues = (List<?>) recuperado.getOrDefault(coleccion, List.of());
            if (antes.size() != despues.size())
                throw new IllegalStateException("Diferencia en " + coleccion);
            System.out.println(coleccion + ": " + despues.size() + " registros migrados.");
        }
        System.out.println(
                "Migración confirmada. El archivo original no se modificó. Las sesiones anteriores"
                    + " no se trasladan.");
    }
}
