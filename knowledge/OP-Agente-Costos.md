---
id: OP-Agente-Costos
tipo: agente
tags: [obraplan, agente]
---

# Agente de costos

Estima costo conocido, informa faltantes y excluye cotizaciones rechazadas/canceladas.

## Fuente

`src/main/java/co/obraplan/agents/CostAgent.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- aplica:: [[OP-Regla-Costos]]
- analiza:: [[OP-Entidad-Material]]
- analiza:: [[OP-Entidad-Cotizacion]]
- implementa:: [[OP-RF-10]]
