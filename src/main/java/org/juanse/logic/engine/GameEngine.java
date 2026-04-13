package org.juanse.logic.engine;

import org.juanse.logic.dto.*;
import org.juanse.logic.entities.HazardBall;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;
import org.juanse.logic.observers.ScoreManager;
import org.juanse.logic.rules.*;
import org.juanse.logic.state.GameOverState;
import org.juanse.logic.state.IGameState;
import org.juanse.logic.state.RunningState;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Núcleo del juego. Coordina entidades, reglas, estados y notificaciones.
 *
 * <p>Principio S (SRP): coordina; no dibuja ni envía paquetes.</p>
 * <p>Principio D (DIP): depende de {@link IGameRule} e {@link IGameEventListener}, no de concretos.</p>
 *
 * <p>Patrones aplicados:</p>
 * <ul>
 *   <li><b>Observer</b>: notifica eventos a los oyentes registrados.</li>
 *   <li><b>Strategy</b>: delega la lógica a reglas ({@link IGameRule}) intercambiables.</li>
 *   <li><b>State</b>: delega el comportamiento del tick al {@link IGameState} actual.</li>
 * </ul>
 */
public class GameEngine {

    /** Ancho del mapa de juego en píxeles. */
    private final double mapWidth;

    /** Alto del mapa de juego en píxeles. */
    private final double mapHeight;

    /** Número máximo de pellets en el mapa. */
    private static final int MAX_PELLETS = 80;

    /** Número máximo de HazardBalls en el mapa. */
    private static final int MAX_HAZARDS = 8;

    /** Duración total de la partida en milisegundos (3 minutos). */
    private static final long GAME_DURATION_MS = 3 * 60 * 1000L;

    /** Masa inicial de cada célula al registrarse. */
    private static final double INITIAL_MASS = 50.0;

    /** Indica si este motor actúa como host. */
    private boolean isHost = false;

    /** Tiempo restante recibido del host (usado solo por clientes). */
    private long clientRemainingTimeMs = GAME_DURATION_MS;

    /** Tiempo total transcurrido al finalizar la partida. */
    private long finalElapsedTimeMs = 0;

    /** Lista de células activas en el juego. Thread-safe. */
    private final List<PlayerCell> players = new CopyOnWriteArrayList<>();

    /** Lista de pellets activos en el mapa. Thread-safe. */
    private final List<Pellet> pellets = new CopyOnWriteArrayList<>();

    /** Lista de HazardBalls activas en el mapa. Thread-safe. */
    private final List<HazardBall> hazardBalls = new CopyOnWriteArrayList<>();

    /** Conjunto de jugadores completamente eliminados. */
    private final Set<String> deadPlayers = new HashSet<>();

    /** Lista de reglas aplicadas en cada tick (Patrón Strategy). */
    private final List<IGameRule> rules = new ArrayList<>();

    /** Lista de observadores de eventos del juego (Patrón Observer). */
    private final List<IGameEventListener> listeners = new ArrayList<>();

    /** Estado actual del juego (Patrón State). */
    private IGameState currentState;

    /** Gestor de puntajes de los jugadores. */
    private final ScoreManager scoreManager;

    /** Timestamp de inicio de la partida. */
    private long startTime;

    /** Generador de números aleatorios para posiciones. */
    private final Random random = new Random();

    /**
     * Constructor del motor del juego.
     * Registra todas las reglas en orden de ejecución e inicializa el estado.
     *
     * @param mapWidth  ancho del mapa en píxeles
     * @param mapHeight alto del mapa en píxeles
     */
    public GameEngine(double mapWidth, double mapHeight) {
        this.mapWidth  = mapWidth;
        this.mapHeight = mapHeight;

        rules.add(new MovementRule());
        rules.add(new BoundaryRule());
        rules.add(new GrowthRule());
        rules.add(new SplitRule());
        rules.add(new AbsorptionRule());
        rules.add(new VictoryRule());

        scoreManager = new ScoreManager();
        addListener(scoreManager);

        currentState = new RunningState();
    }

