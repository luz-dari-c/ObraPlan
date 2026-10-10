---
id: OP-Voz-Bienvenida
tipo: componente
tags: [obraplan, componente]
---

# Bienvenida y resumen por voz de la obra

Guía contextual y recorrido completo de ocho secciones para principiantes: crear obra, materiales/campos, proveedores, cotizaciones/ejemplo, costos/alertas, pedidos/recepción, agentes/grafo y rutina diaria. Ofrece siguiente acción y lectura por sección con voces locales sin Gemini. Se actualiza tras guardar sin repetir voz. Los ejemplos no crean datos; es presentación por reglas, no un agente adicional con modelo.

## Fuente

`src/main/resources/static/welcome.js; src/main/resources/static/tutorial.js`

Nota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.

## Relaciones

- pertenece_a:: [[OP-Proyecto]]
- implementa:: [[OP-RF-19]]
- consulta:: [[OP-Agente-Orquestador]]
- aplica:: [[OP-Regla-Costos]]
- aplica:: [[OP-Regla-Logistica]]
- aplica:: [[OP-Regla-Seguimiento]]
