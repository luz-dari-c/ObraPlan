# Bitácora del proyecto ObraPlan

Esta bitácora registra decisiones del desarrollo. La **actividad reciente** de la aplicación registra operaciones de una obra; son registros con propósitos distintos.

## Antecedente disponible, sin fecha reconstruida

- La documentación inicial define una aplicación local Java 21 para adquisiciones.
- El alcance inicial amplio de lectura de planos/3D se redujo al flujo materiales → cotizaciones → pedidos → recepción.
- Se implementaron autenticación, obras, materiales, directorio, cotizaciones, reglas y persistencia JSON.
- No se reconstruyen conversaciones, fechas ni ejecuciones de agentes que no estén respaldadas por evidencia.

## 1 de octubre de 2026 — Organización, trazabilidad y conocimiento

**Entrada:** solicitud de ordenar carpetas, explicar agentes/entornos, alimentar y conectar el grafo de Obsidian y preparar un video.

**Decisiones:** conservar Java para la aplicación; separar especialistas en clases; usar Markdown con IDs estables como memoria; publicar solo el namespace `Proyectos/ObraPlan`; ofrecer un agente Python de recuperación local y adaptación a entornos independientes; reservar un área de aprendizajes editables.

**Cambios:** README ampliado, mapa operativo, instrucciones de continuidad, roles/skills locales, planteamiento, requisitos con criterios observables, arquitectura, trazabilidad, referencias y guion. Se añade la pantalla de agentes/conocimiento con consulta, fuentes y exploración de relaciones. Cada cambio confirmado alimenta notas de entidades y mantiene relaciones obra/material/proveedor/cotización.

**Memoria compartida:** se registran tres notas generales existentes del vault para lectura de Java/Python, sin modificarlas. Un nodo de integración conecta el proyecto con esas referencias. Leer estas notas no equivale a ejecutar los frameworks que describen.

**Correcciones encontradas:** el cálculo Java incluía cotizaciones canceladas pese a lo descrito por la documentación; se excluyen. Seguimiento revisaba incluso cotizaciones no convertidas en pedido; ahora evalúa pedidos realizados/en tránsito. La base mutaba el estado antes de terminar validaciones; se trabaja con una copia y se restaura el estado si falla la operación.

**Evidencia:** comandos reproducibles en `VERIFICACION.md`; resultados derivados en `output/verification/` y `output/knowledge/`. Las pruebas se realizan con una base temporal sin cambiar registros del usuario.

**Cierre de comprobación:** 33 verificaciones Java, 15 Python y 7 HTTP correctas; tres skills validadas. Obsidian reconoce las notas y conexiones. La revisión a 390 px detectó y corrigió un desbordamiento del selector/panel del grafo. Código formateado con Google Java Format, Prettier y Ruff; herramientas de formato en output, fuera de las dependencias de ejecución.

**Pendiente:** localizar los agentes originales del profesor y confirmar su contrato/entorno. No se marca una integración pendiente como realizada.

## 2 de octubre de 2026 — Registros preparados para el video

El usuario encontró vacías sus obras: los datos iniciales pertenecían a la cuenta demo. Se creó una obra separada **Demostración para video** en la cuenta local del usuario y una copia independiente en la cuenta demo. Cada escenario tiene 10 materiales, 4 ferreterías y 8 cotizaciones en estados distintos, con 2 pedidos activos, 4 materiales sin precio válido y subtotal conocido de COP 2.152.000.

La herramienta `scripts/demo/DemoData.java` utiliza AppService y sus validaciones; cada guardado alimenta KnowledgeAgent. Las obras y registros anteriores se compararon antes/después y quedaron conservados. La base se respaldó en `output/backups/` y la carga se ejecutó con el servidor detenido para evitar dos escritores.

El escenario se comprobó en una base temporal y se repitió sin duplicarlo. Reportes: `output/demo-validation/output/demo/report.json` y `output/demo/report.json`. El recorrido corto con los registros existentes está en `docs/DEMO_VIDEO.md`; GUIA_VIDEO y README apuntan a él. Las fechas, contactos y precios son ficticios para presentación.

## Cómo mantener esta bitácora

Al cerrar un cambio registra fecha local, problema que resolvió, decisión y motivo, archivos principales, comprobaciones con resultado y pendientes reales. Si cambia una regla, actualiza requisito, código y nota relacionada. Guarda evidencia derivada en `output/`, dejando aquí el resumen interpretable.

