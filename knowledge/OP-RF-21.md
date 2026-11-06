---
id: OP-RF-21
tipo: requisito
tags: [obraplan, requisito]
---

# RF-21: Usar persistencia PostgreSQL y sesiones durables.

Usar persistencia PostgreSQL y sesiones durables.

Criterio observable: Producción rechaza arranque sin DATABASE_URL. Reinicio conserva registros y sesiones vigentes; escrituras inválidas revierten; escrituras concurrentes no pierden cambios; cierre revoca sesión en otras instancias. Migración solo admite destino vacío.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
