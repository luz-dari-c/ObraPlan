/* Contenido del recorrido inicial. Ejemplos ilustrativos, no registros reales. */
window.WorkTutorial = [
  {
    title: "1. Entiende qué vas a organizar",
    paragraphs: [
      "ObraPlan reúne las necesidades de materiales, los precios que te ofrecen las ferreterías y el seguimiento de tus pedidos. Tú registras lo que sucede en tu obra; la aplicación organiza la información y te ayuda a detectar pendientes. No necesitas saber programación.",
      "Después de iniciar sesión, entra en Mis obras y pulsa Crear obra si todavía no tienes una. Escribe su nombre, ciudad, dirección, fecha de inicio y etapa actual. Al abrirla, comprueba el nombre de la obra activa en el selector: todos los materiales, ferreterías y cotizaciones que añadas corresponderán a esa obra. Cambia el selector cuando quieras trabajar en otra.",
      "Si empiezas con una obra vacía, es normal ver cero materiales y cero cotizaciones. Esos ceros no significan que la construcción no cueste nada: todavía falta registrar información. Los ejemplos de esta guía son ficticios; usa tus propias cantidades, fechas y precios.",
    ],
  },
  {
    title: "2. Registra lo que necesitas: materiales",
    paragraphs: [
      "Desde Resumen pulsa Registrar material. Escribe un nombre fácil de reconocer, por ejemplo Cemento gris; cantidad 10 y unidad bultos. Cantidad es cuánto necesitas y unidad es cómo lo mides: bultos, metros, kilogramos o unidades. Evita mezclar unidades al comparar precios. La etapa indica en qué parte de la obra lo utilizarás, como Cimentación o Acabados.",
      "Fecha en que se necesita es el día en que el material debe estar disponible para trabajar. Plazo estimado es cuántos días tarda conseguirlo. Por ejemplo, si lo necesitas el día 20 y conseguirlo tarda 3 días, conviene gestionarlo el día 17. Registra el plazo que conozcas y corrígelo cuando el proveedor lo confirme; no es una predicción automática.",
      "Pulsa Guardar material. El material aparecerá en el plan y la guía te indicará cómo seguir. El precio se registra después, mediante una cotización. Añade los demás materiales de la misma forma. En Materiales puedes buscarlos, revisar sus detalles o editar una cantidad o fecha equivocada.",
    ],
  },
  {
    title: "3. Añade las ferreterías que vas a consultar",
    paragraphs: [
      "Abre Ferreterías y pulsa Agregar ferretería. Registra nombre y ciudad, y completa los datos de contacto disponibles. Por ejemplo, Ferretería Central. Guarda y comprueba que aparezca en el directorio de la obra.",
      "Una ferretería representa a quien te ofrece el material. Necesitas al menos un material y una ferretería para registrar una cotización. Puedes añadir varios proveedores para comparar ofertas. La aplicación no contacta a la ferretería: tú pides el precio por el medio que uses y después lo registras aquí.",
    ],
  },
  {
    title: "4. Registra cada oferta: cotizaciones",
    paragraphs: [
      "En Cotizaciones y pedidos pulsa Registrar cotización. Selecciona la ferretería y el material correctos. Escribe la cantidad cotizada, el precio por una unidad, la fecha de cotización y el plazo informado. Si conoces la fecha estimada de entrega, añádela; usa Observaciones para aclarar condiciones de la oferta.",
      "Precio unitario no es el precio de toda la compra. En el ejemplo, si cada bulto cuesta 40.000 pesos, escribe 40000 como precio unitario y 10 como cantidad: diez bultos representan 400.000 pesos. No escribas 400000 como precio por bulto. Es un ejemplo para entender el cálculo, no un precio comercial vigente.",
      "Guarda la cotización y revisa que esté asociada al material y proveedor esperados. Para comparar, registra otra oferta del mismo material con la misma unidad. La estimación del tablero usa la cantidad requerida del material y el menor precio unitario válido; puede diferir del importe de una oferta si sus cantidades son distintas. Confirma cobertura, calidad, transporte e impuestos con el proveedor antes de decidir.",
    ],
  },
  {
    title: "5. Comprende costos, faltantes y fechas",
    paragraphs: [
      "En Resumen revisa materiales por cotizar, cotizaciones registradas y pedidos en curso. El subtotal conocido suma los materiales con precio válido. Si aún falta cotizar algún material, es un subtotal parcial y no el presupuesto completo de la obra. Sin cotización significa que no conocemos el precio; no significa precio cero.",
      "El Plan de adquisiciones ayuda a revisar fechas y prioridades. Una fecha de gestión vencida genera una alerta crítica; una fecha cercana indica que conviene preparar la gestión. Un aviso de entrega por revisar significa que la fecha estimada pasó mientras el pedido seguía en curso: confirma su situación con la ferretería.",
      "Las alertas dependen de las fechas y estados que registres. Si el material ya llegó, actualiza la recepción para evitar interpretar datos antiguos como pendientes actuales. No tener alertas registradas no certifica que toda la construcción esté bien.",
    ],
  },
  {
    title: "6. Decide y lleva el seguimiento del pedido",
    paragraphs: [
      "En Cotizaciones y pedidos revisa el detalle de las ofertas. Una cotización recibida es una propuesta del proveedor; todavía no es un pedido. Cuando ya hayas acordado y realizado el pedido por fuera de la aplicación, registra Pedido realizado y su fecha estimada de entrega.",
      "Cuando el proveedor confirme el despacho, actualiza a En tránsito. Al recibir y verificar el material, registra Recibido. Si descartas una oferta usa Rechazada; si cancelas un pedido usa Cancelado. Las rechazadas y canceladas quedan excluidas de la estimación de costos. Cambiar estados en ObraPlan registra tu seguimiento: no compra, no paga ni envía mensajes.",
      "Revisa también el estado del material en Materiales y actualízalo a Recibido cuando corresponda. El material y la cotización tienen estados propios; comprueba ambos registros al cerrar la recepción. Si cometiste un error, corrige el estado o los datos desde la opción correspondiente y revisa de nuevo el resumen.",
    ],
  },
  {
    title: "7. Pide ayuda a los agentes y conserva conocimiento",
    paragraphs: [
      "Después de registrar datos útiles, abre Agentes y conocimiento. Puedes elegir Revisar prioridades, Analizar costos o Análisis conjunto, y después pulsar Consultar agentes. También puedes escribir una pregunta concreta, como: ¿Qué materiales debo gestionar primero y cuáles siguen sin precio? Elegir un ejemplo prepara la pregunta; Consultar agentes la envía.",
      "El coordinador delega en Logística, Costos y Conocimiento según la pregunta. Ellos consultan herramientas de esta obra y fuentes del grafo. Comprueba la respuesta, las fuentes y los participantes. Sus propuestas te ayudan a decidir; tú confirmas las fechas y precios y realizas las acciones necesarias.",
      "Si el análisis está completo y los datos no cambiaron, puedes pulsar Conservar propuesta en el grafo. Así queda una nota relacionada con la obra y sus fuentes, disponible para consultas posteriores y publicación en Obsidian cuando esté configurado. Si el análisis quedó incompleto o cambiaste datos, revisa el mensaje y consulta de nuevo cuando corresponda. No necesitas Gemini para registrar materiales ni para escuchar esta guía.",
    ],
  },
  {
    title: "8. Tu rutina al volver a la aplicación",
    paragraphs: [
      "Abre la obra correcta y revisa el resumen. Comprueba primero avisos de fechas y entregas, añade materiales nuevos, registra las ofertas que recibiste y actualiza pedidos y recepciones. Después consulta agentes si necesitas interpretar prioridades. La actividad reciente ayuda a ver lo registrado.",
      "Guarda los formularios para conservar los cambios; cerrar un formulario sin guardar no registra nada. Cuando termines puedes cerrar sesión. Tus registros guardados permanecen entre reinicios. Si vuelves a empezar con otra obra, selecciona o crea esa obra para mantener sus datos separados.",
      "La guía siempre queda aquí: abre el paso que necesitas y pulsa Escuchar este paso. Puedes detener la voz o silenciar el saludo automático. El recorrido marcado Con registros indica presencia de información, no porcentaje de construcción terminada.",
    ],
  },
];
