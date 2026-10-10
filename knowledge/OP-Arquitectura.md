---
id: OP-Arquitectura
tipo: arquitectura
tags: [obraplan, arquitectura]
---

# Arquitectura: vista, servicios, agentes y memoria

Vista → Server → AppService → Database/Orchestrator. Cambio persistido → KnowledgeAgent. Entidades conceptuales en objetos JSON.

## Fuente

`docs/ARQUITECTURA.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- contiene:: [[OP-Componente-Server]]
- contiene:: [[OP-Componente-AppService]]
- contiene:: [[OP-Componente-Database]]
- contiene:: [[OP-Agente-Orquestador]]
- contiene:: [[OP-Agente-Conocimiento]]
- contiene:: [[OP-IA-Coordinador]]
- usa:: [[OP-Tecnologia-Java]]
- usa:: [[OP-Tecnologia-Python]]
- usa:: [[OP-Tecnologia-Obsidian]]
