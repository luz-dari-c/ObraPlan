---
id: OP-RNF-11
tipo: requisito
tags: [obraplan, requisito]
---

# RNF-11: Acotar consumo y detener errores del proveedor.

Acotar consumo y detener errores del proveedor.

Criterio observable: Hasta 10 llamadas por análisis, una ejecución simultánea, caché sensible a cambios, límite diario por proceso y ningún reintento automático ante cuota agotada. Google conserva sus propias cuotas.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
