---
id: OP-Entidad-Material
tipo: entidad
tags: [obraplan, entidad]
---

# Entidad Material

Necesidad: nombre, cantidad, unidad, etapa, fecha necesaria, plazo y estado.

## Fuente

`docs/ARQUITECTURA.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Entidad-Obra]]
- recibe:: [[OP-Entidad-Cotizacion]]
- satisface:: [[OP-RF-05]]
