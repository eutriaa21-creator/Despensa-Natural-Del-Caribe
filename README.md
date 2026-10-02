# Despensa natural del caribe

Aplicación web local **Despensa natural del caribe** para administrar productos, laboratorios, presentaciones y lotes, consultar existencias y revisar productos próximos a vencer. La interfaz está hecha con HTML, CSS y JavaScript nativos; la API usa Java 17, Spring Boot, Maven y MySQL. La interfaz conserva el icono de hoja del logo existente.

## Contenido

- `backend/`: API REST, autenticación y persistencia con MySQL.
- `frontend/`: interfaz web estática; no necesita Node.js ni un paso de compilación.
- `vercel-test/`: página mínima para comprobar un despliegue estático de Vercel.

## Requisitos

- Java 17 o posterior.
- MySQL 8 o compatible, iniciado localmente o accesible por red.
- Un navegador moderno.
- Para servir el frontend en local: Python 3, Node.js, o una extensión de servidor estático de tu editor.

No es necesario instalar Maven globalmente: el proyecto incluye Maven Wrapper (`backend/mvnw.cmd`).

## 1. Configurar la base de datos

Crea una base de datos vacía en MySQL, por ejemplo:

```sql
CREATE DATABASE control_vencimientos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

La aplicación genera o actualiza sus tablas al arrancar. Configura las credenciales mediante variables de entorno; la contraseña vacía que aparece como valor predeterminado en desarrollo solo funciona si tu instalación local de MySQL la permite.

En PowerShell, antes de iniciar el backend:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/control_vencimientos?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CLAVE_DE_MYSQL"
```

Usa el usuario y contraseña de MySQL de tu propio equipo. No escribas credenciales reales en archivos que vayas a compartir o incluir en el ZIP.

## 2. Iniciar el backend

Abre PowerShell en la carpeta `backend/` y ejecuta:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

El servidor queda disponible en `http://localhost:8080`. Mantén esta ventana abierta mientras uses la aplicación. Si quieres compilar sin iniciarlo, ejecuta `.\mvnw.cmd clean package` desde `backend/`.

La configuración principal está en `backend/src/main/resources/application.properties`. La API toma estos valores de entorno:

| Variable | Uso | Valor predeterminado |
| --- | --- | --- |
| `DB_URL` | Dirección JDBC de MySQL | Base `control_vencimientos` en `localhost:3306` |
| `DB_USERNAME` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | Vacía |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes adicionales permitidos por CORS, separados por comas | El código también incluye `localhost:5500`, `127.0.0.1:5500` y `localhost:3000` |
| `APP_SESSION_COOKIE_SAME_SITE` | Política SameSite de la cookie de sesión | `lax` |
| `APP_SESSION_COOKIE_SECURE` | Exigir HTTPS para la cookie | `false` |
| `APP_CONSOLE_ENABLED` | Activar el menú antiguo de consola | `false` |

## 3. Iniciar el frontend

El frontend es estático. **No abras `frontend/index.html` con doble clic**: la URL `file://` tiene un origen `null`, y el navegador bloquea las llamadas a la API por CORS. Inicia un servidor local desde la carpeta raíz del proyecto:

**Con Python:**

```powershell
cd frontend
python -m http.server 5500
```

**O con Node.js:**

```powershell
cd frontend
npx serve -l 5500
```

Abre `http://localhost:5500`. En la instalación sin un frontend publicado, la URL de la API debe ser `http://localhost:8080`; se configura en `frontend/config.js`. Si cambias el puerto o el origen del frontend, agrega el origen exacto a `APP_CORS_ALLOWED_ORIGINS` y reinicia el backend.

La interfaz requiere que estén activos tanto el servidor frontend como el backend y MySQL. Al terminar, puedes detener cada servidor con `Ctrl+C` en su ventana de PowerShell.

## Ejecutarlo desde Visual Studio Code en Windows

