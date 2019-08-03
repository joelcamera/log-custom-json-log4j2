# Log Custom JSON Con Log4J2

Este posteo se escribe a partir de una experiencia vivida en donde queríamos loguear
objetos JSON en una aplicación Java con Spring Boot y utilizamos Log4J2 para hacerlo.
Nos fue un poco costoso lograrlo y no encontramos mucha información solida,
por ello la motivación del mismo.

Como ejemplo para probar, hice una pequeña aplicación para poder jugar con los datos.
Ésta se encuentra en https://github.com/joelcamera/log-custom-json-log4j2.
Se va a estar utilizando la versión *2.1.6.RELEASE* de Spring Boot del mismo que tiene la versión *2.11.2* de Log4J2.

## Agregar Log4J2 En Spring Boot

Lo primero para agregar Log4J2 en la aplicación hay que excluir de **TODOS** los
starters de Spring Boot, la dependencia spring-boot-starter-logging ya que
esta importa Logback y no queremos que tome los logs.
Para excluirlos, se hace de la siguiente forma:

```
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
   <version>2.1.6.RELEASE</version>
   <exclusions>
       <exclusion>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-logging</artifactId>
       </exclusion>
   </exclusions>
</dependency>
```

Y luego, agregar la dependencia de Spring Boot _spring-boot-starter-log4j2_:

```
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-log4j2</artifactId>
   <version>2.1.6.RELEASE</version>
</dependency>
```

Con esto ya tenemos Log4J2 corriendo en nuestra aplicación.

## Archivo de Configuración de Log4J2

