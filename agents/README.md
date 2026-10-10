# Agentes de ObraPlan

Este directorio reúne las fichas de roles y el agente Python ejecutable. El código de los especialistas de la aplicación está en `src/main/java/co/obraplan/agents/`.

Los agentes IA durante el uso se definen en `src/main/java/co/obraplan/agents/ai/AgentTeam.java`: coordinador y especialistas de Logística, Costos y Conocimiento. `WorkAgentTools.java` delimita herramientas/datos por obra, y `GeminiClient.java` llama al proveedor. Son contextos independientes con un modelo compartido. Activación y demostración en `docs/IA_CONFIGURACION.md`; cada instalación requiere su propia clave y una consulta exitosa para comprobar la conexión.

| Agente | Tipo | Implementación / skill | Resultado |
|---|---|---|---|
| Planteamiento del problema | Rol de trabajo para el asistente | `.agents/skills/obraplan-problema/SKILL.md` | `docs/PROBLEMA.md` |
| Análisis de requisitos | Rol de trabajo para el asistente | `.agents/skills/obraplan-requisitos/SKILL.md` | `docs/ANALISIS.md`, `docs/TRAZABILIDAD.md` |
| Orquestador | Componente Java por reglas | `agents/Orchestrator.java` | Informe del tablero |
| Logística | Componente Java por reglas | `agents/LogisticsAgent.java` | Fechas de gestión y alertas |
| Costos | Componente Java por reglas | `agents/CostAgent.java` | Subtotal conocido y faltantes |
| Seguimiento | Componente Java por reglas | `agents/TrackingAgent.java` | Entregas que requieren revisión |
| Conocimiento | Componente Java + herramienta Python | `agents/KnowledgeAgent.java`, `agents/knowledge_agent.py` | Consulta con fuentes, relaciones y actualización de notas |

## Cómo se ejecutan

Al pulsar **Consultar agentes**, el coordinador IA interpreta la pregunta con Gemini y puede delegar a Logística, Costos y Conocimiento. Cada agente tiene instrucciones, historial y herramientas propios. La interfaz muestra las consultas, herramientas, fuentes y consumo de la ejecución.

Las clases de cálculo (`LogisticsAgent`, `CostAgent` y `TrackingAgent`) aplican reglas deterministas y sirven como herramientas para los agentes IA. Las skills organizan el desarrollo del proyecto. La herramienta Python recupera notas y relaciones localmente y dispone de un modo opcional con Ollama cuando hay un modelo instalado.
