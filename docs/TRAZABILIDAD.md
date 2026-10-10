# Trazabilidad: necesidad, requisito, agente y evidencia

Los requisitos canónicos están en `ANALISIS.md`; esta tabla explica dónde se materializan y cómo se muestran.

| Necesidad | Requisitos | Componente / agente | Evidencia para el video |
|---|---|---|---|
| N-01 Organizar información por obra | RF-01 a RF-08, RF-12 | Server, AppService, Database, vista | Acceder, crear obra/material/proveedor/cotización; filtrar; reiniciar |
| N-02 Gestionar materiales a tiempo | RF-04, RF-05, RF-11 | LogisticsAgent + Orchestrator | Material necesario mañana con 3 días de plazo → alerta |
| N-03 Conocer costos y faltantes | RF-08, RF-10, RNF-01 | CostAgent | Material sin precio; luego 10 bultos × COP 40.000 = COP 400.000 |
| N-04 Seguir pedidos y entregas | RF-09, RF-11 | AppService + TrackingAgent | Pedido → tránsito → recibido; fecha estimada vencida genera aviso solo mientras está en curso |
| N-01 a N-04 Comprender el estado y por dónde empezar | RF-19, RNF-07 | welcome.js, app.js, tablero de Orchestrator | Obra vacía → ejemplo del primer material y botón al formulario; guardar → siguiente paso; recorrido y resumen por voz con detener/silenciar |
| N-05 Conservar conocimiento para continuar | RF-13 a RF-15, RNF-08 a RNF-10 | KnowledgeAgent, knowledge_agent.py, skills | Consultar regla con fuentes; explorar agente→regla→RF; registrar material y ver nota/relaciones en Obsidian |
| N-02 a N-05 Analizar la obra durante el uso con IA | RF-16 a RF-18, RNF-11 y RNF-12 | AgentTeam, GeminiClient, WorkAgentTools, assistant.js | Pregunta → delegación → herramientas → respuesta/fuentes/tokens → conservar propuesta conectada al grafo; conexión real pendiente hasta configurar clave |

## Requisitos de calidad y evidencia

| Requisito | Dónde comprobarlo |
|---|---|
| RNF-01 Claridad | Sin cotizar/Subtotal parcial y datos demo identificados |
| RNF-02 Integridad | Pruebas de cantidad inválida, edición fallida y vínculo a otra obra |
| RNF-03 Lectura | Clases de agentes separadas y código indentado |
| RNF-04 Reproducibilidad | run.ps1, compile.ps1, pyproject.toml, uv.lock |
| RNF-05 Persistencia | Guardar/reiniciar y simular publicación fallida sin perder datos |
| RNF-06 Aislamiento | Sin sesión: 401; obra de otro usuario: 404; grafo sin usuarios/hashes |
| RNF-07 Usabilidad | Recorrido de interfaz a ancho de escritorio y móvil |
| RNF-08 Evidencia | Fuentes, bitácora, reportes y pendientes explícitos |
| RNF-09 Entornos | Prueba del adaptador con intérprete/cwd propios; integraciones externas requieren configuración comprobada |
| RNF-10 Memoria editable | Sincronizar dos veces: índice único y aprendizajes conservados |

## Lo solicitado por el usuario y visible en las capturas

| Punto de entrega | Ubicación | Qué mostrar |
|---|---|---|
| README: para qué sirve y bitácora | README.md, docs/BITACORA.md | Propósito y enlace a decisiones |
| Mapa de archivos y entornos | COMO_SE_HACEN_LAS_COSAS.md | Ruta de cada componente y registro externo |
| Instrucciones para agentes | AGENTS.md y .agents/skills | Roles, entradas/salidas y consulta previa del conocimiento |
| Carpeta de scripts y salidas | scripts/, output/ | Comandos repetibles y evidencias derivadas |
| Variables y proyecto Python | .env.example, pyproject.toml, uv.lock | Configuración e independencia de Java/Python |
| Planteamiento y análisis | PROBLEMA.md, ANALISIS.md | Necesidad → RF/RNF → solución |
| Grafo alimentado/conectado | knowledge/, Obsidian, Agentes y conocimiento | Relaciones, consulta y nueva nota tras guardar |
| Aplicación funcional | Navegador local | Caso completo de adquisición |
| Guion explicativo | GUIA_VIDEO.md | Narración, clics y resultados esperados |

Esta matriz relaciona necesidades, requisitos, componentes y evidencia observable de ObraPlan.
