package co.obraplan;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Límite por origen para operaciones costosas; no almacena datos de formularios. */
final class LimiteSolicitudes {
    private record Ventana(long minuto, int cantidad) {}

    private final Map<String, Ventana> ventanas = new HashMap<>();

    synchronized boolean permitir(String clave, int maximo) {
        long minuto = Instant.now().getEpochSecond() / 60;
        if (ventanas.size() > 2000) ventanas.entrySet().removeIf(e -> e.getValue().minuto < minuto);
        Ventana previa = ventanas.get(clave);
        int cantidad = previa == null || previa.minuto != minuto ? 0 : previa.cantidad;
        if (cantidad >= maximo || (ventanas.size() >= 10000 && previa == null)) return false;
        ventanas.put(clave, new Ventana(minuto, cantidad + 1));
        return true;
    }
}
