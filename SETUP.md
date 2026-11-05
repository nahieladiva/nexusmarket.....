# Guía de instalación en Windows

Esta guía lleva NexusMarket desde cero hasta probarlo. Los comandos son para PowerShell.

## 1. Requisitos

| Programa | Para qué | Cómo comprobarlo |
|---|---|---|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Levantar la API, MySQL y MongoDB | `docker --version` |
| [Git](https://git-scm.com/download/win) | Clonar el repositorio | `git --version` |
| [Java 17](https://adoptium.net/) o superior | Solo para ejecutar las pruebas y el verificador | `java -version` |
| [Postman](https://www.postman.com/downloads/) | Opcional, para ejecutar la colección con interfaz | |

Para usar la aplicación solo hacen falta Docker Desktop y Git. No necesita instalar Maven: el proyecto trae
`mvnw.cmd`.

Abra Docker Desktop y espere a que indique que está en ejecución. Los puertos `8080`, `3306` y `27017` deben
estar libres.

## 2. Clonar el repositorio

```powershell
git clone https://github.com/nahieladiva/nexusmarket......git nexusmarket
cd nexusmarket
```

## 3. Levantar la aplicación

Desde la raíz del repositorio:

```powershell
docker compose up -d --build
```

La primera vez tarda varios minutos, porque descarga las imágenes y las dependencias. Después compruebe que
los tres contenedores están arriba:

```powershell
docker compose ps
```

Para seguir el arranque de la API:

```powershell
docker compose logs -f api
```

Está lista cuando aparece `Started NexusmarketApplication`. Salga de los logs con `Ctrl+C`; la aplicación
sigue corriendo.

## 4. Administrador inicial

Al arrancar se crea este usuario:

| Dato | Valor |
|---|---|
| Correo | `admin@nexusmarket.com` |
| Contraseña | `Admin2026` |

Se cambian con las variables de entorno `ADMIN_EMAIL` y `ADMIN_PASSWORD`, antes de crear la base de datos.

## 5. Probar con Swagger

1. Abra <http://localhost:8080/swagger-ui.html>.
2. Arriba a la derecha, en **Select a definition**, elija `public`.
3. Abra `POST /api/v1/public/login`, pulse **Try it out** y escriba:

   ```json
   {"email": "admin@nexusmarket.com", "password": "Admin2026"}
   ```

4. Pulse **Execute** y copie el valor de `token` de la respuesta.
5. Pulse **Authorize**, pegue el token sin escribir `Bearer` y confirme.
6. En **Select a definition** elija `admin` y pruebe, por ejemplo, `GET /api/v1/admin/users`.

Cada rol tiene su grupo. Para probar otro rol, inicie sesión con ese usuario y repita **Authorize**.

## 6. Probar con Postman

1. Abra Postman y pulse **Import**.
2. Importe los dos archivos de la carpeta `postman/`:
   `NexusMarket.postman_collection.json` y `NexusMarket.postman_environment.json`.
3. Arriba a la derecha, elija el environment **NexusMarket local**.
4. Abra la colección, pulse **Run** y ejecútala completa, en orden.

Cada petición guarda los tokens y los ids que usan las siguientes. Cada ejecución crea sus propios usuarios y
productos, así que se puede repetir tantas veces como quiera.

## 7. Probar con newman

newman ejecuta la colección sin abrir Postman y corre en Docker, así que no instala nada. Desde la raíz del
repositorio:

```powershell
docker run --rm -v "${PWD}/postman:/etc/newman" postman/newman:6-alpine run NexusMarket.postman_collection.json -e NexusMarket.postman_environment.json --env-var baseUrl=http://host.docker.internal:8080
```

Al final muestra una tabla: la columna `failed` debe estar en 0.

## 8. Ejecutar las pruebas

Las pruebas normales no necesitan bases de datos:

```powershell
cd nexusm
.\mvnw.cmd clean test
```

Al terminar, el informe de cobertura queda en `nexusm\target\site\jacoco\index.html`.

Las pruebas `*IT` usan el MySQL y el MongoDB reales, así que necesitan los contenedores arriba:

```powershell
.\mvnw.cmd test "-Dtest=*IT"
cd ..
```

## 9. Ejecutar el verificador del SDD

`tools/VerifySdd.java` comprueba que la documentación coincide con el código compilado:

```powershell
cd nexusm
.\mvnw.cmd clean test-compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
cd ..
java tools/VerifySdd.java . nexusm/target/classpath.txt
```

El resumen debe terminar con `failed=0`. Más detalles en [tools/README.md](tools/README.md).

## 10. Apagar y reiniciar los datos

| Quiero | Comando |
|---|---|
| Apagar y conservar los datos | `docker compose stop` |
| Volver a encender | `docker compose start` |
| Borrar los contenedores y conservar los datos | `docker compose down` |
| Borrar también todos los datos | `docker compose down -v` |
| Empezar de cero | `docker compose down -v` y luego `docker compose up -d --build` |

## 11. Si algo falla

| Síntoma | Qué hacer |
|---|---|
| `port is already allocated` | Otro programa usa `8080`, `3306` o `27017`: ciérrelo y repita el comando |
| `The container name is already in use` | Hay contenedores de otra copia del proyecto: revíselos con `docker ps -a` |
| `Cannot connect to the Docker daemon` | Docker Desktop no está abierto |
| La API no responde | Revise `docker compose logs api` |
| 401 en Swagger | El token expiró (dura 1 hora) o fue revocado: inicie sesión otra vez y repita **Authorize** |
| Las pruebas no encuentran Java | Instale Java 17 y revise la variable `JAVA_HOME` |
