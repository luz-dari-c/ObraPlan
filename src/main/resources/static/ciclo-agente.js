/* Estado observable del ciclo; consultar esta pantalla no llama al modelo. */
let cicloActual = null;
async function cargarCicloAgente() {
  const obra = workId();
  if (!obra) return;
  try {
    const ciclo = await api(`/works/${encodeURIComponent(obra)}/agent-lifecycle`);
    if (obra !== workId()) return;
    cicloActual = ciclo;
    $('#ciclo-activar').checked = ciclo.enabled;
    $('#ciclo-activar').disabled = !ciclo.configured && !ciclo.enabled;
    $('#ciclo-aviso').textContent = ciclo.notice + (!ciclo.configured ? ' Falta configurar Gemini; no se ejecutan llamadas.' : '');
    $('#ciclo-memoria').innerHTML = ciclo.memory.recuerdos.length
      ? ciclo.memory.recuerdos.map(r => `<article class="knowledge-source"><strong>${esc(r.trigger)}</strong><p class="minor">${esc(new Date(r.createdAt).toLocaleString('es-CO'))} · ${esc(r.model)}</p><p>${esc(r.resumen)}</p></article>`).join('')
      : '<p class="minor">Todavía no hay recuerdos. Una consulta completa guardará su resumen automáticamente como antecedente, sin cambiar pedidos.</p>';
    $('#ciclo-ejecuciones').innerHTML = ciclo.jobs.length
      ? ciclo.jobs.map(j => `<article class="knowledge-source"><strong>${esc(j.trigger)} · ${esc(j.status)}</strong><p class="minor">${esc(new Date(j.createdAt).toLocaleString('es-CO'))}</p><p>${esc(j.error || j.result?.answer || 'Esperando una ejecución disponible. Si el servidor está dormido, continúa al despertar.')}</p>${j.result?.ciclo ? `<p class="minor">Memorias recuperadas: ${j.result.ciclo.memoriasRecuperadas} · Memoria guardada: ${j.result.ciclo.memoriaGuardada ? 'sí' : 'no'}</p>` : ''}</article>`).join('')
      : '<p class="minor">No hay eventos. Activa el seguimiento para iniciar una revisión y luego registra un material o actualiza una cotización.</p>';
  } catch (error) { if (obra === workId()) $('#ciclo-aviso').textContent = error.message; }
}

$('#ciclo-activar').addEventListener('change', async event => {
  const obra = workId();
  const activar = event.target.checked;
  event.target.disabled = true;
  try {
    await api(`/works/${encodeURIComponent(obra)}/agent-lifecycle`, {method:'PATCH', body: JSON.stringify({enabled: activar})});
  } catch (error) { toast(error.message); }
  finally { await cargarCicloAgente(); }
});

setInterval(() => {
  if (!document.hidden && state.user && state.route === 'knowledge') cargarCicloAgente();
}, 15000);
