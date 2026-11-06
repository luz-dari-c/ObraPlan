---
id: OP-RNF-06
tipo: requisito
tags: [obraplan, requisito]
---

# RNF-06: Seguridad local y aislamiento por obra.

Seguridad local y aislamiento por obra.

Criterio observable: Loopback en desarrollo; HTTPS y cookie Secure en producción; PBKDF2 y cookie HttpOnly/SameSite; conocimiento exige propietario y no exporta hashes/sesiones.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
