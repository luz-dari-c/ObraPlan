---
id: OP-Componente-Server
tipo: componente
tags: [obraplan, componente]
---

# Server: controlador HTTP y sesión

Enruta solicitudes, verifica sesión y entrega JSON o recursos. Escucha solo en 127.0.0.1.

## Fuente

`src/main/java/co/obraplan/Server.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- delega_en:: [[OP-Componente-AppService]]
- implementa:: [[OP-RF-01]]
- implementa:: [[OP-RNF-06]]
