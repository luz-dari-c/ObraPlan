# Grafo, memoria y conexión con Obsidian

## Conocimiento que ya contiene

El grafo documental tiene notas de proyecto, problema, roles de análisis, 15 RF, 10 RNF, arquitectura, componentes, agentes, reglas, entidades, tecnologías, fuentes, bitácora y video. Cada nota tiene ID estable y enlaces. `catalog.json` define conceptos/relaciones; RF/RNF se derivan de la tabla canónica de `ANALISIS.md`.

Ejemplo de relación semántica:

```text
OP-Problema ─origina→ OP-RF-10
OP-Agente-Costos ─implementa→ OP-RF-10
OP-Agente-Costos ─aplica→ OP-Regla-Costos
OP-Regla-Costos ─sustenta→ OP-RNF-01
```

Obsidian dibuja conexiones a partir de `[[wikilinks]]`. El texto `implementa::`, `aplica::`, etc., conserva el significado de cada relación; no se requiere plugin comunitario. La vista web permite explorar el mismo tipo de conexiones y ver sus nombres.

## Ubicaciones

- Fuente documental: `knowledge/` en el proyecto.
- Vault existente: `C:\cerebro_dev`.
- Publicación documental: `Proyectos/ObraPlan/Conocimiento/` y `Documentacion/`.
- Estado operativo generado: `Proyectos/ObraPlan/Obras/<workId>/`.
- Conocimiento manual: `Proyectos/ObraPlan/Aprendizajes/`.
- Reportes locales: `output/knowledge/graph.json`, `sync-report.json` y `runtime/`.

El índice existente de proyectos recibe un enlace a OP-Proyecto, sin reemplazar su contenido. Antes del primer cambio del índice se guarda una copia bajo `output/backups/`.

`sharedKnowledgeFiles` en `config/integrations.json` permite consultar conocimiento general de otras carpetas del mismo vault. Ya están registradas las notas existentes `Conocimiento/LangChain.md`, `Conocimiento/Ollama.md` e `Integracion-LangChain-Gemini-Ollama.md`. Java y Python leen su contenido actual sin sobrescribirlas. `OP-Integracion-Cerebro` conecta esas notas con el proyecto; las notas y relaciones compartidas también aparecen en la consulta. Otras notas se incorporan mediante rutas explícitas dentro del vault. Esto reutiliza conocimiento; no ejecuta automáticamente un framework ni habilita otro agente.

## Cómo se alimenta

1. Cambiar requisitos o conceptos: editar `ANALISIS.md` o `knowledge/catalog.json`; ejecutar `build_knowledge.py` y `sync_knowledge.py`.
2. Registrar/editar una obra, material, ferretería, cotización o estado en la aplicación: Database confirma el guardado; KnowledgeAgent publica el estado y sus relaciones automáticamente.
3. Escribir un aprendizaje: crear Markdown en `Aprendizajes/`, con nombre único OP-Aprendizaje-..., título y enlaces a nodos existentes. La consulta Java/Python lo incluye directamente. Esa nota no cambia reglas ni pedidos del sistema.

Las notas operativas muestran material → obra, cotización → material/proveedor y registros → entidad/agente. Cada obra tiene su propia carpeta. Se minimizan datos: no se exportan usuarios, hashes, sesiones, teléfonos ni direcciones de proveedores. Un problema de escritura aparece como sincronización pendiente y se reintenta desde la aplicación o al reiniciar.

## Cómo consulta el agente

La consulta web recupera notas por términos normalizados, devuelve extractos, fuentes y relaciones; permite navegar el grafo y abrir una nota en Obsidian. La herramienta Python selecciona notas relevantes y añade vecinos conectados antes de devolver evidencia. No usa un modelo de lenguaje por defecto.

El modo opcional `--llm` envía la evidencia recuperada a un Ollama **local** ya instalado, mediante su API. Debe indicarse un modelo existente con `OBRAPLAN_OLLAMA_MODEL`. No se instala/descarga un modelo automáticamente y no se presenta ese modo como validado si no se ejecutó.

```powershell
python agents/knowledge_agent.py query "costo sin cotización"
python agents/knowledge_agent.py query "cemento" --work-id w-demo
# Solo cuando exista un modelo local configurado:
python agents/knowledge_agent.py query "explica la logística" --llm
```

## Ver el grafo en Obsidian

Abre el vault cerebro_dev y busca `OP-Proyecto`. Usa la vista de grafo global o el grafo local de esa nota. Para enfocar la entrega, filtra por `path:"Proyectos/ObraPlan"`. Los enlaces conectan notas del proyecto; sus referencias muestran rutas a otras carpetas. El índice compartido de proyectos conecta ObraPlan con el cerebro existente.

El archivo JSON es una exportación para herramientas, no un requisito para que Obsidian dibuje el grafo. Las notas Markdown con enlaces son suficientes. El agente no consulta una imagen del grafo: consulta las notas y la estructura de relaciones que produce esa imagen.

## Mantener las relaciones

Usa IDs únicos, enlaces que apunten a notas existentes y una relación con significado. Ejecuta `sync_knowledge.py --check` para detectar destinos internos inexistentes. No presentes una nota provisional como validación del profesor ni una regla documental como acción comercial ejecutada.
## Análisis generados durante el uso

El especialista de Conocimiento IA consulta extractos y relaciones de la obra mediante WorkAgentTools. Los agentes y requisitos nuevos se relacionan en el catálogo con herramientas, reglas y evidencia. No basta almacenar notas para que exista razonamiento IA: el modelo decide cuándo consultar esas herramientas durante una ejecución.

Conservar propuesta crea `OP-Analisis-<runId>` con procedencia y vínculos a obra, coordinador y fuentes. Las carpetas `output/knowledge/analyses/<workId>` y `Proyectos/ObraPlan/Analisis/<workId>` se mantienen separadas de Aprendizajes y de las notas operativas regenerables. Cada consulta web mantiene autorización por obra. Python requiere `--work-id` para incluirlas y funciona con permisos del usuario del equipo. La sincronización documental reintenta publicar los análisis locales, sin sobrescribir aprendizajes.
