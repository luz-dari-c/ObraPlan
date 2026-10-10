# Fuentes, procedencia y límites de la revisión

Revisión: **1 de octubre de 2026**, zona horaria America/Bogota.

| Fuente | Qué se comprobó | Cómo se utilizó |
|---|---|---|
| Solicitud del usuario | README, bitácora, mapa de archivos/entornos, agentes, orden, grafo y video | Requisitos de esta revisión |
| Dos capturas suministradas | Se ven `output`, `scripts`, `.env.example`, `AGENTS.md`, `COMO_SE_HACEN_LAS_COSAS.md`, `pyproject.toml`, `README.md`, `uv.lock` | Referencia de organización; no prueba del contenido de los archivos |
| Código y documentación inicial de ObraPlan | Java 21, adquisiciones, agentes por reglas y JSON | Base real del producto conservada y mejorada |
| `C:\cerebro_dev` | Vault activo según `%APPDATA%\obsidian\obsidian.json`, índices y notas existentes | Destino de integración local |
| `Definicion Proceso de Analisis/08_Propuesta_integrada_v0.4/02_Proceso_operativo.md` | Necesidad → reglas/requisitos → revisión y evidencia; distingue propuestas de aprobaciones | Referencia metodológica del material local; no se atribuye automáticamente al profesor |
| `Liderazgo Proyecto` | Solo se encontraron salidas de planificación y carpetas auxiliares | No se identificó un agente Python ejecutable |
| Notas locales `Conocimiento/LangChain.md` e `Integracion-LangChain-Gemini-Ollama.md` | Describen agentes con modelo/herramientas y citan un sitio de Gerardo Esquivia | Antecedente del interés en agentes y memoria; no se adoptaron comandos/modelos sin comprobación |

## Referencias técnicas consultadas

- [Enlaces de Obsidian](https://help.obsidian.md/links): base del vínculo entre notas mediante wikilinks.
- [API de chat de Ollama](https://docs.ollama.com/api/chat): contrato del modo LLM opcional del agente Python.
- [CLI de Prettier](https://prettier.io/docs/cli): formato del cliente HTML, CSS y JavaScript.
- [Google Java Format](https://github.com/google/google-java-format): formato del código Java; se utilizó la versión 1.24.0 compatible con el entorno de esta revisión.
- [Sitio público de Gerardo Esquivia](https://gerardoesquivia.com/): su página describe proyectos y un sistema de conocimiento personal. Esta consulta no permitió recuperar los agentes concretos de planteamiento y requisitos. El sitio fue identificado por enlaces en el vault; la identidad del profesor no está confirmada por otro documento.

## Pendiente específico

No se encontraron los archivos originales de los agentes del profesor ni una rúbrica completa. `config/integrations.json` deja deshabilitada esa integración hasta conocer raíz, intérprete y entrypoint. El usuario puede proporcionar la ubicación; entonces se registran las rutas reales, se adapta el contrato y se conserva evidencia de la ejecución. Las skills de esta entrega son propias de ObraPlan y los documentos de análisis son una reconstrucción del fundamento actual, no una historia inventada de ejecuciones previas.

No se afirma conformidad oficial ni aprobación del profesor. La matriz de evidencia muestra cómo demostrar los puntos pedidos por el usuario y visibles en las capturas.