## 8 de octubre de 2026 — Agentes IA durante el uso

El usuario aclaró que la aplicación debe ejecutar agentes cuando la utiliza. Se corrigió la denominación anterior: clases por reglas y skills no equivalían a un sistema multiagente con modelos. Se conserva esa implementación como herramientas y se añade AgentTeam con coordinador y tres contextos especialistas, GeminiClient REST y WorkAgentTools con autorización por propietario/obra.

Se eligió nube con nivel gratuito por el equipo comprobado (i3-1115G4, aproximadamente 8 GB RAM y 1,1 GB libre durante la revisión, gráficos integrados). No se instaló Ollama. El modelo predeterminado verificado en documentación es gemini-3.5-flash-lite; disponibilidad/cuota real y condición de proyecto sin facturación deben comprobarse en la cuenta del usuario. No se habilitó facturación ni se generó una clave.

La interfaz muestra participantes, herramientas, fuentes, metadata de tokens y caché. Las consultas usan proyecciones acotadas y no pueden comprar ni editar adquisiciones. Una confirmación conserva propuestas completas y vigentes vinculadas a obra/coordinador/fuentes, recuperables en su obra. Se añaden RF-16 a RF-18 y RNF-11/RNF-12, catálogo/relaciones y guía de activación/video.

Evidencia: 19 verificaciones del runtime con proveedor simulado, 9 HTTP sin clave sobre base temporal, interfaz pendiente de configuración revisada en navegador. El proveedor real no se probó: falta la clave de la cuenta Google. La evidencia simulada se etiqueta explícitamente; no se afirma IA cloud operativa hasta realizar una consulta real.

## 9 de octubre de 2026 — Primera conexión real y espera visible

El usuario configuró su clave y mostró un aviso de espera tras consultar. Ese aviso corresponde al límite local de 15 segundos y no permite reconstruir el primer error. Se comprobó la disponibilidad de modelos de la cuenta (HTTP 200), una llamada pública de coordinación y la conexión HTTPS desde Java, sin exponer la clave.

Una ejecución real con coordinador y agente de Costos sobre una base temporal terminó completa: 4 llamadas, 1.985 tokens reportados y herramienta calcular_costos. Evidencia en output/verification/live-ai-report.json; no se modificaron adquisiciones del usuario ni se conservaron propuestas en su vault durante la prueba. No se afirma ejecución del análisis conjunto del usuario a partir de esta prueba limitada.

La interfaz ahora deshabilita el envío durante 15 segundos después de un error y muestra una cuenta regresiva en el botón, conservando el mensaje original. Se verificó sintaxis JavaScript. La espera no activa reintentos ni llamadas automáticas.

## 9 de octubre de 2026 — Síntesis del análisis conjunto

El usuario recibió tres informes especialistas pero una ejecución incompleta tras ocho llamadas. La versión anterior no registraba qué límite detuvo la síntesis, por lo que esa traza no demuestra agotamiento de cuota. El coordinador compartía el tope de 768 tokens de los especialistas y las citas repetían identificadores largos.

Se reservan 1.280 tokens de salida al coordinador, se solicitan informes breves y se incorporan referencias F1, F2 vinculadas a los identificadores canónicos. Tras completar los tres especialistas se exige una síntesis sin nuevas delegaciones. Se conservan los límites globales de diez llamadas y tiempo; ahora cada detención informa su causa. Las propuestas parciales siguen sin poder conservarse como completas. Las notas guardadas incluyen la correspondencia entre referencias y fuentes.

Gemini real completó un análisis conjunto en una base temporal de cuatro materiales: ocho llamadas y 6.527 tokens reportados. Participaron coordinador, Logística, Costos y Conocimiento con sus herramientas. Evidencia: output/verification/live-ai-joint-report.json. La prueba no escribió la base del usuario ni demuestra una reducción porcentual frente a su consulta de diez materiales. El servidor del usuario necesita reiniciarse para cargar las clases nuevas.

## 9 de octubre de 2026 — Bienvenida hablada

Se implementó RF-19 a petición del usuario: al abrir/cambiar obra, welcome.js saluda con el nombre registrado y narra fecha, materiales por cotizar, pedidos, subtotal parcial, faltantes y alertas del tablero autorizado. Una obra vacía invita a registrar materiales. La vista ofrece el texto y controles de repetición, detención y silencio persistente en ese navegador. Guardar formularios actualiza el texto sin volver a saludar; salir de la obra o cerrar sesión detiene el audio.

