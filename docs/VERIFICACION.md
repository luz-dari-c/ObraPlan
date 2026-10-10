# Verificación reproducible de ObraPlan

Revisión del 1 de octubre de 2026. Esta evidencia comprueba el comportamiento implementado.

## Ejecutar las comprobaciones

Desde la raíz, con JDK 21 y Python >=3.11:

```powershell
./scripts/verify.ps1
python scripts/build_knowledge.py
python scripts/sync_knowledge.py --check
# Con la aplicación iniciada en 8080:
.venv/Scripts/python.exe scripts/verify_http.py
```

Las pruebas crean bases y vaults temporales en `output/verification/`; no sustituyen `data/obraplan.json`. El aviso de vault inválido durante la prueba Java es deliberado: verifica que un fallo de publicación no pierda el registro de la aplicación.

## Resultados ejecutados

| Comprobación | Resultado | Evidencia |
|---|---|---|
| Java: reglas, validación, autorización, persistencia y memoria | 33 comprobaciones correctas | `output/verification/java-report.json` |
| Python: relaciones, recuperación, publicación conservadora y contrato externo | 15 comprobaciones correctas | `output/verification/python-report.json` |
| API HTTP en ejecución: autenticación, consulta, memoria y sincronización | 7 comprobaciones correctas | `output/verification/http-report.json` |
| Skills locales: problema, requisitos y conocimiento | Los tres pasan el validador de estructura | `.agents/skills/*/SKILL.md` |
| Navegador | Acceso demo, obra, agentes, consulta con fuentes y selección de nodos comprobados | Pantalla Agentes y conocimiento |
| Obsidian real | OP-Proyecto abierto; vínculos y grafo global reconocidos; filtro por carpeta | Vault `C:\cerebro_dev` |
| RNF-07: ancho pequeño | Vista revisada a 390 px; ancho de contenido = 375 px, igual al área útil; sin desbordamiento horizontal | `output/verification/conocimiento-movil.jpg` |

Java comprueba, entre otros casos: 10 × COP 40.000 = COP 400.000; faltantes sin precio nulo; cotizaciones canceladas/rechazadas excluidas; fechas vencidas de pedidos activos; recepción; rechazo de valores inválidos; rollback de una edición fallida; separación de obras; notas actualizadas y ausencia de credenciales en la exportación.

Python comprueba IDs únicos, los 25 requisitos, enlaces internos, consulta sin evidencia, conservación del índice y aprendizajes al sincronizar dos veces. Una integración sintética comprueba JSON, carpeta de trabajo e intérprete registrado. Esa prueba verifica el adaptador con un agente sintético, no una integración externa de producción.

La lectura de conocimiento compartido se comprueba con una nota temporal y, en Java, se vuelve a consultar después de editarla. Python también rechaza rutas compartidas fuera del vault.

La API de la obra demo devolvió **67 nodos y 232 relaciones**, sin destinos internos OP- inexistentes. La exportación documental contiene **58 nodos y 190 relaciones**: 55 notas del proyecto y tres notas generales registradas. Estos números corresponden a esta revisión; cambian cuando se incorporan registros o aprendizajes.

## Comprobación manual para la entrega

Sigue `GUIA_VIDEO.md` usando una obra de demostración separada: material → cotización → pedido → recepción. Luego consulta una regla, explora sus relaciones y abre la nota correspondiente en Obsidian. Para probar alimentación automática, modifica un material y comprueba su nota de obra. Para probar memoria editable, usa una nota de Aprendizajes.

Los reportes JSON son derivados de la ejecución más reciente. La exportación sin `--work-id` contiene conocimiento documental/compartido; la consulta de una obra agrega solamente los registros autorizados de esa obra. Por eso el número de nodos puede variar según la vista.

## Límites de esta revisión

### Carga del escenario para video, 2 de octubre

