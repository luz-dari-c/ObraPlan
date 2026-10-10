---
id: OP-Agente-Seguimiento
tipo: agente
tags: [obraplan, agente]
---

# Agente de seguimiento

Revisa pedidos realizados/en tránsito con fecha estimada vencida. Propone confirmación manual.

## Fuente

`src/main/java/co/obraplan/agents/TrackingAgent.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- aplica:: [[OP-Regla-Seguimiento]]
- analiza:: [[OP-Entidad-Cotizacion]]
- implementa:: [[OP-RF-11]]
