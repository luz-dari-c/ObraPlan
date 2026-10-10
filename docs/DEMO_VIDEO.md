# Recorrido rápido con los registros preparados

Entra con tu cuenta habitual y abre **Demostración para video**. La cuenta `demo@obraplan.co` también tendrá una copia independiente del escenario. Los registros son ficticios y están marcados como demostrativos. Las obras anteriores se conservan.

## Qué contiene la obra

- 10 materiales repartidos entre Cimentación, Estructura, Mampostería y Acabados.
- 4 ferreterías ficticias con contactos de ejemplo.
- 8 cotizaciones: recibida, pendientes de decisión, pedido realizado, en tránsito, recibido, cancelado y rechazado.
- Actividad reciente y recomendaciones generadas por los agentes.
- Notas de obra, materiales, proveedores y cotizaciones relacionadas en Obsidian.

Los datos se crean con fechas relativas al día de carga. No se actualizan solos para simular nuevos pedidos ni representan precios comerciales reales.

## Video de 2–3 minutos: solo mostrar y explicar

| Pantalla | Acción | Qué decir |
|---|---|---|
| Login / Mis obras | Inicia sesión y abre **Demostración para video** | “Cada usuario accede a sus propias obras. Esta obra contiene registros ficticios para demostrar el funcionamiento.” |
| Resumen | Señala el costo, los faltantes, los pedidos y las alertas | “El tablero reúne pendientes, actividad y recomendaciones de los agentes. El costo conocido es parcial.” |
| Materiales | Busca **Cemento gris** y abre Detalle | “El material tiene cantidad, unidad, fecha necesaria y plazo. Estos 10 bultos tienen un precio válido de COP 40.000: subtotal COP 400.000.” |
| Plan de adquisiciones | Muestra fechas y estados; identifica **Malla electrosoldada** | “El plan permite ver qué gestionar y qué falta cotizar. La fecha sugerida considera el plazo.” |
| Ferreterías | Abre una ficha de **Ferretería Demo Centro** | “El directorio guarda proveedores y contactos. Las cotizaciones se solicitan y registran manualmente.” |
| Cotizaciones y pedidos | Muestra **Arena lavada** en tránsito, **Varilla corrugada 1/2** pedida y **Bloque estructural** recibido | “Aquí sigo el ciclo de la compra. Hay ejemplos de distintas etapas y una entrega estimada vencida para demostrar el aviso de seguimiento.” |
| Agentes y conocimiento | Consulta **costo sin cotización** y selecciona Agente de costos | “La consulta tiene fuentes. Las relaciones conectan el requisito, la regla y los materiales de esta obra.” |

No necesitas crear todos los registros ni cambiar sus estados mientras grabas. Si quieres demostrar una edición, cambia las notas o una fecha de un material y muestra su actualización en Obsidian.

## Cifras del escenario inicial

**Subtotal conocido: COP 2.152.000**, con **4 materiales sin precio válido** y **2 pedidos activos**. El cemento tiene una segunda oferta rechazada de COP 38.500: esa oferta no participa en el cálculo. La cotización de cerámica está cancelada y también queda excluida.

Estas cifras cambian si editas los registros durante la grabación. “Sin cotizar” no equivale a precio cero ni el subtotal conocido equivale al presupuesto total de la obra.

## Herramienta de carga

`scripts/demo/DemoData.java` ejecuta los casos de uso de AppService y activa KnowledgeAgent. Es una utilidad local de preparación, sin un endpoint administrativo público. Se compila junto con las fuentes principales a `build/demo-classes/` y recibe raíz del proyecto e IDs de usuarios existentes. Solo debe ejecutarse con el servidor detenido y un respaldo de la base, para evitar dos escritores simultáneos.

Cada escenario completo se identifica con `demoScenario: video-v1`; una repetición no lo duplica. El reporte queda en `output/demo/report.json`. La evidencia de la prueba temporal queda en `output/demo-validation/`.