    /**
     * Inicializa el mapa con jugadores, pellets y HazardBalls.
     * Solo debe llamarse una vez antes del primer {@link #update()}.
     *
     * @param playerNames lista de nombres de jugadores
     * @param initialMass masa inicial de cada célula
     */
    public void startGame(List<String> playerNames, double initialMass) {
        startTime = System.currentTimeMillis();
        finalElapsedTimeMs = 0;
        deadPlayers.clear();

        for (String name : playerNames) {
            double x = random.nextDouble() * (mapWidth  - 200) + 100;
            double y = random.nextDouble() * (mapHeight - 200) + 100;
            PlayerCell cell = new PlayerCell(name, x, y, initialMass);
            players.add(cell);
            scoreManager.registerPlayer(name);
        }

        for (int i = 0; i < MAX_PELLETS; i++) spawnPellet();
        for (int i = 0; i < MAX_HAZARDS;  i++) spawnHazardBall();
    }

    /**
     * Tick del juego. Solo el host ejecuta las reglas.
     * Llamar desde el game loop cada 50ms aproximadamente.
     */
    public void update() {
        if (isHost) {
            currentState.update(this);
        }
    }

    /**
     * Genera un nuevo Pellet en posición aleatoria si no se ha alcanzado el máximo.
     */
    public void spawnPellet() {
        if (pellets.size() >= MAX_PELLETS) return;
        double x = random.nextDouble() * mapWidth;
        double y = random.nextDouble() * mapHeight;
        pellets.add(new Pellet(x, y));
    }

    /**
     * Genera una nueva HazardBall en posición aleatoria.
     */
    public void spawnHazardBall() {
        double x = random.nextDouble() * mapWidth;
        double y = random.nextDouble() * mapHeight;
        hazardBalls.add(new HazardBall(x, y));
    }

    /**
     * Agrega una nueva célula al juego y la registra en el ScoreManager.
     *
     * @param cell célula a agregar
     */
    public void addPlayer(PlayerCell cell) {
        players.add(cell);
        scoreManager.registerPlayer(cell.getOwnerName());
    }

    /**
     * Elimina una célula del juego.
     * Si el jugador no tiene más células activas, se marca como eliminado.
     *
     * @param cell célula a eliminar
     */
    public void removePlayer(PlayerCell cell) {
        players.remove(cell);

        boolean isCompletelyDead = true;
        for (PlayerCell p : players) {
            if (p.getOwnerName().equals(cell.getOwnerName())) {
                isCompletelyDead = false;
                break;
            }
        }

        if (isCompletelyDead) {
            deadPlayers.add(cell.getOwnerName());
        }
    }

    /**
     * Elimina un Pellet del mapa.
     *
     * @param pellet pellet a eliminar
     */
    public void removePellet(Pellet pellet) {
        pellets.remove(pellet);
    }

    /**
     * Activa el estado de fin de juego y notifica a todos los oyentes.
     *
     * @param winnerName nombre del jugador ganador
     * @param reason     razón de la victoria
     */
    public void triggerGameOver(String winnerName, String reason) {
        finalElapsedTimeMs = System.currentTimeMillis() - startTime;
        currentState = new GameOverState(winnerName, reason);
        notifyGameOver(winnerName, reason);
    }

