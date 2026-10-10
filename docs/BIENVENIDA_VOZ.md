# Bienvenida por voz

La **Guía completa: desde tu primera obra hasta la entrega** se abre al entrar en una obra sin materiales. Tiene ocho secciones: propósito y creación de obra; campos del material; proveedores; campos y ejemplo de cotización; costos y alertas; decisión y estados de seguimiento; agentes y conservación en el grafo; rutina al volver. Cada sección tiene Escuchar este paso. La persona puede abrirla siempre, aunque ya tenga registros.

El saludo automático narra el estado y la próxima acción. El contenido extenso se lee a petición por sección. Los ejemplos no crean datos y todos los controles de detención/silencio siguen disponibles. El contenido canónico del recorrido está en tutorial.js.

También es la guía de inicio del proyecto. Muestra cuatro etapas con registros/por empezar y propone el siguiente paso según los datos de la obra: primer material, primera ferretería, cotización válida o seguimiento. Si faltan precios, vuelve a orientar hacia cotizaciones. Las rechazadas/canceladas no se consideran una cotización vigente.

El botón abre el formulario o la pantalla correspondiente y detiene la narración. Una obra vacía incluye ejemplo y explicación de campos; al guardar se actualiza la orientación sin repetir audio. El resumen completo queda desplegable. El recorrido indica presencia de registros, no porcentaje de avance físico ni cumplimiento de la obra.

Al abrir una obra o cambiarla en el selector, aparece un resumen y se intenta reproducir automáticamente. Incluye tu nombre registrado, obra, fecha y datos actuales del tablero. Si no hay materiales, propone registrar el primero. No interpreta progreso físico de construcción: narra registros, subtotales y avisos.

**Escuchar resumen** repite el texto; **Detener** cancela la lectura; **Silenciar bienvenida** desactiva saludos automáticos y conserva esa preferencia en el navegador. Puedes seguir escuchando manualmente. Guardar un formulario actualiza el resumen sin repetir la bienvenida. Cerrar sesión cancela el audio.

Usa voces locales disponibles en el navegador, con preferencia por español. No necesita micrófono, API Key adicional ni llamadas Gemini. Si no hay una voz local disponible se conserva el texto. Algunos navegadores requieren pulsar Escuchar resumen. El idioma y timbre dependen de las voces instaladas.

Código: src/main/resources/static/welcome.js; conexión al tablero en app.js; controles en index.html; estilo en style.css. Requisito RF-19 y nodo OP-Voz-Bienvenida. Complementa a los cuatro agentes IA existentes; es una presentación hablada del análisis por reglas.

Referencia de la API: [SpeechSynthesisVoice.localService](https://developer.mozilla.org/en-US/docs/Web/API/SpeechSynthesisVoice/localService) distingue voces locales de servicios remotos. [SpeechSynthesisUtterance](https://developer.mozilla.org/en-US/docs/Web/API/SpeechSynthesisUtterance) define la lectura y sus eventos.
