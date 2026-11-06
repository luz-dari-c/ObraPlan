# Operación de ObraPlan

## Arquitectura y decisiones

El servicio Java sirve la interfaz y la API desde el mismo dominio. PostgreSQL externo conserva registros, sesiones con hash/caducidad y análisis confirmados. El disco de Render solo contiene proyecciones regenerables del conocimiento. `AlmacenPostgres` aplica transacciones y un bloqueo entre instancias; los documentos JSONB por registro conservan el contrato actual del dominio. No es todavía un esquema completamente normalizado ni una implementación para carga masiva.

## Publicación

- Aplicación: https://obraplan.onrender.com
- Backend e interfaz: Render, servicio ObraPlan, plan Free, región Virginia.
- Base: Neon, proyecto ObraPlan, plan Free, PostgreSQL 18, región Ohio.
- Frontend y backend se despliegan juntos con Docker; no hay un segundo frontend desactualizado.
- La primera visita después de inactividad puede tardar más de 50 segundos. Las cuotas gratuitas y los tiempos de arranque no son un SLA de producción.

Render: https://render.com/docs/free. Neon: https://neon.com/docs/introduction/plans.

## Credenciales y acceso

Las cuentas conservan sus correos. La cuenta histórica demo@obraplan.co contenía obras de uso real: su contraseña pública se revocó solo en la nube y se generó una nueva en config/acceso.local.json. También se revocaron sus sesiones previas. La otra cuenta mantiene su contraseña anterior. Una cuenta nueva puede crearse desde la página y solo accede a sus obras.

La conexión generada por Neon está en `config/database.local.txt` y las variables del servicio en `config/render.local.env`, ambos ignorados por Git. Contienen secretos: no adjuntarlos a correos, capturas ni issues. La consola Neon permite consultar rol/base y administrar credenciales. No se imprimen claves en README ni en logs.

`DATABASE_URL` es obligatoria en producción. `GEMINI_API_KEY` solo vive en el servidor; su facturación depende del proyecto Google. La cuota de llamadas del coordinador es por proceso: reiniciar la instancia reinicia ese contador, no la cuota de Google. No se garantiza gasto cero si se habilita facturación en el proveedor.

## Datos y migración

Se migró el archivo local a un destino PostgreSQL vacío mediante `MigrarDatos`. La base local original se conserva. La nube y el archivo local son ahora instalaciones independientes: no editar ambas esperando una sincronización bidireccional. Para trabajar con los mismos registros, utiliza la URL pública o inicia localmente con la misma `DATABASE_URL`.

La migración exige un destino vacío y no traslada sesiones antiguas. Un error revierte la transacción; no ejecutar importaciones sobre una base con trabajo nuevo. Las cantidades de registros y comprobaciones están en VERIFICACION.md.

## Respaldos y recuperación

La historia de restauración de Neon Free es limitada; no sustituye respaldos propios. Antes de cambios de datos: generar un `pg_dump --format=custom` con la conexión privada, guardarlo fuera de Git y comprobar la restauración en una base separada. Nunca restaurar sobre la base activa sin revisar qué registros nuevos se perderían. La copia JSON original permite recuperar el estado previo a la migración, no los cambios posteriores en la nube.

El servidor regenera notas de las obras desde PostgreSQL al arrancar y restaura propuestas confirmadas. Los aprendizajes locales editados en Obsidian no se suben automáticamente a Neon ni a Render. Esa memoria personal sigue en el vault autorizado del equipo; la nube dispone del catálogo documental y los registros/propuestas de cada obra.

## Catálogos y comparación

Ferretería en Galerías y Ferresmart ofrecen Store API pública WooCommerce. La aplicación consulta un máximo de ocho productos por proveedor y conserva resultados cinco minutos. Las consultas requieren sesión y acceso a la obra. No recibe URLs arbitrarias ni sigue redirecciones. Una tienda caída no impide mostrar la otra.

La moneda declarada se muestra sin corregirla por intuición. Ferresmart devolvió USD durante la comprobación: su precio no se prepara como COP. Los precios variables por presentación tampoco se preparan automáticamente. Incluso en COP, el usuario debe comprobar material, unidad, proveedor, transporte, impuestos y disponibilidad antes de guardar. No se confirman stock ni pedidos comerciales.

## Mantenimiento

`scripts/verify.ps1` ejecuta regresión con bases/vaults temporales. `PersistenciaVerification` exige una base PostgreSQL de pruebas vacía: no usarla en producción. `scripts/dependencies.ps1` descarga el controlador fijado y comprueba SHA-256. El contenedor lo verifica igualmente y ejecuta la aplicación sin usuario root.

El plan, las decisiones y sus límites están en PLAN_EVOLUCION.md. Las siguientes ampliaciones de escala deberán contemplar consultas por entidad, un pool de conexiones, un esquema relacional normalizado, auditoría de seguridad, monitorización y pruebas de carga antes de prometer capacidad de producción.
