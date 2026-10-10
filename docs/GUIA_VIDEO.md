# Guion para explicar y demostrar ObraPlan

Duración sugerida: **10–12 minutos**. Prioriza el origen, los requisitos, la estructura y las funcionalidades. El código se muestra solo para ubicar responsabilidades. Este guion corresponde a lo implementado.

**Si ya explicaste el backend y quieres mostrar rápidamente la interfaz:** sigue [DEMO_VIDEO.md](DEMO_VIDEO.md). Abre la obra **Demostración para video**, que contiene los registros preparados; no necesitas llenar formularios durante la grabación.

## Antes de grabar

1. Desde la raíz ejecuta `./scripts/verify.ps1` y comprueba el resultado. Luego inicia `./run.ps1`.
2. Abre `http://127.0.0.1:8080` y el vault `cerebro_dev` en Obsidian. Ten preparados README, PROBLEMA, ANALISIS, COMO_SE_HACEN_LAS_COSAS, AGENTES y TRAZABILIDAD.
3. Inicia sesión con tu cuenta habitual o con `demo@obraplan.co` / `ObraPlan2026!` y abre **Demostración para video** para usar los datos preparados. Si deseas grabar la creación manual, crea otra obra aparte con ubicación ficticia y sigue el recorrido de formularios de abajo.
4. Usa datos ficticios. La fecha de necesidad del material será **mañana** y el plazo **3 días**, para producir una alerta logística reproducible. Para mostrar seguimiento, la entrega estimada será **ayer** y el estado debe ser Pedido realizado o En tránsito.
5. En Obsidian busca OP-Proyecto y abre el grafo local o global. Filtro útil: `path:"Proyectos/ObraPlan"`. Si agregas un aprendizaje manual, usa el ejemplo de abajo y conserva la nota en Aprendizajes.

## 0:00–0:45 — Inicio y propósito

**Muestra:** README y pantalla de acceso.

**Di:** “Mi proyecto se llama ObraPlan. Es una bitácora de adquisiciones para obras de construcción. Su propósito es relacionar los materiales que necesito, las fechas, las ferreterías, las cotizaciones y el estado de los pedidos. También conserva el conocimiento del proyecto en Obsidian para que otro agente pueda entenderlo y continuar el trabajo.”

“Voy a explicar primero el problema y el análisis, después la estructura y los agentes, y finalmente demostraré la aplicación y cómo alimenta el grafo.”

## 0:45–2:00 — Cómo nace el proyecto

**Muestra:** docs/PROBLEMA.md y el nodo OP-Problema.

**Di:** “La necesidad inicial es organizar información que puede estar dispersa entre listas, contactos y precios. Una lista no me dice cuándo gestionar un material ni qué costo sigue pendiente. La pregunta central es cómo relacionar esas necesidades, las cotizaciones y las entregas por obra.”

“La idea amplia incluía planos y visualización 3D. El alcance de esta entrega se concentra en adquisiciones manuales para demostrar un flujo completo y verificable. El responsable registra cantidades y toma decisiones; el sistema no interpreta planos ni compra materiales por su cuenta.”

“El rol de planteamiento organiza contexto, actores, problema y objetivos. Su salida es este documento. Lo dejé como una skill reutilizable para un asistente. Distingo hechos del proyecto de hipótesis del caso académico: no presento entrevistas o mediciones que no se realizaron.”

## 2:00–3:15 — Del problema a RF y RNF

**Muestra:** tabla de ANALISIS y TRAZABILIDAD; evita leer las 30 filas.

**Di:** “El siguiente rol es análisis de requisitos. Convierte las necesidades en comportamientos que se pueden comprobar. Por ejemplo, RF-05 exige registrar materiales; RF-10 exige calcular el costo conocido y mostrar faltantes; RF-15 exige actualizar el conocimiento después de guardar.”

“Los no funcionales describen cómo debe funcionar: RNF-03 pide organización y lectura; RNF-06 pide aislamiento por obra; RNF-10 pide conservar los aprendizajes editables. Cada uno tiene un criterio observable.”