1. Abre en VS Code la carpeta principal del proyecto (`control-vencimientos UV`).
2. Abre **Terminal → Nueva terminal**. Si la terminal no es PowerShell, usa el menú junto al botón `+` para elegir **PowerShell**.
3. Antes de iniciar el backend, inicia MySQL. Puedes hacerlo desde **Servicios** de Windows o abrir MySQL Workbench y conectar con tu servidor local.
4. En la primera terminal integrada, ejecuta:

   ```powershell
   cd backend
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "TU_CLAVE_DE_MYSQL"
   .\mvnw.cmd spring-boot:run
   ```

   Sustituye `TU_CLAVE_DE_MYSQL` por la contraseña del usuario `root` que configuraste al instalar MySQL. Esa contraseña pertenece a MySQL y **no** es la contraseña de inicio de sesión de esta aplicación. Si tu MySQL local permite que `root` se conecte sin contraseña, omite la línea `$env:DB_PASSWORD = ...`. Si no recuerdas haber configurado una contraseña, prueba primero sin esa línea. Si aparece un error de acceso denegado, revisa la contraseña en tu administrador de MySQL o restablécela; el proyecto no puede recuperarla.

   La base predeterminada se llama `control_vencimientos`, usa el puerto `3306` y Spring Boot crea o actualiza las tablas al iniciar. Si esa base no existe, créala desde MySQL Workbench o ejecuta:

   ```sql
   CREATE DATABASE control_vencimientos
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```

5. Mantén abierta esa terminal. Abre otra desde **Terminal → Nueva terminal** o el botón `+`. Elige PowerShell y ejecuta:

   ```powershell
   cd frontend
   python -m http.server 5500
   ```

   Si PowerShell no encuentra `python`, prueba `py -m http.server 5500`. Mantén abierta también esta terminal.

6. En el navegador abre `http://localhost:5500`. No abras `frontend/index.html` con doble clic: usar una URL `file://` produce el error CORS de origen `null`.

Para apagar cada servidor, vuelve a su terminal y presiona `Ctrl+C`. Estas instrucciones no requieren instalar Node.js ni Maven globalmente; sí necesitas tener Java 17 y MySQL instalados.

## 4. Iniciar sesión y crear el inventario

No hay credenciales incluidas en el proyecto. La cuenta administradora se registra desde la pantalla de acceso en la primera ejecución sobre una base de datos sin usuarios. La base nueva no incluye registros de demostración ni cuentas preconfiguradas.

1. Arranca MySQL, el backend y el frontend.
2. Abre `http://localhost:5500`.
3. Si todavía no hay usuarios registrados, la pantalla permite crear la primera cuenta administradora. Guarda esa contraseña: no existe una clave compartida por defecto.
4. Inicia sesión. Si el inventario está vacío, crea primero un laboratorio.
5. Crea un producto y su presentación inicial; después registra un lote con cantidad y fecha de vencimiento.

Desde el panel puedes consultar existencias, buscar y filtrar productos y lotes, revisar alertas, editar datos y presentaciones, exportar CSV e imprimir. Cada producto tiene una acción con icono de papelera: solicita confirmación e informa si también borrará presentaciones, lotes y existencias relacionadas. La eliminación es permanente y no se puede deshacer. La sesión expira después de 30 minutos.

## 5. Ejecutar las pruebas del backend

Desde `backend/`:

```powershell
.\mvnw.cmd test
```

## Despliegue de prueba en Vercel

`vercel-test/` solo es una página estática para confirmar que Vercel sirve archivos HTML. Para publicar la aplicación frontend completa:

1. Crea un proyecto de Vercel con `frontend/` como directorio raíz y sin comando de build; el sitio es estático.
2. Publica el backend y MySQL en servicios accesibles desde internet: `localhost` en el navegador de cada visitante no apunta a tu computadora.
3. Cambia `API_BASE_URL` en `frontend/config.js` por la URL HTTPS pública del backend. El frontend ya no intenta contactar `localhost:8080` cuando se visita desde Vercel.
4. Configura `APP_CORS_ALLOWED_ORIGINS` en el backend con el origen exacto de Vercel, por ejemplo `https://tu-proyecto.vercel.app`.
5. Si frontend y backend usan HTTPS en dominios distintos, configura `APP_SESSION_COOKIE_SAME_SITE=none` y `APP_SESSION_COOKIE_SECURE=true` en el backend.

### Preparar y desplegar backend + MySQL en Render

