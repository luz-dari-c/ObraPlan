---
id: OP-IA-Conocimiento
tipo: agente-ia
tags: [obraplan, agente-ia]
---

# Agente IA de conocimiento

Recupera fuentes y vecinos del grafo para el modelo. Propuestas confirmadas se vinculan a obra, coordinador y fuentes.

## Fuente

`src/main/java/co/obraplan/WorkAgentTools.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- usa:: [[OP-Agente-Conocimiento]]
- implementa:: [[OP-RF-16]]
- implementa:: [[OP-RF-18]]
- alimenta:: [[OP-Tecnologia-Obsidian]]
