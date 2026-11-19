# sockets-multiclient-chat

Chat multicliente en Java sobre **TCP sockets**. Un servidor central registra a los clientes (por nickname) y reparte los mensajes: globales, privados y avisos del sistema. Tiene interfaz Swing y modo consola, con el backend de red separado de la GUI.

## 1. Idea general

Es una topología **estrella**: ningún cliente habla con otro directamente. Todos se conectan al servidor y el servidor decide a quién reenvía cada línea.

```
 Cliente A ─┐
 Cliente B ─┼──►  SERVIDOR  ──►  a todos / a uno / error
 Cliente C ─┘
```

El servidor mantiene una tabla en memoria `nickname → ClientManager` (el `ClientManager` es el dueño del socket de ese cliente). Esa tabla es, en la práctica, la tabla de enrutamiento de la aplicación.

## 2. Estructura y capas

```
src/
├── servidor/
│   ├── Main.java                    decide GUI o CLI
│   ├── backend/
│   │   ├── ServidorSocket.java      bind/accept, registro, enrutamiento, broadcast
│   │   ├── ClientManager.java       un hilo por cliente: lee su socket y escribe en él
│   │   └── ServerListener.java      eventos hacia la GUI
│   └── frontend/                    Swing: conexión, log, lista de clientes
└── cliente/
    ├── Main.java                    decide GUI o CLI
    ├── backend/
    │   ├── ClientBackend.java       abre el socket, hilo receptor, envío
    │   ├── ClientConfig.java        lee cliente.properties una sola vez
    │   └── ClientListener.java      eventos hacia la GUI
    └── frontend/                    Swing: nombre y panel de chat
```

Las capas se comunican solo por interfaces:

- La GUI llama a `AccionesCliente` / `AccionesServidor` (métodos como `conectar`, `enviarMensaje`, `iniciar`, `expulsar`). No ve un `Socket`.
- El backend avisa a la GUI con `ClientListener` / `ServerListener` (callbacks como `message`, `disconnected`, `clientJoined`).

Las clases `AccionesDemoCliente` y `AccionesDemo` implementan esas interfaces sin abrir sockets; sirven para probar la interfaz sola. Los `Main` reales usan `ClientBackendActions` y `ServerBackendActions`, que sí conectan con el backend.

## 3. Compilación y ejecución

`Compilador.bat` borra `out/`, genera `sources.txt` con `dir /s /b src\*.java`, compila con `javac -d out @sources.txt` y borra el archivo temporal.

```
java -cp out servidor.Main          # servidor con GUI
java -cp out servidor.Main cli      # servidor por consola
java -cp out cliente.Main           # cliente con GUI
java -cp out cliente.Main cli       # cliente por consola (pide nickname y lee teclado)
```

Siempre **primero el servidor**. En la GUI del servidor el puerto se escribe arriba (6969 por defecto); en el cliente se toma del properties.

## 4. Configuración del cliente

`cliente.properties` (partir de `cliente.properties.example`):

```properties
server.host=192.168.1.10
server.port=6969
```

`ClientConfig` es un `final` con bloque `static`: lee el archivo con `Properties`, guarda `HOST` y `PORT` en constantes y falla con `ExceptionInInitializerError` si no existe o falta `server.host`. La IP debe ser la del equipo servidor dentro de la LAN; el firewall debe permitir el puerto.

## 5. El protocolo: cómo se formatea un mensaje

TCP entrega **bytes ordenados y confiables, pero sin fronteras de mensaje**. Si el cliente hace dos `println` seguidos, el servidor podría recibirlos juntos en un solo `read`. Por eso el proyecto impone su propia capa de mensajería:

