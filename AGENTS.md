# Continuidad de ObraPlan

Lee `README.md`, `COMO_SE_HACEN_LAS_COSAS.md` y `docs/BITACORA.md` antes de cambiar el proyecto. El mapa operativo contiene las rutas de código, datos, conocimiento, scripts y entornos.

## Roles y skills de este proyecto

| Encargo | Skill | Salida canónica |
|---|---|---|
| Aclarar origen, problema, actores u objetivos | `.agents/skills/obraplan-problema/SKILL.md` | `docs/PROBLEMA.md` |
| Cambiar alcance, requisitos, reglas o aceptación | `.agents/skills/obraplan-requisitos/SKILL.md` | `docs/ANALISIS.md`, `docs/TRAZABILIDAD.md` |
| Alimentar/consultar memoria y relaciones | `.agents/skills/obraplan-conocimiento/SKILL.md` | `knowledge/`, namespace de Obsidian y evidencia |

Son skills propias. No las atribuyas al profesor. Los agentes Java ejecutables están separados en `src/main/java/co/obraplan/agents/`; la herramienta Python está en `agents/knowledge_agent.py`. Una skill indica cómo trabajar; no es por sí sola un proceso en ejecución.

## Contexto y reglas que se deben conservar

- Aplicación Java 21 local para adquisiciones. No añadir 3D/planos/compras automáticas como si ya fueran parte del alcance.
- Datos de obra en `data/obraplan.json`; nunca reemplazarlos con datos de prueba. Usa una base temporal para comprobar cambios.
- Sin precio significa nulo; Costos excluye Rechazada/Cancelado. Seguimiento evalúa solo pedidos en curso.
- Autorización por propietario y obra también para conocimiento. No exportar usuarios, hashes o sesiones al vault.
- Notas generadas: derivadas del catálogo/requisitos o de la base. Aprendizajes: área editable que no se sobrescribe.
- `config/integrations.json` registra integraciones. El agente del profesor permanece deshabilitado mientras no haya rutas/contrato comprobados.
- No importes dependencias de otro entorno: usa el adaptador de subproceso con intérprete/cwd explícitos y JSON.

## Memoria y cierre de cambios

Consulta `python agents/knowledge_agent.py query "tema"` antes de alterar reglas del negocio; las fuentes y los vecinos ayudan a localizar requisitos/componentes. Revisa después los archivos fuente, pues una nota derivada puede estar desactualizada.

Cuando cambie comportamiento, actualiza requisito, criterio observable, nota/relación y bitácora. Regenera notas con `python scripts/build_knowledge.py`, verifica enlaces con `python scripts/sync_knowledge.py --check` y publica dentro del vault autorizado. Comprueba solo lo relevante y registra resultados reales en `docs/VERIFICACION.md` y `output/verification/`.

Mantén indentación con `.editorconfig`; añade funciones/clases legibles, evitando volver a comprimir métodos en una línea.
