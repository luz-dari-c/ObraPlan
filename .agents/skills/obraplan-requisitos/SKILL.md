---
name: obraplan-requisitos
description: Definir o revisar RF, RNF, reglas y criterios de aceptación de ObraPlan y su trazabilidad hacia componentes y evidencia.
---

# Análisis de requisitos de ObraPlan

Lee `docs/PROBLEMA.md`, `docs/ANALISIS.md` y `docs/TRAZABILIDAD.md`. Consulta el grafo para localizar componentes afectados; abre el código antes de afirmar que un criterio está implementado.

Mantén `ANALISIS.md` como fuente canónica: IDs RF/RNF estables, conducta y criterio observable. Deriva cada requisito de una necesidad; no confundas un requisito de negocio con una herramienta escogida. Los RNF deben ser comprobables sin inventar cifras de carga o aprobación humana. Distingue propuesta, implementación comprobada y pendiente.

Conserva las reglas de costos sin precio nulo y exclusión Rechazada/Cancelado, logística por fecha menos plazo y seguimiento solo de pedidos en curso. Evalúa autorización, integridad y efecto de un fallo cuando cambie una integración.

Actualiza trazabilidad y bitácora. Ejecuta `python scripts/build_knowledge.py` y `python scripts/sync_knowledge.py --check` para que las notas deriven de los requisitos. Registra comprobaciones reales en `docs/VERIFICACION.md`. No atribuyas esta skill ni una reconstrucción de análisis a ejecuciones de agentes originales del profesor.
