---
name: obraplan-problema
description: Plantear o revisar el problema, actores, objetivos y alcance del proyecto ObraPlan, conservando procedencia y límites comprobados.
---

# Planteamiento del problema de ObraPlan

Lee `COMO_SE_HACEN_LAS_COSAS.md`, `docs/PROBLEMA.md` y `docs/FUENTES_Y_REFERENCIAS.md` desde la raíz del proyecto. Consulta `python agents/knowledge_agent.py query "problema alcance"` para ubicar relaciones; verifica las fuentes del resultado.

Reúne contexto, situación actual, problema central, actores, objetivo y alcance. Separa hechos del repositorio, hipótesis del caso académico y preguntas pendientes. No inventes entrevistas, mediciones ni aprobaciones. El producto actual se centra en adquisiciones manuales; cantidades estructurales, planos y 3D requieren un cambio de alcance explícito.

Actualiza `docs/PROBLEMA.md` con una explicación entendible sin código. Entrega necesidades identificadas al rol de requisitos y conserva la decisión de alcance en `docs/BITACORA.md`. Regenera notas con `scripts/build_knowledge.py` cuando cambie el fundamento. Esta skill es propia de ObraPlan.
