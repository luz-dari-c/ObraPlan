---
id: OP-Componente-AppService
tipo: componente
tags: [obraplan, componente]
---

# AppService: casos de uso y autorización

Valida datos, autoriza por propietario/obra y coordina registros y análisis.

## Fuente

`src/main/java/co/obraplan/AppService.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- usa:: [[OP-Componente-Database]]
- usa:: [[OP-Agente-Orquestador]]
- usa:: [[OP-Agente-Conocimiento]]
- implementa:: [[OP-RF-03]]
- implementa:: [[OP-RF-05]]
- implementa:: [[OP-RF-07]]
- implementa:: [[OP-RF-08]]
- implementa:: [[OP-RF-09]]
