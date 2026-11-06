---
id: OP-Persistencia-Postgres
tipo: componente
tags: [obraplan, componente]
---

# Persistencia PostgreSQL y sesiones

Registros JSONB por entidad e índices, transacciones con bloqueo entre instancias, sesiones con hash y caducidad y propuestas confirmadas durables. JSON compatible para migración explícita.

## Fuente

`src/main/java/co/obraplan/AlmacenPostgres.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- implementa:: [[OP-RF-21]]
- pertenece_a:: [[OP-Proyecto]]
