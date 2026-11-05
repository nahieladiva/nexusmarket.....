# Herramientas

## `VerifySdd.java`

Comprueba que el SDD coincide con el código compilado. Solo lee: no modifica el repositorio.

Está fuera de `nexusm/src`, así que Maven no lo compila, no entra en el `.jar` y la imagen de Docker no
lo incluye. Se ejecuta directamente con `java`, sin compilarlo antes. Necesita un JDK 17 o superior.

### Cómo ejecutarlo

Desde la raíz del repositorio, en PowerShell:

```powershell
cd nexusm
.\mvnw.cmd clean test-compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
cd ..
java tools/VerifySdd.java . nexusm/target/classpath.txt
```

En Linux o macOS se usa `./mvnw` y lo demás es igual.

El primer argumento es la carpeta del repositorio y el segundo, el archivo con el classpath de las
dependencias. Las clases se leen de `nexusm/target/classes`, así que hay que compilar antes de ejecutarlo
para no comparar el SDD con clases viejas.

Si cambian las rutas, esta opción imprime las secciones de `SDD/domain/REST-API.md` a partir del código:

```powershell
java tools/VerifySdd.java . nexusm/target/classpath.txt --print-rest-api
```

### Qué comprueba

- Cada clase, método, constante y paquete citado en `SDD/` existe, incluidos los diagramas.
- Los 7 puertos de entrada: firma exacta, servicio que llama cada operación y conteos de `Input-ports.md`.
- La matriz de `Services.md`: puertos y roles de cada servicio, quién usa a los auxiliares y el enlace a
  su detalle. Esto se obtiene del bytecode, no de buscar texto.
- Los documentos de `SDD/domain/services/`: los 6 campos de cada servicio, que las entradas sean sus
  métodos públicos y que las excepciones citadas se puedan alcanzar.
- Los puertos de salida: métodos y adaptador.
- La API REST en `SDD/domain/REST-API.md`: cada ruta, su código de éxito y la operación del puerto que
  llama. Cada controlador usa un solo puerto, y cada operación tiene exactamente una ruta.
- `application.domain` no importa Spring, Lombok, Jakarta, `adapters` ni `infrastructure`.

### Cómo leer el resultado

| Sección | Significado |
|---|---|
| `SUMMARY` | Comprobaciones correctas y fallidas por tipo |
| `FAILURES` | El SDD y el código no coinciden. El programa termina con código 1 |
| `WARNINGS` | Posibles diferencias que hay que revisar a mano. No hacen fallar la ejecución |
| `UNCLASSIFIED` | Citas que no son código, como rutas o imágenes de Docker. Conviene revisarlas: aquí también caería el nombre de un método planeado que aún no existe |

### Límites

- No valida el texto de las reglas de negocio ni los estados que cambia cada servicio.
- La revisión de excepciones es aproximada: sigue las llamadas sin saber qué condiciones se cumplen. Por
  eso algunos avisos de `exception not cited` son caminos imposibles en la práctica. Por ejemplo,
  `ApplyShipmentProgressService` solo pide transiciones válidas, así que nunca llega a
  `OrderStateTransitionException`.
