---
id: OP-Regla-Seguimiento
tipo: regla
tags: [obraplan, regla]
---

# Regla: entregas de pedidos en curso

Pedido realizado/En tránsito con fecha anterior a hoy: revisar con proveedor. Recibido/Cancelado/Rechazada y cotización aún no pedida no alertan.

## Fuente

`docs/ANALISIS.md; src/main/java/co/obraplan/agents/TrackingAgent.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- sustenta:: [[OP-RF-09]]
- sustenta:: [[OP-RF-11]]
- implementada_por:: [[OP-Agente-Seguimiento]]
