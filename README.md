# Instrucciones para levantar y probar la solución

## Requisitos previos
- Docker y Docker Compose instalados

## Levantar toda la solución

1. Abre una terminal en la raíz del proyecto (donde está el archivo `docker-compose.yml`).
2. Ejecuta:

   ```sh
   docker compose up --build
   ```

Esto levantará:
- PostgreSQL en el puerto 5432
- Backend Spring Boot en el puerto 8080
- Frontend Vite/React en el puerto 5173

## Acceso a la aplicación
- Frontend: http://localhost:5173
- Backend (API): http://localhost:8080
- Base de datos: localhost:5432 (usuario: postgres, contraseña: root, base: PruebaTecnica)

## Notas
- Si tienes una base de datos PostgreSQL externa, puedes seguir usándola cambiando la configuración en `application.properties`.
- Para detener los servicios, presiona `Ctrl+C` en la terminal y luego ejecuta:
  ```sh
  docker compose down
  ```