Con solo tener la dependencia de Log4J2, al levantar la aplicación corre la configuración por
defecto que tiene en la que loguea por consola con el PatternLayout.
Según la [documentación](https://logging.apache.org/log4j/2.x/manual/configuration.html#AutomaticConfiguration),
para manejar nuestros propios loggers, appenders y
layouts debemos crear un archivo de configuración con el nombre _log4j2.*_ donde
por * puede ser _YAML_, _JSON_, _properties_ o _xml_.

    Atención: Intentamos utilizar un .properties pero no nos lo detectaba y no
    pudimos encontrar bien el porqué. La documentación hasta el momento solo dice que se puede usar.

En nuestro caso, utilizamos un archivo _YAML_ y lo agregamos en resources.
Para que Log4J2 pueda entender este archivo también hay que importar la dependencia de Jackson:

```
<dependency>
   <groupId>com.fasterxml.jackson.dataformat</groupId>
   <artifactId>jackson-dataformat-yaml</artifactId>
   <version>2.9.0</version>
</dependency>
```

El primer archivo de configuracion nuestro fue de la siguiente forma:

```
Configuration:
 status: error

 Appenders:
   Console:
     - name: CONSOLE_ROOT
       target: SYSTEM_OUT
       PatternLayout:
         alwaysWriteExceptions: true
         pattern: "[%threadName] %-5level %logger{36} - %message{nolookups} %ex{separator(|)} %n"

 Loggers:
   Root:
     level: info
     AppenderRef:
       ref: CONSOLE_ROOT
```

Con este, se loguea a consola con el PatternLayout y en una sola línea
ya que es el root logger.

## JSONLayout y Porque No Lo Usamos En Nuestro Caso

Log4J2 te ofrece un layout para imprimir JSONs, este es [JSONLayout](https://logging.apache.org/log4j/2.x/manual/layouts.html#JSONLayout).
Para agregarlo simplemente agregamos un nuevo appender con el layout y un
nuevo logger (esto podría ser un solo appender pero a modo de ejemplo quiero
separarlos para mostrar las diferencias). Entonces nuestro archivo de configuración
queda de la siguiente forma:

```
Configuration:
 status: error

 Appenders:
   Console:
     - name: JSON_LAYOUT_APPENDER
       target: SYSTEM_OUT
       JSONLayout:
         compact: true
         complete: false
         objectMessageAsJsonObject: true

     - name: CONSOLE_ROOT
       target: SYSTEM_OUT
       PatternLayout:
         alwaysWriteExceptions: true
         pattern: "[%threadName] %-5level %logger{36} - %message{nolookups} %ex{separator(|)} %n"

 Loggers:
   logger:
     - name: LOGGER_WITH_JSON_LAYOUT
       level: info
       additivity: false
       AppenderRef:
         ref: JSON_LAYOUT_APPENDER

   Root:
     level: info
     AppenderRef:
       ref: CONSOLE_ROOT
```

Con esto ya tenemos un logger listo para poder usar que loguee JSONs. El problema
con este layout es que tiene campos definidos y varios de ellos no se pueden quitar.
Aquí un ejemplo utilizando este logger hecho con el repositorio de ejemplo donde
envío un JSON en el body:

```
{
    “field”: “value”
}
```

El log obtenido es el siguiente:

```
{"thread":"http-nio-8080-exec-2","level":"INFO","loggerName":"LOGGER_WITH_JSON_LAYOUT","message":{"field":”value”},"endOfBatch":false,"loggerFqcn":"org.apache.logging.log4j.spi.AbstractLogger","instant":{"epochSecond":1564775298,"nanoOfSecond":516000000},"threadId":25,"threadPriority":5}
```

Entonces, lo que loguee aparece en el campo “message” y el resto son
datos del layout. Pero en nuestro caso no nos interesaban, generan
ruido y ocupan espacio.

```
Atención: El campo “message” no loguea JSONs por defecto sino que
logguea un String. Para que loguee JSON hay que agregar en el archivo
de configuración “objectMessageAsJsonObject: true” como lo pueden ver
en el YAML nuestro de configuración. Aún así, esta funcionalidad no
la tienen todas las versiones de Log4J2. Solamente desde la versión
2.11.0 en adelante. Les dejo el link donde esta el issue resuelto
https://issues.apache.org/jira/browse/LOG4J2-2190 
```

## Cambiar SLF4J por Log4J2 API

Para loguear JSONs hechos por nosotros primero debemos dejar de usar
SLF4J como logger y cambiarla por la Log4J2 API ya que esta última
ofrece algunas características más que la primera. La que más nos
interesa es la del objeto [Message](https://logging.apache.org/log4j/2.x/manual/messages.html)
ya que con este podremos crear eljson. Entonces para crear instanciar
el logger, lo que debe hacerse es utilizar el LogManager con el nombre
del logger en cuestión en el yaml.

```
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
...
private static Logger loggerWithJsonLayout = LogManager.getLogger(LOGGER_WITH_JSON_LAYOUT);
```

## CustomLayout y CustomMessage

Para generar el CustomLayout nos referimos a la [documentación](https://logging.apache.org/log4j/2.x/manual/extending.html#Layouts)
de Log4J2. En nuestro ejemplo queda de la siguiente manera:

```
@Plugin(name = "CustomLayout", category = "Core", elementType = "layout", printObject = true)
public class CustomLayout extends AbstractStringLayout {

   private static final String DEFAULT_EOL = "\r\n";

   protected CustomLayout(Charset charset) {
       super(charset);
   }

   @PluginFactory
   public static CustomLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
       return new CustomLayout(charset);
   }

   @Override
   public String toSerializable(LogEvent logEvent) {
       return logEvent.getMessage().getFormattedMessage() + DEFAULT_EOL;
   }
}
```

Lo importante aquí es el método `#toSerializable` que toma un LogEvent,
le pide el objeto Message y a este lo formatea para devolver un string.
Notar también el EOL ya que sino quedarían todos los logs en una sola línea.

Para utilizarlo vamos a crear un nuevo logger y un nuevo appender en
nuestro YAML de configuración, y este quedaría de la siguiente manera:

```
Configuration:
 status: error

 Appenders:
   Console:
     - name: CUSTOM_LAYOUT_APPENDER
       target: SYSTEM_OUT
       CustomLayout: {}

     - name: JSON_LAYOUT_APPENDER
       target: SYSTEM_OUT
       JSONLayout:
         compact: true
         complete: false
         objectMessageAsJsonObject: true

     - name: CONSOLE_ROOT
       target: SYSTEM_OUT
       PatternLayout:
         alwaysWriteExceptions: true
         pattern: "[%threadName] %-5level %logger{36} - %message{nolookups} %ex{separator(|)} %n"

 Loggers:
   logger:
     - name: LOGGER_WITH_CUSTOM_LAYOUT
       level: info
       additivity: false
       AppenderRef:
         ref: CUSTOM_LAYOUT_APPENDER

     - name: LOGGER_WITH_JSON_LAYOUT
       level: info
       additivity: false
       AppenderRef:
         ref: JSON_LAYOUT_APPENDER

   Root:
     level: info
     AppenderRef:
       ref: CONSOLE_ROOT
```

Y también, en el controller instanciamos el nuevo logger:

```
private static final String LOGGER_WITH_CUSTOM_LAYOUT = "LOGGER_WITH_CUSTOM_LAYOUT";
private static Logger loggerWithCustomLayout = LogManager.getLogger(LOGGER_WITH_CUSTOM_LAYOUT);
```

Luego, necesitamos generar nuestro objeto Message:

```
public class CustomMessage implements Message {
   private static final String TYPE = "type";
   private static final String BODY = "body";

   private final Map<String, Object> requestBody;

   public CustomMessage(Map<String, Object> requestBody) {
       this.requestBody = requestBody;
   }

   @Override
   public String getFormattedMessage() {
       JSONObject jsonBody = new JSONObject(requestBody);
       JSONObject jsonToLog = new JSONObject(new HashMap<String, Object>() {{
           put(TYPE, "custom");
           put(BODY, jsonBody);
       }});

       return jsonToLog.toString();
   }

   @Override
   public String getFormat() {
       return requestBody.toString();
   }

   @Override
   public Object[] getParameters() {
       return new Object[0];
   }

   @Override
   public Throwable getThrowable() {
       return null;
   }
}
```

Lo importante del mismo es el constructor que toma el JSON que enviamos en
el request y el método `#getFormattedMessage` donde se puede observar que
generamos el JSON de la forma que queremos y con los parámetros que queremos
y lo devolvemos como string.

Entonces, en el controller de ejemplo simplemente loggeamos el JSON que
se recibe de la siguiente manera:

```
@PostMapping()
@ResponseStatus(HttpStatus.OK)
void toLogWithTheCustomLayout(@RequestBody Map<String, Object> requestBody) {
   loggerWithCustomLayout.info(new CustomMessage(requestBody));
}
```

Si enviamos un JSON de la forma:

```
{
    “key1”: “value1”,
    “key2: 123
}
```
Obtenemos como log:

`{"type":"custom","body":{"key1":"value1","key2":123}}`

Y es el JSON que armamos en el CustomMessage.

Como último, ¿qué pasaría si no usamos el CustomMessage y solamente
imprimimos el Map que recibimos como cuerpo del request?

O sea, de a siguiente forma:

```
@PostMapping()
@ResponseStatus(HttpStatus.OK)
void toLogWithTheCustomLayout(@RequestBody Map<String, Object> requestBody) {
   loggerWithCustomLayout.info(requestBody);
}
```

Lo que se imprimiría es un objeto que no es un JSON válido:

`{key1=value1, key2=123}`