package org.juanse.logic.engine;

import org.juanse.logic.entities.HazardBall;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;
import org.juanse.logic.observers.ScoreManager;
import org.juanse.logic.rules.*;
import org.juanse.logic.state.GameOverState;
import org.juanse.logic.state.IGameState;
import org.juanse.logic.state.RunningState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Núcleo del juego. Coordina entidades, reglas, estados y notificaciones.
 *
 * Principio S (SRP): coordina; no dibuja ni envía paquetes.
 * Principio D (DIP): depende de IGameRule y GameEventListener, no de concretos.
 *
 * Patrones:
 *  - Observer: notifica eventos a los oyentes registrados.
 *  - Strategy: delega la lógica a reglas (IGameRule) intercambiables.
 *  - State:    delega el comportamiento del tick al GameState actual.
 */
public class GameEngine {

    // ── Configuración del mapa ────────────────────────────────
    private final double mapWidth;
    private final double mapHeight;
    private static final int MAX_PELLETS   = 60;
    private static final int MAX_HAZARDS   = 8;
    private static final long GAME_DURATION_MS = 3 * 60 * 1000L; // 3 minutos

    // ── Estado del juego ──────────────────────────────────────
    private final List<PlayerCell>        players     = new ArrayList<>();
    private final List<Pellet>            pellets     = new ArrayList<>();
    private final List<HazardBall>        hazardBalls = new ArrayList<>();
    private final List<IGameRule>         rules       = new ArrayList<>();
    private final List<IGameEventListener> listeners   = new ArrayList<>();

    private IGameState currentState;
    private final ScoreManager scoreManager;
    private long startTime;
    private final Random random = new Random();

    // ── Constructor ───────────────────────────────────────────

    public GameEngine(double mapWidth, double mapHeight) {
        this.mapWidth  = mapWidth;
        this.mapHeight = mapHeight;

        // Registrar reglas (Strategy pattern) — orden importa
        rules.add(new BoundaryRule());
        rules.add(new GrowthRule());
        rules.add(new SplitRule());
        rules.add(new AbsorptionRule());
        rules.add(new VictoryRule());

        // El ScoreManager es un Observer integrado por defecto
        scoreManager = new ScoreManager();
        addListener(scoreManager);

        currentState = new RunningState();
    }

    // ── Ciclo de vida ─────────────────────────────────────────

    /**
     * Inicializa el mapa: coloca pellets, hazards y registra jugadores.
     * Llamar una sola vez antes del primer update().
     */
    public void startGame(List<String> playerNames, double initialMass) {
        startTime = System.currentTimeMillis();

        // Crear células iniciales para cada jugador
        for (String name : playerNames) {
            double x = random.nextDouble() * (mapWidth  - 200) + 100;
            double y = random.nextDouble() * (mapHeight - 200) + 100;
            PlayerCell cell = new PlayerCell(name, x, y, initialMass);
            players.add(cell);
            scoreManager.registerPlayer(name);
        }

        // Poblar mapa
        for (int i = 0; i < MAX_PELLETS; i++) spawnPellet();
        for (int i = 0; i < MAX_HAZARDS;  i++) spawnHazardBall();
    }

    /**
     * Tick del juego. Llamar desde un timer (ej. cada 50 ms).
     * El estado actual decide si se aplican reglas o no.
     */
    public void update() {
        currentState.update(this);
    }

    // ── API de spawn ──────────────────────────────────────────

    public void spawnPellet() {
        if (pellets.size() >= MAX_PELLETS) return;
        double x = random.nextDouble() * mapWidth;
        double y = random.nextDouble() * mapHeight;
        pellets.add(new Pellet(x, y));
    }

    public void spawnHazardBall() {
        double x = random.nextDouble() * mapWidth;
        double y = random.nextDouble() * mapHeight;
        hazardBalls.add(new HazardBall(x, y));
    }

    // ── Mutadores internos (usados por las reglas) ────────────

    public void addPlayer(PlayerCell cell) {
        players.add(cell);
        scoreManager.registerPlayer(cell.getOwnerName());
    }

    public void removePlayer(PlayerCell cell) {
        players.remove(cell);
    }

    public void removePellet(Pellet pellet) {
        pellets.remove(pellet);
    }

    /**
     * Activa el estado de fin de juego y notifica a todos los oyentes.
     */
    public void triggerGameOver(String winnerName, String reason) {
        currentState = new GameOverState(winnerName, reason);
        notifyGameOver(winnerName, reason);
    }

    // ── Observer: registro y notificaciones ───────────────────

    public void addListener(IGameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IGameEventListener listener) {
        listeners.remove(listener);
    }

    /** Llamado por AbsorptionRule */
    public void notifyAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        listeners.forEach(l -> l.onAbsorption(absorber, absorbed));
    }

    /** Llamado por SplitRule */
    public void notifySplit(PlayerCell original, PlayerCell newCell) {
        listeners.forEach(l -> l.onSplit(original, newCell));
    }

    /** Llamado por GrowthRule */
    public void notifyPelletEaten(PlayerCell player, Pellet pellet) {
        listeners.forEach(l -> l.onPelletEaten(player, pellet));
    }

    /** Llamado por VictoryRule */
    public void notifyGameOver(String winnerName, String reason) {
        listeners.forEach(l -> l.onGameOver(winnerName, reason));
    }

    // ── Snapshot (para el componente de red) ─────────────────

    /**
     * Genera una foto del estado actual para serializar y enviar por UDP.
     */
    public GameSnapshot createSnapshot() {
        return new GameSnapshot(
                Collections.unmodifiableList(players),
                Collections.unmodifiableList(pellets),
                Collections.unmodifiableList(hazardBalls),
                scoreManager.getAllScores(),
                getRemainingTimeMs(),
                currentState.getStateName()
        );
    }

    // ── Consultas de tiempo ───────────────────────────────────

    public long getRemainingTimeMs() {
        if (!currentState.isRunning()) return 0;
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, GAME_DURATION_MS - elapsed);
    }

    public boolean isTimeUp() {
        return getRemainingTimeMs() == 0;
    }

    public boolean isGameOver() {
        return !currentState.isRunning();
    }

    public long getTotalElapsedMs() {
        return System.currentTimeMillis() - startTime;
    }

    // ── Getters de estado (para reglas y UI) ──────────────────

    public List<PlayerCell>        getPlayers()     { return players; }
    public List<Pellet>            getPellets()      { return pellets; }
    public List<HazardBall>        getHazardBalls()  { return hazardBalls; }
    public List<IGameRule>         getRules()        { return rules; }
    public double                  getMapWidth()     { return mapWidth; }
    public double                  getMapHeight()    { return mapHeight; }
    public ScoreManager            getScoreManager() { return scoreManager; }
    public IGameState               getCurrentState() { return currentState; }
}

