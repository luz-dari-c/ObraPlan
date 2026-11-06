/* Referencias de tiendas públicas: se consultan a petición, nunca cambian pedidos. */
let productosCatalogo = [];
let consultaCatalogo = 0;

document.querySelector('#catalogo-form').addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const solicitud = ++consultaCatalogo;
  const obra = workId();
  const boton = document.querySelector('#catalogo-buscar');
  const mensaje = document.querySelector('#catalogo-estado');
  boton.disabled = true;
  mensaje.textContent = 'Consultando las tiendas. Puede tardar unos segundos…';
  try {
    const resultado = await api(`/works/${encodeURIComponent(obra)}/catalog?query=${encodeURIComponent(document.querySelector('#catalogo-consulta').value)}`);
    if (solicitud !== consultaCatalogo || obra !== workId()) return;
    productosCatalogo = resultado.products;
    mensaje.textContent = [resultado.notice, ...resultado.warnings].join(' ');
    document.querySelector('#catalogo-resultados').innerHTML = productosCatalogo.length
      ? productosCatalogo.map((producto, indice) => `<article class="producto-publico">
          <div><span class="minor">${esc(producto.supplier)}</span><h3>${esc(producto.name)}</h3></div>
          <strong>${producto.price == null ? 'Precio no publicado' : esc(new Intl.NumberFormat('es-CO', { style: 'currency', currency: producto.currency || 'COP' }).format(producto.price))}</strong>
          <p class="minor">${esc(producto.warning)}</p>
          <p class="minor">Consultado: ${esc(new Date(producto.observedAt).toLocaleString('es-CO'))}</p>
          <div class="actions"><a class="link" href="${esc(producto.url)}" target="_blank" rel="noopener noreferrer">Ver en la tienda ↗</a>
          ${producto.comparable ? `<button class="btn btn-secondary" data-preparar-cotizacion="${indice}">Preparar cotización</button>` : '<span class="minor">Requiere verificación manual</span>'}</div>
        </article>`).join('')
      : '<p class="empty">No hay productos disponibles para esta búsqueda. Prueba otro nombre o consulta directamente la tienda.</p>';
  } catch (error) {
    if (obra === workId()) mensaje.textContent = error.message;
  } finally { boton.disabled = false; }
});

document.querySelector('#catalogo-resultados').addEventListener('click', (evento) => {
  const boton = evento.target.closest('[data-preparar-cotizacion]');
  if (!boton) return;
  const producto = productosCatalogo[Number(boton.dataset.prepararCotizacion)];
  if (!producto?.comparable) return;
  // Abre el formulario existente: el usuario debe escoger proveedor y material equivalentes.
  document.querySelector('#add-quote-btn').click();
  const formulario = document.querySelector('#quote-form');
  if (!formulario) return;
  formulario.elements.unitPrice.value = producto.price;
  formulario.elements.notes.value = `Referencia pública: ${producto.name}. Tienda: ${producto.supplier}. Fuente: ${producto.url}. Consultado: ${producto.observedAt}. Verificar presentación, disponibilidad, impuestos y transporte antes de confirmar.`;
  const aviso = document.createElement('p');
  aviso.className = 'note';
  aviso.textContent = 'Confirma que el proveedor, la unidad y la presentación coincidan. Esta referencia no es una oferta confirmada por la ferretería.';
  formulario.prepend(aviso);
});
