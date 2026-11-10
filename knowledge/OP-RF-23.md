---
id: OP-RF-23
tipo: requisito
tags: [obraplan, requisito]
---

# RF-23: Activar agentes mediante eventos durables.

Activar agentes mediante eventos durables.

Criterio observable: Seguimiento optativo por obra: activar, guardar materiales o cambiar cotizaciones genera un evento transaccional. Se agrupan pendientes, se procesan con reserva exclusiva y se muestran disparador, estado y resultado. Pendientes se recuperan tras reinicio; interrumpidos no se reintentan automáticamente.

## Fuente

`docs/ANALISIS.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- se_demuestra_en:: [[OP-Trazabilidad]]
