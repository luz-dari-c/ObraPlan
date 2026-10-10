---
id: OP-Componente-Database
tipo: componente
tags: [obraplan, componente]
---

# Database: datos y cambios confirmados

JSON con escritura atómica. Opera sobre una copia y notifica al agente de conocimiento después del guardado.

## Fuente

`src/main/java/co/obraplan/Database.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- implementa:: [[OP-RF-12]]
- implementa:: [[OP-RNF-05]]
- notifica:: [[OP-Agente-Conocimiento]]
