---
id: OP-Entidad-Cotizacion
tipo: entidad
tags: [obraplan, entidad]
---

# Entidad Cotización

Precio, cantidad y plazo vinculados a material y proveedor de la misma obra.

## Fuente

`docs/ARQUITECTURA.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- cotiza:: [[OP-Entidad-Material]]
- ofertada_por:: [[OP-Entidad-Ferreteria]]
- satisface:: [[OP-RF-08]]
