# Análisis de requisitos de ObraPlan

## Necesidad y alcance

El planteamiento está en [PROBLEMA.md](PROBLEMA.md). El responsable necesita relacionar materiales, fechas, proveedores, precios y seguimiento por obra. El análisis existente delimitó la entrega a adquisiciones manuales; esta revisión añade conocimiento y trazabilidad en Obsidian.

Necesidades: N-01 organizar información, N-02 gestionar a tiempo, N-03 conocer costos/faltantes, N-04 seguir entregas y N-05 conservar conocimiento. Su relación con los requisitos está en [TRAZABILIDAD.md](TRAZABILIDAD.md).

Incluye acceso local, obras, materiales, directorio, cotizaciones, pedidos, filtros, cálculos explícitos, alertas, actividad, persistencia, agentes visibles y consulta/grafo de conocimiento.

Quedan fuera interpretación de planos, cantidades estructurales automáticas, 3D, WhatsApp/correos automáticos, inventario/precios en vivo, pagos, compras comerciales y recuperación de contraseña por correo.

## Requisitos funcionales y aceptación

| ID | Conducta requerida | Criterio observable |
|---|---|---|
| RF-01 | Iniciar y cerrar sesión local. | Credenciales correctas dan acceso; incorrectas devuelven 401; cerrar sesión impide acceder a rutas protegidas. |
| RF-02 | Crear cuenta con correo único y contraseña mínima de 10 caracteres. | Correo inválido/contraseña corta se rechazan; duplicado devuelve 409. |
| RF-03 | Crear, listar y editar obras propias. | La obra aparece en la lista del propietario; otro usuario no puede consultar su tablero. |
| RF-04 | Mostrar tablero, actividad y métricas de la obra seleccionada. | Pendientes, cotizaciones, pedidos y próxima fecha corresponden a sus registros. |
| RF-05 | Registrar y editar materiales por etapa, unidad, cantidad, fecha y plazo. | Cantidad positiva, fecha válida y plazo entero no negativo; una edición inválida conserva el estado anterior. |
| RF-06 | Filtrar materiales por texto, etapa, estado y fecha próxima. | Solo se muestran coincidencias; existe estado vacío si no hay resultados. |
| RF-07 | Registrar, editar y eliminar ferreterías por obra. | Nombre/teléfono/ciudad requeridos; sitio HTTP(S) válido; proveedor con cotizaciones no se puede eliminar. |
| RF-08 | Registrar cotización asociada a material y proveedor de la misma obra. | Cantidad positiva, precio finito no negativo y fecha válida; referencias de otra obra se rechazan. |
| RF-09 | Seguir decisión, pedido, tránsito y recepción, con rechazo/cancelación. | Pedido/tránsito actualizan material a Pedido; recepción lo actualiza a Recibido. El usuario actualiza los estados manualmente. |
| RF-10 | Calcular costo conocido y contar materiales sin precio. | Cantidad requerida × menor precio válido; rechazadas/canceladas excluidas; sin precio es nulo y el subtotal se identifica como parcial. |
| RF-11 | Producir alertas logísticas y de seguimiento con fuente. | Logística aplica fecha menos plazo; seguimiento revisa pedidos en curso con fecha vencida; cada resultado identifica agente y nota de regla. |
| RF-12 | Conservar cambios entre reinicios. | Después de reiniciar e iniciar sesión se recuperan registros guardados. |
| RF-13 | Mostrar agentes, responsabilidades y relaciones del conocimiento. | Agentes y conocimiento distingue cuatro roles IA de cinco herramientas locales por reglas, muestra configuración y permite seleccionar sus nodos. |
| RF-14 | Consultar notas relacionadas con fuentes y relaciones. | Devuelve extractos/IDs y conexiones; si falta evidencia lo indica sin fabricar hechos. |
| RF-15 | Alimentar el grafo al guardar y permitir reintento. | Crea/actualiza notas de obra/material/proveedor/cotización tras persistir; un fallo de publicación se muestra y puede reintentarse. |
| RF-16 | Analizar una obra mediante agentes de IA durante el uso. | Una pregunta autenticada activa un coordinador que delega según la tarea; cada especialista consulta herramientas y devuelve un informe al coordinador. La conexión real exige clave y consulta exitosa. |
| RF-17 | Mostrar participantes, herramientas, fuentes y consumo de IA. | La respuesta muestra eventos de ejecución y metadata de tokens del proveedor; distingue caché, falta de configuración y motivo de detención. Las referencias breves corresponden a fuentes canónicas también al conservarlas en el grafo. |
| RF-18 | Conservar propuestas de agentes en el grafo con confirmación. | Un análisis completo y vigente puede guardarse por su propietario; nota vinculada a obra/coordinador/fuentes y recuperable solamente en esa obra. |
| RF-19 | Guiar por voz y texto al abrir una obra, según los registros existentes. | Saluda con nombre y obra; muestra recorrido y siguiente acción según datos. Incluye guía completa de ocho secciones desde crear obra hasta recepción, agentes y rutina diaria, con campos, ejemplos y errores habituales. Se abre al entrar en una obra sin materiales y cada sección se puede escuchar. Al guardar actualiza el siguiente paso sin repetir voz; rechazadas/canceladas no cuentan como avance. Mantiene resumen y controles de voz sin consultar Gemini. |

