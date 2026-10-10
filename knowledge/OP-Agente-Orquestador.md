---
id: OP-Agente-Orquestador
tipo: agente
tags: [obraplan, agente]
---

# Agente orquestador

Java por reglas: reúne métricas, costos y alertas. No utiliza un LLM.

## Fuente

`src/main/java/co/obraplan/agents/Orchestrator.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- coordina:: [[OP-Agente-Logistica]]
- coordina:: [[OP-Agente-Costos]]
- coordina:: [[OP-Agente-Seguimiento]]
- implementa:: [[OP-RF-04]]
- implementa:: [[OP-RF-11]]
- implementa:: [[OP-RF-13]]
