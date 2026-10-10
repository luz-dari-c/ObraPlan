# Nacimiento de ObraPlan: planteamiento del problema

## Contexto y origen

La idea parte de una necesidad de construcción: coordinar los materiales de una obra y saber qué comprar, en qué momento y con qué información del proveedor. Una obra avanza por etapas —cimentación, estructura, mampostería y acabados— y cada etapa necesita recursos en fechas distintas. Tener una lista de materiales no basta para organizar las adquisiciones: también hacen falta cantidades, unidades, fecha de necesidad, plazo de entrega, cotizaciones y estado del pedido.

El antecedente disponible en el proyecto planteaba una plataforma más amplia, con lectura de planos, cantidades automáticas y visualización 3D. El análisis existente redujo ese alcance al flujo de adquisiciones. Esta revisión conserva esa decisión y documenta sus razones: permite demostrar un recorrido completo y verificable con datos ingresados por el usuario. La interpretación de planos y el cálculo estructural requieren otras fuentes, técnicas y validaciones que esta entrega no implementa.

## Situación que se quiere resolver

El escenario de trabajo supone que el responsable de una obra administra información dispersa: una lista de materiales, contactos de ferreterías y precios recibidos por distintos medios. Es un escenario del proyecto, no el resultado de una encuesta o entrevista que se haya realizado. Si esa información no se relaciona, cuesta responder preguntas prácticas: ¿qué falta cotizar?, ¿qué material se necesita primero?, ¿cuándo conviene gestionarlo si el proveedor tarda varios días?, ¿qué costo se conoce y qué parte todavía falta?, ¿qué pedidos siguen en tránsito?

Un precio aislado tampoco equivale al presupuesto completo de la obra. Si solo se cotizó cemento y arena, sumar esos valores es un subtotal conocido. Presentarlo como costo total ocultaría los materiales pendientes y podría inducir a una interpretación equivocada. Por esa razón el sistema distingue explícitamente un material sin cotización de un material con precio cero registrado.

## Problema central

¿Cómo organizar las necesidades de materiales, las cotizaciones y el seguimiento de entregas de una obra, de manera que el responsable pueda identificar pendientes y fechas de gestión sin perder la relación entre esos datos?

La causa propuesta es la fragmentación de información y la falta de un seguimiento compartido por obra. Las consecuencias posibles son gestión tardía de materiales, dificultad para comparar precios equivalentes y desconocimiento de los pendientes. Son hipótesis razonables para diseñar el caso académico; no se afirma que se haya medido su frecuencia o su impacto económico.

## Actores y objetivos

El actor principal es el coordinador o responsable de la obra. Registra la información, consulta las recomendaciones y toma las decisiones. La ferretería es un proveedor externo cuyos datos y cotizaciones se registran manualmente; no accede a un portal ni recibe mensajes automáticos. El responsable técnico confirma cantidades y unidades cuando corresponda. El responsable del proyecto documenta, demuestra y conserva evidencia de la solución.

El objetivo general es construir una bitácora de adquisiciones que reúna materiales por etapa, proveedores, cotizaciones y pedidos, con análisis comprensible y memoria de conocimiento conectada a Obsidian. Los objetivos específicos son identificar materiales pendientes, calcular fechas orientativas de gestión, conservar el costo conocido con sus faltantes, seguir el ciclo del pedido y relacionar problema, requisitos, componentes y evidencias.

## De la necesidad a los agentes

El rol de **planteamiento del problema** convierte la idea en contexto, actores, pregunta central, objetivo y alcance. Su salida es este documento. El rol de **análisis de requisitos** transforma esas necesidades en conductas observables y criterios de calidad: qué registra el sistema, qué muestra, qué valida y cómo se comprueba. Sus salidas son `ANALISIS.md` y `TRAZABILIDAD.md`. Se entregan skills locales para repetir ese trabajo con un asistente.

Después se construyen especialistas de la aplicación: Logística calcula fechas, Costos calcula subtotales, Seguimiento revisa entregas y el Orquestador reúne los resultados. Conocimiento publica y consulta notas relacionadas. Son componentes Java con reglas explícitas. La herramienta Python recupera evidencia del grafo y puede usar un modelo Ollama instalado si se activa ese modo. Esta separación permite explicar tanto el proceso de construcción como el comportamiento real de la aplicación.

Las skills de ObraPlan organizan el trabajo de planteamiento, requisitos y conocimiento. Los agentes IA de la aplicación se ejecutan mediante Gemini; las integraciones externas requieren rutas, entornos y contratos comprobados.

## Resultado esperado y límites

El resultado demostrable es un recorrido desde la creación de una obra hasta la recepción de un pedido, acompañado de recomendaciones, persistencia y notas conectadas. La evidencia debe mostrar acciones y resultados, no solo pantallas vacías. La evaluación con usuarios reales, los ahorros, la precisión de cantidades, la interpretación de planos y las compras comerciales quedan fuera de lo comprobado en esta versión.

La propuesta combina software funcional y documentación trazable. Cada requisito tiene relación con una necesidad; cada componente tiene una responsabilidad; cada regla tiene una nota; cada cambio confirmado puede alimentar el grafo. Así el conocimiento del proyecto queda consultable para el siguiente agente que continúe el trabajo.
