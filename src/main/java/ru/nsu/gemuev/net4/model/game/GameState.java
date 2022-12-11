package ru.nsu.gemuev.net4.model.game;

import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.model.game.presentation.Cell;
import ru.nsu.gemuev.net4.model.game.presentation.CellType;
import ru.nsu.gemuev.net4.model.game.presentation.FieldPresenter;

import java.util.*;


//TODO возвращать неизменяемые обертки
public class GameState {

    private final List<Player> players = new ArrayList<>();
    private final List<Snake> snakes = new ArrayList<>();
    private final List<Coordinate> foods = new ArrayList<>();
    @Getter
    private final GameConfig gameConfig;
    @Getter
    private int stateOrder = 0;

    public List<Snake> getSnakes() {
        return Collections.unmodifiableList(snakes);
    }

    public List<Coordinate> getFoods() {
        return Collections.unmodifiableList(foods);
    }

    public List<Player> getPlayers(){
        return Collections.unmodifiableList(players);
    }

    public GameState(@NonNull GameConfig gameConfig,
                     @NonNull Collection<? extends Snake> snakes,
                     @NonNull Collection<? extends Player> players,
                     @NonNull Collection<? extends Coordinate> foods,
                     int stateOrder) {
        this.snakes.addAll(snakes);
        this.players.addAll(players);
        this.foods.addAll(foods);
        this.gameConfig = gameConfig;
        this.stateOrder = stateOrder;
    }

    public GameState(@NonNull GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        generateFood();
    }

    private void generateFood() {
        Random random = new Random();
        var field = FieldPresenter.fieldPresentation(this);
        while (foods.size() < gameConfig.foodStatic()) {
            int x = random.nextInt(gameConfig.width());
            int y = random.nextInt(gameConfig.height());
            if (field[x][y].getType() == CellType.FREE) {
                field[x][y] = new Cell(CellType.FOOD, -1);
                foods.add(new Coordinate(x, y));
            }
        }
    }

    private boolean isAreaWithoutSnakes(Cell[][] field, int leftX, int topY) {
        for (int x = leftX; x < leftX + 5; ++x) {
            for (int y = topY; y < topY + 5; ++y) {
                if (field[x][y].getType() == CellType.SNAKE_BODY) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addPlayer(@NonNull Player player) {
        var field = FieldPresenter.fieldPresentation(this);
        for (int i = 0; i < gameConfig.width() - 4; ++i) {
            for (int j = 0; j < gameConfig.height() - 4; ++j) {
                if (isAreaWithoutSnakes(field, i, j) && field[i + 2][j + 2].getType() == CellType.FREE
                        && field[i + 2][j + 3].getType() == CellType.FREE) {

                    Snake snake = new Snake(gameConfig.width(), gameConfig.height(),
                            Direction.UP, new Coordinate(i + 2, j + 2), player.getId());
                    snake.grow();
                    snakes.add(snake);
                    players.add(player);
                    return true;
                }
            }
        }
        return false;
    }

    public void steer(int snakeId,
                      @NonNull Direction newDirection) {
        snakes.stream()
                .filter(snake -> snake.getPlayerId() == snakeId && snake.getSnakeState() == SnakeState.ALIVE)
                .findAny()
                .ifPresent(snake -> snake.setHeadDirection(newDirection));
    }

    private List<Murder> findMurders() {
        List<Murder> murders = new ArrayList<>();
        snakes.forEach(snake -> {
            for (Snake otherSnake : snakes) {
                if (snake.isSuicide()) {
                    murders.add(new Murder(snake.getPlayerId(), snake.getPlayerId()));
                    break;
                }
                if (otherSnake.getPlayerId() != snake.getPlayerId()
                        && otherSnake.isContain(snake.getHead().x(), snake.getHead().y())) {
                    murders.add(new Murder(otherSnake.getPlayerId(), snake.getPlayerId()));
                }
            }
        });

        return murders;
    }

    public Optional<Player> findPlayerById(int playerId){
        return players.stream().filter(player -> player.getId() == playerId).findAny();
    }

    public void nextState() {
        Set<Coordinate> ateFoods = new HashSet<>();
        for (Snake snake : snakes) {
            snake.forward();
            Coordinate headCoord = snake.getHeadCoordinate();
            if (foods.contains(headCoord)) {
                snake.grow();
                ateFoods.add(headCoord);
                findPlayerById(snake.getPlayerId()).ifPresent(Player::increaseScore);
            }
        }
        foods.removeAll(ateFoods);

        List<Murder> murders = findMurders();
        Random random = new Random();
        List<Snake> deadSnakes = murders.stream()
                .peek(murder -> findPlayerById(murder.killerId()).ifPresent(Player::increaseScore))
                .map(Murder::victimId)
                .distinct()
                .flatMap(victim -> snakes.stream()
                        .filter(s -> s.getPlayerId() == victim)).toList();

        deadSnakes.forEach(dead -> {
            players.removeIf(player -> player.getId() == dead.getPlayerId());
            for(int i=1; i<dead.getBody().size(); ++i){
                if(random.nextBoolean()){
                    foods.add(dead.getBody().get(i).getCoordinate());
                }
            }
        });
        snakes.removeAll(deadSnakes);

        generateFood();
        ++stateOrder;
    }

    public void playerLeave(int playerId){
        snakes.stream()
                .filter(s -> s.getPlayerId() == playerId).findAny()
                .ifPresent(s -> s.setSnakeState(SnakeState.ZOMBIE));
        players.removeIf(player -> player.getId() == playerId);
    }
}
