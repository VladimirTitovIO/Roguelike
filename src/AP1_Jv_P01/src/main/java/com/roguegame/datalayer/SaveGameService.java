//package datalayer;
//
//import com.roguegame.datalayer.SaveMapper;
//import com.roguegame.datalayer.SaveModels;
//import com.roguegame.datalayer.SaveRepository;
//import com.roguegame.domain.*;
//import com.roguegame.domain.Character;
//
//import java.nio.file.Path;
//import java.util.Optional;
//
//public class SaveGameService {
//
//    private final SaveRepository repository;
//
//    public SaveGameService() {
//        this.repository = new SaveRepository(Path.of("savegame.json"));
//    }
//
//    // SAVE
//    public void saveGame(Character player,
//                         DungeonLevel level,
//                         DungeonLevel.Position playerPos,
//                         int levelNumber,
//                         int score) throws Exception {
//
//        SaveModels.Session session =
//                SaveMapper.toSession(player, level, playerPos, levelNumber, score);
//
//        repository.saveSession(session);
//    }
//
//    //  LOAD
//    public Optional<LoadedGame> loadGame() {
//
//        Optional<SaveModels.Session> opt = repository.loadLastSession();
//        if (opt.isEmpty()) return Optional.empty();
//
//        SaveModels.Session s = opt.get();
//
//        Character player = SaveMapper.toCharacter(s.player);
//        DungeonLevel level = SaveMapper.toDungeonLevel(s.dungeon);
//        DungeonLevel.Position pos =
//                new DungeonLevel.Position(s.playerPos.x, s.playerPos.y);
//
//        return Optional.of(new LoadedGame(
//                player,
//                level,
//                pos,
//                s.levelNumber,
//                s.score
//        ));
//    }
//
//    //  END GAME
//    public void finishGame(int score, int levelNumber, boolean win) throws Exception {
//        SaveModels.Attempt a = new SaveModels.Attempt();
//        a.finishedAt = System.currentTimeMillis();
//        a.score = score;
//        a.levelReached = levelNumber;
//        a.success = win;
//
//        repository.addAttempt(a);
//        repository.clearSession();
//    }
//
//    // DTO для возврата
//    public record LoadedGame(
//            Character player,
//            DungeonLevel level,
//            DungeonLevel.Position playerPos,
//            int levelNumber,
//            int score
//    ) {}
//}
///*
//in main:
//SaveGameService saveService = new SaveGameService();
//
//при старте:
//Optional<SaveGameService.LoadedGame> loaded = saveService.loadGame();
//
//Character player;
//DungeonLevel level;
//DungeonLevel.Position playerPos;
//int levelNumber;
//int score;
//
//if (loaded.isPresent()) {
//var g = loaded.get();
//player = g.player();
//level = g.level();
//playerPos = g.playerPos();
//levelNumber = g.levelNumber();
//score = g.score();
//
//    System.out.println("Continue game loaded!");
//} else {
//player = new Character(10,10,3,3,0);
//level = new DungeonLevel();
//playerPos = level.getStartPosition();
//levelNumber = 1;
//score = 0;
//        }
//
//после прохождения уровня:
//saveService.saveGame(player, level, playerPos, levelNumber, score);
//
//при окончании игры:
//saveService.finishGame(score, levelNumber, win);*/
//
