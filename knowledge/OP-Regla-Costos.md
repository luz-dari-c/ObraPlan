---
id: OP-Regla-Costos
tipo: regla
tags: [obraplan, regla]
---

# Regla: costo conocido y materiales sin cotización

Subtotal = cantidad requerida × menor precio unitario válido. Cancelado/Rechazada se excluyen. Sin cotización: precio/subtotal nulos, no costo cero. El total es parcial con faltantes; no incluye flete/impuestos/calidad.

## Fuente

`docs/ANALISIS.md; src/main/java/co/obraplan/agents/CostAgent.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- sustenta:: [[OP-RF-10]]
- sustenta:: [[OP-RNF-01]]
- implementada_por:: [[OP-Agente-Costos]]
