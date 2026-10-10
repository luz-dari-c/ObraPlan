let knowledgeState = null;

function knowledgeExcerpt(value) {
  return value
    .replace(/\r\n/g, "\n")
    .split("## Fuente")[0]
    .replace(/^#.*\n/gm, "")
    .trim();
}
let knowledgeRequest = 0;

async function renderKnowledge(question = "") {
  resetAssistantWork();
  const request = ++knowledgeRequest;
  const activeWork = workId();
  try {
    const result = await api(
      `/works/${encodeURIComponent(activeWork)}/knowledge?q=${encodeURIComponent(question)}`,
    );
    if (request !== knowledgeRequest || activeWork !== workId()) return;
    knowledgeState = result;
    const status = result.status;
    $("#knowledge-status").textContent = status.ok
      ? `${status.vaultConfigured ? "Obsidian conectado" : "Memoria local activa"} · ${result.nodeCount} notas · ${result.edgeCount} relaciones · Última actualización: ${status.lastSync ? new Date(status.lastSync).toLocaleString("es-CO") : "pendiente"}`
      : `Datos guardados. Sincronización pendiente: ${status.error}`;
    $("#knowledge-agents").innerHTML = (state.data.analysis.agents || [])
      .map(
        (agent) =>
          /* HTML */ `<article class="agent-card">
            <b>${esc(agent.name)}</b>
            <p>${esc(agent.role)}</p>
            <span class="minor">${esc(agent.mode)}</span
            ><button
              class="text-action"
              data-knowledge-node="${esc(agent.source)}"
            >
              Ver sus relaciones ↗
            </button>
          </article>`,
      )
      .join("");
    $("#knowledge-answer").textContent = result.answer;
    $("#knowledge-sources").innerHTML = result.sources
      .map(
        (source) =>
          /* HTML */ `<article class="knowledge-source">
            <span class="eyebrow">${esc(source.id)}</span>
            <h3>${esc(source.title)}</h3>
            <p>${esc(knowledgeExcerpt(source.excerpt))}</p>
            <div class="actions">
              <button
                class="text-action"
                data-knowledge-node="${esc(source.id)}"
              >
                Explorar relaciones</button
              >${source.obsidianUri
                ? /* HTML */ `<a class="link" href="${esc(source.obsidianUri)}"
                    >Abrir en Obsidian ↗</a
                  >`
                : ""}
            </div>
          </article>`,
      )
      .join("");
    const previous = $("#graph-node").value;
    $("#graph-node").innerHTML = result.nodes
      .map(
        (node) =>
          /* HTML */ `<option value="${esc(node.id)}">
            ${esc(node.title)}
          </option>`,
      )
      .join("");
    const chosen =
      question && result.sources.length
        ? result.sources[0].id
        : previous || "OP-Proyecto";
    $("#graph-node").value = result.nodes.some((node) => node.id === chosen)
      ? chosen
      : result.nodes[0]?.id;
    $("#graph-count").textContent =
      `${result.nodeCount} nodos y ${result.edgeCount} relaciones. Vista centrada en el nodo seleccionado.`;
    drawKnowledgeGraph($("#graph-node").value);
    await renderAssistantStatus();
  } catch (error) {
    $("#knowledge-status").textContent = error.message;
  }
}

function drawKnowledgeGraph(id) {
  if (!knowledgeState) return;
  const center = knowledgeState.nodes.find((node) => node.id === id);
  if (!center) return;
  const edges = knowledgeState.edges.filter(
    (edge) => edge.source === id || edge.target === id,
  );
  const neighbors = [
    ...new Set(
      edges.map((edge) => (edge.source === id ? edge.target : edge.source)),
    ),
  ];
  const nodes = neighbors
    .slice(0, 16)
    .map((nodeId) => knowledgeState.nodes.find((node) => node.id === nodeId))
    .filter(Boolean);
  const points = new Map([[id, { x: 400, y: 270 }]]);
  nodes.forEach((node, index) => {
    const angle =
      (index / Math.max(nodes.length, 1)) * Math.PI * 2 - Math.PI / 2;
    points.set(node.id, {
      x: 400 + Math.cos(angle) * 265,
      y: 270 + Math.sin(angle) * 205,
    });
  });
  const lines = nodes
    .map((node) => {
      const point = points.get(node.id);
      return /* HTML */ `<line
        x1="400"
        y1="270"
        x2="${point.x}"
        y2="${point.y}"
        stroke="var(--line-dark)"
        stroke-width="1.4"
      />`;
    })
    .join("");
  const circles = [center, ...nodes]
    .map((node) => {
      const point = points.get(node.id);
      const main = node.id === id;
      const label =
        node.title.length > 29 ? node.title.slice(0, 27) + "…" : node.title;
      return /* HTML */ `<g
        tabindex="0"
        role="button"
        aria-label="Explorar ${esc(node.title)}"
        data-knowledge-node="${esc(node.id)}"
        ><title>${esc(node.title)}</title
        ><circle
          cx="${point.x}"
          cy="${point.y}"
          r="${main ? 22 : 12}"
          fill="${main
            ? "var(--petrol)"
            : node.type === "agente"
              ? "var(--terra)"
              : "var(--moss)"}"
        /><text
          x="${point.x}"
          y="${point.y + (main ? 41 : 30)}"
          text-anchor="middle"
          fill="var(--ink)"
          font-size="14"
          >${esc(label)}</text
        ></g
      >`;
    })
    .join("");
  $("#knowledge-graph").innerHTML = /* HTML */ `<svg
    viewBox="0 0 800 560"
    role="img"
    aria-label="Relaciones de ${esc(center.title)}"
  >
    ${lines}${circles}
  </svg>`;
  $("#graph-detail").innerHTML = /* HTML */ `<span class="eyebrow"
      >${esc(center.type)}</span
    >
    <h3>${esc(center.title)}</h3>
    <p class="minor">${esc(center.id)}</p>
    ${center.obsidianUri
      ? /* HTML */ `<a class="link" href="${esc(center.obsidianUri)}"
          >Abrir nota en Obsidian ↗</a
        >`
      : ""}
    <ul>
      ${edges
        .map(
          (edge) =>
            /* HTML */ `<li>
              <span class="minor"
                >${esc(
                  edge.source === id
                    ? "→ " + edge.relation
                    : "← " + edge.relation,
                )}</span
              ><button
                class="text-action"
                data-knowledge-node="${esc(
                  edge.source === id ? edge.target : edge.source,
                )}"
              >
                ${esc(
                  knowledgeState.nodes.find(
                    (node) =>
                      node.id ===
                      (edge.source === id ? edge.target : edge.source),
                  )?.title || (edge.source === id ? edge.target : edge.source),
                )}
              </button>
            </li>`,
        )
        .join("")}
    </ul>
    ${neighbors.length > 16
      ? /* HTML */ `<p class="minor">
          El dibujo muestra 16 vecinos; la lista incluye todas las relaciones.
        </p>`
      : ""}`;
}

$("#knowledge-form").addEventListener("submit", (event) => {
  event.preventDefault();
  renderKnowledge($("#knowledge-question").value);
});
$("#graph-node").addEventListener("change", (event) =>
  drawKnowledgeGraph(event.target.value),
);
$("#knowledge-sync").addEventListener("click", async (event) => {
  event.target.disabled = true;
  try {
    await api(`/works/${encodeURIComponent(workId())}/knowledge/sync`, {
      method: "POST",
    });
    await renderKnowledge($("#knowledge-question").value);
  } catch (error) {
    errorText(error);
  } finally {
    event.target.disabled = false;
  }
});
function selectKnowledgeNode(event) {
  const target = event.target.closest("[data-knowledge-node]");
  if (!target) return;
  const id = target.dataset.knowledgeNode;
  if (!knowledgeState?.nodes.some((node) => node.id === id)) return;
  $("#graph-node").value = id;
  drawKnowledgeGraph(id);
}
document.addEventListener("click", selectKnowledgeNode);
document.addEventListener("keydown", (event) => {
  if (event.key === "Enter" && event.target.matches("g[data-knowledge-node]"))
    selectKnowledgeNode(event);
});
