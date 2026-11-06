---
id: OP-Componente-Database
tipo: componente
tags: [obraplan, componente]
---

# Database: datos y cambios confirmados

Fachada compatible con JSON local y PostgreSQL transaccional. Notifica al conocimiento después del commit sin deshacer datos confirmados si falla una proyección.

## Fuente

`src/main/java/co/obraplan/Database.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- implementa:: [[OP-RF-12]]
- implementa:: [[OP-RNF-05]]
- notifica:: [[OP-Agente-Conocimiento]]