“Esta matriz conecta necesidad, requisito, componente y demostración. Así puedo explicar qué construí y qué evidencia respalda cada parte.”


## 3:15–4:15 — Carpetas, archivos y entornos

**Muestra:** raíz y COMO_SE_HACEN_LAS_COSAS.md.

**Di:** “README explica para qué sirve y enlaza la bitácora de decisiones. AGENTS guía al siguiente asistente. COMO_SE_HACEN_LAS_COSAS indica dónde está cada componente y con qué entorno trabaja. scripts contiene acciones repetibles; output conserva reportes; docs explica el sistema; knowledge conserva notas y relaciones.”

“La aplicación usa Java 21. El agente de conocimiento Python tiene su proyecto y lockfile. Para reutilizar un agente de otra carpeta se registra su propio intérprete, su entrada y su carpeta de trabajo. El intercambio es JSON, de modo que no se mezclan entornos Python.”

## 4:15–5:15 — Entidades, clases y agentes

**Muestra:** diagrama de ARQUITECTURA y carpeta Java agents.

**Di:** “Una obra contiene materiales y proveedores. Una cotización relaciona un material con una ferretería y sus condiciones. Esas son las entidades del negocio. En esta implementación se almacenan como objetos JSON.”

“Server recibe solicitudes, AppService valida y coordina casos de uso y Database persiste. El Orquestador reúne tres especialistas: Logística calcula cuándo gestionar; Costos suma precios válidos y muestra faltantes; Seguimiento revisa entregas de pedidos en curso. Conocimiento publica y consulta las notas relacionadas.”

“Estas clases son herramientas por reglas. Sobre ellas hay un coordinador IA y tres agentes con contextos propios: Logística y seguimiento, Costos y Conocimiento. El modelo decide a quién delegar y qué herramientas consultar. La conexión requiere configurar Gemini y comprobar una consulta real; mostraré sus participantes y fuentes. La búsqueda local del grafo sigue disponible sin IA.”

## 5:15–8:15 — Aplicación funcionando

| Acción en pantalla | Resultado que debes mostrar | Qué puedes decir |
|---|---|---|
| Crear/abrir Demostración del video | Obra con ubicación y etapa | “Toda la información queda asociada a esta obra.” |
| Registrar **Cemento video**, 10 bultos, Cimentación, mañana, plazo 3 días | Material en el plan; Sin cotizar; alerta logística | “RF-05 queda visible. Logística resta el plazo; el material requiere gestión.” |
| Filtrar por nombre/etapa | Solo el material que coincide | “Puedo localizar necesidades dentro de la obra.” |
| Agregar **Ferretería demo video**, teléfono ficticio y ciudad | Contacto en directorio | “La plataforma conserva el contacto; yo solicito la cotización manualmente.” |
| Cotizar Cemento video: cantidad 10, precio COP 40.000, fecha hoy, entrega estimada ayer | Subtotal COP 400.000 | “Costos multiplica cantidad requerida por precio válido. Antes no inventaba costo cero.” |
| Abrir otra cotización más cara, opcional | Se conserva el menor precio válido | “Es una comparación básica entre unidades equivalentes; no incluye flete o calidad.” |
| Marcar Pedido realizado y luego En tránsito | Material en Pedido; aviso de entrega por revisar | “Seguimiento revisa una fecha vencida de un pedido activo, sin afirmar una confirmación externa.” |
| Marcar Recibido | Estado recibido; deja de alertar por seguimiento | “El flujo conserva el estado y registra actividad.” |
| Mostrar Actividad reciente | Operaciones del recorrido | “Esta es la bitácora operativa; la bitácora de desarrollo documenta decisiones del software.” |

No presentes la entrega estimada de ayer como un proveedor real: es un dato deliberado de prueba para mostrar la regla. Si la obra tiene otros materiales sin precio, el subtotal seguirá siendo parcial aunque el cemento ya tenga cotización.

## 8:15–10:15 — Cerebro de conocimiento y alimentación automática

**Muestra:** Agentes y conocimiento, luego Obsidian.