Se probó `scripts/demo/DemoData.java` en `output/demo-validation/`, incluida una segunda ejecución sin duplicar registros. Después se ejecutó con respaldo y servidor detenido en la base local. El reporte confirmó preservación de los registros anteriores y, por cuenta, 10 materiales, 4 proveedores y 8 cotizaciones. Costos: COP 2.152.000, cuatro faltantes; métricas: dos pedidos activos. El cemento tiene subtotal COP 400.000; las ofertas rechazadas y canceladas quedan excluidas. Evidencia en `output/demo/report.json` y explicación rápida en `DEMO_VIDEO.md`.

No se probaron carga masiva, despliegue multiusuario en red, recuperación ante corte de energía ni interpretación de planos. Ollama es opcional y no se ejecutó. Los datos y precios de la demostración no prueban disponibilidad comercial real.

## Integración IA, 8 de octubre de 2026

`./scripts/verify.ps1` incluye AgentVerification. El runtime se prueba con un proveedor simulado exclusivo de tests: 19 verificaciones de delegación a tres especialistas, selección de herramientas, firmas/metadata, caché, alcance por propietario/obra, conservación y publicación en vault temporal, contexto cambiado, falta de configuración, cuota agotada, rechazo de herramientas y detención de bucles. Evidencia: `output/verification/ai-report.json`.

La revisión HTTP utiliza un servidor sin clave con base y vault temporales en `output/verification/ai-http`, puerto 8091. Pasaron 9 verificaciones: sesión, cuatro roles, no exposición de clave, 503 pendiente de configuración, obra ajena 404 antes del proveedor, análisis inexistente, relaciones válidas y tablero disponible. Evidencia: `output/verification/ai-http-report.json`.

La comprobación HTTP quedó automatizada en `scripts/verify_ai_http.py` e incluida en `verify.ps1`: crea otra carpeta temporal y elige un puerto disponible, retira GEMINI_API_KEY del proceso hijo y detiene únicamente su servidor al terminar. Las pruebas Java/Python existentes volvieron a pasar (33 y 15); el grafo documental contiene 68 nodos y 227 relaciones, sin enlaces internos rotos. También se comprobó la sintaxis de configuración y el cifrado/descifrado Windows de una credencial sintética en el perfil normal del usuario.

Navegador: acceso demo, obra temporal, nuevo asistente, cuatro roles IA pendientes, instrucciones de activación, ejemplos y envío deshabilitado sin clave comprobados. JavaScript revisado con `node --check`. La base `data/obraplan.json` no se escribió durante estas pruebas.

**Pendiente real:** clave Google del usuario, disponibilidad/cuota gratuita en su proyecto y consulta real exitosa con Gemini. No se verificaron velocidad, calidad de respuestas del proveedor ni facturación de la cuenta; las pruebas simuladas validan el código de integración, no el modelo.

## Conexión real, 9 de octubre de 2026

Con la clave configurada por el usuario se consultó el catálogo real de modelos y se comprobó HTTPS en Java (200). El coordinador y Costos terminaron una consulta de subtotal/faltantes con datos temporales ficticios: 4 llamadas, 1.729 tokens de entrada, 256 de salida y 1.985 totales. La traza muestra delegación consultar_costos y herramienta calcular_costos. Reporte: output/verification/live-ai-report.json. Esta evidencia sí corresponde a Gemini real; no prueba el análisis conjunto de la obra del usuario ni la calidad de todas las consultas. La clave no se imprimió, y la base del usuario permaneció sin escribir.

Se añadió espera visible tras error para evitar reemplazar el diagnóstico por una segunda consulta prematura. JavaScript pasa node --check. Falta que el usuario repita su consulta en la interfaz actualizada para observar el resultado o el mensaje original.

## Síntesis conjunta, 9 de octubre de 2026

