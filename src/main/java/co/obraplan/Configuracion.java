package co.obraplan;

/** Configuración única del proceso; los secretos se reciben del entorno. */
final class Configuracion {
    private Configuracion() {}

    static boolean produccion() {
        return "production".equals(System.getenv("OBRAPLAN_ENV"));
    }

    static int puerto() {
        return Integer.parseInt(
                System.getenv()
                        .getOrDefault(
                                "PORT", System.getenv().getOrDefault("OBRAPLAN_PORT", "8080")));
    }

    static String interfazRed() {
        return System.getenv()
                .getOrDefault("OBRAPLAN_HOST", produccion() ? "0.0.0.0" : "127.0.0.1");
    }

    static Database base(java.nio.file.Path archivo) {
        String url = System.getenv("DATABASE_URL");
        if (url != null && !url.isBlank()) return new Database(new AlmacenPostgres(url));
        if (produccion())
            throw new IllegalStateException(
                    "Producción requiere DATABASE_URL; no se guardarán datos en disco temporal.");
        return new Database(archivo);
    }
}
