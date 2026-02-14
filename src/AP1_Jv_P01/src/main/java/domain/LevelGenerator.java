package domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private final DungeonLevel level;
    private final Random random = new Random();

    public DungeonLevel getLevel() {
        return level;
    }

    public LevelGenerator(DungeonLevel level) {
        this.level = level;
    }

    public List<Item> generateLevelItems(int level, double struggleCounter) {
        List<Item> itemsAtLevel = new ArrayList<>();
        List<ItemTypes.Subtype> pool = buildWeightedPool();
        int baseItems = 9; //9 rooms
        int extraItems = (int) Math.round(level * struggleCounter);
        int totalItems = baseItems + extraItems;
        for (ItemTypes.Type t : ItemTypes.Type.values()) {
            if (struggleCounter < 0.35 && (t.equals(ItemTypes.Type.FOOD) || t.equals(ItemTypes.Type.MEDKIT))) continue;
            itemsAtLevel.add(new Item(buildSubtypeByWeight(t, random), 0, 0));
        }
        if (struggleCounter > 0.65 && struggleCounter < 0.8) {
            itemsAtLevel.add(new Item(buildSubtypeByWeight(ItemTypes.Type.FOOD, random), 0, 0));
            itemsAtLevel.add(new Item(buildSubtypeByWeight(ItemTypes.Type.FOOD, random), 0, 0));
        } else if (struggleCounter > 0.8) {
            itemsAtLevel.add(new Item(buildSubtypeByWeight(ItemTypes.Type.MEDKIT, random), 0, 0));
            itemsAtLevel.add(new Item(buildSubtypeByWeight(ItemTypes.Type.MEDKIT, random), 0, 0));
        }
        for (Item i : itemsAtLevel) {
            int room = random.nextInt(0, 9);
            i.setPosX(getLevel().getRandomRoomX(room));
            i.setPosY(getLevel().getRandomRoomY(room));
        }
        for (int i = 0; i < totalItems - itemsAtLevel.size(); i++) {
            int room = random.nextInt(0, 9);
            itemsAtLevel.add(new Item(pool.get(random.nextInt(pool.size()))
                    ,getLevel().getRandomRoomX(room), getLevel().getRandomRoomY(room)));
        }
        return itemsAtLevel;
    }

    public ItemTypes.Subtype buildSubtypeByWeight(ItemTypes.Type type, Random random) {
        List<ItemTypes.Subtype> list = new ArrayList<>();
        for (ItemTypes.Subtype s : ItemTypes.Subtype.values()) {
            if (s == ItemTypes.Subtype.FISTS) continue;
            if (s.getType() == type) list.add(s);
        }
        int totalWeight = list.stream().mapToInt(ItemTypes.Subtype::getWeight).sum();
        int roll = random.nextInt(totalWeight);
        for (ItemTypes.Subtype s : list) {
            roll -= s.getWeight();
            if (roll < 0) return s;
        }
        throw new IllegalStateException("weight failed");
    }

    private List<ItemTypes.Subtype> buildWeightedPool() {
        List<ItemTypes.Subtype> pool = new ArrayList<>();
        for (ItemTypes.Subtype s : ItemTypes.Subtype.values()) {
            if (s == ItemTypes.Subtype.FISTS) continue;
            for (int i = 0; i < s.getWeight(); i++) {
                pool.add(s);
            }
        }
        return pool;
    }

    public List<Enemy> generateLevelEnemies(int level, double struggleCounter) {
        List<Enemy> enemiesAtLevel = new ArrayList<>();
        int baseEnemies = 6;
        double ratio = 1.0 - struggleCounter;
        int extraEnemies = (int) Math.round(level * ratio * 0.5);
        int totalEnemies = baseEnemies + extraEnemies;
        Enemy.Type randomEnemy = null;
        for (int i = 0; i < totalEnemies; i++) {
            int room = random.nextInt(0, 9);
            if (level < 3) {
                randomEnemy = Enemy.Type.getEasyEnemy();
            }
            if (level >= 3 && struggleCounter >= 0.4) {
                randomEnemy = Enemy.Type.values()[random.nextInt(Enemy.Type.values().length)];
            } else {
                randomEnemy = Enemy.Type.getEasyEnemy();
            }
            if (randomEnemy != null) {
                enemiesAtLevel.add(new Enemy(randomEnemy,
                        getLevel().getRandomRoomX(room), getLevel().getRandomRoomY(room)));
            }
        }
        return enemiesAtLevel;
    }
}
