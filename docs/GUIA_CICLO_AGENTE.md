# Cómo explicar y demostrar el agente

## Frase de presentación

«Este es un agente porque recibe un disparador, recupera memoria de la obra, reúne instrucciones y herramientas, llama al modelo y guarda un recuerdo del resultado. En esta pantalla puedo ver qué lo activó y cómo terminó».

## Orden para mostrar el código

1. **Disparador:** `DisparadoresAgente.detectarCambios()` detecta cambios de materiales y cotizaciones. `encolar()` guarda un evento pendiente en la misma transacción del cambio. Solo funciona si el propietario activó el seguimiento de esa obra.
2. **Ciclo:** `AgentePersistente.ejecutar()` coordina el análisis con memoria y guarda su resultado. `procesarPendiente()` recupera el siguiente evento durable. No usa RabbitMQ ni un servicio de mensajería.
3. **Recuperar memoria:** `MemoriaAgente.recuperarMemoria()` devuelve hasta tres recuerdos del propietario y la obra. `WorkAgentTools` obtiene una instantánea autorizada. `AgentTeam.Run.agent()` incorpora esos antecedentes como datos históricos dentro de los mensajes; las instrucciones y las herramientas siguen separadas. Los hechos actuales se consultan con herramientas.
4. **Mensajes y configuración:** `SolicitudModelo.crearSolicitudModelo()` construye el JSON. `contents` es la lista de mensajes y respuestas de herramientas. `systemInstruction` contiene las instrucciones por rol. `generationConfig` controla tokens, temperatura y razonamiento. `tools` declara las funciones disponibles.
5. **Modelo y llamada API:** en `GeminiClient` se crea el cliente HTTP y se lee `model`. `generate()` llama a `llamarApi(model, request)`. Dentro de esa función se ve la URL del servidor Gemini con el modelo, el cuerpo JSON y `http.send()`. La respuesta vuelve como JSON y se convierte en un mapa Java.
6. **Guardar memoria:** después de un resultado completo, `MemoriaAgente.guardarMemoria()` conserva un resumen acotado en `agentMemory`. En producción queda en PostgreSQL. No cambia cotizaciones ni convierte la recomendación en un hecho comprobado. Guardar la propuesta en el grafo sigue siendo una acción separada del usuario.

En Gemini REST el modelo forma parte de la URL, no es un campo añadido arbitrariamente al cuerpo. No se utiliza un SDK de Google ni una función ficticia `cliente.create`: el cliente real es `java.net.http.HttpClient`. Los tres conceptos solicitados son visibles: modelo, mensajes y configuración. Referencia: https://ai.google.dev/api/generate-content?hl=en.

## Demostración desde la interfaz

1. Abre una obra tuya con materiales y entra en **Agentes y conocimiento**.
2. Localiza **El ciclo de tu agente**. Abre la explicación de la llamada y la memoria recuperable.
3. Activa **Seguimiento IA de esta obra**. Se registra `inicio_seguimiento` como pendiente. Espera la agrupación de 20 segundos y el siguiente turno del trabajador, que revisa cada 15 segundos.
4. Revisa el historial: pendiente → en curso → completado, o el error real si el proveedor no está disponible. El resultado muestra el disparador, recuerdos recuperados y si guardó memoria.
5. Abre **Memoria recuperable**: debe aparecer el resumen. Reinicia o recarga y comprueba que permanece.
6. Guarda un material o actualiza una cotización. El evento se registra sin pulsar Consultar agentes. Varios cambios cercanos se agrupan en un pendiente.
7. Ejecuta una consulta manual posterior. La evidencia del ciclo indica cuántos recuerdos recuperó. Sus mensajes contienen los antecedentes; el agente vuelve a consultar herramientas para confirmar el estado actual.
8. Desactiva el seguimiento al terminar la demostración. Los pendientes se cancelan; una llamada ya iniciada puede terminar.

## Límites que debes explicar correctamente

- Máximo tres eventos automáticos por obra y día, además del límite del coordinador y las cuotas de Google. Activar y desactivar no reinicia ese conteo.
- Una consulta de estado o una recarga de página no llama al modelo.
- Los eventos pendientes sobreviven a reinicios. Una ejecución que pierde su proceso se marca interrumpida cuando vence su reserva; no se repite a ciegas para evitar duplicar consumo.
- El plan gratuito de Render puede dormir. El trabajador actúa mientras el servidor está funcionando y continúa pendientes al despertar; no es vigilancia ininterrumpida garantizada.
- La memoria episódica no es entrenamiento del modelo ni aprobación de una compra. Los agentes recomiendan; el usuario decide.
