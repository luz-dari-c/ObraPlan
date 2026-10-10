---
id: OP-Agente-Logistica
tipo: agente
tags: [obraplan, agente]
---

# Agente de logística

Calcula fecha sugerida de gestión a partir de necesidad y plazo; alerta fechas próximas/vencidas.

## Fuente

`src/main/java/co/obraplan/agents/LogisticsAgent.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- aplica:: [[OP-Regla-Logistica]]
- analiza:: [[OP-Entidad-Material]]
- implementa:: [[OP-RF-11]]
