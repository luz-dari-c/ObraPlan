package co.obraplan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class Database {
    private final Path file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, Object> state;
    private java.util.function.Consumer<Map<String, Object>> onCommit = snapshot -> {};

    Database(Path file) {
        this.file = file;
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file))
                state = Json.object(Json.parse(Files.readString(file, StandardCharsets.UTF_8)));
            else {
                state = seed();
                persist();
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo abrir la base local: " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> read() {
        lock.readLock().lock();
        try {
            return (Map<String, Object>) deep(state);
        } finally {
            lock.readLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> readList(String key) {
        return (List<Map<String, Object>>) (List<?>) read().getOrDefault(key, new ArrayList<>());
    }

    List<Map<String, Object>> readUsers() {
        return readList("users");
    }

    List<Map<String, Object>> readWorks() {
        return readList("works");
    }

    List<Map<String, Object>> readMaterials() {
        return readList("materials");
    }

    List<Map<String, Object>> readSuppliers() {
        return readList("suppliers");
    }

    List<Map<String, Object>> readQuotes() {
        return readList("quotes");
    }

    List<Map<String, Object>> readActivities() {
        return readList("activities");
    }

    @SuppressWarnings("unchecked")
    <T> T write(java.util.function.Function<Map<String, Object>, T> action) {
        lock.writeLock().lock();
        Map<String, Object> previous = state;
        try {
            state = Json.object(deep(previous));
            T out = action.apply(state);
            persist();
            onCommit.accept(Json.object(deep(state)));
            return out;
        } catch (RuntimeException error) {
            state = previous;
            throw error;
        } finally {
            lock.writeLock().unlock();
        }
    }

    void onCommit(java.util.function.Consumer<Map<String, Object>> listener) {
        this.onCommit = listener;
    }

    private void persist() {
        try {
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(
                    temp,
                    Json.stringify(state),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(
                        temp,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la base de datos", e);
        }
    }

    private static Object deep(Object x) {
        return Json.parse(Json.stringify(x));
    }

    private static Map<String, Object> seed() {
        String today = LocalDate.now().toString();
        Map<String, Object> user =
                Json.map(
                        "id",
                        "u-demo",
                        "name",
                        "Camilo Ríos",
                        "email",
                        "demo@obraplan.co",
                        "passwordHash",
                        Password.hash("ObraPlan2026!"));
        Map<String, Object> work =
                Json.map(
                        "id",
                        "w-demo",
                        "ownerId",
                        "u-demo",
                        "name",
                        "Vivienda San Miguel",
                        "address",
                        "Sector San Miguel",
                        "city",
                        "Medellín, Antioquia",
                        "startDate",
                        today,
                        "description",
                        "Obra demostrativa para explorar la planificación de adquisiciones.",
                        "stage",
                        "Cimentación",
                        "createdAt",
                        today,
                        "demo",
                        true);
        List<Object> materials = new ArrayList<>();
        materials.add(
                Json.map(
                        "id",
                        "m-cemento",
                        "workId",
                        "w-demo",
                        "name",
                        "Cemento gris",
                        "category",
                        "Cimentación",
                        "stage",
                        "Cimentación",
                        "quantity",
                        50,
                        "unit",
                        "bultos",
                        "neededDate",
                        today,
                        "leadDays",
                        3,
                        "notes",
                        "Datos demostrativos; confirma cantidades con el responsable técnico.",
                        "status",
                        "Cotizado",
                        "createdAt",
                        today));
        materials.add(
                Json.map(
                        "id",
                        "m-varilla",
                        "workId",
                        "w-demo",
                        "name",
                        "Varilla corrugada 1/2\"",
                        "category",
                        "Acero",
                        "stage",
                        "Estructura",
                        "quantity",
                        600,
                        "unit",
                        "kg",
                        "neededDate",
                        LocalDate.now().plusDays(12).toString(),
                        "leadDays",
                        5,
                        "notes",
                        "Datos demostrativos.",
                        "status",
                        "Por cotizar",
                        "createdAt",
                        today));
        materials.add(
                Json.map(
                        "id",
                        "m-arena",
                        "workId",
                        "w-demo",
                        "name",
                        "Arena lavada",
                        "category",
                        "Agregados",
                        "stage",
                        "Cimentación",
                        "quantity",
                        3,
                        "unit",
                        "m³",
                        "neededDate",
                        LocalDate.now().plusDays(7).toString(),
                        "leadDays",
                        2,
                        "notes",
                        "Datos demostrativos.",
                        "status",
                        "Pedido",
                        "createdAt",
                        today));
        materials.add(
                Json.map(
                        "id",
                        "m-bloque",
                        "workId",
                        "w-demo",
                        "name",
                        "Bloque estructural",
                        "category",
                        "Mampostería",
                        "stage",
                        "Estructura",
                        "quantity",
                        600,
                        "unit",
                        "unidades",
                        "neededDate",
                        LocalDate.now().plusDays(30).toString(),
                        "leadDays",
                        5,
                        "notes",
                        "Datos demostrativos.",
                        "status",
                        "Por cotizar",
                        "createdAt",
                        today));
        List<Object> suppliers =
                List.of(
                        Json.map(
                                "id",
                                "s-demo-1",
                                "workId",
                                "w-demo",
                                "name",
                                "Materiales Ejemplo S.A.S.",
                                "phone",
                                "+57 300 000 1234",
                                "address",
                                "Centro, Medellín",
                                "city",
                                "Medellín, Antioquia",
                                "website",
                                "https://example.com",
                                "notes",
                                "Registro de demostración",
                                "createdAt",
                                today),
                        Json.map(
                                "id",
                                "s-demo-2",
                                "workId",
                                "w-demo",
                                "name",
                                "Ferretería Punto Obra",
                                "phone",
                                "+57 301 000 5678",
                                "address",
                                "Zona centro, Envigado",
                                "city",
                                "Envigado, Antioquia",
                                "website",
                                "https://example.org",
                                "notes",
                                "Registro de demostración",
                                "createdAt",
                                today));
        List<Object> quotes =
                List.of(
                        Json.map(
                                "id",
                                "q-demo-1",
                                "workId",
                                "w-demo",
                                "materialId",
                                "m-cemento",
                                "supplierId",
                                "s-demo-1",
                                "quantity",
                                50,
                                "unitPrice",
                                38500,
                                "quotedAt",
                                today,
                                "leadDays",
                                3,
                                "expectedDate",
                                LocalDate.now().plusDays(3).toString(),
                                "status",
                                "Pendiente de decisión",
                                "notes",
                                "Precio ilustrativo: reemplazar con cotización real.",
                                "createdAt",
                                today),
                        Json.map(
                                "id",
                                "q-demo-2",
                                "workId",
                                "w-demo",
                                "materialId",
                                "m-arena",
                                "supplierId",
                                "s-demo-2",
                                "quantity",
                                3,
                                "unitPrice",
                                78000,
                                "quotedAt",
                                today,
                                "leadDays",
                                2,
                                "expectedDate",
                                LocalDate.now().plusDays(2).toString(),
                                "status",
                                "En tránsito",
                                "notes",
                                "Precio ilustrativo: reemplazar con cotización real.",
                                "createdAt",
                                today));
        List<Object> activities =
                List.of(
                        Json.map(
                                "id",
                                "a-demo-1",
                                "workId",
                                "w-demo",
                                "type",
                                "seed",
                                "message",
                                "Obra de demostración preparada; revisa o reemplaza sus datos.",
                                "createdAt",
                                today + "T09:00:00"));
        return Json.map(
                "version",
                1,
                "users",
                new ArrayList<>(List.of(user)),
                "works",
                new ArrayList<>(List.of(work)),
                "materials",
                materials,
                "suppliers",
                new ArrayList<>(suppliers),
                "quotes",
                new ArrayList<>(quotes),
                "activities",
                new ArrayList<>(activities));
    }
}
