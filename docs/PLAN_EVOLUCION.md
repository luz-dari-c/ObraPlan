# Plan de evolución de ObraPlan

Inicio: 5 de noviembre de 2026. Solicitud: reestructurar backend, persistencia, agentes, diseño y publicación, incorporando catálogos públicos.

## Etapas y aceptación

1. Diagnóstico y conservación. Revisar componentes y registrar decisiones. No reemplazar la base del usuario ni publicar credenciales.
2. Persistencia. Separar almacenamiento del servicio; incorporar PostgreSQL transaccional y migración explícita desde JSON con comprobación de integridad. Las sesiones deben caducar y sobrevivir al reinicio.
3. Backend y seguridad. Extraer autenticación/configuración; nombres nuevos en español; conservar contratos HTTP y reglas de costo, autorización y seguimiento. Preparar escucha pública y cookies HTTPS.
4. Agentes. Instrucciones independientes, contexto temporal y comercial, límites y fuentes. Conservar análisis confirmados en almacenamiento durable y restaurarlos tras reiniciar.
5. Catálogos. Integración de lectura con proveedores explícitos, límites y procedencia. Mostrar precio observado, unidad y fecha; confirmar antes de crear una cotización. Sin scraping arbitrario de direcciones ni compras automáticas.
6. Interfaz. Eliminar texturas, fuentes remotas y decoración innecesaria. Mantener jerarquía, contraste, formularios y navegación móvil. Catálogo accesible desde una obra.
7. Comprobaciones. Regresión de reglas, HTTP, PostgreSQL real, reinicio, concurrencia, aislamiento y recorrido visual. Evidencia bajo output/verification.
8. Entrega. Documentación, grafo y GitHub. Contenedor y configuración Render. Despliegue público solo se marca completo con URL funcional verificada; falta de sesión/cuenta se registra como bloqueo externo, no como despliegue realizado.

## Decisiones

- PostgreSQL como almacenamiento de producción; JSON queda compatible para desarrollo y migración. No se trasladan datos personales a un proveedor sin un destino configurado.
- Un único servicio sirve interfaz y API. Evita cookies entre dominios, CORS y dos despliegues descoordinados; el frontend puede separarse posteriormente si existe necesidad real.
- Render gratuito no conserva disco y su PostgreSQL gratuito caduca a los 30 días. Se necesita PostgreSQL externo durable (por ejemplo Neon) para una demo gratuita sostenida. No contratar planes ni fingir permanencia ilimitada de una cuota gratuita.
- La API existente se conserva para evitar romper formularios y pruebas durante el cambio de almacenamiento. Las nuevas capas llevan nombres y documentación en español.
- Catálogos son referencias comerciales: no inventan stock, flete, equivalencia de unidades ni precio final.

Fuentes operativas consultadas: https://render.com/docs/free y https://neon.com/blog/neon-free-plan-1-gb-per-project.

## Estado

En ejecución. Las comprobaciones y limitaciones finales se registrarán en VERIFICACION.md y BITACORA.md.
