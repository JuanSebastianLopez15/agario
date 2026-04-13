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

public class GameEngine {

    private final double mapWidth;
    private final double mapHeight;
    private static final int MAX_PELLETS      = 80;
    private static final int MAX_HAZARDS      = 8;
    private static final long GAME_DURATION_MS = 3 * 60 * 1000L;
    private static final double INITIAL_MASS  = 50.0;

    private boolean isHost = false;
    private long clientRemainingTimeMs = GAME_DURATION_MS;

    private final List<PlayerCell>         players     = new CopyOnWriteArrayList<>();
    private final List<Pellet>             pellets     = new CopyOnWriteArrayList<>();
    private final List<HazardBall>         hazardBalls = new CopyOnWriteArrayList<>();

    private final List<IGameRule>          rules       = new ArrayList<>();
    private final List<IGameEventListener> listeners   = new ArrayList<>();

    private IGameState   currentState;
    private final ScoreManager scoreManager;
    private long startTime;
    private final Random random = new Random();

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

    public void startGame(List<String> playerNames, double initialMass) {
        startTime = System.currentTimeMillis();

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

    public void update() {
        if (isHost) {
            currentState.update(this);
        }
    }

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

    public void triggerGameOver(String winnerName, String reason) {
        currentState = new GameOverState(winnerName, reason);
        notifyGameOver(winnerName, reason);
    }

    public void addListener(IGameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IGameEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        listeners.forEach(l -> l.onAbsorption(absorber, absorbed));
    }

    public void notifySplit(PlayerCell original, PlayerCell newCell) {
        listeners.forEach(l -> l.onSplit(original, newCell));
    }

    public void notifyPelletEaten(PlayerCell player, Pellet pellet) {
        listeners.forEach(l -> l.onPelletEaten(player, pellet));
    }

    public void notifyGameOver(String winnerName, String reason) {
        listeners.forEach(l -> l.onGameOver(winnerName, reason));
    }

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

    public void applySnapshot(GameSnapshot snapshot) {

        int oldPlayerCells = this.players.size();
        double oldTotalMass = 0.0;
        for (PlayerCell p : this.players) oldTotalMass += p.getMass();

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
            this.currentState = new GameOverState(scoreManager.getLeader(), "Fin de partida");
        }

        if (!isHost) {
            int newPlayerCells = this.players.size();
            double newTotalMass = 0.0;
            for (PlayerCell p : this.players) newTotalMass += p.getMass();

            if (newPlayerCells < oldPlayerCells) {
                notifyAbsorption(null, null);
            } else if (newPlayerCells > oldPlayerCells) {
                notifySplit(null, null);
            } else if (newTotalMass > oldTotalMass) {
                notifyPelletEaten(null, null);
            }
        }
    }

    public long getRemainingTimeMs() {
        if (!isHost) return clientRemainingTimeMs;
        if (!currentState.isRunning()) return 0;
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, GAME_DURATION_MS - elapsed);
    }

    public boolean isTimeUp()    { return getRemainingTimeMs() == 0; }
    public boolean isGameOver()  { return !currentState.isRunning(); }

    public long getTotalElapsedMs() {
        if (!isHost) return GAME_DURATION_MS - clientRemainingTimeMs;
        return System.currentTimeMillis() - startTime;
    }

    public List<PlayerCell>  getPlayers()     { return players; }
    public List<Pellet>      getPellets()      { return pellets; }
    public List<HazardBall>  getHazardBalls()  { return hazardBalls; }
    public List<IGameRule>   getRules()        { return rules; }
    public double            getMapWidth()     { return mapWidth; }
    public double            getMapHeight()    { return mapHeight; }
    public ScoreManager      getScoreManager() { return scoreManager; }
    public IGameState        getCurrentState() { return currentState; }
    public void setHost(boolean host)          { this.isHost = host; }
    public boolean isHost()                    { return isHost; }

    public void updatePlayerTarget(String playerName, double targetX, double targetY) {
        boolean found = false;

        for (PlayerCell p : players) {
            if (p.getOwnerName().equals(playerName)) {
                p.setTarget(targetX, targetY);
                found = true;
            }
        }

        if (!found && currentState.isRunning()) {
            double x = random.nextDouble() * (mapWidth  - 200) + 100;
            double y = random.nextDouble() * (mapHeight - 200) + 100;
            PlayerCell newCell = new PlayerCell(playerName, x, y, INITIAL_MASS);
            newCell.setTarget(targetX, targetY);
            addPlayer(newCell);
        }
    }
}