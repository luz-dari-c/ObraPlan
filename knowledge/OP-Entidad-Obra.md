---
id: OP-Entidad-Obra
tipo: entidad
tags: [obraplan, entidad]
---

# Entidad Obra

Proyecto con propietario, ubicación, etapa e inicio. Agrupa necesidades y precios.

## Fuente

`docs/ARQUITECTURA.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- contiene:: [[OP-Entidad-Material]]
- contiene:: [[OP-Entidad-Ferreteria]]
- contiene:: [[OP-Entidad-Cotizacion]]
- satisface:: [[OP-RF-03]]
