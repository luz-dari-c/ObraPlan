package co.obraplan;

import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

/** PostgreSQL durable con registros JSONB, índices y escrituras serializadas entre instancias.
 * Conserva el contrato del dominio durante la migración, sin una copia global en memoria.
 */
final class AlmacenPostgres implements AlmacenEstado {
    private static final List<String> COLECCIONES = List.of(
            "users", "works", "materials", "suppliers", "quotes", "activities", "sessions", "analyses");
    private final String url;
    private final Properties propiedades = new Properties();

    AlmacenPostgres(String direccion) {
        if (direccion.startsWith("jdbc:postgresql:")) {
            url = direccion;
        } else {
            URI uri = URI.create(direccion);
            if (!Set.of("postgres", "postgresql").contains(uri.getScheme()))
                throw new IllegalArgumentException("DATABASE_URL debe apuntar a PostgreSQL.");
            String[] credenciales = uri.getUserInfo().split(":", 2);
            propiedades.setProperty("user", java.net.URLDecoder.decode(credenciales[0], java.nio.charset.StandardCharsets.UTF_8));
            propiedades.setProperty("password", java.net.URLDecoder.decode(credenciales[1], java.nio.charset.StandardCharsets.UTF_8));
            url = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() < 0 ? "" : ":" + uri.getPort())
                    + uri.getPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        }
        propiedades.setProperty("connectTimeout", "15");
        propiedades.setProperty("socketTimeout", "30");
        try (Connection conexion = conectar(); Statement sql = conexion.createStatement()) {
            sql.execute("CREATE TABLE IF NOT EXISTS obraplan_registros (coleccion text NOT NULL, id text NOT NULL, documento jsonb NOT NULL, PRIMARY KEY (coleccion,id))");
            sql.execute("CREATE INDEX IF NOT EXISTS obraplan_por_obra ON obraplan_registros ((documento->>'workId'))");
            sql.execute("CREATE UNIQUE INDEX IF NOT EXISTS obraplan_correo_unico ON obraplan_registros ((lower(documento->>'email'))) WHERE coleccion='users'");
            sql.execute("CREATE INDEX IF NOT EXISTS obraplan_por_propietario ON obraplan_registros ((documento->>'ownerId'))");
        } catch (SQLException error) { throw fallo(error); }
    }

    private Connection conectar() throws SQLException { return DriverManager.getConnection(url, propiedades); }

    private Map<String, Object> cargar(Connection conexion) throws SQLException {
        Map<String, Object> estado = Json.map("version", 2);
        COLECCIONES.forEach(nombre -> estado.put(nombre, new ArrayList<>()));
        try (Statement sql = conexion.createStatement(); ResultSet filas = sql.executeQuery(
                "SELECT coleccion, documento::text FROM obraplan_registros ORDER BY coleccion,id")) {
            while (filas.next()) {
                @SuppressWarnings("unchecked")
                List<Object> lista = (List<Object>) estado.get(filas.getString(1));
                if (lista != null) lista.add(Json.parse(filas.getString(2)));
            }
        }
        return estado;
    }

    public Map<String, Object> leer() {
        try (Connection conexion = conectar()) { return cargar(conexion); }
        catch (SQLException error) { throw fallo(error); }
    }

    public <T> T modificar(Function<Map<String, Object>, T> operacion) {
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                // Bloqueo compartido por todas las instancias; evita perder actualizaciones.
                try (Statement sql = conexion.createStatement()) {
                    sql.execute("SELECT pg_advisory_xact_lock(70421105)");
                }
                Map<String, Object> anterior = cargar(conexion);
                Map<String, Object> siguiente = Json.object(Json.parse(Json.stringify(anterior)));
                T resultado = operacion.apply(siguiente);
                guardarDiferencias(conexion, anterior, siguiente);
                conexion.commit();
                return resultado;
            } catch (RuntimeException | SQLException error) {
                conexion.rollback();
                throw error;
            }
        } catch (SQLException error) { throw fallo(error); }
    }

    private void guardarDiferencias(Connection conexion, Map<String, Object> antes, Map<String, Object> despues) throws SQLException {
        try (PreparedStatement guardar = conexion.prepareStatement("INSERT INTO obraplan_registros VALUES (?,?,?::jsonb) ON CONFLICT (coleccion,id) DO UPDATE SET documento=EXCLUDED.documento");
             PreparedStatement borrar = conexion.prepareStatement("DELETE FROM obraplan_registros WHERE coleccion=? AND id=?")) {
            for (String coleccion : COLECCIONES) {
                Map<String, String> anteriores = registros(antes, coleccion);
                Map<String, String> nuevos = registros(despues, coleccion);
                for (var registro : nuevos.entrySet()) {
                    if (registro.getValue().equals(anteriores.get(registro.getKey()))) continue;
                    guardar.setString(1, coleccion); guardar.setString(2, registro.getKey());
                    guardar.setString(3, registro.getValue()); guardar.addBatch();
                }
                for (String id : anteriores.keySet()) {
                    if (nuevos.containsKey(id)) continue;
                    borrar.setString(1, coleccion); borrar.setString(2, id); borrar.addBatch();
                }
            }
            guardar.executeBatch(); borrar.executeBatch();
        }
    }

    private Map<String, String> registros(Map<String, Object> estado, String coleccion) {
        Map<String, String> salida = new LinkedHashMap<>();
        for (Object valor : (List<?>) estado.getOrDefault(coleccion, List.of())) {
            Map<String, Object> registro = Json.object(valor);
            String id = Objects.toString(registro.get("id"), "");
            if (id.isBlank() || salida.put(id, Json.stringify(registro)) != null)
                throw new IllegalArgumentException("Registro sin identificador único en " + coleccion);
        }
        return salida;
    }

    private IllegalStateException fallo(SQLException error) {
        // No incluir la URL ni la contraseña en los mensajes de la aplicación.
        return new IllegalStateException("No se pudo completar la operación en PostgreSQL (" + error.getSQLState() + ").", error);
    }
}
