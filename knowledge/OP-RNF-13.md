---
id: OP-RNF-13
tipo: requisito
tags: [obraplan, requisito]
---

# RNF-13: Controlar consumo y observar el ciclo persistente.

Controlar consumo y observar el ciclo persistente.

Criterio observable: Máximo tres eventos automáticos por obra/día, contexto histórico acotado, sin llamadas al modelo por consultar estado, cancelación de pendientes al pausar y explicación visible de modelo, mensajes, configuración y memoria. Render dormido no implica trabajo en ejecución.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
