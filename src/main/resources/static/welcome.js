/* Resumen del tablero y voz local: no invoca al proveedor de IA. */
window.WorkWelcome = (() => {
  const panel = document.querySelector("#work-welcome");
  const transcript = document.querySelector("#welcome-text");
  const status = document.querySelector("#welcome-status");
  const play = document.querySelector("#welcome-play");
  const stop = document.querySelector("#welcome-stop");
  const toggle = document.querySelector("#welcome-toggle");
  const guideTitle = document.querySelector("#welcome-guide-title");
  const guideText = document.querySelector("#welcome-guide-text");
  const guideAction = document.querySelector("#welcome-next");
  const steps = document.querySelector("#welcome-steps");
  const lessons = document.querySelector("#welcome-lessons");
  const supported =
    "speechSynthesis" in window && "SpeechSynthesisUtterance" in window;
  let enabled = true,
    message = "",
    current = null,
    requestedText = "",
    pending = false,
    timer = null;
  try {
    enabled = localStorage.getItem("obraplan.voice.enabled") !== "false";
  } catch {}

  function guidance(data) {
    const materials = data.materials || [];
    const suppliers = data.suppliers || [];
    const quotes = (data.quotes || []).filter(
      (q) => !["Rechazada", "Cancelado"].includes(q.status),
    );
    const hasOrders = quotes.some((q) =>
      ["Pedido realizado", "En tránsito", "Recibido"].includes(q.status),
    );
    const completed = [
      materials.length > 0,
      suppliers.length > 0,
      quotes.length > 0,
      hasOrders,
    ];
    let next;
    if (!materials.length)
      next = {
        title: "Paso 1 · Registra tu primer material",
        action: "add-material",
        button: "Empezar: registrar material",
        text: "Empieza indicando qué necesita tu obra. Pulsa Registrar material, escribe su nombre, cantidad y unidad; después indica cuándo lo necesitas y el plazo de entrega. Por ejemplo: cemento, 10 bultos. Al guardarlo podrás avanzar a la ferretería y la cotización.",
      };
    else if (!suppliers.length)
      next = {
        title: "Paso 2 · Añade una ferretería",
        action: "add-supplier",
        button: "Agregar mi primera ferretería",
        text: "Ya tienes materiales. Ahora registra una ferretería con su nombre y ciudad; agrega los datos de contacto que tengas. Así podrás asociar cada precio con el proveedor que lo ofreció.",
      };
    else if (
      !quotes.length ||
      Number(data.analysis?.estimate?.missingPriceCount || 0) > 0
    )
      next = {
        title: "Paso 3 · Registra una cotización",
        action: "guide-quote",
        button: "Registrar cotización",
        text: "Ya puedes cotizar. Elige el material y la ferretería, registra la cantidad, el precio unitario y el plazo que te informaron. La aplicación calculará el subtotal y señalará los materiales que aún no tienen precio. Registra solo precios que hayas recibido.",
      };
    else if (!hasOrders)
      next = {
        title: "Paso 4 · Revisa cotizaciones y pedidos",
        action: "guide-followup",
        button: "Revisar cotizaciones y pedidos",
        text: "Revisa las cotizaciones y decide cuál gestionar. Si ya realizaste un pedido fuera de la aplicación, registra su estado y la fecha estimada de entrega. Después podrás actualizarlo a En tránsito o Recibido. Cambiar el estado aquí no hace una compra.",
      };
    else
      next = {
        title: "Continúa · Revisa el seguimiento y consulta a los agentes",
        action: "guide-followup",
        button: "Ver seguimiento de pedidos",
        text: "Tu obra ya tiene materiales, proveedores, cotizaciones y seguimiento. Revisa las fechas y actualiza las entregas. En Agentes y conocimiento puedes pedir un análisis de prioridades y costos; cuando termine, puedes conservar su propuesta en el grafo.",
      };
    return { ...next, completed };
  }
  function brief(user, work, data, guide) {
    const materials = data.materials || [];
    const analysis = data.analysis || {};
    const metrics = analysis.metrics || {};
    const estimate = analysis.estimate || {};
    const alerts = analysis.insights || [];
    const date = new Intl.DateTimeFormat("es-CO", {
      weekday: "long",
      day: "numeric",
      month: "long",
    }).format(new Date());
    const parts = [
      `Hola, ${user.name}. Bienvenido a ${work.name}. Hoy es ${date}.`,
    ];
    if (work.demo) parts.push("Esta obra contiene datos de demostración.");
    if (!materials.length) {
      parts.push(
        "Te voy a guiar para organizar esta obra: primero registra materiales, después una ferretería, luego sus cotizaciones y finalmente el seguimiento de los pedidos.",
      );
    } else {
      parts.push(
        `Tienes ${materials.length} materiales registrados, ${Number(metrics.toQuote || 0)} por cotizar y ${Number(metrics.activeOrders || 0)} pedidos en curso.`,
      );
      if (estimate.knownSubtotal != null) {
        const amount = new Intl.NumberFormat("es-CO", {
          maximumFractionDigits: 0,
        }).format(estimate.knownSubtotal);
        parts.push(
          `El subtotal conocido es de ${amount} pesos colombianos. Es un subtotal parcial.`,
        );
      }
      if (estimate.missingPriceCount)
        parts.push(
          `Falta conocer el precio de ${estimate.missingPriceCount} materiales.`,
        );
      const critical = alerts.filter((alert) => alert.severity === "critical");
      if (critical.length) {
        parts.push(
          `Atención: hay ${critical.length} alertas críticas para revisar. ${critical[0].title}. Consulta los detalles en el tablero.`,
        );
      } else if (alerts.length) {
        parts.push(
          `Hay ${alerts.length} avisos para revisar en el tablero. ${alerts[0].title}.`,
        );
      } else {
        parts.push("No hay alertas de fechas en los registros actuales.");
      }
    }
    parts.push(guide.text);
    return parts.join(" ");
  }
  function controls() {
    toggle.textContent = enabled
      ? "Silenciar bienvenida"
      : "Activar bienvenida";
    toggle.setAttribute("aria-pressed", String(enabled));
    play.disabled = !supported || !message;
    stop.disabled = !current;
  }
  function cancel() {
    pending = false;
    clearTimeout(timer);
    current = null;
    if (supported) window.speechSynthesis.cancel();
    controls();
  }
  function speak(text = message) {
    cancel();
    requestedText = text;
    if (!supported || !message || panel.hidden) return;
    const voices = window.speechSynthesis.getVoices();
    const local = voices.filter((voice) => voice.localService);
    const voice =
      local.find((voice) => /^es(-|_)/i.test(voice.lang)) || local[0];
    if (!voice) {
      pending = voices.length === 0;
      status.textContent =
        "No hay una voz local disponible todavía. Puedes leer el resumen o pulsar Escuchar de nuevo.";
      return;
    }
    const utterance = new SpeechSynthesisUtterance(text);
    current = utterance;
    utterance.voice = voice;
    utterance.lang = voice.lang;
    utterance.rate = 1;
    utterance.onstart = () => {
      if (current !== utterance) return;
      clearTimeout(timer);
      status.textContent = "Leyendo la guía de tu obra…";
    };
    utterance.onend = () => {
      if (current !== utterance) return;
      clearTimeout(timer);
      current = null;
      status.textContent = "Lectura finalizada. Puedes escuchar otro paso.";
      controls();
    };
    utterance.onerror = () => {
      if (current !== utterance) return;
      cancel();
      status.textContent =
        "La voz no pudo reproducirse. Pulsa Escuchar resumen para intentarlo.";
    };
    status.textContent = "Preparando la bienvenida…";
    controls();
    timer = setTimeout(() => {
      if (current !== utterance) return;
      cancel();
      status.textContent =
        "Pulsa Escuchar resumen para permitir la voz en este navegador.";
    }, 4000);
    try {
      window.speechSynthesis.speak(utterance);
    } catch {
      utterance.onerror();
    }
  }
  function show(user, work, data, announce = true) {
    if (announce) cancel();
    panel.hidden = false;
    const guide = guidance(data);
    message = brief(user, work, data, guide);
    transcript.textContent = message;
    guideTitle.textContent = guide.title;
    guideText.textContent = guide.text;
    guideAction.textContent = guide.button;
    guideAction.dataset.action = guide.action;
    const labels = ["Materiales", "Ferreterías", "Cotizaciones", "Seguimiento"];
    steps.replaceChildren(
      ...labels.map((label, index) => {
        const step = document.createElement("li");
        step.textContent = `${index + 1}. ${label} · ${guide.completed[index] ? "Con registros" : "Por empezar"}`;
        step.className = guide.completed[index] ? "guide-done" : "";
        return step;
      }),
    );
    controls();
    if (announce) {
      document.querySelector("#welcome-manual").open = !(data.materials || [])
        .length;
      status.textContent = !supported
        ? "Tu navegador no admite voz. El resumen está disponible en texto."
        : enabled
          ? "Bienvenida por voz activada."
          : "Bienvenida automática silenciada.";
      if (enabled && supported) speak();
    }
  }
  play.onclick = () => speak();
  for (const lesson of window.WorkTutorial || []) {
    const section = document.createElement("details");
    section.className = "welcome-lesson";
    section.open = lessons.childElementCount === 0;
    const title = document.createElement("summary");
    title.textContent = lesson.title;
    section.append(title);
    for (const paragraph of lesson.paragraphs) {
      const text = document.createElement("p");
      text.textContent = paragraph;
      section.append(text);
    }
    const listen = document.createElement("button");
    listen.type = "button";
    listen.className = "btn btn-secondary btn-small";
    listen.textContent = "Escuchar este paso";
    listen.disabled = !supported;
    listen.onclick = () =>
      speak([lesson.title, ...lesson.paragraphs].join(". "));
    section.append(listen);
    const stopLesson = document.createElement("button");
    stopLesson.type = "button";
    stopLesson.className = "text-action";
    stopLesson.textContent = "Detener lectura";
    stopLesson.onclick = () => {
      cancel();
      status.textContent = "Lectura detenida.";
    };
    section.append(stopLesson);
    lessons.append(section);
  }
  stop.onclick = () => {
    cancel();
    status.textContent = "Lectura detenida.";
  };
  toggle.onclick = () => {
    enabled = !enabled;
    try {
      localStorage.setItem("obraplan.voice.enabled", String(enabled));
    } catch {}
    cancel();
    status.textContent = enabled
      ? "Bienvenida por voz activada."
      : "Bienvenida automática silenciada.";
    controls();
    if (enabled) speak();
  };
  if (supported)
    window.speechSynthesis.addEventListener("voiceschanged", () => {
      if (pending && !panel.hidden) speak(requestedText);
    });
  window.addEventListener("pagehide", cancel);
  controls();
  return {
    show,
    stop() {
      cancel();
      status.textContent = "Lectura detenida. Continúa con el siguiente paso.";
    },
    hide() {
      cancel();
      panel.hidden = true;
    },
  };
})();