Se eligió síntesis de voz del navegador con voces locales (localService=true) para evitar tokens y transmisión del resumen a un proveedor remoto de voz. Prefiere español; sin voz local o reproducción permitida mantiene texto y ofrece Escuchar resumen. No es un nuevo agente LLM ni altera reglas, adquisiciones o grafo operativo. Se añade el nodo OP-Voz-Bienvenida relacionado con RF-19 y el orquestador/reglas.

La cuenta demo mostró el resumen correcto de diez materiales, cuatro pendientes, dos pedidos y subtotal COP 2.152.000. El navegador emitió el inicio de síntesis y mostró Leyendo el resumen; Detener dejó Lectura detenida. Se guardó captura en output/verification/voice-welcome.png. No se verificó el volumen físico de los altavoces. Diez comprobaciones de ciclo/texto con voz simulada y sintaxis JavaScript pasaron; no se hicieron llamadas Gemini ni cambios en registros de obra.

## 9 de octubre de 2026 — Guía de inicio contextual

El usuario pidió orientación para quien entra sin datos y no sabe empezar. RF-19 se amplía con un recorrido visible materiales → ferreterías → cotizaciones → seguimiento, explicación del siguiente paso y botón que abre el formulario/pantalla. Una obra vacía incluye campos y ejemplo de cemento/10 bultos; después de guardar cambia la orientación sin repetir voz. Las cotizaciones rechazadas/canceladas no cuentan como avance; si falta precio se recomienda cotizar. El recorrido señala registros presentes, no progreso físico.

Se comprobaron siete escenarios del módulo y la obra vacía existente de la cuenta demo en navegador: explicó Paso 1 y el botón abrió Registrar material. Se canceló el formulario sin guardar datos. Captura output/verification/guide-start.png y reporte guide-report.json. Se actualizaron requisito, trazabilidad, nota, guía y evidencia. No se ejecutó Gemini.

## 9 de octubre de 2026 — Recorrido completo para principiantes

Se amplió la guía para explicar el proceso completo a quien nunca usó ObraPlan. tutorial.js contiene ocho secciones: propósito/crear obra, materiales y campos, proveedores, cotizaciones con cálculo ilustrativo, costos/alertas, decisión/seguimiento, agentes/grafo y rutina diaria. Distingue fechas, cantidad/precio unitario, registros y compras reales, subtotal/presupuesto y estados de material/cotización. Los ejemplos no crean datos.

En una obra sin materiales la guía completa se abre; en otras queda disponible como desplegable. Cada sección permite escuchar y detener la lectura local sin Gemini. Los controles y siguiente acción quedan antes del manual extenso. Se comprobó navegador con la obra vacía existente: ocho secciones, apertura de materiales, botón de lectura por sección y detención. Evidencia visual output/verification/full-guide.png. No se escribieron adquisiciones ni se probó volumen físico de altavoces.

## 10 de octubre de 2026 — Preparación de publicación

Se reescribió README para visitantes: propósito, funcionalidades, recorrido, arquitectura, agentes, ejecución desde una copia nueva, configuración IA, memoria y estado real de despliegue. Se añadió config/integrations.example.json con rutas vacías; integraciones personales, data/, output/ y secretos se excluyen de Git. Se conservaron la configuración y base locales.

Vercel ofrece Container Runtime en beta, capaz de ejecutar Java. Un despliegue estable requiere adaptar persistencia JSON, sesiones en memoria y escucha local. PUBLICACION.md distingue código publicado, interfaz estática y aplicación completa. No se desplegó ni habilitó facturación.

Cuenta GitHub comprobada: luz-dari-c. Se prepara el commit inicial con su identidad y fecha real, sin inventar historial. La creación pública de ObraPlan quedó pendiente de confirmación explícita de visibilidad/destino requerida por revisión automática; todavía no se creó repositorio ni se subieron archivos.

## 10 de octubre de 2026 — Publicación confirmada en GitHub

El usuario confirmó explícitamente la publicación pública en luz-dari-c/ObraPlan. El repositorio ya creado se comprobó mediante GitHub y se subieron los commits locales a main. Se verificaron README y AgentTeam mediante el conector, y git ls-remote confirmó que el commit remoto coincidía con el local. Se añadió al README la URL real y el comando de clonación. La aplicación desplegada en una URL sigue pendiente; la publicación de código no se presenta como despliegue funcional.
