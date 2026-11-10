---
id: OP-Memoria-Episodica
tipo: componente
tags: [obraplan, componente]
---

# Memoria episódica de los agentes

recuperarMemoria filtra por propietario y obra; guardarMemoria conserva resúmenes completos deduplicados. Son antecedentes incluidos como datos históricos en contents, no hechos actuales ni instrucciones.

## Fuente

`src/main/java/co/obraplan/MemoriaAgente.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- implementa:: [[OP-RF-22]]
- usa:: [[OP-Persistencia-Postgres]]
- pertenece_a:: [[OP-Proyecto]]
