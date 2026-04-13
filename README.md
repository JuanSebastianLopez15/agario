# Proyecto Multijugador: Juego en Red AGAR.IO

Este proyecto es un juego interactivo multijugador en tiempo real desarrollado en **Java**, donde dos (o más) jugadores compiten en red. La comunicación es mediante **UDP**, manteniendo el estado del juego sincronizado, actuando uno de los clientes como *Host*.

El desarrollo se enfocó en crear una arquitectura robusta, aplicando **Principios SOLID** y **Patrones de Diseño**, cumpliendo con los estándares de código limpio y separación de responsabilidades (tal y como nos indica la profe Diana en el documento pdf).

---

## ¿Cómo Jugar? (Reglas y Mecánicas)

Tu objetivo es sobrevivir, comer y convertirte en la célula más grande del mapa. El juego tiene una duración máxima de **3 minutos**.

### Controles
* **Movimiento:** La célula seguirá automáticamente la posición de tu **cursor (Mouse)**.Entre más masa tengas, más lento te moverás.

### Reglas del Juego
1. **Comida (Pellets):** Repartidos por todo el mapa(puntos estaticos de colores distribuidos por el mapa). Cómelos para aumentar tu tamaño.
2. **Absorción (Depredador/Presa):** Puedes comerte a otro jugador **solo si tu masa es al menos un 10% mayor** que la de él.
3. **Peligros (Hazard Balls):** Ten cuidado con las bolas rojas gigantes con el símbolo `!`. Si chocas con una y tienes masa suficiente (>20), **te dividirás en dos partes iguales**. Las Hazard Balls no se pueden comer.
4. **Potenciador (celula rapida):** Con la tecla espacio, aumenta la velocidad de la celula durante 4s, la podras utilizar cada 10s.
5. **Victoria:** El juego termina bajo dos condiciones:
    * Eres el único jugador que queda vivo (eliminaste a todos).
    * Se acaba el tiempo (Gana el jugador con mayor masa acumulada).

### Sistema de Puntaje
* Comer un pellet: **+1 Punto**
* Absorber a otro jugador: **+10 Puntos**
* Ganar la partida: **+50 Puntos (Bono extra)**

---

## Arquitectura y Calidad de Software

El código está estructurado para separar completamente la Lógica, la Interfaz (UI/Renderers) y la Comunicación (Red).

### Principios SOLID Aplicados
* **SRP (Single Responsibility Principle):** Clases como `ScoreManager` solo cuentan puntos; los *Renderers* (`PlayerRenderer`, `PelletRenderer`) solo dibujan; la lógica se mantiene pura en el `GameEngine`.
* **OCP (Open/Closed Principle):** Se pueden añadir nuevas reglas de juego simplemente creando una nueva clase que implemente `IGameRule` sin alterar el motor principal.
* **DIP (Dependency Inversion Principle):** El motor no depende de implementaciones concretas de red o UI, sino de interfaces (como `IGameEventListener` o `INetworkListener`).

### Patrones de Diseño Utilizados
1. **Patrón Strategy:** Implementado en el sistema de reglas (`IGameRule`). El ciclo principal del juego aplica estrategias dinámicas (`MovementRule`, `GrowthRule`, `AbsorptionRule`, etc.) de forma desacoplada.
2. **Patrón Observer:** Usado para notificar a la Interfaz, al sistema de Sonido (`SoundManager`) y al gestor de puntajes (`ScoreManager`) sobre los eventos del juego (ej: `onAbsorption`, `onPelletEaten`) sin que el núcleo lógico los conozca directamente.
3. **Patrón State:** Controla el flujo del juego dividiendo el comportamiento en `RunningState` y `GameOverState`.

---

## Sincronización de Red (UDP)
El proyecto utiliza Sockets UDP puros (`DatagramSocket`):
* **Host:** Procesa las reglas físicas, genera colisiones, empaqueta el estado global (`GameSnapshot`) y lo envía constantemente a los clientes.
* **Cliente:** Intercepta la información de red, renderiza la pantalla y envía un DTO ligero (`MouseInputDTO`) con sus intenciones de movimiento al Host de manera asíncrona.

---

## ¿Cómo probar el juego?

### En el mismo PC (Localhost)
1. Ejecuta el proyecto (dale `RUN` a la clase principal).
2. Ponle un nombre a tu jugador, **marca** la casilla de `"Eres el host"` y dale a jugar.
3. Vuelve a darle a `RUN` para abrir una segunda ventana.
    * *Nota:* Asegúrate de tener habilitado `Allow multiple instances` en tu IDE (En IntelliJ: Ve arriba al selector del Main -> `Edit Configurations` -> `Modify options` -> Selecciona `Allow multiple instances` -> Aplicar).
4. En esta nueva ventana, pon el Nombre del *Jugador 2*, **no marques** la casilla de host, y en la "IP del Host" escribe `localhost` o `127.0.0.1`.
5. ¡Dale a jugar y las pantallas se sincronizarán!

### En diferentes PCs (LAN / Red Local)
1. **Busca la IP del Host:** En la PC que será el servidor, abre la terminal (cmd) -> ingresa el comando `ipconfig` y presiona *Enter*. Busca la línea que diga **Dirección IPv4** (ej: `192.168.1.15`).
2. Abre el juego en esa PC, pon tu nombre, **marca** la casilla de Host y dale jugar.
3. En la segunda PC, ejecuta el juego, pon tu nombre, **no marques** la casilla de Host.
4. En la casilla de "IP del Host", **escribe el número IPv4** que obtuviste en el paso 1.
5. Dale a Jugar. El sistema UDP Peer-to-Peer conectará ambas máquinas de inmediato.

---
# Integrantes: 
- Cristian Camilo Salazar Arenas
- Isabela Quintero M.
- Juan Sebastian Lopez G.