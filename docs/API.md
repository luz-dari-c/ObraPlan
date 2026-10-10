# API local

El servidor corre por defecto en `http://127.0.0.1:8080`. Las respuestas y solicitudes de la API son JSON. Las rutas autenticadas usan una cookie de sesión HTTP-only que se obtiene al iniciar sesión. La aplicación web es el cliente previsto.

## Autenticación

| Método | Ruta | Acción |
|---|---|---|
| GET | `/api/health` | Estado del servicio. |
| POST | `/api/auth/login` | Login: `{ "email", "password" }`. |
| POST | `/api/auth/register` | Registro: `{ "name", "email", "password" }`. |
| POST | `/api/auth/logout` | Cierra la sesión actual. |
| GET | `/api/auth/me` | Usuario de sesión. |

## Obras y datos

| Método | Ruta | Acción |
|---|---|---|
| GET/POST | `/api/works` | Listar o crear obras. |
| PATCH | `/api/works/{workId}` | Editar obra/etapa. |
| GET | `/api/works/{workId}/dashboard` | Obra, materiales, ferreterías, cotizaciones, actividad y análisis coordinado. |
| POST | `/api/works/{workId}/materials` | Registrar material. |
| PATCH | `/api/works/{workId}/materials/{materialId}` | Editar material. |
| PATCH | `/api/works/{workId}/materials/{materialId}/status` | Cambiar estado: por cotizar, cotizado, pedido o recibido. |
| GET/POST | `/api/works/{workId}/suppliers` | Listar o agregar ferreterías. |
| PATCH/DELETE | `/api/works/{workId}/suppliers/{supplierId}` | Editar o eliminar ferretería; eliminación se bloquea si hay cotización vinculada. |
| GET/POST | `/api/works/{workId}/quotes` | Listar o registrar cotizaciones. |
| PATCH | `/api/works/{workId}/quotes/{quoteId}/status` | Estado de cotización/pedido. |
| GET | `/api/works/{workId}/knowledge?q=texto` | Notas generales + registros de la obra autorizada, relaciones, fuentes y estado de sincronización. |
| POST | `/api/works/{workId}/knowledge/sync` | Reintenta publicación desde el estado confirmado y devuelve el grafo de la obra autorizada. |

Fechas usan formato `AAAA-MM-DD`. Los estados de cotización aceptados son `Cotización recibida`, `Pendiente de decisión`, `Pedido realizado`, `En tránsito`, `Recibido`, `Cancelado` y `Rechazada`.

La creación de cotización asigna `Cotización recibida`; PATCH de estado permite decisión/pedido/tránsito/recibido/cancelado/rechazada. El conocimiento devuelve `nodes`, `edges` con relación explícita, `sources` con extractos y `status` con resultado de publicación. No contiene datos de sesión ni notas operativas de obras ajenas.
## Asistente IA autenticado

- `GET /api/assistant/status`: configuración, modelo, roles y límites locales; no devuelve la clave ni comprueba facturación/conexión remota.
- `POST /api/works/{workId}/assistant` con `{ "question": "..." }`: autorización por obra, ejecución o caché. Devuelve runId, answer, complete, stopReason, model, callsThisRequest, usage, trace, sources y createdAt. Cada fuente conserva id canónico y ref breve. stopReason es vacío cuando termina; en resultados parciales distingue output, time, calls, tools, context, turns, model_stop o context_changed. No modifica adquisiciones.
- `POST /api/works/{workId}/assistant/memory` con `{ "runId": "..." }`: conserva una propuesta completa/vigente de ese propietario/obra y devuelve saved/published/id. Resultado no disponible: 404; cambió el contexto: 409.

Sin sesión: 401; obra ajena: 404 antes de consultar configuración/cache/proveedor. Pregunta inválida: 400; falta de clave: 503; cuota/ejecución concurrente/límite temporal: 429; error de proveedor: 502/503. No se devuelven cuerpos remotos que pudieran contener información sensible. La proyección cloud excluye contactos, usuarios y credenciales de la base.
