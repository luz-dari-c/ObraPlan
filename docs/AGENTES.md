# Agentes utilizados, roles y skills

## Agentes de IA durante el uso

La capa nueva está en `src/main/java/co/obraplan/agents/ai/`. `AgentTeam.java` define instrucciones, herramientas y contextos separados: Coordinador delega; Logística y seguimiento consulta fechas/pedidos/alertas; Costos consulta cálculos/cotizaciones; Conocimiento consulta fuentes y vecinos del grafo. `GeminiClient.java` conecta el modelo y `WorkAgentTools.java` autoriza y proyecta la información de una obra.

Se ejecutan por solicitud del usuario desde la interfaz, con delegación y selección de herramientas decididas por el modelo. La conexión real exige una clave y una consulta exitosa; consulta [IA_CONFIGURACION.md](IA_CONFIGURACION.md). Los contextos comparten el modelo, no el historial. Una respuesta guardada alimenta el grafo como propuesta confirmada, con fuentes.

## Herramientas locales por reglas

| Agente | Implementación | Entradas | Resultado | Conexión con el grafo |
|---|---|---|---|---|
| Orquestador | `agents/Orchestrator.java` en fuentes Java | Materiales, proveedores y cotizaciones de una obra | Métricas, estimación, alertas y catálogo de agentes | Coordina los tres especialistas; nodo OP-Agente-Orquestador |
| Logística | `agents/LogisticsAgent.java` | Fecha necesaria, plazo y estado del material | Fecha sugerida y alerta crítica/próxima | Aplica OP-Regla-Logistica; implementa RF-11 |
| Costos | `agents/CostAgent.java` | Cantidades requeridas y cotizaciones válidas | Subtotal conocido, líneas y faltantes | Aplica OP-Regla-Costos; implementa RF-10 |
| Seguimiento | `agents/TrackingAgent.java` | Estado de pedido y fecha estimada | Aviso de confirmación manual | Aplica OP-Regla-Seguimiento; implementa RF-11 |
| Conocimiento | `agents/KnowledgeAgent.java` | Estado confirmado, notas documentales y aprendizajes | Publicación de notas, consulta, fuentes y relaciones | Implementa RF-14/RF-15; conecta entidades, reglas y requisitos |

Ruta base Java: `src/main/java/co/obraplan/`. Las clases comparten `AgentData` y devuelven avisos mediante `Insight`, que también identifica su nota fuente.

Estos componentes son módulos deterministas por reglas. Su separación por sí sola no constituye el sistema multiagente de IA solicitado. La capa anterior los utiliza como herramientas; los hechos comerciales siguen siendo datos ingresados por el usuario.

## Roles para construir y continuar el proyecto

| Rol | Skill local | Qué produce |
|---|---|---|
| Planteamiento del problema | `.agents/skills/obraplan-problema/SKILL.md` | Contexto, actores, problema central, objetivos y alcance en PROBLEMA.md |
| Análisis de requisitos | `.agents/skills/obraplan-requisitos/SKILL.md` | RF/RNF, reglas, aceptación y trazabilidad |
| Curador de conocimiento | `.agents/skills/obraplan-conocimiento/SKILL.md` | Consulta previa, relaciones y publicación conservando aprendizajes |

Una skill contiene instrucciones para el asistente; no es un servicio permanentemente activo. `AGENTS.md` orienta su selección y `COMO_SE_HACEN_LAS_COSAS.md` localiza sus fuentes. Son skills creadas para continuar el desarrollo de ObraPlan.

## Agente Python de conocimiento

`agents/knowledge_agent.py` tiene tres operaciones:

- `query`: busca conceptos relevantes, incorpora vecinos del grafo y devuelve fuentes/relaciones. El modo base no genera texto con un LLM.
- `graph`: exporta nodos y relaciones, opcionalmente incluyendo registros de una obra.
- `external`: ejecuta un agente registrado con su intérprete/carpeta y JSON stdin/stdout.

`query --llm` puede usar un modelo Ollama local ya instalado y configurado con `OBRAPLAN_OLLAMA_MODEL`. No se descarga automáticamente ni se afirma validación de ese modo sin ejecutarlo.

La consulta web ofrece recuperación con extractos y navegación de relaciones dentro de la obra autorizada. La herramienta Python es local y opera con permisos del usuario del computador; no tiene autenticación de usuarios web propia.

## Integraciones externas

`config/integrations.json` permite registrar agentes externos con raíz, intérprete, punto de entrada y contrato JSON. Se habilitan después de comprobar su funcionamiento. El adaptador conserva el entorno de cada agente mediante un subproceso con carpeta de trabajo e intérprete explícitos. Las salidas verificadas pueden conectarse a requisitos y notas del proyecto.
