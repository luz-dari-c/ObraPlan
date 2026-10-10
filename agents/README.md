# Agentes de ObraPlan

Este directorio reúne las fichas de roles y el agente Python ejecutable. El código de los especialistas de la aplicación está en `src/main/java/co/obraplan/agents/`.

Los agentes IA durante el uso se definen en `src/main/java/co/obraplan/agents/ai/AgentTeam.java`: coordinador y especialistas de Logística, Costos y Conocimiento. `WorkAgentTools.java` delimita herramientas/datos por obra, y `GeminiClient.java` llama al proveedor. Son contextos independientes con un modelo compartido. Activación y demostración en `docs/IA_CONFIGURACION.md`; conexión real pendiente de clave y consulta.

| Agente | Tipo | Implementación / skill | Resultado |
|---|---|---|---|
| Planteamiento del problema | Rol de trabajo para el asistente | `.agents/skills/obraplan-problema/SKILL.md` | `docs/PROBLEMA.md` |
| Análisis de requisitos | Rol de trabajo para el asistente | `.agents/skills/obraplan-requisitos/SKILL.md` | `docs/ANALISIS.md`, `docs/TRAZABILIDAD.md` |
| Orquestador | Componente Java por reglas | `agents/Orchestrator.java` | Informe del tablero |
| Logística | Componente Java por reglas | `agents/LogisticsAgent.java` | Fechas de gestión y alertas |
| Costos | Componente Java por reglas | `agents/CostAgent.java` | Subtotal conocido y faltantes |
| Seguimiento | Componente Java por reglas | `agents/TrackingAgent.java` | Entregas que requieren revisión |
| Conocimiento | Componente Java + herramienta Python | `agents/KnowledgeAgent.java`, `agents/knowledge_agent.py` | Consulta con fuentes, relaciones y actualización de notas |

Los roles de trabajo son instrucciones reutilizables para un asistente; no son procesos autónomos permanentemente activos. Los especialistas Java no utilizan un LLM. El agente Python funciona con recuperación local y ofrece un modo LLM opcional mediante Ollama cuando existe un modelo instalado. Los agentes originales del profesor aún no están localizados: `config/integrations.json` registra ese estado sin simular su ejecución.
