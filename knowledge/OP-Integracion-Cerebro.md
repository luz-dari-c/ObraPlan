---
id: OP-Integracion-Cerebro
tipo: integracion
tags: [obraplan, integracion]
---

# Memoria compartida entre proyectos y entornos

El agente consulta notas generales ya existentes mediante una lista explícita en config/integrations.json. Leer sobre LangChain u Ollama no significa ejecutar esos frameworks. Cada agente externo conserva su intérprete y contrato.

## Fuente

`docs/CONOCIMIENTO.md`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- conecta:: [[OP-Proyecto]]
- conecta:: [[OP-Agente-Conocimiento]]
- consulta:: [[Conocimiento/LangChain]]
- consulta:: [[Conocimiento/Ollama]]
- consulta:: [[Conocimiento/Integracion-LangChain-Gemini-Ollama]]
- se_configura_con:: [[OP-Tecnologia-Python]]