- **Una línea por mensaje**, terminada en `\n` (`PrintWriter.println` / `BufferedReader.readLine`).
- **UTF-8** declarado explícitamente en los dos extremos (`OutputStreamWriter` / `InputStreamReader`).
- `autoFlush = true` en el `PrintWriter`: cada `println` empuja la línea hacia la pila TCP de inmediato.
- Campos separados por `|`, con prefijo de tipo. El `split("\\|", 3)` usa límite 3: el tercer campo se queda con **todo el resto de la línea**, así que el texto puede contener más `|`.

| Línea en el socket | Tipo | Quién la genera |
|---|---|---|
| `ana` | registro | cliente, como primera línea |
| `G\|ana\|hola` | global | `route()` → `sendToAll()` |
| `P\|ana\|hola luis` | privado | `route()` / `sendAdminPrivate()` |
| `S\|* ana joined` | sistema | `register()`, `unregister()`, errores |
| `X\|Nickname invalid or already in use` | rechazo | `register()` si el nick está mal o repetido |

Recepción en el cliente (`ClientBackend.receive()`):

```
G|P con 3 partes  → message("G"/"P", from, text)
otro con 2 partes → message(tipo, "", text)
sin ningún "|"    → message("S", "", línea completa)
```

La GUI traduce esos tipos: `G` → `ana: hola`, `P` → `[private] ana: hola`, `S` → `* aviso`, `X` → vuelve a la pantalla del nombre.

## 6. Recorrido de un mensaje, por dentro

### 6.1 Arrancar el servidor

`Main.runCli()` (o `ServerBackendActions.iniciar(p)` desde la GUI) llama a `ServidorSocket.start(p)`:

1. `serverSocket = new ServerSocket(p)` → el SO hace **bind** del puerto y lo pone en LISTEN.
2. `running = true` y `new Thread(this).start()` → el hilo aceptador.
3. Ese hilo entra en `run()`: `while (running) { Socket s = serverSocket.accept(); ... }`.
4. `accept()` se **bloquea** hasta que llega una conexión. Cuando vuelve, ha creado un socket ya conectado con la IP/puerto efímero del cliente.
5. Por cada socket: `new ClientManager(s, this)` y `new Thread(manager).start()`. Luego vuelve a `accept()`.

### 6.2 Conectar y registrarse

Lado cliente (`ClientBackendActions.conectar` → hilo → `ClientBackend.connect`):

1. `new Socket(HOST, PORT)`: el SO cliente elige un puerto local efímero y hace el handshake TCP (SYN → SYN-ACK → ACK) con el servidor.
2. Se crea el `PrintWriter` de salida.
3. Arranca **el hilo receptor** (`new Thread(this::receive).start()`).
4. Se envía `output.println(nickname)`. Esa primera línea es el "registro de aplicación" (después del handshake TCP).

Lado servidor, en el hilo de ese `ClientManager.run()`:

1. Crea `BufferedReader` y `PrintWriter` sobre el socket.
2. `nickname = input.readLine()` → bloquea hasta que llega la primera línea.
3. `server.register(nickname, this)` valida: no vacío, sin `|`, y que no esté ya en `clients`.
   - Si falla: `manager.send("X|...")` y `socket.close()` → el cliente recibe X y se cierra.
   - Si pasa: `clients.put(nickname, manager)`, `listener.clientJoined(...)` y `sendToAll("S|* ana joined", null)` (con `except=null`, así que **el nuevo cliente también recibe su propio aviso**).
4. Entra en `while ((line = input.readLine()) != null) server.route(this, line);`.

Visto como secuencia:

```
Cliente                         Servidor
   |-- TCP connect --------------->|  accept() → nuevo ClientManager + hilo
   |-- "ana\n" ------------------->|  readLine() → register("ana")
   |<-- "S|* ana joined\n" --------|  sendToAll (incluye al nuevo)
   |<-- "S|* ana joined\n" --------|  (si hay otro cliente conectado)
```

### 6.3 Mensaje global (`hola`)