**Di:** “El conocimiento no queda en notas aisladas. Este grafo conecta el problema, los requisitos, los componentes, las reglas y los datos de la obra.”

1. Selecciona Agente de costos. Muestra sus enlaces `aplica → Regla de costos` e `implementa → RF-10`.
2. Consulta **costo sin cotización**. Abre un extracto y su nota en Obsidian. Di: “La respuesta tiene fuentes. El agente consulta contenido y relaciones, no una captura de la imagen del grafo.”
3. Abre en Obsidian la nota de Cemento video y sus vínculos a obra, entidad y agentes. Di: “Esta nota apareció después de guardar. Database confirmó el cambio y KnowledgeAgent publicó la proyección.”
4. Cambia un dato del material en la aplicación y muestra la nota actualizada. No edites directamente la nota generada para cambiar un pedido.
5. Muestra la carpeta Aprendizajes y, si quieres probar memoria editable, agrega la siguiente nota. Después consulta **unidades equivalentes** desde la aplicación o herramienta Python.

También puedes abrir **Memoria compartida entre proyectos y entornos**. Muestra los enlaces hacia las notas existentes de LangChain y Ollama y la lista `sharedKnowledgeFiles`. Di: “El agente lee conocimiento general de otras carpetas mediante referencias explícitas. Para ejecutar código de otro agente, se registra por separado su carpeta, intérprete y contrato. Consultar una nota sobre LangChain no significa que esta aplicación ejecute LangChain.”

```markdown
---
tipo: aprendizaje
tags: [obraplan, aprendizaje]
---
# Comparar unidades equivalentes

Antes de comparar precios, confirmar que cada cotización usa la misma unidad comercial.

- complementa:: [[OP-Regla-Costos]]
- usado_por:: [[OP-Agente-Costos]]
```

Nombre de archivo: `OP-Aprendizaje-Unidades.md`, dentro de `C:\cerebro_dev\Proyectos\ObraPlan\Aprendizajes`. Esta nota se consulta directamente y no se sobrescribe. No cambia por sí sola la regla Java.

## 10:15–11:15 — Comprobación, límites y cierre

**Muestra:** VERIFICACION y BITACORA.

**Di:** “La verificación usa datos temporales para comprobar cálculos, validación, persistencia, autorización y actualización de notas. Los resultados quedan en output. La documentación registra qué se hizo y qué falta.”

“ObraPlan demuestra un flujo de adquisiciones con seguimiento y memoria conectada. Sus límites actuales son datos manuales, ejecución local y especialistas por reglas. La estructura permite continuar con nuevas integraciones conservando requisitos, fuentes, entornos y conocimiento.”

## Si necesitas reducirlo a 5 minutos

Usa 40 segundos para problema, 40 para RF/RNF, 40 para estructura/agentes, 90 para material→cotización→pedido→recibido y 90 para consulta/grafo/actualización. Conserva la distinción entre implementado y pendiente.
## Añadir demostración de agentes durante el uso

Al abrir la obra, deja que la bienvenida pronuncie tu nombre y lea su estado. Si el navegador solicita una interacción, pulsa **Escuchar resumen**. Puedes detenerla para continuar la explicación. Di: «La bienvenida usa el resumen de las herramientas locales y la voz del navegador sin gastar tokens. Cuando solicito un análisis, participan los agentes con Gemini». No presentes el saludo como un quinto agente LLM.

Primero activa y comprueba Gemini según `IA_CONFIGURACION.md`. En la obra demostrativa abre **Agentes y conocimiento**, elige **Análisis conjunto** y envía la pregunta. Narra: «El coordinador decide qué especialistas necesita; ellos consultan herramientas de mi obra y fuentes del grafo. Los importes siguen calculándose con Java». Muestra los participantes y herramientas reales, fuentes y consumo. Repite la misma pregunta sin cambiar datos para mostrar el resultado reutilizado sin llamadas nuevas. Pulsa **Conservar propuesta en el grafo** y muestra sus relaciones con obra, coordinador y fuentes. No presentes la pantalla pendiente de clave ni pruebas simuladas como una ejecución real de Gemini.