La prueba real en output/verification/live-ai-joint-report.json terminó completa con los cuatro roles, ocho llamadas, 5.932 tokens de entrada, 595 de salida y 6.527 totales. Usa cuatro materiales ficticios en una base temporal; no escribió data/obraplan.json. No se compara el consumo con el escenario del usuario porque los datos son distintos.

Pasaron 33 comprobaciones Java existentes, 15 Python y 9 HTTP. AgentVerification añade comprobaciones de referencias canónicas/breves, reserva de salida y cierre de delegaciones del coordinador, identificación de MAX_TOKENS y rechazo de conservación de un análisis truncado. También comprueba que la nota conservada mantiene la correspondencia F1 con su fuente. El reporte actualizado es output/verification/ai-report.json. El HTTP se ejecutó en el perfil normal porque el sandbox bloqueó el socket interno de Java; las nueve comprobaciones pasaron allí. JavaScript pasa node --check.

## Bienvenida por voz, 9 de octubre de 2026

Recorrido ampliado: tutorial.js y welcome.js pasan node --check. Navegador sobre obra demo vacía existente mostró el manual abierto y ocho secciones. La sección de materiales explica nombre/cantidad/unidad/etapa/fechas y guardar; su botón de lectura se ejecutó y Detener dejó Lectura detenida. Captura output/verification/full-guide.png. El módulo también conserva el texto pedido cuando las voces aún no han cargado. No se probaron todos los navegadores ni altavoces físicos; no hubo llamadas Gemini ni escritura de adquisiciones.

Ampliación a guía: siete escenarios aislados pasaron (obra vacía con ejemplo, ferretería siguiente, cotización siguiente, rechazadas/canceladas excluidas, seguimiento, pedidos existentes y precios faltantes). Reporte output/verification/guide-report.json. En navegador, la obra vacía existente de demo mostró Paso 1 y su botón abrió el formulario Registrar material, sin guardar adquisiciones. Captura output/verification/guide-start.png. Sintaxis welcome.js/app.js comprobada con node --check.

node --check pasó para welcome.js y app.js. Diez comprobaciones mediante ejecución aislada del módulo con voz simulada verificaron obra vacía, nombre personalizado, cambio de obra, subtotal cero, alertas críticas, descarte de callbacks antiguos, actualización sin saludo, silencio persistente, repetición manual y cancelación al salir. Reporte: output/verification/voice-report.json. No prueban calidad acústica ni una llamada a IA.

En el navegador real, la cuenta demo abrió Demostración para video y mostró nombre/obra/fecha y cifras correspondientes al tablero. Se observó el evento de inicio de síntesis (Leyendo el resumen de tu obra) y el resultado del control Detener (Lectura detenida). Captura: output/verification/voice-welcome.png. No se verificaron altavoces físicos ni todos los navegadores; si el navegador bloquea la lectura automática se ofrece Escuchar resumen. La selección usa exclusivamente voces locales y no ejecuta Gemini.

## Preparación para GitHub, 10 de octubre de 2026

La compilación Java pasó con scripts/compile.ps1. Los enlaces locales del README se comprobaron y no hay destinos inexistentes. Se revisaron los archivos preparados con patrones de claves Gemini/GitHub y claves privadas, sin coincidencias. Git check-ignore confirmó exclusión de la clave protegida, configuración personal, base local y reportes. La comprobación del grafo pasó con 70 nodos, 235 relaciones y cero enlaces rotos antes de incorporar la nota de publicación. Evidencia local: output/verification/publication-report.json. No se probó un despliegue remoto ni se publicaron registros privados.

## 10 de octubre de 2026 — Revisión editorial

Se revisaron documentación, instrucciones de desarrollo, configuración local y notas derivadas para centrar las explicaciones en ObraPlan. Se aclaró la separación entre agentes Gemini y herramientas por reglas. Se regeneraron 68 notas documentales: el grafo local contiene 71 nodos y 239 relaciones, sin enlaces internos rotos. La revisión no cambia código ejecutable ni datos de obra.