```
1. PanelChatCliente: el campo o el botón ejecuta alEnviar.accept(texto)
2. ClientePrueba.enviarMensaje → acciones.enviarMensaje(texto)
3. ClientBackendActions: comprueba connected y llama backend.send(text)
4. ClientBackend.send (synchronized): output.println("hola") → bytes al kernel
5. El hilo del ClientManager de ana está bloqueado en readLine(): despierta, lee "hola"
6. ServidorSocket.route(ana, "hola") → no empieza con /msg →
   sendToAll("G|ana|hola", ana) → recorre clients.values() y llama send() a todos menos ana
7. Cada ClientManager.send (synchronized) hace println() en el socket de su cliente
8. En cada cliente, su hilo receptor despierta en readLine(), hace split("|",3)
   y llama listener.message("G","ana","hola")
9. ClientBackendActions → window.agregarMensaje("ana: hola")
10. ClientePrueba agregarMensaje usa SwingUtilities.invokeLater → lo pinta el EDT
```

Dos detalles: quien escribe **no** recibe su propio mensaje global (se pasa `ana` como `except`), y el `synchronized` de `send()` evita que dos hilos del servidor escriban líneas entrelazadas en el mismo socket.

### 6.4 Mensaje privado (`/msg luis hola`)

El cliente no distingue el privado: manda la línea tal cual. La decisión está en `ServidorSocket.route()`:

```
if (line.startsWith("/msg ")):
    partes = line.substring(5).split(" ", 2)     // ["luis", "hola"]
    si no hay 2 partes → S|Usage: /msg Nickname message  (solo al remitente)
    to = clients.get("luis")                     // ← decisión de destino
    si to == null → S|User not connected: luis   (solo al remitente)
    si existe:
        to.send("P|ana|hola")        → luis ve "[private] ana: hola"
        from.send("P|luis|hola")     → ana ve el eco "[private] luis: hola"
else:
    sendToAll("G|ana|" + line, ana)
```

`ClientBackend.sendPrivate(nick, text)` existe y construye `/msg nick text`, pero la GUI actual no tiene un botón que lo llame: en la práctica se escribe el comando en el campo de texto.

### 6.5 Mensajes y expulsión desde el servidor

`ServidorSocket` expone tres operaciones que usa la GUI del servidor:

- `broadcastAdmin(texto)` → `G|ADMIN|texto` a **todos** (`except=null`).
- `sendAdminPrivate(nick, texto)` → `clients.get(nick)` y `P|ADMIN|texto` solo a ese socket. Si el nick no está, no hace nada.
- `kick(nick)` → `manager.closeSocket()`; al cerrar, el `readLine()` de ese cliente devuelve `null` y se desencadena su desconexión normal.

### 6.6 Desconexión y limpieza

- Cliente: `backend.close()` → `socket.close()`. El kernel envía FIN.
- Servidor: el `readLine()` del `ClientManager` devuelve `null`, sale del bucle y en `finally` ejecuta `unregister(manager)` (borra el nick del mapa y hace broadcast de `S|* ana left`), cierra el socket y escribe el log.
- Cliente receptor: si su `readLine()` devuelve `null` o lanza `IOException`, avisa `listener.disconnected()` → la GUI vuelve a la pantalla del nombre. Los flags `closing` y `rejected` evitan mostrar "Server disconnected" cuando el cierre fue voluntario o fue un rechazo.
- `ServidorSocket.stop()` pone `running=false`, cierra el `ServerSocket` (con lo que `accept()` falla y el bucle termina) y cierra todos los sockets de clientes; desde el `finally` del hilo aceptador se llama a `listener.stopped()`.

## 7. Los hilos: quiénes son y por qué existen

La razón de fondo es que **`accept()`, `readLine()` y la lectura de teclado son bloqueantes**. Un hilo que está esperando bytes no puede hacer otra cosa.

### Proceso servidor

