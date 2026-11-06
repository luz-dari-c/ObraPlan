# Publicación y despliegue de ObraPlan

La aplicación está disponible en **https://obraplan.onrender.com**. Render sirve interfaz y API desde un contenedor Java 21; Neon conserva la base PostgreSQL fuera del disco temporal del servicio. Ambos recursos se crearon en planes gratuitos.

El repositorio público es https://github.com/luz-dari-c/ObraPlan. No incluye base personal, claves, respaldos ni configuración privada. Los registros previos se migraron al PostgreSQL privado; no se publicaron como archivos en GitHub.

El despliegue se realiza desde main con Dockerfile. Las variables DATABASE_URL, OBRAPLAN_ENV y GEMINI_API_KEY viven en el servidor. Vercel no es necesario para esta arquitectura: separar el frontend introduciría otro dominio y configuración de sesiones sin aportar una necesidad actual.

El plan gratuito puede suspender el cómputo por inactividad; los datos permanecen en Neon sujetos a sus cuotas y condiciones. No se promete disponibilidad garantizada ni capacidad de producción masiva. Consulta [OPERACION.md](OPERACION.md) para acceso, respaldo, mantenimiento y límites, y [VERIFICACION.md](VERIFICACION.md) para pruebas reales.
