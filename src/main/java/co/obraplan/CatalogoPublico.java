package co.obraplan;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Lectura limitada de Store API pública WooCommerce. No recibe URLs del usuario. */
final class CatalogoPublico {
    private static final Map<String, String> TIENDAS =
            Map.of(
                    "ferreteriaya.com.co", "Ferretería en Galerías",
                    "ferresmart.com.co", "Ferresmart");
    private final HttpClient cliente =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();

    private record Entrada(Instant fecha, Map<String, Object> resultado) {}

    synchronized Map<String, Object> buscar(String consulta) {
        String texto = consulta == null ? "" : consulta.trim();
        if (texto.length() < 3 || texto.length() > 80)
            throw new HttpError(400, "Busca un material con entre 3 y 80 caracteres.");
        Entrada anterior = cache.get(texto.toLowerCase(Locale.ROOT));
        if (anterior != null && anterior.fecha.plusSeconds(300).isAfter(Instant.now()))
            return anterior.resultado;
        List<Map<String, Object>> productos = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        List<CompletableFuture<Void>> tareas = new ArrayList<>();
        for (var tienda : TIENDAS.entrySet()) {
            URI url =
                    URI.create(
                            "https://"
                                    + tienda.getKey()
                                    + "/wp-json/wc/store/v1/products?per_page=8&search="
                                    + URLEncoder.encode(texto, StandardCharsets.UTF_8));
            HttpRequest solicitud =
                    HttpRequest.newBuilder(url)
                            .timeout(Duration.ofSeconds(12))
                            .header("User-Agent", "ObraPlan/2.0 (consulta de catalogo publico)")
                            .GET()
                            .build();
            tareas.add(
                    cliente.sendAsync(solicitud, HttpResponse.BodyHandlers.ofInputStream())
                            .thenAccept(
                                    respuesta -> {
                                        try (var cuerpo = respuesta.body()) {
                                            if (respuesta.statusCode() != 200)
                                                throw new IllegalStateException();
                                            byte[] datos = cuerpo.readNBytes(750001);
                                            if (datos.length > 750000)
                                                throw new IllegalStateException();
                                            Object contenido =
                                                    Json.parse(
                                                            new String(
                                                                    datos, StandardCharsets.UTF_8));
                                            if (!(contenido instanceof List<?> lista))
                                                throw new IllegalStateException();
                                            synchronized (productos) {
                                                for (Object valor : lista) {
                                                    Map<String, Object> p = Json.object(valor);
                                                    Map<String, Object> precios =
                                                            Json.object(p.get("prices"));
                                                    String moneda =
                                                            Objects.toString(
                                                                    precios.get("currency_code"),
                                                                    "");
                                                    BigDecimal precio = null;
                                                    try {
                                                        precio =
                                                                new BigDecimal(
                                                                                precios.get("price")
                                                                                        .toString())
                                                                        .movePointLeft(
                                                                                ((Number)
                                                                                                precios
                                                                                                        .get(
                                                                                                                "currency_minor_unit"))
                                                                                        .intValue());
                                                    } catch (RuntimeException ignored) {
                                                    }
                                                    String enlace =
                                                            Objects.toString(
                                                                    p.get("permalink"), "");
                                                    if (!enlace.startsWith(
                                                                    "https://"
                                                                            + tienda.getKey()
                                                                            + "/")
                                                            && !enlace.startsWith(
                                                                    "https://www."
                                                                            + tienda.getKey()
                                                                            + "/")) continue;
                                                    productos.add(
                                                            Json.map(
                                                                    "id",
                                                                    tienda.getKey()
                                                                            + ":"
                                                                            + p.get("id"),
                                                                    "name",
                                                                    limpiar(p.get("name")),
                                                                    "supplier",
                                                                    tienda.getValue(),
                                                                    "url",
                                                                    enlace,
                                                                    "price",
                                                                    precio,
                                                                    "currency",
                                                                    moneda,
                                                                    "observedAt",
                                                                    Instant.now().toString(),
                                                                    "unit",
                                                                    "Presentación publicada;"
                                                                        + " verificar en la tienda",
                                                                    "comparable",
                                                                    "COP".equals(moneda)
                                                                            && precio != null
                                                                            && precios.get(
                                                                                            "price_range")
                                                                                    == null,
                                                                    "warning",
                                                                    "COP".equals(moneda)
                                                                            ? "Confirma"
                                                                                  + " presentación,"
                                                                                  + " impuestos,"
                                                                                  + " transporte y"
                                                                                  + " disponibilidad."
                                                                            : "La tienda declara "
                                                                                    + moneda
                                                                                    + "; no se"
                                                                                    + " convierte a"
                                                                                    + " COP ni se"
                                                                                    + " compara"
                                                                                    + " automáticamente."));
                                                }
                                            }
                                        } catch (Exception error) {
                                            synchronized (avisos) {
                                                avisos.add(
                                                        tienda.getValue()
                                                                + ": catálogo no disponible"
                                                                + " ahora.");
                                            }
                                        }
                                    })
                            .exceptionally(
                                    error -> {
                                        synchronized (avisos) {
                                            avisos.add(
                                                    tienda.getValue()
                                                            + ": conexión no disponible.");
                                        }
                                        return null;
                                    }));
        }
        CompletableFuture.allOf(tareas.toArray(CompletableFuture[]::new)).join();
        Map<String, Object> resultado =
                Json.map(
                        "products",
                        productos,
                        "warnings",
                        avisos,
                        "notice",
                        "Precios observados, no cotizaciones confirmadas. Compara la misma unidad y"
                            + " presentación; verifica la fuente antes de comprar.");
        if (cache.size() >= 100) cache.clear();
        cache.put(texto.toLowerCase(Locale.ROOT), new Entrada(Instant.now(), resultado));
        return resultado;
    }

    private String limpiar(Object valor) {
        return Objects.toString(valor, "")
                .replaceAll("<[^>]*>", "")
                .replace("&#8211;", "–")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");
    }
}
