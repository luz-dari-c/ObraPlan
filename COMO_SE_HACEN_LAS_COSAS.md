# Cómo se hacen las cosas: mapa operativo de ObraPlan

## Punto de entrada para otro agente

Raíz actual: `C:\Users\ASUS\OneDrive\Documentos\ChatGPT\Plataforma de Construccion`.

Primero lee `README.md` para propósito/ejecución, `AGENTS.md` para continuidad y `docs/BITACORA.md` para decisiones. Después consulta el conocimiento del tema y abre su fuente. Los archivos relativos de este mapa se resuelven desde la raíz, salvo rutas externas explícitas.

## Estructura de carpetas

```text
Plataforma de Construccion/
├── README.md                     Propósito, ejecución y navegación
├── AGENTS.md                     Instrucciones de continuidad y roles
├── COMO_SE_HACEN_LAS_COSAS.md      Este mapa de archivos y entornos
├── .env.example                  Variables reconocidas
├── .editorconfig                 Indentación y codificación
├── pyproject.toml / uv.lock       Entorno de herramientas Python
├── run.ps1 / run.bat              Inicio sencillo
├── .agents/skills/               Skills locales de problema, requisitos y conocimiento
├── agents/                       Agente Python y catálogo de responsabilidades
├── config/integrations.json      Vault e integraciones externas
├── docs/                         Problema, análisis, arquitectura, bitácora, video y evidencia
├── knowledge/                    Catálogo y notas documentales relacionadas
├── scripts/                      Compilar, derivar/sincronizar conocimiento y verificar
├── src/main/java/co/obraplan/     Controlador, servicio y persistencia
│   └── agents/                   Especialistas Java separados
├── src/main/resources/static/    index.html, app.js, knowledge.js y style.css
├── src/test/java/co/obraplan/     Comprobaciones de reglas e integridad
├── data/                         Base del usuario; no versionada
├── build/                        Clases compiladas; no versionadas
└── output/                       Grafos, notas operativas, reportes y respaldos derivados
```

`.idea` y el archivo `.iml` son metadatos existentes de IntelliJ, ignorados por Git. No forman parte de la lógica; no se borraron configuraciones del usuario.

## A dónde apunta cada grupo

| Necesitas trabajar con… | Abre… | Entorno / conexión |
|---|---|---|
| Origen y objetivos | `docs/PROBLEMA.md` | Rol problema; referencia local de análisis |
| RF/RNF y reglas | `docs/ANALISIS.md` | Rol requisitos; criterios canónicos |
| Relación necesidad → demostración | `docs/TRAZABILIDAD.md` | IDs RF/RNF y nodos OP- |
| Entidades/clases | `docs/ARQUITECTURA.md` | Java y modelo conceptual JSON |
| Agentes utilizados | `docs/AGENTES.md`, `agents/README.md` | Tipos, entradas/salidas y limitaciones |
| Rutas HTTP | `src/main/java/co/obraplan/Server.java`, `docs/API.md` | JDK 21; HTTP en loopback |
| Casos de uso | `src/main/java/co/obraplan/AppService.java` | Java; autorización y validación |
| Base/persistencia | `src/main/java/co/obraplan/Database.java`, `data/obraplan.json` | JSON; cambios confirmados notifican KnowledgeAgent |
| Conversión JSON | `src/main/java/co/obraplan/Json.java` | Codifica/decodifica API y base sin dependencias |
| Contraseñas y errores HTTP | `src/main/java/co/obraplan/Password.java`, `HttpError.java` en esa misma carpeta | Hash de credenciales y errores de validación |
| Logística/costos/seguimiento | `src/main/java/co/obraplan/agents/` | Especialistas por reglas coordinados por Orchestrator |
| Consulta/alimentación Java | `src/main/java/co/obraplan/agents/KnowledgeAgent.java` | Lee knowledge + registros de obra + Aprendizajes + notas compartidas registradas |
| Datos/resultados de especialistas | `AgentData.java`, `Insight.java` en la carpeta Java agents | Lectura de valores y resultado común de recomendaciones |
| Consulta/agente Python | `agents/knowledge_agent.py` en raíz | Biblioteca estándar; recuperación y vecinos; Ollama opcional |
| Datos del grafo documental | `knowledge/catalog.json`, `docs/ANALISIS.md` | Fuentes para `scripts/build_knowledge.py` |
| Exportación del grafo | `output/knowledge/graph.json` | Derivada; regenerar, no mantener a mano |
| Conocimiento de otras carpetas | `sharedKnowledgeFiles` de `config/integrations.json` | Lectura de notas registradas dentro del vault; no se sobrescriben |
| Notas operativas | `output/knowledge/runtime/<workId>/` | Derivadas de la base y copiadas a Obsidian |
| Vista web | `src/main/resources/static/` | Navegador; API JSON; sin compilación Node |
| Saludo y resumen por voz | `src/main/resources/static/welcome.js` | app.js entrega el tablero autorizado; voz local del navegador, sin llamadas Gemini; preferencia de silencio local |
| Guía del proceso para nuevos usuarios | `src/main/resources/static/tutorial.js` | Ocho secciones con explicaciones y ejemplos; welcome.js las muestra y permite escucharlas |
| Verificación | `scripts/verify.ps1`, `src/test/`, `docs/VERIFICACION.md` | Base/vault temporales bajo output |
| Video | `docs/GUIA_VIDEO.md` | Narración y clics con resultados esperados |
| Datos preparados para video | `scripts/demo/DemoData.java`, `docs/DEMO_VIDEO.md` | Carga local mediante AppService; servidor detenido, respaldo y reporte en output/demo |