| Hilo | Creado en | Qué hace |
|---|---|---|
| `main` | JVM | Arranca Swing (`SwingUtilities.invokeLater`) o llama a `start()` y termina |
| EDT (Swing) | `invokeLater` | Pinta la ventana y atiende botones; único hilo que toca componentes |
| Hiloaceptador | `ServidorSocket.start()` | Bucle `accept()`, crea un hilo por conexión |
| Hilo por cliente | `ServidorSocket.run()` | `ClientManager.run()`: lee líneas y llama a `route()`; bloqueado en `readLine()` |

### Proceso cliente

| Hilo | Creado en | Qué hace |
|---|---|---|
| `main` / EDT | JVM / `invokeLater` | Interfaz; en modo CLI, `main` además lee el teclado en bucle |
| Hilo de conexión | `ClientBackendActions.conectar()` | Ejecuta `connect()` (puede bloquear en `new Socket`) y luego muestra el chat |
| Hilo receptor | `ClientBackend.connect()` | Bloqueado en `readLine()`, reparte lo que llega al `ClientListener` |

### Estado compartido y sincronización

- `clients` es `ConcurrentHashMap`: varios hilos de cliente consultan y modifican el mapa a la vez sin corromperlo.
- `send()` está `synchronized` tanto en `ClientManager` como en `ClientBackend`: una línea = una escritura atómica en el stream de salida.
- `running`, `serverSocket`, `connected`, `rejected` y `closing` son `volatile` para que los cambios se vean entre hilos sin cacheos raros.
- Toda actualización de Swing pasa por `SwingUtilities.invokeLater(...)`: el hilo de red nunca toca un `JTextArea` directamente, le pasa el dato a la ventana y esta lo encola al EDT.

Modelo resultante: **thread-per-connection** en el servidor + **un hilo receptor por cliente** + EDT para la interfaz. Simple de razonar, pero cada cliente consume un hilo (no escala bien a miles).

## 8. La red, capa por capa

1. **Direcciones y puertos**: el servidor hace bind en `0.0.0.0:6969` (todas sus interfaces); el cliente usa `192.168.1.10:6969` del properties. La conexión se identifica por `(IP cliente, puerto efímero cliente, IP servidor, 6969)`. De ahí que el servidor pueda distinguir dos clientes con la misma IP.
2. **Handshake y datos**: TCP establece la conexión (SYN/SYN-ACK/ACK), garantiza orden y reintentos, y el SO mantiene búferes de envío/recepción por socket. El programa solo ve un `InputStream` y un `OutputStream` continuos.
3. **Framing propio**: como TCP no preserva líneas, el `\n` y el `split("|", 3)` reconstruyen los mensajes. No se usa `ObjectOutputStream` ni serialización: todo es texto legible con `telnet`/`nc`.
4. **Escritura**: `println` con autoFlush entrega la línea al búfer del kernel; TCP se encarga de segmentarla, retransmitirla y controlar el flujo. No se configura `TCP_NODELAY`, así que el algoritmo de Nagle puede agrupar escrituras pequeñas (el framing por línea sigue funcionando).
5. **Cierre**: `close()` genera FIN; `readLine()` devuelve `null` al terminar el flujo. Un cierre brusco produce `IOException`, que se captura y se trata como desconexión.
6. **Errores típicos**: IP mal puesta o firewall → excepción al conectar; puerto distinto → no hay SERVICIO; puerto ocupado → falla `new ServerSocket()` y la GUI muestra "Unable to start server".

## 9. Detalles y limitaciones

- El nick no puede contener `|` porque desplazaría los campos del protocolo; se valida tanto en la GUI como en el servidor.
- El texto de un mensaje sí puede contener `|`: el `split` con límite 3 lo conserva entero en el tercer campo.
- Cualquier línea que empiece con `/msg ` se interpreta como comando, no como texto normal.
- No hay cifrado, autenticación ni persistencia: si el servidor se apaga, se pierde el registro de conectados.
- `src/servidor/backend/models/Cliente.java` guarda IP y nombre pero no se usa: el servidor enruta por nickname mediante `ClientManager`.
