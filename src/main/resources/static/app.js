const state = {
  user: null,
  works: [],
  work: null,
  data: null,
  route: "summary",
  quoteTab: "Todas",
  authMode: "login",
  toastTimer: null,
};
const $ = (s, r = document) => r.querySelector(s),
  $$ = (s, r = document) => [...r.querySelectorAll(s)];
const api = async (path, opts = {}) => {
  const r = await fetch(`/api${path}`, {
    credentials: "same-origin",
    headers: { "Content-Type": "application/json", ...(opts.headers || {}) },
    ...opts,
  });
  let d = {};
  try {
    d = await r.json();
  } catch {}
  if (!r.ok) throw Error(d.error || "No se pudo completar la solicitud.");
  return d;
};
const esc = (s) =>
  String(s ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const money = (n) =>
  n == null
    ? "Sin cotizar"
    : "COP " +
      new Intl.NumberFormat("es-CO", { maximumFractionDigits: 0 }).format(
        Number(n),
      );
const fmtDate = (s) => {
  if (!s) return "—";
  const d = new Date(`${s}T12:00:00`);
  return new Intl.DateTimeFormat("es-CO", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  })
    .format(d)
    .replace(".", "")
    .toUpperCase();
};
const today = () => {
  const d = new Date(),
    pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const workId = () => state.work?.id;
const toast = (msg) => {
  const el = $("#toast");
  el.textContent = msg;
  el.classList.add("show");
  clearTimeout(state.toastTimer);
  state.toastTimer = setTimeout(() => el.classList.remove("show"), 2800);
};
const errorText = (e) => toast(e.message || "Ocurrió un error.");
function screen(name) {
  if (name !== "app") window.WorkWelcome?.hide();
  ["login", "works", "app"].forEach((x) =>
    $("#" + x + "-screen").classList.toggle("hidden", x !== name),
  );
}
function openModal(title, content, kicker = "") {
  $("#modal-title").textContent = title;
  $("#modal-kicker").textContent = kicker;
  $("#modal-content").innerHTML = content;
  $("#modal-backdrop").classList.add("open");
  $("#modal-backdrop").setAttribute("aria-hidden", "false");
  setTimeout(
    () => $("#modal-content input, #modal-content select")?.focus(),
    50,
  );
}
function closeModal() {
  $("#modal-backdrop").classList.remove("open");
  $("#modal-backdrop").setAttribute("aria-hidden", "true");
}
async function loadWorks() {
  state.works = (await api("/works")).works;
  $("#works-user").textContent = state.user.name;
  renderWorks();
  screen("works");
}
function renderWorks() {
  const list = $("#works-list");
  $("#works-empty").classList.toggle("hidden", state.works.length > 0);
  list.classList.toggle("hidden", state.works.length === 0);
  list.innerHTML = state.works
    .map(
      (w, i) =>
        /* HTML */ `<article class="work-sheet">
          <div class="blueprint-thumb ${i % 2 ? "alt" : ""}"></div>
          <span class="eyebrow"
            >${w.demo ? "PROYECTO DEMOSTRATIVO" : "OBRA ACTIVA"}</span
          >
          <h2>${esc(w.name)}</h2>
          <p class="muted">${esc(w.city)} · ${esc(w.address)}</p>
          <div class="sheet-meta">
            <span
              >ETAPA ACTUAL<br /><b>${esc(w.stage || "Sin definir")}</b></span
            ><span>INICIO<br /><b>${fmtDate(w.startDate)}</b></span>
          </div>
          <div class="work-actions">
            <button
              class="btn btn-primary btn-small"
              data-action="open-work"
              data-id="${esc(w.id)}"
            >
              Abrir obra →</button
            ><button
              class="text-action"
              data-action="edit-work"
              data-id="${esc(w.id)}"
            >
              Editar detalles
            </button>
          </div>
        </article>`,
    )
    .join("");
}
async function enterWork(id) {
  state.work = state.works.find((w) => w.id === id) || state.work;
  if (!state.work) throw Error("No se encontró la obra.");
  state.data = await api(`/works/${encodeURIComponent(id)}/dashboard`);
  $("#side-name").textContent = state.user.name;
  $("#project-select").innerHTML = state.works
    .map(
      (w) =>
        /* HTML */ `<option
          value="${esc(w.id)}"
          ${w.id === id ? "selected" : ""}
        >
          ${esc(w.name)}
        </option>`,
    )
    .join("");
  screen("app");
  renderAll();
  route(state.route);
  window.WorkWelcome?.show(state.user, state.work, state.data);
}
function route(name) {
  state.route = name;
  $$(".view").forEach((v) => v.classList.add("hidden"));
  const target = $("#" + name + "-view");
  if (target) target.classList.remove("hidden");
  $$(".nav-item").forEach((a) =>
    a.classList.toggle(
      "active",
      a.dataset.route === name || (name === "plan" && a.hash === "#materials"),
    ),
  );
  const labels = {
    summary: "RESUMEN",
    plan: "PLAN DE ADQUISICIONES",
    hardware: "FERRETERÍAS",
    quotes: "COTIZACIONES Y PEDIDOS",
    knowledge: "AGENTES Y CONOCIMIENTO",
  };
  $("#breadcrumbs").textContent =
    `${state.work?.name?.toUpperCase() || "OBRA"} / ${labels[name] || ""}`;
  if (name === "plan") renderMaterials();
  if (name === "hardware") renderSuppliers();
  if (name === "quotes") renderQuotes();
  if (name === "knowledge") renderKnowledge();
}
function renderAll() {
  renderSummary();
  renderMaterials();
  renderSuppliers();
  renderQuotes();
}
function statusBadge(s) {
  const cls =
    s === "Por cotizar"
      ? "status-quote"
      : s === "Cotizado" ||
          s.includes("Cotización") ||
          s === "Pendiente de decisión"
        ? "status-quoted"
        : s === "Recibido"
          ? "status-received"
          : s === "Cancelado" || s === "Rechazada"
            ? "status-risk"
            : "status-order";
  return /* HTML */ `<span class="status ${cls}">${esc(s)}</span>`;
}
function joined() {
  const d = state.data || {};
  return {
    materials: d.materials || [],
    suppliers: d.suppliers || [],
    quotes: d.quotes || [],
    analysis: d.analysis || {},
    activity: d.activity || [],
  };
}
function renderSummary() {
  const { materials, suppliers, quotes, analysis, activity } = joined(),
    w = state.work;
  $("#summary-date").textContent = fmtDate(today());
  $("#summary-location").textContent = `${w.name} · ${w.city}`;
  $("#data-mode-chip").textContent = w.demo
    ? "Datos demostrativos"
    : "Registros de tu obra";
  $("#stage-select").value = w.stage || "Cimentación";
  const est = analysis.estimate || {},
    met = analysis.metrics || {};
  const recent = materials
    .filter((m) => m.status !== "Recibido")
    .sort((a, b) => (a.neededDate || "").localeCompare(b.neededDate || ""))
    .slice(0, 4);
  $("#work-dossier").innerHTML = /* HTML */ `<div>
      <span class="dossier-label">PROYECTO</span><strong>${esc(w.name)}</strong>
      <div class="progress-line">
        <span
          style="width:${materials.length
            ? Math.min(
                94,
                Math.max(
                  12,
                  (materials.filter((m) => m.status === "Recibido").length /
                    materials.length) *
                    100,
                ),
              )
            : 0}%"
        ></span>
      </div>
    </div>
    <div>
      <span class="dossier-label">UBICACIÓN</span
      ><strong>${esc(w.city)}</strong>
    </div>
    <div>
      <span class="dossier-label">ETAPA ACTUAL</span
      ><strong>${esc(w.stage || "Por definir")}</strong>
    </div>
    <div>
      <span class="dossier-label">COSTO REGISTRADO</span
      ><strong>${money(est.knownSubtotal)}</strong>
    </div>`;
  $("#summary-metrics").innerHTML = /* HTML */ `<div class="metric">
      <span class="metric-label">MATERIALES POR COTIZAR</span>
      <div class="metric-value">${met.toQuote || 0}</div>
      <span class="metric-sub">Requieren contacto con proveedores</span>
    </div>
    <div class="metric">
      <span class="metric-label">COTIZACIONES RECIBIDAS</span>
      <div class="metric-value">${met.quotes || 0}</div>
      <span class="metric-sub">Registradas manualmente</span>
    </div>
    <div class="metric">
      <span class="metric-label">PEDIDOS EN CURSO</span>
      <div class="metric-value">${met.activeOrders || 0}</div>
      <span class="metric-sub">Pedido realizado o en tránsito</span>
    </div>
    <div class="metric critical">
      <span class="metric-label">PRÓXIMA FECHA CRÍTICA</span>
      <div class="metric-value" style="font-size:1.28rem">
        ${met.nextCriticalDate ? fmtDate(met.nextCriticalDate) : "—"}
      </div>
      <span class="metric-sub">Fecha requerida más próxima</span>
    </div>`;
  $("#upcoming-list").innerHTML = recent.length
    ? recent
        .map(
          (m) =>
            /* HTML */ `<div class="acquisition">
              <div>
                <b>${esc(m.name)}</b>
                <div class="minor">${esc(m.stage)}</div>
              </div>
              <div>${esc(m.quantity)} ${esc(m.unit)}</div>
              <div>${fmtDate(m.neededDate)}</div>
              <div>
                ${m.leadDays ? `${m.leadDays} días plazo` : "Plazo sin definir"}
              </div>
              <div>${statusBadge(m.status)}</div>
              <button
                class="text-action"
                data-action="material-detail"
                data-id="${esc(m.id)}"
              >
                Detalle ↗
              </button>
            </div>`,
        )
        .join("")
    : /* HTML */ `<div class="empty">
        <p>No hay materiales registrados en esta obra.</p>
        <button class="btn btn-secondary" data-action="add-material">
          Registrar material
        </button>
      </div>`;
  $("#summary-suppliers").innerHTML =
    suppliers
      .slice(0, 3)
      .map(
        (s) =>
          /* HTML */ `<div class="supplier-mini-row">
            <span
              ><b>${esc(s.name)}</b
              ><span class="minor">${esc(s.city)}</span></span
            ><span>${esc(s.phone)}</span
            ><button
              class="text-action"
              data-action="supplier-detail"
              data-id="${esc(s.id)}"
            >
              Ficha
            </button>
          </div>`,
      )
      .join("") ||
    /* HTML */ `<div class="empty">
      <p>No hay ferreterías registradas.</p>
    </div>`;
  $("#activity-list").innerHTML =
    activity
      .slice(0, 6)
      .map(
        (a) =>
          /* HTML */ `<div class="timeline-item">
            ${esc(a.message)}<time
              >${fmtDate(String(a.createdAt || "").slice(0, 10))}</time
            >
          </div>`,
      )
      .join("") || /* HTML */ `<p class="muted">Todavía no hay actividad.</p>`;
  const insights = analysis.insights || [],
    agents = analysis.agents || [];
  $("#agent-panel").innerHTML = /* HTML */ `<span class="eyebrow"
      >ANÁLISIS LOCAL POR REGLAS</span
    >
    <h2>Herramientas de apoyo a la adquisición</h2>
    <div class="agent-grid">
      ${agents
        .map(
          (a) =>
            /* HTML */ `<div class="agent-card">
              <b>${esc(a.name)}</b>${esc(a.role)}
            </div>`,
        )
        .join("")}
    </div>
    ${insights.length
      ? /* HTML */ `<div style="margin-top:14px">
          ${insights
            .map(
              (i) =>
                /* HTML */ `<div class="insight ${i.severity}">
                  <b>${esc(i.agent)} · ${esc(i.title)}</b><br />${esc(i.detail)}
                </div>`,
            )
            .join("")}
        </div>`
      : /* HTML */ `<p class="minor">
          No hay alertas logísticas próximas con la información ingresada.
        </p>`}`;
}
function renderMaterials() {
  if (!state.data) return;
  const { materials, quotes, analysis } = joined(),
    qMap = new Map();
  for (const q of quotes) {
    if (q.status === "Rechazada" || q.status === "Cancelado") continue;
    const arr = qMap.get(q.materialId) || [];
    arr.push(q);
    qMap.set(q.materialId, arr);
  }
  const estimate = analysis.estimate || {};
  $("#cost-summary").innerHTML = /* HTML */ `<div>
      <span class="eyebrow"
        >${estimate.isComplete
          ? "ESTIMACIÓN REGISTRADA"
          : "SUBTOTAL PARCIAL"}</span
      >
      <p>${esc(estimate.message || "")}</p>
    </div>
    <strong>${money(estimate.knownSubtotal)}</strong>`;
  const search = $("#material-search").value.toLocaleLowerCase("es"),
    stage = $("#stage-filter").value,
    status = $("#status-filter").value,
    soon = $("#date-filter").value === "soon",
    limit = new Date();
  limit.setDate(limit.getDate() + 7);
  const rows = materials.filter(
    (m) =>
      (!search || m.name.toLocaleLowerCase("es").includes(search)) &&
      (!stage || m.stage === stage) &&
      (!status || m.status === status) &&
      (!soon || new Date(`${m.neededDate}T12:00:00`) <= limit),
  );
  $("#material-table-body").innerHTML =
    rows
      .map((m) => {
        const qs = qMap.get(m.id) || [],
          best = qs.reduce(
            (a, b) => (!a || Number(b.unitPrice) < Number(a.unitPrice) ? b : a),
            null,
          ),
          unitPrice = best?.unitPrice ?? null,
          subtotal =
            unitPrice == null ? null : Number(m.quantity) * Number(unitPrice),
          late =
            unitPrice != null &&
            best?.expectedDate &&
            best.expectedDate > m.neededDate;
        return /* HTML */ `<tr>
          <td>
            <span class="material-name">${esc(m.name)}</span>
            <div class="minor">
              ${m.demo ? "DATOS DEMOSTRATIVOS" : esc(m.category || "")}
            </div>
          </td>
          <td>${esc(m.stage)}</td>
          <td class="amount">${esc(m.quantity)} ${esc(m.unit)}</td>
          <td class="mono">
            ${fmtDate(m.neededDate)}${late
              ? '<div class="minor" style="color:var(--terra)">PLAZO SUPERA FECHA</div>'
              : ""}
          </td>
          <td>${m.leadDays ? `${esc(m.leadDays)} días` : "Sin dato"}</td>
          <td>${money(unitPrice)}</td>
          <td><b>${money(subtotal)}</b></td>
          <td>${statusBadge(m.status)}</td>
          <td>
            <div class="row-actions">
              <button
                class="text-action"
                data-action="material-detail"
                data-id="${esc(m.id)}"
              >
                Detalle</button
              ><button
                class="text-action"
                data-action="edit-material"
                data-id="${esc(m.id)}"
              >
                Editar</button
              ><button
                class="text-action"
                data-action="quote-material"
                data-id="${esc(m.id)}"
              >
                Cotizar</button
              ><button
                class="text-action"
                data-action="material-status"
                data-id="${esc(m.id)}"
              >
                Estado
              </button>
            </div>
          </td>
        </tr>`;
      })
      .join("") ||
    /* HTML */ `<tr>
      <td colspan="9" style="text-align:center;padding:28px">
        No hay materiales que coincidan con los filtros.
      </td>
    </tr>`;
}
function renderSuppliers() {
  if (!state.data) return;
  const { suppliers, quotes } = joined(),
    term = $("#supplier-search").value.toLocaleLowerCase("es"),
    city = $("#city-filter").value;
  const cities = [...new Set(suppliers.map((s) => s.city).filter(Boolean))],
    sel = $("#city-filter").value;
  $("#city-filter").innerHTML =
    '<option value="">Todas las ubicaciones</option>' +
    cities.map((c) => /* HTML */ `<option>${esc(c)}</option>`).join("");
  $("#city-filter").value = cities.includes(sel) ? sel : "";
  const rows = suppliers.filter(
    (s) =>
      (!term ||
        s.name.toLocaleLowerCase("es").includes(term) ||
        s.city.toLocaleLowerCase("es").includes(term)) &&
      (!city || s.city === city),
  );
  $("#supplier-directory").innerHTML =
    rows
      .map(
        (s) =>
          /* HTML */ `<article class="supplier-row">
            <div class="monogram">
              ${esc(
                s.name
                  .split(/\s+/)
                  .slice(0, 2)
                  .map((x) => x[0])
                  .join("")
                  .toUpperCase(),
              )}
            </div>
            <div>
              <b>${esc(s.name)}</b>
              <div class="minor">
                ${s.demo
                  ? "Datos demostrativos"
                  : `${quotes.filter((q) => q.supplierId === s.id).length} cotizaciones`}
              </div>
            </div>
            <div class="supplier-address">
              <span class="eyebrow">UBICACIÓN</span>
              <div>${esc(s.address || s.city)}</div>
              <div class="minor">${esc(s.city)}</div>
            </div>
            <div class="supplier-site">
              <span class="eyebrow">CONTACTO</span>
              <div>${esc(s.phone)}</div>
              ${s.website
                ? /* HTML */ `<a
                    class="link"
                    href="${esc(s.website)}"
                    target="_blank"
                    rel="noopener noreferrer"
                    >Sitio web ↗</a
                  >`
                : /* HTML */ `<span class="minor">Sin sitio web</span>`}
            </div>
            <div class="row-actions">
              <button
                class="text-action"
                data-action="supplier-detail"
                data-id="${esc(s.id)}"
              >
                Ficha</button
              ><button
                class="text-action"
                data-action="edit-supplier"
                data-id="${esc(s.id)}"
              >
                Editar</button
              ><button
                class="text-action danger-text"
                data-action="delete-supplier"
                data-id="${esc(s.id)}"
              >
                Eliminar
              </button>
            </div>
          </article>`,
      )
      .join("") ||
    /* HTML */ `<div class="empty">
      <p>No hay ferreterías que coincidan. Puedes registrar un contacto.</p>
      <button class="btn btn-primary" data-action="add-supplier">
        Agregar ferretería
      </button>
    </div>`;
}
function renderQuotes() {
  if (!state.data) return;
  const { quotes, materials, suppliers } = joined(),
    status = $("#quote-status-filter").value,
    supplier = $("#quote-supplier-filter").value;
  const prev = $("#quote-supplier-filter").value;
  $("#quote-supplier-filter").innerHTML =
    '<option value="">Todas las ferreterías</option>' +
    suppliers
      .map(
        (s) =>
          /* HTML */ `<option value="${esc(s.id)}">${esc(s.name)}</option>`,
      )
      .join("");
  $("#quote-supplier-filter").value = prev;
  const rows = quotes.filter(
    (q) =>
      (!status || q.status === status) &&
      (!supplier || q.supplierId === supplier) &&
      (state.quoteTab === "Todas" ||
        (state.quoteTab === "Cotizaciones" &&
          ["Cotización recibida", "Pendiente de decisión"].includes(
            q.status,
          )) ||
        (state.quoteTab === "Pedidos" &&
          ["Pedido realizado", "En tránsito"].includes(q.status)) ||
        (state.quoteTab === "Recibidos" && q.status === "Recibido")),
  );
  $("#quote-table-body").innerHTML = rows
    .map((q) => {
      const m = materials.find((x) => x.id === q.materialId),
        s = suppliers.find((x) => x.id === q.supplierId);
      return /* HTML */ `<tr>
        <td>
          <b>${esc(m?.name || "Material eliminado")}</b>
          <div class="minor">
            ${q.demo ? "DATOS DEMOSTRATIVOS" : "REGISTRO MANUAL"}
          </div>
        </td>
        <td>${esc(s?.name || "Ferretería")}</td>
        <td>${money(q.unitPrice)}</td>
        <td class="mono">${esc(q.quantity)} ${esc(m?.unit || "")}</td>
        <td class="mono">${fmtDate(q.quotedAt)}</td>
        <td>
          ${q.leadDays != null
            ? `${esc(q.leadDays)} días${q.expectedDate ? ` · ${fmtDate(q.expectedDate)}` : ""}`
            : "Sin dato"}
        </td>
        <td>${statusBadge(q.status)}</td>
        <td>
          <div class="row-actions">
            <button
              class="text-action"
              data-action="quote-detail"
              data-id="${esc(q.id)}"
            >
              Detalle</button
            >${quoteActions(q)}
          </div>
        </td>
      </tr>`;
    })
    .join("");
  const empty = $("#quote-empty");
  empty.classList.toggle("hidden", rows.length > 0);
  $("#quote-table-body")
    .closest(".table-wrap")
    .classList.toggle("hidden", rows.length === 0);
}
function quoteActions(q) {
  if (["Cotización recibida", "Pendiente de decisión"].includes(q.status))
    return /* HTML */ `<button
        class="text-action"
        data-action="quote-order"
        data-id="${esc(q.id)}"
      >
        Marcar pedido</button
      ><button
        class="text-action"
        data-action="quote-reject"
        data-id="${esc(q.id)}"
      >
        Rechazar
      </button>`;
  if (q.status === "Pedido realizado")
    return /* HTML */ `<button
      class="text-action"
      data-action="quote-transit"
      data-id="${esc(q.id)}"
    >
      En tránsito
    </button>`;
  if (q.status === "En tránsito")
    return /* HTML */ `<button
      class="text-action"
      data-action="quote-received"
      data-id="${esc(q.id)}"
    >
      Recibido
    </button>`;
  return "";
}
function formFooter(cancel, label = "Guardar") {
  return /* HTML */ `<div class="form-footer">
    <button type="button" class="btn btn-secondary" data-action="close-modal">
      Cancelar</button
    ><button type="submit" class="btn btn-primary">${label}</button>
  </div>`;
}
function addWorkForm() {
  openModal(
    "Crear obra",
    /* HTML */ `<form id="work-form">
      <div class="modal-grid">
        <div class="field full">
          <label>Nombre de la obra *</label
          ><input name="name" required maxlength="100" />
        </div>
        <div class="field full">
          <label>Dirección o ubicación *</label
          ><input name="address" required maxlength="160" />
        </div>
        <div class="field">
          <label>Ciudad y departamento *</label
          ><input name="city" required placeholder="Medellín, Antioquia" />
        </div>
        <div class="field">
          <label>Fecha estimada de inicio *</label
          ><input name="startDate" type="date" required value="${today()}" />
        </div>
        <div class="field full">
          <label>Descripción opcional</label
          ><textarea name="description" maxlength="400"></textarea>
        </div>
      </div>
      ${formFooter("close-modal", "Guardar obra")}
    </form>`,
    "NUEVO PROYECTO",
  );
}
function editWorkForm() {
  const w = state.work;
  openModal(
    "Editar obra",
    /* HTML */ `<form id="work-form">
      <div class="modal-grid">
        <div class="field full">
          <label>Nombre de la obra *</label
          ><input name="name" required value="${esc(w.name)}" />
        </div>
        <div class="field full">
          <label>Dirección o ubicación *</label
          ><input name="address" required value="${esc(w.address)}" />
        </div>
        <div class="field">
          <label>Ciudad y departamento *</label
          ><input name="city" required value="${esc(w.city)}" />
        </div>
        <div class="field">
          <label>Etapa actual</label
          ><select name="stage">
            ${["Cimentación", "Estructura", "Mampostería", "Acabados"]
              .map(
                (x) =>
                  /* HTML */ `<option ${w.stage === x ? "selected" : ""}>
                    ${x}
                  </option>`,
              )
              .join("")}
          </select>
        </div>
      </div>
      ${formFooter("close-modal", "Guardar cambios")}
    </form>`,
    "OBRA",
  );
}
function materialForm(item = null) {
  openModal(
    item ? "Editar material" : "Registrar material",
    /* HTML */ `<form
      id="material-form"
      ${item ? `data-id="${esc(item.id)}"` : ""}
    >
      <p class="lead">
        Ingresa cantidades y fechas. El precio se añade manualmente al registrar
        una cotización.
      </p>
      <div class="modal-grid">
        <div class="field full">
          <label>Nombre del material *</label
          ><input
            name="name"
            required
            maxlength="100"
            value="${esc(item?.name || "")}"
          />
        </div>
        <div class="field">
          <label>Categoría</label
          ><input
            name="category"
            value="${esc(item?.category || "")}"
            placeholder="Ej. Agregados"
          />
        </div>
        <div class="field">
          <label>Etapa de la obra *</label
          ><select name="stage" required>
            ${["Cimentación", "Estructura", "Mampostería", "Acabados"]
              .map(
                (x) =>
                  /* HTML */ `<option ${item?.stage === x ? "selected" : ""}>
                    ${x}
                  </option>`,
              )
              .join("")}
          </select>
        </div>
        <div class="field">
          <label>Cantidad *</label
          ><input
            name="quantity"
            type="number"
            min="0.01"
            step="any"
            required
            value="${esc(item?.quantity || "")}"
          />
        </div>
        <div class="field">
          <label>Unidad *</label
          ><input
            name="unit"
            required
            placeholder="bultos, m³, kg"
            value="${esc(item?.unit || "")}"
          />
        </div>
        <div class="field">
          <label>Fecha en que se necesita *</label
          ><input
            name="neededDate"
            type="date"
            required
            value="${esc(item?.neededDate || "")}"
          />
        </div>
        <div class="field">
          <label>Plazo estimado (días)</label
          ><input
            name="leadDays"
            type="number"
            min="0"
            step="1"
            value="${esc(item?.leadDays || "")}"
          />
        </div>
        <div class="field full">
          <label>Notas opcionales</label
          ><textarea name="notes">${esc(item?.notes || "")}</textarea>
        </div>
      </div>
      ${formFooter(
        "close-modal",
        item ? "Guardar cambios" : "Guardar material",
      )}
    </form>`,
    "MATERIALES",
  );
}
function supplierForm(item = null) {
  openModal(
    item ? "Editar ferretería" : "Agregar ferretería",
    /* HTML */ `<form id="supplier-form">
      <p class="lead">
        Contacto manual para solicitar cotizaciones; la plataforma no contactará
        al proveedor automáticamente.
      </p>
      <div class="modal-grid">
        <div class="field full">
          <label>Nombre de la ferretería *</label
          ><input name="name" required value="${esc(item?.name || "")}" />
        </div>
        <div class="field">
          <label>Teléfono *</label
          ><input
            name="phone"
            type="tel"
            required
            value="${esc(item?.phone || "")}"
          />
        </div>
        <div class="field">
          <label>Ciudad y departamento *</label
          ><input
            name="city"
            required
            placeholder="Medellín, Antioquia"
            value="${esc(item?.city || "")}"
          />
        </div>
        <div class="field full">
          <label>Dirección o ubicación</label
          ><input name="address" value="${esc(item?.address || "")}" />
        </div>
        <div class="field full">
          <label>Enlace al sitio web, opcional</label
          ><input
            name="website"
            type="url"
            placeholder="https://"
            value="${esc(item?.website || "")}"
          />
        </div>
        <div class="field full">
          <label>Notas opcionales</label
          ><textarea name="notes">${esc(item?.notes || "")}</textarea>
        </div>
      </div>
      ${formFooter(
        "close-modal",
        item ? "Guardar cambios" : "Guardar ferretería",
      )}
    </form>`,
    "DIRECTORIO",
  );
}
function quoteForm(preselectedSupplier = "", preselectedMaterial = "") {
  const { materials, suppliers } = joined();
  if (!materials.length || !suppliers.length) {
    toast(
      !suppliers.length
        ? "Registra una ferretería antes de cotizar."
        : "Registra un material antes de cotizar.",
    );
    return;
  }
  openModal(
    "Registrar cotización",
    /* HTML */ `<form id="quote-form">
      <p class="lead">
        Precio y plazo ingresados manualmente a partir del contacto con el
        proveedor.
      </p>
      <div class="modal-grid">
        <div class="field">
          <label>Ferretería *</label
          ><select name="supplierId" required>
            ${suppliers
              .map(
                (s) =>
                  /* HTML */ `<option
                    value="${esc(s.id)}"
                    ${s.id === preselectedSupplier ? "selected" : ""}
                  >
                    ${esc(s.name)}
                  </option>`,
              )
              .join("")}
          </select>
        </div>
        <div class="field">
          <label>Material asociado *</label
          ><select name="materialId" required>
            ${materials
              .map(
                (m) =>
                  /* HTML */ `<option
                    value="${esc(m.id)}"
                    ${m.id === preselectedMaterial ? "selected" : ""}
                  >
                    ${esc(m.name)}
                  </option>`,
              )
              .join("")}
          </select>
        </div>
        <div class="field">
          <label>Cantidad cotizada *</label
          ><input
            name="quantity"
            type="number"
            min="0.01"
            step="any"
            required
          />
        </div>
        <div class="field">
          <label>Precio unitario COP *</label
          ><input name="unitPrice" type="number" min="0" step="1" required />
        </div>
        <div class="field">
          <label>Fecha de cotización *</label
          ><input name="quotedAt" type="date" required value="${today()}" />
        </div>
        <div class="field">
          <label>Plazo en días</label
          ><input name="leadDays" type="number" min="0" step="1" />
        </div>
        <div class="field full">
          <label>Fecha estimada de entrega (opcional)</label
          ><input name="expectedDate" type="date" />
        </div>
        <div class="field full">
          <label>Observaciones</label><textarea name="notes"></textarea>
        </div>
      </div>
      ${formFooter("close-modal", "Guardar cotización")}
    </form>`,
    "COTIZACIONES",
  );
}
function materialDetail(m) {
  const { quotes, suppliers } = joined(),
    qs = quotes.filter((q) => q.materialId === m.id);
  openModal(
    m.name,
    /* HTML */ `<span class="demo-chip"
        >${m.demo ? "Datos demostrativos" : "Registro de obra"}</span
      >
      <div class="detail-grid" style="margin-top:18px">
        <div class="detail-item"><b>Etapa</b>${esc(m.stage)}</div>
        <div class="detail-item">
          <b>Cantidad requerida</b>${esc(m.quantity)} ${esc(m.unit)}
        </div>
        <div class="detail-item">
          <b>Fecha necesaria</b>${fmtDate(m.neededDate)}
        </div>
        <div class="detail-item">
          <b>Plazo estimado</b>${m.leadDays
            ? esc(m.leadDays) + " días"
            : "Sin dato"}
        </div>
        <div class="detail-item"><b>Estado</b>${statusBadge(m.status)}</div>
        <div class="detail-item">
          <b>Cotizaciones registradas</b>${qs.length}
        </div>
        <div class="detail-item">
          <b>Notas</b>${esc(m.notes || "Sin notas")}
        </div>
      </div>
      <h3>Cotizaciones de este material</h3>
      ${qs
        .map(
          (q) =>
            /* HTML */ `<p>
              ${esc(
                suppliers.find((s) => s.id === q.supplierId)?.name ||
                  "Ferretería",
              )}
              · ${money(q.unitPrice)} · ${esc(q.status)}
            </p>`,
        )
        .join("") || '<p class="muted">Aún no hay cotizaciones.</p>'}
      <div class="form-footer">
        <button
          type="button"
          class="btn btn-secondary"
          data-action="close-modal"
        >
          Cerrar</button
        ><button
          class="btn btn-primary"
          data-action="quote-material"
          data-id="${esc(m.id)}"
        >
          Registrar cotización
        </button>
      </div>`,
    `DETALLE DE MATERIAL`,
  );
}
function supplierDetail(s) {
  openModal(
    s.name,
    /* HTML */ `<span class="demo-chip"
        >${s.demo ? "Datos demostrativos" : "Directorio de la obra"}</span
      >
      <div class="detail-grid" style="margin-top:18px">
        <div class="detail-item"><b>Teléfono</b>${esc(s.phone)}</div>
        <div class="detail-item">
          <b>Ubicación</b>${esc(s.address || s.city)} · ${esc(s.city)}
        </div>
        <div class="detail-item">
          <b>Sitio web</b>${s.website
            ? /* HTML */ `<a
                class="link"
                href="${esc(s.website)}"
                target="_blank"
                rel="noopener noreferrer"
                >Abrir sitio web ↗</a
              >`
            : "No registrado"}
        </div>
        <div class="detail-item">
          <b>Cotizaciones registradas</b>${(state.data.quotes || []).filter(
            (q) => q.supplierId === s.id,
          ).length}
        </div>
      </div>
      <p class="notice" style="margin-top:18px">
        El contacto con esta ferretería se realiza manualmente. ObraPlan no
        consulta inventario ni confirma disponibilidad.
      </p>
      <div class="form-footer">
        <button class="btn btn-secondary" data-action="close-modal">
          Cerrar</button
        ><button
          class="btn btn-primary"
          data-action="quote-supplier"
          data-id="${esc(s.id)}"
        >
          Registrar cotización
        </button>
      </div>`,
    "DIRECTORIO",
  );
}
function quoteDetail(q) {
  const { materials, suppliers } = joined(),
    m = materials.find((x) => x.id === q.materialId),
    s = suppliers.find((x) => x.id === q.supplierId);
  openModal(
    "Detalle de cotización",
    /* HTML */ `<div class="detail-grid">
        <div class="detail-item"><b>Material</b>${esc(m?.name)}</div>
        <div class="detail-item"><b>Ferretería</b>${esc(s?.name)}</div>
        <div class="detail-item">
          <b>Precio unitario</b>${money(q.unitPrice)}
        </div>
        <div class="detail-item">
          <b>Cantidad</b>${esc(q.quantity)} ${esc(m?.unit || "")}
        </div>
        <div class="detail-item">
          <b>Fecha cotizada</b>${fmtDate(q.quotedAt)}
        </div>
        <div class="detail-item">
          <b>Plazo</b>${q.leadDays == null
            ? "Sin dato"
            : esc(q.leadDays) + " días"}
        </div>
        <div class="detail-item">
          <b>Entrega estimada</b>${fmtDate(q.expectedDate)}
        </div>
        <div class="detail-item"><b>Estado</b>${statusBadge(q.status)}</div>
        <div class="detail-item full">
          <b>Observaciones</b>${esc(q.notes || "Sin observaciones")}
        </div>
      </div>
      <div class="form-footer">
        <button class="btn btn-secondary" data-action="close-modal">
          Cerrar
        </button>
      </div>`,
    `REGISTRO MANUAL`,
  );
}
function statusForm(m) {
  openModal(
    "Actualizar estado del material",
    /* HTML */ `<form id="material-status-form">
      <p class="lead">
        Actualiza el seguimiento interno; este cambio no confirma inventario ni
        un pedido con la ferretería.
      </p>
      <div class="field">
        <label>Estado</label
        ><select name="status">
          ${["Por cotizar", "Cotizado", "Pedido", "Recibido"]
            .map(
              (x) =>
                /* HTML */ `<option ${m.status === x ? "selected" : ""}>
                  ${x}
                </option>`,
            )
            .join("")}
        </select>
      </div>
      <input type="hidden" name="id" value="${esc(m.id)}" />${formFooter(
        "close-modal",
        "Guardar estado",
      )}
    </form>`,
    "SEGUIMIENTO",
  );
}
async function refresh() {
  state.data = await api(`/works/${encodeURIComponent(workId())}/dashboard`);
  renderAll();
  window.WorkWelcome?.show(state.user, state.work, state.data, false);
}
function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

$("#auth-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("#auth-error").textContent = "";
  const register = state.authMode === "register";
  const payload = {
    email: $("#auth-email").value,
    password: $("#auth-password").value,
    ...(register ? { name: $("#auth-name").value } : {}),
  };
  try {
    const r = await api(register ? "/auth/register" : "/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    state.user = r.user;
    await loadWorks();
  } catch (err) {
    $("#auth-error").textContent = err.message;
  }
});
$("#auth-switch").onclick = () => {
  state.authMode = state.authMode === "login" ? "register" : "login";
  const reg = state.authMode === "register";
  $("#auth-title").textContent = reg
    ? "Crea tu cuenta de obra"
    : "Organiza las compras de tu obra";
  $("#auth-name").closest(".field").classList.toggle("hidden", !reg);
  $("#auth-name").required = reg;
  $("#auth-password").autocomplete = reg ? "new-password" : "current-password";
  $("#auth-submit").textContent = reg ? "Crear cuenta" : "Iniciar sesión";
  $("#auth-switch").textContent = reg
    ? "Ya tengo una cuenta"
    : "Crear una cuenta";
  $("#forgot-link").classList.toggle("hidden", reg);
  $("#auth-error").textContent = "";
};
$("#toggle-password").onclick = () => {
  const p = $("#auth-password");
  p.type = p.type === "password" ? "text" : "password";
  $("#toggle-password").textContent =
    p.type === "password" ? "Mostrar" : "Ocultar";
};
$("#forgot-link").onclick = () =>
  toast(
    "La recuperación por correo requiere configurar un servicio de envío. Contacta al administrador de esta instalación.",
  );
async function logout() {
  await api("/auth/logout", { method: "POST" });
  state.user = null;
  state.work = null;
  state.data = null;
  screen("login");
}
$("#works-logout").onclick = logout;
$("#logout-btn").onclick = logout;
$("#account-btn").onclick = () =>
  openModal(
    "Mi cuenta",
    /* HTML */ `<div class="detail-grid">
        <div class="detail-item"><b>Nombre</b>${esc(state.user.name)}</div>
        <div class="detail-item"><b>Correo</b>${esc(state.user.email)}</div>
      </div>
      <div class="form-footer">
        <button class="btn btn-secondary" data-action="close-modal">
          Cerrar
        </button>
      </div>`,
    "PERFIL",
  );
$("#create-work-btn").onclick = addWorkForm;
$("#empty-create").onclick = addWorkForm;
$("#summary-material-btn").onclick = () => materialForm();
$("#plan-material-btn").onclick = () => materialForm();
$("#summary-supplier-btn").onclick = () => supplierForm();
$("#add-supplier-btn").onclick = () => supplierForm();
$("#plan-quote-btn").onclick = () => quoteForm();
$("#add-quote-btn").onclick = () => quoteForm();
$("#quote-empty-button").onclick = () => quoteForm();
$("#full-plan-btn").onclick = () => route("plan");
$("#directory-btn").onclick = () => route("hardware");
$("#stage-select").onchange = async (e) => {
  try {
    await api(`/works/${encodeURIComponent(workId())}`, {
      method: "PATCH",
      body: JSON.stringify({ stage: e.target.value }),
    });
    state.work.stage = e.target.value;
    await refresh();
    toast("Etapa actualizada");
  } catch (err) {
    errorText(err);
  }
};
$("#project-select").onchange = async (e) => {
  state.work = state.works.find((w) => w.id === e.target.value);
  try {
    await enterWork(e.target.value);
  } catch (err) {
    errorText(err);
  }
};
$("#material-search").oninput = renderMaterials;
["#stage-filter", "#status-filter", "#date-filter"].forEach(
  (s) => ($(s).onchange = renderMaterials),
);
$("#supplier-search").oninput = renderSuppliers;
$("#city-filter").onchange = renderSuppliers;
["#quote-status-filter", "#quote-supplier-filter"].forEach(
  (s) => ($(s).onchange = renderQuotes),
);
$$(".tab").forEach(
  (t) =>
    (t.onclick = () => {
      state.quoteTab = t.dataset.tab;
      $$(".tab").forEach((x) => x.classList.toggle("active", x === t));
      renderQuotes();
    }),
);
document.addEventListener("click", async (e) => {
  const button = e.target.closest("button,a");
  if (!button) return;
  if (button.dataset.route) {
    e.preventDefault();
    route(button.dataset.route);
    return;
  }
  const action = button.dataset.action,
    id = button.dataset.id;
  if (!action) return;
  try {
    const { materials, suppliers, quotes } = joined();
    if (action === "close-modal") closeModal();
    else if (action === "open-work") await enterWork(id);
    else if (action === "edit-work") {
      state.work = state.works.find((w) => w.id === id);
      editWorkForm();
    } else if (action === "material-detail")
      materialDetail(materials.find((m) => m.id === id));
    else if (action === "edit-material")
      materialForm(materials.find((m) => m.id === id));
    else if (action === "supplier-detail")
      supplierDetail(suppliers.find((s) => s.id === id));
    else if (action === "quote-detail")
      quoteDetail(quotes.find((q) => q.id === id));
    else if (action === "add-material") {
      window.WorkWelcome?.stop();
      materialForm();
    } else if (action === "add-supplier") {
      window.WorkWelcome?.stop();
      supplierForm();
    } else if (action === "guide-quote") {
      window.WorkWelcome?.stop();
      quoteForm();
    } else if (action === "guide-followup") {
      window.WorkWelcome?.stop();
      route("quotes");
    } else if (action === "quote-material") {
      closeModal();
      quoteForm("", id);
    } else if (action === "quote-supplier") {
      closeModal();
      quoteForm(id);
    } else if (action === "edit-supplier")
      supplierForm(suppliers.find((s) => s.id === id));
    else if (action === "material-status")
      statusForm(materials.find((m) => m.id === id));
    else if (action === "delete-supplier") {
      const s = suppliers.find((x) => x.id === id);
      openModal(
        "¿Eliminar ferretería?",
        /* HTML */ `<p>
            Eliminarás <b>${esc(s.name)}</b> del directorio. No podrás hacerlo
            si tiene cotizaciones vinculadas.
          </p>
          <div class="form-footer">
            <button class="btn btn-secondary" data-action="close-modal">
              Cancelar</button
            ><button
              class="btn btn-danger"
              data-action="confirm-delete-supplier"
              data-id="${esc(id)}"
            >
              Eliminar
            </button>
          </div>`,
        "CONFIRMAR ACCIÓN",
      );
    } else if (action === "confirm-delete-supplier") {
      await api(
        `/works/${encodeURIComponent(workId())}/suppliers/${encodeURIComponent(id)}`,
        { method: "DELETE" },
      );
      closeModal();
      await refresh();
      toast("Ferretería eliminada");
    } else if (action.startsWith("quote-")) {
      const q = quotes.find((x) => x.id === id),
        next = {
          "quote-order": "Pedido realizado",
          "quote-reject": "Rechazada",
          "quote-transit": "En tránsito",
          "quote-received": "Recibido",
        }[action];
      if (next) {
        await api(
          `/works/${encodeURIComponent(workId())}/quotes/${encodeURIComponent(id)}/status`,
          { method: "PATCH", body: JSON.stringify({ status: next }) },
        );
        await refresh();
        toast("Estado de la cotización actualizado");
      }
    }
  } catch (err) {
    errorText(err);
  }
});
$("#modal-close").onclick = closeModal;
$("#modal-backdrop").addEventListener("click", (e) => {
  if (e.target === $("#modal-backdrop")) closeModal();
});
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeModal();
});
document.addEventListener("submit", async (e) => {
  const f = e.target;
  if (
    ![
      "work-form",
      "material-form",
      "supplier-form",
      "quote-form",
      "material-status-form",
    ].includes(f.id)
  )
    return;
  e.preventDefault();
  const b = formData(f);
  try {
    if (f.id === "work-form") {
      if (state.work && b.stage) {
        const d = await api(`/works/${encodeURIComponent(workId())}`, {
          method: "PATCH",
          body: JSON.stringify(b),
        });
        state.work = d.work;
        state.works = state.works.map((w) =>
          w.id === state.work.id ? d.work : w,
        );
        closeModal();
        await enterWork(state.work.id);
        toast("Obra actualizada");
      } else {
        await api("/works", { method: "POST", body: JSON.stringify(b) });
        closeModal();
        await loadWorks();
        toast("Obra creada");
      }
    } else if (f.id === "material-form") {
      if (f.dataset.id)
        await api(
          `/works/${encodeURIComponent(workId())}/materials/${encodeURIComponent(f.dataset.id)}`,
          { method: "PATCH", body: JSON.stringify(b) },
        );
      else
        await api(`/works/${encodeURIComponent(workId())}/materials`, {
          method: "POST",
          body: JSON.stringify(b),
        });
      closeModal();
      await refresh();
      toast("Material guardado");
    } else if (f.id === "supplier-form") {
      if (f.dataset.id)
        await api(
          `/works/${encodeURIComponent(workId())}/suppliers/${encodeURIComponent(f.dataset.id)}`,
          { method: "PATCH", body: JSON.stringify(b) },
        );
      else
        await api(`/works/${encodeURIComponent(workId())}/suppliers`, {
          method: "POST",
          body: JSON.stringify(b),
        });
      closeModal();
      await refresh();
      toast("Ferretería guardada");
    } else if (f.id === "quote-form") {
      await api(`/works/${encodeURIComponent(workId())}/quotes`, {
        method: "POST",
        body: JSON.stringify(b),
      });
      closeModal();
      await refresh();
      toast("Cotización registrada");
    } else if (f.id === "material-status-form") {
      await api(
        `/works/${encodeURIComponent(workId())}/materials/${encodeURIComponent(b.id)}/status`,
        { method: "PATCH", body: JSON.stringify({ status: b.status }) },
      );
      closeModal();
      await refresh();
      toast("Estado actualizado");
    }
  } catch (err) {
    const old = $("#form-error", f);
    if (old) old.textContent = err.message;
    else errorText(err);
  }
});
async function init() {
  try {
    const d = await api("/auth/me");
    state.user = d.user;
    await loadWorks();
  } catch {
    screen("login");
  }
}
init();
