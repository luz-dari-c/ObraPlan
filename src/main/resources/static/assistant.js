let assistantRun = null;
let assistantWork = null;
let assistantRequest = 0;
let assistantBusy = false;
let assistantConfigured = false;
let assistantCooldownUntil = 0;
let assistantCooldownTimer = null;

function updateAssistantButton() {
  const remaining = Math.max(
    0,
    Math.ceil((assistantCooldownUntil - Date.now()) / 1000),
  );
  $("#assistant-submit").disabled =
    !assistantConfigured || assistantBusy || remaining > 0;
  $("#assistant-submit").textContent =
    remaining > 0 ? `Esperar ${remaining} s` : "Consultar agentes";
  if (remaining === 0 && assistantCooldownTimer !== null) {
    clearInterval(assistantCooldownTimer);
    assistantCooldownTimer = null;
  }
}

function waitBeforeAssistantRetry() {
  assistantCooldownUntil = Date.now() + 15000;
  if (assistantCooldownTimer !== null) clearInterval(assistantCooldownTimer);
  assistantCooldownTimer = setInterval(updateAssistantButton, 250);
  updateAssistantButton();
}

function resetAssistantWork() {
  const activeWork = workId();
  if (assistantWork !== activeWork) {
    assistantRequest++;
    assistantWork = activeWork;
    assistantRun = null;
    assistantBusy = false;
    $("#assistant-result").classList.add("hidden");
    $("#assistant-progress").textContent = "";
    $("#assistant-question").value = "";
  }
}

async function renderAssistantStatus() {
  const activeWork = workId();
  resetAssistantWork();
  try {
    const status = await api("/assistant/status");
    if (activeWork !== workId()) return;
    $("#assistant-status").textContent = status.configured
      ? `${status.model} configurado · hasta ${status.maxCalls} llamadas por análisis · ${status.callsToday}/${status.dailyCallLimit} llamadas desde el inicio de hoy. La conexión se comprueba al consultar.`
      : "Gemini pendiente de configuración. La aplicación y la búsqueda local siguen disponibles.";
    $("#assistant-setup").open = !status.configured;
    assistantConfigured = status.configured;
    updateAssistantButton();
    const rules = state.data?.analysis.agents || [];
    $("#knowledge-agents").innerHTML = [...status.agents, ...rules]
      .map(
        (agent) =>
          /* HTML */ ` <article class="agent-card">
            <b>${esc(agent.name)}</b>
            <p>${esc(agent.role)}</p>
            <span class="minor">${esc(agent.mode)}</span>
            <button
              class="text-action"
              data-knowledge-node="${esc(agent.source)}"
            >
              Ver sus relaciones ↗
            </button>
          </article>`,
      )
      .join("");
  } catch (error) {
    if (activeWork === workId())
      $("#assistant-status").textContent = error.message;
  }
}

document.querySelectorAll("[data-assistant-question]").forEach((button) => {
  button.addEventListener("click", () => {
    $("#assistant-question").value = button.dataset.assistantQuestion;
    $("#assistant-question").focus();
  });
});

$("#assistant-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  if (assistantBusy || Date.now() < assistantCooldownUntil) return;
  const activeWork = workId();
  const request = ++assistantRequest;
  assistantBusy = true;
  assistantRun = null;
  $("#assistant-result").classList.add("hidden");
  $("#assistant-submit").disabled = true;
  $("#assistant-progress").textContent =
    "Los agentes están consultando la obra. El análisis puede tardar hasta unos dos minutos…";
  try {
    const result = await api(
      `/works/${encodeURIComponent(activeWork)}/assistant`,
      {
        method: "POST",
        body: JSON.stringify({ question: $("#assistant-question").value }),
      },
    );
    if (request !== assistantRequest || activeWork !== workId()) return;
    assistantRun = result;
    $("#assistant-answer").textContent = result.answer;
    $("#assistant-usage").textContent = result.cached
      ? "Resultado reutilizado: esta consulta no hizo llamadas nuevas al modelo."
      : `${result.callsThisRequest} llamadas al modelo · ${result.usage.totalTokenCount ?? "no informado"} tokens reportados por Gemini · ${result.complete ? "análisis terminado" : "análisis incompleto"}.`;
    const names = {
      coordinador: "Coordinador",
      logistica: "Logística y seguimiento",
      costos: "Costos",
      conocimiento: "Conocimiento",
    };
    $("#assistant-trace").innerHTML = result.trace
      .map(
        (step) =>
          /* HTML */ ` <li>
            <b>${esc(names[step.agent] || step.agent)}</b>:
            ${step.event === "model"
              ? "consulta al modelo"
              : `herramienta ${esc(step.tool)}`}
          </li>`,
      )
      .join("");
    $("#assistant-sources").innerHTML = result.sources
      .map(
        (source) =>
          /* HTML */ ` <li>
            <button
              type="button"
              class="text-action"
              data-knowledge-node="${esc(source.id)}"
            >
              ${esc(source.ref || source.id)} · ${esc(source.title)}
            </button>
          </li>`,
      )
      .join("");
    $("#assistant-save").disabled = !result.complete;
    $("#assistant-result").classList.remove("hidden");
    $("#assistant-progress").textContent = result.complete
      ? "Análisis disponible."
      : result.stopReason === "output"
        ? "El modelo no terminó de redactar la respuesta. Los informes parciales siguen disponibles."
        : result.stopReason === "context_changed"
          ? "Los datos cambiaron durante la consulta. Ejecuta un análisis nuevo."
          : "El análisis se detuvo antes de terminar. Consulta el motivo y los informes parciales.";
  } catch (error) {
    if (request === assistantRequest && activeWork === workId()) {
      $("#assistant-progress").textContent = error.message;
      waitBeforeAssistantRetry();
    }
  } finally {
    if (request === assistantRequest && activeWork === workId()) {
      assistantBusy = false;
      await renderAssistantStatus();
    }
  }
});

$("#assistant-save").addEventListener("click", async (event) => {
  if (!assistantRun || assistantWork !== workId()) return;
  const activeWork = workId();
  event.target.disabled = true;
  try {
    const result = await api(
      `/works/${encodeURIComponent(activeWork)}/assistant/memory`,
      {
        method: "POST",
        body: JSON.stringify({ runId: assistantRun.runId }),
      },
    );
    if (activeWork !== workId()) return;
    $("#assistant-progress").textContent =
      result.message ||
      (result.published
        ? "Propuesta conservada en el grafo y publicada en Obsidian."
        : "Propuesta conservada en la memoria local.");
    await renderKnowledge();
  } catch (error) {
    if (activeWork === workId()) {
      $("#assistant-progress").textContent = error.message;
      event.target.disabled = false;
    }
  }
});
