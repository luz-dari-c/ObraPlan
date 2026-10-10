# Activar y demostrar los agentes de ObraPlan

## Estado y configuración

La aplicación implementa un coordinador y tres especialistas con contextos independientes y herramientas. El proveedor es Gemini REST; todos comparten un modelo y una clave. No se presentan pruebas simuladas como ejecuciones reales del modelo. Hasta configurar una clave y realizar una consulta, la conexión real está pendiente.

1. Abre https://aistudio.google.com/api-keys con tu cuenta Google.
2. Crea una clave para un proyecto **sin facturación habilitada**. Comprueba en AI Studio que tiene cuota gratuita para el modelo. No habilites facturación para esta demostración.
3. Desde PowerShell en la raíz del proyecto ejecuta:

```powershell
./scripts/configure_ai.ps1
```

4. Confirma que el proyecto no tiene facturación y escribe la clave en la entrada oculta. Windows la cifra para tu usuario/equipo en `config/gemini-key.local.xml`, excluido de Git. No compartas ese archivo ni pegues la clave en el chat.
5. Detén el servidor anterior con Ctrl+C y ejecuta `./run.ps1`. El script carga la clave en el proceso Java. Si ya existe `GEMINI_API_KEY` en el proceso, esa variable tiene prioridad.
6. Inicia sesión, abre una obra y entra en **Agentes y conocimiento**. Envía una pregunta y comprueba respuesta, participantes, herramientas, fuentes y tokens reales reportados.

El modelo predeterminado es `gemini-3.5-flash-lite`. El catálogo verificado también permite `gemini-3.1-flash-lite` y `gemini-3.8-flash`; la disponibilidad y cuota dependen de tu proyecto Google. Cambiarlo requiere definir `OBRAPLAN_GEMINI_MODEL` antes de iniciar. No hay selección automática de un modelo alternativo.

El código no puede verificar la facturación de tu cuenta ni garantizar que Google mantenga el nivel gratuito. Los modelos también tienen precios en proyectos con facturación. La condición para evitar cobros es conservar el proyecto Google sin facturación. La integración no habilita facturación, no compra créditos y no reintenta errores 429.

Google indica que puede usar el contenido del nivel gratuito para mejorar sus productos. Se envían la pregunta y los campos de adquisiciones/fuentes necesarios. No se envían usuarios, hashes, sesiones, claves ni contactos de proveedores desde la base; evita introducir datos personales en tus preguntas o notas compartidas.

## Cómo trabajan

`Server` autentica; `WorkAgentTools` autoriza la obra antes de acceder a caché o al modelo. `AgentTeam` inicia un contexto del coordinador. Gemini elige llamadas a `consultar_logistica`, `consultar_costos` o `consultar_conocimiento`. Cada llamada inicia el contexto del especialista, con instrucciones y herramientas propias. El especialista consulta datos, recibe resultados y vuelve al modelo para redactar. El coordinador recibe los informes y reúne la respuesta.

Las partes de respuesta del proveedor se conservan completas al continuar una conversación, incluidas sus firmas. La interfaz muestra eventos de modelos/herramientas, no razonamiento interno. Los resultados se identifican como propuestas y sus fuentes son las efectivamente consultadas por herramientas.

Herramientas locales: leer materiales/pedidos, evaluar logística/seguimiento, calcular costos, leer cotizaciones y buscar fuentes/vecinos del grafo. Las proyecciones de registros tienen campos permitidos y páginas de 12 elementos; el grafo devuelve hasta seis extractos de 1.200 caracteres y veinte relaciones. Las notas se tratan como datos no confiables, no como órdenes. No hay herramientas de compra, envío ni edición de adquisiciones.

## Consumo y límites

- Solo una solicitud explícita activa IA; formularios y búsqueda local no gastan tokens del proveedor.
- Hasta 10 llamadas al modelo y 24 herramientas por análisis; hasta 4 turnos por agente y una ventana de 120 segundos, más el tiempo de la última llamada en curso (máximo 25 segundos).
- Hasta 768 tokens de salida por especialista y 1.280 para el coordinador, pensamiento mínimo y solicitudes de hasta 32.000 caracteres. Los tokens de entrada y pensamiento reportados se muestran cuando Gemini informa el uso.
- Tras completar los tres especialistas, el coordinador sintetiza sin volver a delegar. Las referencias breves [F1], [F2] corresponden a fuentes con identificadores canónicos; esa correspondencia también se conserva en la nota del grafo.
- Si el análisis se detiene, informa si alcanzó el límite de salida, tiempo, llamadas, herramientas, contexto o turnos. Un resultado parcial no habilita conservar la propuesta como completa.
- Una ejecución global a la vez y 15 segundos entre análisis nuevos.
- Límite local de 100 intentos de llamadas por día y proceso. Se reinicia al reiniciar el servidor; no sustituye la cuota Google ni una garantía de costo.
- Caché de cinco minutos para misma pregunta, propietario, obra, fecha, datos y contenido del conocimiento. Hasta 30 entradas en memoria; no se conserva al reiniciar.
- No hay reintentos automáticos ni proveedores alternativos. Sin clave/cuota/conexión, se informa el error y el resto de la aplicación sigue disponible.

Los límites pueden producir una respuesta incompleta; se indica y no se permite conservarla como análisis completo. Un análisis tampoco se conserva si los datos o el conocimiento cambiaron desde su ejecución.

## Alimentar el grafo y grabar el video

Prueba **Análisis conjunto** en la obra con datos demostrativos. La delegación concreta depende de la decisión del modelo; revisa los participantes reales, no anuncies previamente que siempre actuarán los cuatro.

Puedes narrar: «Pido una revisión de mi obra. El coordinador decide qué especialistas necesita. Cada especialista consulta herramientas autorizadas: los costos se calculan con Java y el conocimiento se recupera del grafo. Aquí vemos sus llamadas, fuentes y consumo. La respuesta es una propuesta, no una compra».

Después pulsa **Conservar propuesta en el grafo**. Esa acción confirma guardar el análisis. Se crea una nota `OP-Analisis-<runId>` vinculada a la obra, al coordinador y a sus fuentes. La nota se guarda bajo `output/knowledge/analyses/<workId>/` y se publica en `Proyectos/ObraPlan/Analisis/<workId>/` cuando el vault está disponible. Java y Python la recuperan únicamente al consultar esa obra. Los aprendizajes manuales existentes no se sobrescriben.

La ejecución permanece disponible para conservar durante treinta minutos y hasta treinta resultados en memoria. Si reinicias, consulta de nuevo. Ante publicación fallida puedes ejecutar `python scripts/sync_knowledge.py` para publicar las notas locales conservadas.

## Fuentes técnicas consultadas

- https://ai.google.dev/api/generate-content — contrato REST, funciones y firmas.
- https://ai.google.dev/gemini-api/docs/models — modelos disponibles; modelos 2.5 restringidos para proyectos nuevos.
- https://ai.google.dev/gemini-api/docs/pricing — nivel gratuito y tratamiento del contenido.
- https://ai.google.dev/gemini-api/docs/rate-limits — cuotas según proyecto.

Consulta realizada el 8 de octubre de 2026. La validación con la cuenta real se registra separadamente de las pruebas del proveedor simulado.

## Primera comprobación real y aviso de espera

El 9 de octubre de 2026 terminó una consulta real con coordinador y Costos usando datos ficticios temporales: cuatro llamadas y 1.985 tokens reportados. La configuración de clave está realizada; el análisis conjunto específico de cada obra debe comprobarse desde su interfaz. Ante un error, el botón muestra una espera de 15 segundos sin reintentar automáticamente y conserva el mensaje original.
