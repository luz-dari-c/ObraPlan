---
id: OP-Ciclo-Persistente
tipo: componente
tags: [obraplan, componente]
---

# Ciclo persistente por eventos

Disparador transaccional, reserva exclusiva y ejecución con herramientas. Pendientes sobreviven a reinicios; interrupciones no disparan reintentos ciegos. Seguimiento optativo limitado a tres eventos por obra/día.

## Fuente

`src/main/java/co/obraplan/AgentePersistente.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- implementa:: [[OP-RF-23]]
- implementa:: [[OP-RNF-13]]
- usa:: [[OP-Memoria-Episodica]]
- usa:: [[OP-IA-Coordinador]]
