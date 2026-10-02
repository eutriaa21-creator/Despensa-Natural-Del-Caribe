# Despensa natural del caribe · Frontend

Panel de control de vencimientos hecho con HTML, CSS y JavaScript nativos. No añade frameworks ni un paso de compilación.

## Funciones

- Primera configuración con creación de la cuenta inicial y acceso posterior con usuario y contraseña.
- Panel de productos, existencias por producto, lotes y alertas configurables de 30, 60 o 90 días.
- Formularios para crear y editar laboratorios, productos y lotes, y para añadir presentaciones.
- La creación de un producto guarda su primera presentación dentro de la misma transacción.
- Búsqueda de productos, filtro por laboratorio y búsqueda de lotes.
- Apartados separados para productos, laboratorios y detalle de lotes.
- Exportación CSV de productos o lotes filtrados y vista de impresión.
- Sesión con cookie `HttpOnly`, cierre de sesión y aviso si la sesión expira.
- Diseño adaptable, animaciones sutiles y opción de movimiento reducido.

## Conectar con el backend

1. Inicia el backend Spring Boot y MySQL.
2. Sirve esta carpeta desde un servidor estático local, por ejemplo `http://localhost:5500`. No abras el HTML desde `file://`.
3. La URL de la API está en `config.js` (por defecto `http://localhost:8080`).
4. CORS admite por defecto `localhost:5500`, `127.0.0.1:5500` y `localhost:3000`. Para otro origen, define `APP_CORS_ALLOWED_ORIGINS` en el backend, separado por comas.

La primera persona que abre un sistema sin usuarios puede crear la cuenta inicial. Después, el registro público queda cerrado y los apartados de inventario requieren iniciar sesión. Para Vercel, aloja el backend por separado, configura `API_BASE_URL` en `config.js`, permite el dominio en `APP_CORS_ALLOWED_ORIGINS` y configura `APP_SESSION_COOKIE_SAME_SITE=none` junto con `APP_SESSION_COOKIE_SECURE=true` para HTTPS entre dominios.

Todas las rutas de inventario requieren una sesión. Se pueden crear y editar laboratorios, productos, presentaciones y lotes. Para iniciar, registra un laboratorio; después crea el producto con su presentación inicial y registra el lote con cantidad y vencimiento. El icono de papelera en cada producto permite eliminarlo con sus presentaciones y lotes; pide confirmación y la acción es permanente.
