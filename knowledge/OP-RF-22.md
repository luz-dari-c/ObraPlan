---
id: OP-RF-22
tipo: requisito
tags: [obraplan, requisito]
---

# RF-22: Recuperar y guardar memoria episódica del agente.

Recuperar y guardar memoria episódica del agente.

Criterio observable: Cada consulta recupera hasta tres recuerdos del propietario y obra y los incluye en contents como antecedentes. Solo resultados completos generan recuerdos deduplicados por runId. Persisten tras reiniciar y no modifican adquisiciones ni publican propuestas en el grafo sin confirmación.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
