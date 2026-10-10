---
id: OP-IA-Coordinador
tipo: agente-ia
tags: [obraplan, agente-ia]
---

# Coordinador IA durante el uso

Interpreta la pregunta y delega mediante funciones del modelo en tres contextos especialistas. Tras completarlos sintetiza sin volver a delegar, con espacio de salida reservado y referencias breves vinculadas a fuentes canónicas. Informa el motivo de detención y conserva solo propuestas completas y vigentes.

## Fuente

`src/main/java/co/obraplan/agents/ai/AgentTeam.java`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- coordina:: [[OP-IA-Logistica]]
- coordina:: [[OP-IA-Costos]]
- coordina:: [[OP-IA-Conocimiento]]
- implementa:: [[OP-RF-16]]
- implementa:: [[OP-RF-17]]
- implementa:: [[OP-RNF-11]]
- se_configura_en:: [[OP-IA-Configuracion]]