    /**
     * Registra un oyente de eventos del juego.
     *
     * @param listener oyente a registrar
     */
    public void addListener(IGameEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Elimina un oyente de eventos del juego.
     *
     * @param listener oyente a eliminar
     */
    public void removeListener(IGameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica a todos los oyentes que una célula absorbió a otra.
     *
     * @param absorber célula que absorbió
     * @param absorbed célula absorbida
     */
    public void notifyAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        listeners.forEach(l -> l.onAbsorption(absorber, absorbed));
    }

    /**
     * Notifica a todos los oyentes que una célula se dividió.
     *
     * @param original célula original
     * @param newCell  célula nueva generada
     */
    public void notifySplit(PlayerCell original, PlayerCell newCell) {
        listeners.forEach(l -> l.onSplit(original, newCell));
    }

    /**
     * Notifica a todos los oyentes que una célula comió un pellet.
     *
     * @param player célula que comió
     * @param pellet pellet comido
     */
    public void notifyPelletEaten(PlayerCell player, Pellet pellet) {
        listeners.forEach(l -> l.onPelletEaten(player, pellet));
    }

    /**
     * Notifica a todos los oyentes que el juego terminó.
     *
     * @param winnerName nombre del ganador
     * @param reason     razón de la victoria
     */
    public void notifyGameOver(String winnerName, String reason) {
        listeners.forEach(l -> l.onGameOver(winnerName, reason));
    }

    /**
     * Genera un snapshot inmutable del estado actual del juego
     * para serializar y enviar por UDP a los clientes.
     *
     * @return snapshot del estado actual
     */
    public GameSnapshot createSnapshot() {
        List<PlayerDTO> playerDTOs = players.stream()
                .map(p -> new PlayerDTO(p.getCellId(), p.getOwnerName(), p.getX(), p.getY(), p.getMass()))
                .toList();

        List<PelletDTO> pelletDTOs = pellets.stream()
                .map(p -> new PelletDTO(p.getPelletId(), p.getX(), p.getY()))
                .toList();

        List<HazardDTO> hazardDTOs = hazardBalls.stream()
                .map(h -> new HazardDTO(h.getHazardId(), h.getX(), h.getY()))
                .toList();

        return new GameSnapshot(
                playerDTOs, pelletDTOs, hazardDTOs,
                scoreManager.getAllScores(),
                getRemainingTimeMs(),
                currentState.getStateName()
        );
    }

    /**
     * Aplica un snapshot recibido del host al estado local del cliente.
     * Sincroniza jugadores, pellets, HazardBalls, puntajes y tiempo.
     * También dispara eventos de sonido detectando cambios de estado.
     *
     * @param snapshot snapshot recibido del host
     */
    public void applySnapshot(GameSnapshot snapshot) {

        Map<String, Double> oldMasses = new HashMap<>();
        Map<String, Integer> oldCellCounts = new HashMap<>();
        for (PlayerCell p : this.players) {
            oldMasses.put(p.getOwnerName(), oldMasses.getOrDefault(p.getOwnerName(), 0.0) + p.getMass());
            oldCellCounts.put(p.getOwnerName(), oldCellCounts.getOrDefault(p.getOwnerName(), 0) + 1);
        }

        Map<String, PlayerCell> currentPlayers = new HashMap<>();
        for (PlayerCell p : players) currentPlayers.put(p.getCellId(), p);

        players.clear();
        for (PlayerDTO dto : snapshot.getPlayers()) {
            PlayerCell existing = currentPlayers.get(dto.getId());
            if (existing != null) {
                existing.setX(dto.getX());
                existing.setY(dto.getY());
                existing.setMass(dto.getMass());
                players.add(existing);
            } else {
                players.add(new PlayerCell(dto.getOwner(), dto.getX(), dto.getY(), dto.getMass()));
            }
        }

        pellets.clear();
        for (PelletDTO dto : snapshot.getPellets()) {
            pellets.add(new Pellet(dto.getX(), dto.getY()));
        }

        hazardBalls.clear();
        for (HazardDTO dto : snapshot.getHazards()) {
            hazardBalls.add(new HazardBall(dto.getX(), dto.getY()));
        }

        scoreManager.setScores(snapshot.getScores());
        this.clientRemainingTimeMs = snapshot.getRemainingTimeMs();

        if (snapshot.getGameStateName().equals("FIN DEL JUEGO") && currentState.isRunning()) {
            this.finalElapsedTimeMs = GAME_DURATION_MS - snapshot.getRemainingTimeMs();
            this.currentState = new GameOverState(scoreManager.getLeader(), "Fin de partida");
        }

        if (!isHost) {
            Map<String, Double> newMasses = new HashMap<>();
            Map<String, Integer> newCellCounts = new HashMap<>();

            for (PlayerCell p : this.players) {
                newMasses.put(p.getOwnerName(), newMasses.getOrDefault(p.getOwnerName(), 0.0) + p.getMass());
                newCellCounts.put(p.getOwnerName(), newCellCounts.getOrDefault(p.getOwnerName(), 0) + 1);
            }

            for (String owner : newMasses.keySet()) {
                double oldM = oldMasses.getOrDefault(owner, 0.0);
                double newM = newMasses.get(owner);
                int oldC = oldCellCounts.getOrDefault(owner, 0);
                int newC = newCellCounts.get(owner);

                PlayerCell rep = null;
                for (PlayerCell p : this.players) {
                    if (p.getOwnerName().equals(owner)) { rep = p; break; }
                }

                if (newC > oldC)       notifySplit(rep, rep);
                else if (newC < oldC)  notifyAbsorption(rep, rep);
                else if (newM > oldM)  notifyPelletEaten(rep, new Pellet(0, 0));
            }

            for (String oldOwner : oldCellCounts.keySet()) {
                if (!newCellCounts.containsKey(oldOwner)) {
                    PlayerCell dummy = new PlayerCell(oldOwner, 0, 0, 0);
                    notifyAbsorption(dummy, dummy);
                }
            }
        }
    }

    /**
     * Retorna el tiempo restante de la partida en milisegundos.
     * El host calcula el tiempo localmente; el cliente usa el valor del snapshot.
     *
     * @return tiempo restante en milisegundos
     */
    public long getRemainingTimeMs() {
        if (!isHost) return clientRemainingTimeMs;

        if (!currentState.isRunning() && finalElapsedTimeMs > 0) {
            return Math.max(0, GAME_DURATION_MS - finalElapsedTimeMs);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, GAME_DURATION_MS - elapsed);
    }

    /**
     * Indica si el tiempo de la partida se agotó.
     *
     * @return true si el tiempo llegó a cero
     */
    public boolean isTimeUp() { return getRemainingTimeMs() == 0; }

    /**
     * Indica si el juego terminó.
     *
     * @return true si el estado actual no es RunningState
     */
    public boolean isGameOver() { return !currentState.isRunning(); }

    /**
     * Retorna el tiempo total transcurrido desde el inicio de la partida.
     *
     * @return tiempo transcurrido en milisegundos
     */
    public long getTotalElapsedMs() {
        if (!currentState.isRunning() && finalElapsedTimeMs > 0) return finalElapsedTimeMs;
        if (!isHost) return GAME_DURATION_MS - clientRemainingTimeMs;
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Actualiza el objetivo de movimiento de un jugador y activa el dash si fue solicitado.
     * Si el jugador no existe y aún está en curso la partida, lo registra automáticamente.
     *
     * @param playerName  nombre del jugador
     * @param targetX     coordenada X del mouse
     * @param targetY     coordenada Y del mouse
     * @param dashPressed true si el jugador presionó la tecla de dash
     */
    public void updatePlayerTarget(String playerName, double targetX, double targetY, boolean dashPressed) {
        boolean found = false;

        for (PlayerCell p : players) {
            if (p.getOwnerName().equals(playerName)) {
                p.setTarget(targetX, targetY);
                if (dashPressed) p.tryDash();
                found = true;
            }
        }

        if (!found && currentState.isRunning() && !deadPlayers.contains(playerName)) {
            double x = random.nextDouble() * (mapWidth  - 200) + 100;
            double y = random.nextDouble() * (mapHeight - 200) + 100;
            PlayerCell newCell = new PlayerCell(playerName, x, y, INITIAL_MASS);
            newCell.setTarget(targetX, targetY);
            addPlayer(newCell);
        }
    }

    /** @return lista de células activas */
    public List<PlayerCell>  getPlayers()      { return players; }

    /** @return lista de pellets activos */
    public List<Pellet>      getPellets()      { return pellets; }

    /** @return lista de HazardBalls activas */
    public List<HazardBall>  getHazardBalls()  { return hazardBalls; }

    /** @return lista de reglas del juego */
    public List<IGameRule>   getRules()        { return rules; }

    /** @return ancho del mapa */
    public double            getMapWidth()     { return mapWidth; }

    /** @return alto del mapa */
    public double            getMapHeight()    { return mapHeight; }

    /** @return gestor de puntajes */
    public ScoreManager      getScoreManager() { return scoreManager; }

    /** @return estado actual del juego */
    public IGameState        getCurrentState() { return currentState; }

    /** @param host true si este motor actúa como host */
    public void setHost(boolean host)          { this.isHost = host; }

    /** @return true si este motor actúa como host */
    public boolean isHost()                    { return isHost; }
}