Scripts: `compile.ps1` compila; `verify.ps1` coordina pruebas; `verify_knowledge.py` comprueba memoria/entornos; `verify_http.py` comprueba la API iniciada; `build_knowledge.py` deriva notas documentales y `sync_knowledge.py` publica. En la interfaz, `index.html` define pantallas; `app.js` coordina formularios/API; `knowledge.js` consulta y dibuja relaciones; `style.css` define la presentación. `src/test/java/co/obraplan/Verification.java` es la verificación Java sin bibliotecas externas.

## Intérpretes y configuración comprobados

| Uso | Ruta actual | Requisito |
|---|---|---|
| Java | `C:\Program Files\Java\jdk-21\bin\java.exe` | JDK 21 |
| Compilador | `C:\Program Files\Java\jdk-21\bin\javac.exe` | Módulo jdk.httpserver |
| Python propio del proyecto | `.venv\Scripts\python.exe`, desde la raíz actual | Python 3.14.7; biblioteca estándar; ruta absoluta registrada en config |
| Python base | `C:\Python314\python.exe` | >=3.11; utilizado para crear .venv |
| uv | `C:\Users\ASUS\.local\bin\uv.exe` | Opcional para .venv y lockfile |
| Vault Obsidian | `C:\cerebro_dev` | Vault existente con .obsidian |
| Namespace de publicación | `C:\cerebro_dev\Proyectos\ObraPlan` | Notas, documentación, obras y aprendizajes |

`run.ps1` lee `config/integrations.json`. Las variables de entorno tienen prioridad. `.env.example` es referencia: no se carga automáticamente un `.env`. Para desactivar publicación externa: `$env:OBRAPLAN_VAULT = ''` antes de iniciar. La memoria local sigue disponible.

## Acciones habituales

```powershell
# Desde la raíz: ejecutar aplicación
./run.ps1

# Compilar y verificar sin tocar datos de usuario
./scripts/verify.ps1

# Reconstruir y publicar conocimiento documental
python scripts/build_knowledge.py
python scripts/sync_knowledge.py --check
python scripts/sync_knowledge.py

# Consultar reglas con fuentes y vecinos
python agents/knowledge_agent.py query "costo sin cotización"
python agents/knowledge_agent.py graph --work-id w-demo
```

También se puede ejecutar con el intérprete absoluto del registro si `python` apunta a otro entorno. Ya existe un `.venv` propio, creado con `uv sync --locked --offline --python C:\Python314\python.exe`. Para reproducirlo con uv: `uv sync --locked`, luego `uv run --locked python agents/knowledge_agent.py query "logística"`. uv es opcional; la aplicación Java no necesita .venv.

## Reutilizar otro agente sin mezclar entornos

En `config/integrations.json`, cada agente externo necesita `id`, `root`, `python`, `entrypoint`, `kind: python-agent` y `enabled: true`. `python` debe apuntar al ejecutable del entorno **de ese agente**, por ejemplo la ruta real de su `.venv/Scripts/python.exe`; no se inventa una ruta ni se habilita antes de comprobarla.

Contrato: recibe un objeto JSON por stdin y devuelve un objeto JSON por stdout; diagnósticos por stderr; máximo 60 segundos. El adaptador utiliza una lista de argumentos, `cwd` externo y `shell=False`. Si el agente original usa otro contrato, primero se construye un adaptador en su proyecto autorizado y se prueba con una solicitud sintética. No basta apuntar a un script interactivo incompatible.

```powershell
python agents/knowledge_agent.py external ID_REGISTRADO ruta/solicitud.json
```

La referencia de análisis disponible apunta a `C:\Users\ASUS\OneDrive\Documentos\ChatGPT\Definicion Proceso de Analisis\08_Propuesta_integrada_v0.4\02_Proceso_operativo.md`. Es documentación, no un agente ejecutable. El registro `agentes-profesor` permanece deshabilitado porque el código y el entorno original no fueron localizados.

## Qué editar y qué regenerar

Edita código, requisitos y catálogo de conceptos. Luego regenera las notas documentales. Edita aprendizajes en `C:\cerebro_dev\Proyectos\ObraPlan\Aprendizajes`; el agente los consulta directamente. No edites notas de `Obras` para cambiar pedidos: usa la aplicación. No muevas fuentes sin actualizar este mapa, scripts y rutas de configuración.
## Integración IA durante el uso (8 de octubre de 2026)

| Archivo/ruta | Responsabilidad |
|---|---|
| `src/main/java/co/obraplan/agents/ai/AgentTeam.java` | Cuatro roles, instrucciones, delegación y bucles de herramientas; caché/límites |
| `src/main/java/co/obraplan/agents/ai/GeminiClient.java` | Modelo REST con clave exclusivamente en servidor |
| `src/main/java/co/obraplan/agents/ai/ModelClient.java`, `AgentTools.java` | Contratos de proveedor y herramientas; proveedor simulado solo en tests |
| `src/main/java/co/obraplan/WorkAgentTools.java` | Autorización por obra, proyecciones y herramientas locales |
| `src/main/resources/static/assistant.js` | Formulario, respuesta, eventos, fuentes y conservación |
| `scripts/configure_ai.ps1` | Entrada oculta y archivo de clave cifrado para el usuario Windows |
| `config/gemini-key.local.xml` | Credencial local protegida e ignorada por Git; no compartir |
| `docs/IA_CONFIGURACION.md` | Activación gratuita, límites y demostración |
| `src/test/java/co/obraplan/AgentVerification.java` | Pruebas de coordinación con proveedor simulado |
| `output/knowledge/analyses/<workId>/` | Propuestas confirmadas, conservadas aparte de notas regenerables |

`run.ps1` carga la credencial protegida si no existe GEMINI_API_KEY en el proceso. No instala modelos locales ni carga automáticamente `.env`. La clave necesita un proyecto Google sin facturación; la aplicación no comprueba esa condición en Google.