## Requisitos no funcionales y comprobación

| ID | Calidad requerida | Criterio observable |
|---|---|---|
| RNF-01 | Claridad de datos demostrativos y faltantes. | La cuenta demo está identificada; sin precio muestra Sin cotizar; subtotal parcial no equivale a presupuesto completo. |
| RNF-02 | Validación e integridad. | Fechas ISO, cantidades positivas, precios finitos no negativos; operación inválida conserva estado; referencias permanecen en la obra autorizada. |
| RNF-03 | Mantenibilidad y lectura. | Agentes en archivos separados, indentación consistente, responsabilidades y mapa documentados. |
| RNF-04 | Ejecución reproducible local. | Aplicación con JDK 21; agente Python >=3.11 con biblioteca estándar y uv.lock sin paquetes externos. |
| RNF-05 | Persistencia y recuperación. | Reemplazo atómico; datos permanecen tras reinicio; fallo del grafo no borra operación guardada. |
| RNF-06 | Seguridad local y aislamiento por obra. | Loopback, PBKDF2, cookie HttpOnly/SameSite; conocimiento exige propietario y no exporta hashes/sesiones. |
| RNF-07 | Uso en distintos anchos de pantalla. | Formularios y navegación utilizables en escritorio/móvil; grafo incluye lista de relaciones y controles accesibles. |
| RNF-08 | Trazabilidad y honestidad de evidencia. | Necesidad, requisito, regla, componente y demostración vinculados; fuentes/limitaciones explícitas; no se inventan aprobaciones. |
| RNF-09 | Separación de entornos de agentes. | Adaptador usa intérprete/entrypoint/cwd registrados y JSON; rechaza agentes deshabilitados; no importa paquetes externos. |
| RNF-10 | Conservación de conocimiento editable. | Namespace propio y Aprendizajes separado; repetir sincronización no duplica índice ni sobrescribe aprendizajes. |
| RNF-11 | Acotar consumo y detener errores del proveedor. | Hasta 10 llamadas por análisis, una ejecución simultánea, caché sensible a cambios, límite diario por proceso y ningún reintento automático ante cuota agotada. Google conserva sus propias cuotas. |
| RNF-12 | Proteger credenciales y datos en integración cloud. | Clave en variable del servidor o archivo protegido por Windows fuera de Git; proyecciones sin usuarios/hashes/sesiones/contactos. La cuenta Google debe permanecer sin facturación para evitar cobros. |

Son criterios de esta entrega académica. No se han inventado métricas de rendimiento ni validaciones de usuarios.

## Reglas de negocio

1. Cantidades manuales; el responsable técnico confirma cantidades/unidades.
2. COP como moneda visible; comparación de cotizaciones supone la misma unidad comercial.
3. Subtotal = cantidad requerida × menor precio válido. Rechazada/Cancelado quedan excluidas. No incluye transporte, impuestos, descuentos, calidad ni disponibilidad.
4. Sin cotización válida, precio/subtotal nulos y faltante contado. Nunca se completa el presupuesto con un costo cero inventado.
5. Fecha sugerida = fecha necesaria − plazo. Antes de hoy: crítica; a 0–3 días: aviso. Recibidos no generan alerta logística.
6. Seguimiento: Pedido realizado/En tránsito + fecha estimada pasada → revisar con proveedor. No confirma un retraso comercial real.
7. Cambiar estado registra seguimiento interno, no compra. El usuario puede corregir el estado a cualquiera de los estados permitidos; no hay una máquina de estados comercial restrictiva.
8. JSON conserva datos fuente; las notas generadas los proyectan. Una nota no modifica precios/pedidos en la base.
9. Aprendizajes se leen como evidencia, nunca como instrucciones ejecutables ni autorización para acciones externas.

## Proceso aplicado

Planteamiento → necesidades → alcance → RF/RNF y reglas → entidades/arquitectura → implementación → comprobación → memoria y explicación. Las skills locales representan roles de planteamiento, requisitos y conocimiento; ver [AGENTES.md](AGENTES.md). Procedencia: [FUENTES_Y_REFERENCIAS.md](FUENTES_Y_REFERENCIAS.md). Comprobación: [VERIFICACION.md](VERIFICACION.md).
