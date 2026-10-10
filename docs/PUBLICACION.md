# Publicación en GitHub y aplicación en línea

## Qué se publica

Código, requisitos, documentación, catálogo y notas documentales. No se publica data/, output/, claves, configuración local de integraciones, entornos virtuales ni clases compiladas. config/integrations.example.json permite configurar otra instalación sin compartir rutas de este equipo.

El repositorio puede contener un commit inicial bajo la identidad Git del propietario, con la fecha real de publicación. Publicar como proyecto propio no requiere inventar historial, pruebas o atribuciones. La bitácora conserva las decisiones y los límites existentes.

## Vercel: situación comprobada el 10 de octubre de 2026

[Vercel Container Runtime](https://vercel.com/docs/functions/runtimes/container) está en beta y puede construir una imagen desde Dockerfile.vercel para ejecutar un servidor HTTP de cualquier lenguaje. Por tanto Java no impide por sí solo utilizar Vercel. Sus funciones escalan y se apagan; los archivos locales no son una base persistente compartida.

La aplicación actual escucha en 127.0.0.1:8080, guarda JSON local y mantiene sesiones/cache de análisis en memoria. Para un despliegue funcional estable hacen falta: contenedor, escucha/puerto compatibles, almacenamiento persistente externo y estrategia de sesiones/ejecuciones para varias instancias. La clave Gemini se configura exclusivamente en el servidor; el archivo cifrado Windows no se traslada al hosting. El vault de este computador tampoco está disponible en la nube.

Publicar solo index.html en Vercel mostraría la interfaz, pero no resolvería login, formularios ni agentes. No debe etiquetarse como aplicación completa. Tampoco debe prometerse persistencia usando /tmp. Referencia: [runtimes y filesystem](https://vercel.com/docs/functions/runtimes).

## Alternativa con servidor Java continuo

Una plataforma con contenedores y un volumen persistente permite conservar el backend y su JSON con menos cambios. [Render Free](https://render.com/docs/free) permite demostraciones, pero duerme por inactividad y no ofrece disco persistente; las escrituras se pierden al reiniciar/recrear la instancia. Un servicio así solo sirve como demo de datos descartables claramente identificada. No es respaldo ni alojamiento permanente de adquisiciones.

Para conservar registros: servidor con disco persistente, o migración a una base externa. No se ha contratado hosting ni habilitado facturación. Deben comprobarse cuotas/condiciones de cualquier plan antes de desplegar.

## URL en el repositorio

Después de verificar login, guardar/leer datos, reinicio, análisis IA y permisos en el despliegue elegido, coloca su URL en el README y en GitHub → About → Website. Hasta entonces el estado es ejecución local comprobada, despliegue público pendiente.