El repositorio incluye `render.yaml` y `backend/Dockerfile` para publicar Spring Boot en el plan gratuito de Render, conectado a MySQL 8 Free de Aiven. Se mantienen las tecnologías del proyecto y no se configura almacenamiento pago. Aiven ofrece 1 GB de almacenamiento y no requiere tarjeta; puede apagar servicios gratuitos con poca actividad. Render Free duerme el backend después de 15 minutos sin tráfico, y el primer acceso posterior puede tardar cerca de un minuto. Es suficiente para pruebas con datos ficticios, no para datos importantes. [Límites gratuitos de Aiven](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier) y [límites gratuitos de Render](https://render.com/docs/free).

1. En Aiven crea un servicio **MySQL → Free**. Espera hasta que esté disponible y copia el host, puerto, nombre de base de datos, usuario y contraseña que aparecen en su consola. Usa solo datos de prueba.
2. En Render elige **New → Blueprint**, conecta el repositorio GitHub y selecciona la rama `main`. Confirma que el servicio `despensa-backend` use el plan **Free**. El Blueprint pedirá `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; introduce ahí las credenciales de Aiven. No las pongas en el repositorio.
3. Construye `DB_URL` con los datos de Aiven:

   ```text
   jdbc:mysql://HOST_AIVEN:PUERTO/BASE_AIVEN?sslMode=REQUIRED&serverTimezone=UTC
   ```

   Reemplaza los marcadores. Usa el usuario y contraseña entregados por Aiven para `DB_USERNAME` y `DB_PASSWORD`.
4. Copia la URL HTTPS pública del backend y úsala en `frontend/config.js`:

   ```js
   API_BASE_URL: isLocalDevelopment ? "http://localhost:8080" : "https://TU-BACKEND.onrender.com"
   ```

   Reemplaza `https://TU-BACKEND.onrender.com` con la URL asignada por Render. Guarda, crea el commit y haz `git push` a `main`; Vercel reconstruirá el frontend.
5. Comprueba `https://TU-BACKEND.onrender.com/auth/status`. Debe responder con JSON (por ejemplo `setupRequired: true` en una base nueva). Luego comprueba el registro inicial y las operaciones de inventario desde Vercel.

El Blueprint incluye el origen publicado de Vercel en `APP_CORS_ALLOWED_ORIGINS` y configura cookies seguras para HTTPS. Si cambia el dominio de Vercel, actualiza el valor de CORS en Render y vuelve a desplegar el backend. El frontend publicado no puede acceder a una API que solo esté en `localhost`.

Este proyecto guarda las sesiones en memoria, por lo que un reinicio del backend cierra las sesiones. Para evitar cargos, no agregues una tarjeta a las cuentas gratuitas; si se agota una cuota, el proveedor puede suspender el servicio. Usa únicamente información ficticia durante la prueba y exporta tus datos de inventario antes de eliminar la instancia MySQL.

No publiques el backend o la base de datos con credenciales personales, claves de prueba o datos reales. La configuración y seguridad del proveedor de base de datos deben cubrir el acceso remoto y proteger sus secretos.

## Solución de problemas

### “No se pudo conectar” o `ERR_CONNECTION_REFUSED`

Comprueba que MySQL y el backend siguen activos y que el backend escucha en el puerto `8080`. Abre `http://localhost:8080/auth/status` para comprobar que la API responde. Revisa también `API_BASE_URL` en `frontend/config.js`.

### Error CORS o un origen `null`

Sirve la carpeta `frontend/` en `http://localhost:5500`; no abras el archivo como `file://`. Si usas otro puerto u origen, inclúyelo en `APP_CORS_ALLOWED_ORIGINS` y reinicia el backend.

### Error de conexión a MySQL al iniciar Spring Boot

Comprueba que MySQL esté iniciado, que la base exista y que `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` correspondan a tu instalación. El proceso backend muestra el error de conexión en la consola.

### La cuenta inicial ya se registró

El registro de administrador solo está disponible mientras no haya usuarios en la base de datos. Usa la cuenta que creaste. No se incluye una contraseña fija de recuperación.

## Preparar y compartir un ZIP

Incluye el código fuente, los archivos de Maven Wrapper y la documentación. Excluye `target/`, `node_modules/`, `dist/`, `.vercel/`, `.env`, copias locales de bases de datos y cualquier archivo con credenciales o datos personales. El `.gitignore` del proyecto ya excluye las salidas habituales de compilación y los archivos locales de secretos.
