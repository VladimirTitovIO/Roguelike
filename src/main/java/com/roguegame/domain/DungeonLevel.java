package com.roguegame.domain;

import java.util.*;

public class DungeonLevel implements GameMap {

    public static final int WIDTH = 78;
    public static final int HEIGHT = 21;

    private static final int ROOMS_IN_WIDTH = 3;
    private static final int ROOMS_IN_HEIGHT = 3;
    private static final int ROOMS_NUM = ROOMS_IN_WIDTH * ROOMS_IN_HEIGHT;

    private static final int REGION_WIDTH = WIDTH / ROOMS_IN_WIDTH;      // 26
    private static final int REGION_HEIGHT = HEIGHT / ROOMS_IN_HEIGHT;   // 7

    private static final int MIN_ROOM_WIDTH = 10;
    private static final int MAX_ROOM_WIDTH = 20;
    private static final int MIN_ROOM_HEIGHT = 5;
    private static final int MAX_ROOM_HEIGHT = 6;

    private static final int MIN_INNER_SIZE = 5;

    private static final int MAX_GENERATION_ATTEMPTS = 120;

    private static final int MIN_LOCKED_DOORS = 1;
    private static final int MAX_LOCKED_DOORS = 3;

    private Position startPosition;
    private Position exitPosition;
    // tiles[x][y]
    private TileType[][] tiles;
    private final Random random = new Random();

    private Room[] rooms;
    private Room startRoom;
    private Room endRoom;

    // ---------- Doors & Keys (Task 6) ----------
    public enum DoorColor { RED, BLUE, YELLOW }

    public static class Door {
        public final DoorColor color;
        public boolean locked;

        public Door(DoorColor color, boolean locked) {
            this.color = color;
            this.locked = locked;
        }
    }

    // Position must have equals/hashCode for Maps
    public static class Position {
        public final int x, y;
        public Position(int x, int y) { this.x = x; this.y = y; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Position p)) return false;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
        @Override public String toString() { return "(" + x + "," + y + ")"; }
    }

    // doors: position -> Door
    private final Map<Position, Door> doors = new HashMap<>();
    // keys: position -> color
    private final Map<Position, DoorColor> keysOnGround = new HashMap<>();
    // list of all door tile positions
    private final List<Position> doorPositions = new ArrayList<>();

    public DungeonLevel() {
        generateOrThrow();
    }

    // -------------------- Generation Pipeline --------------------

    private void generateOrThrow() {
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            initEmpty();
            generateRooms();
            generatePassagesWithDoors();
            pickStartEndRooms();

            if (!generateLocksAndKeysNoSoftlock()) {
                continue;
            }

            // ВАЖНО: "запечатываем" рамку ПОСЛЕ всей генерации (иначе коридоры/комнаты могли ее перезаписать)
            enforceBorderWalls();

            // После запечатывания рамки ещё раз проверим отсутствие софтлока
            if (validateNoSoftlockBfs()) {
                return;
            }
        }
        throw new IllegalStateException("Failed to generate a valid dungeon without softlocks after many attempts.");
    }

    private void initEmpty() {
        tiles = new TileType[WIDTH][HEIGHT];
        rooms = new Room[ROOMS_NUM];

        doors.clear();
        keysOnGround.clear();
        doorPositions.clear();

        // Fill all with walls
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[x][y] = TileType.WALL;
            }
        }
        startPosition = null;
        exitPosition = null;
        // Можно сразу поставить рамку, но всё равно в конце мы её запечатываем ещё раз
        enforceBorderWalls();
    }

    private void pickStartEndRooms() {
        int startIdx = random.nextInt(ROOMS_NUM);
        int endIdx = random.nextInt(ROOMS_NUM);
        while (endIdx == startIdx) endIdx = random.nextInt(ROOMS_NUM);
        startRoom = rooms[startIdx];
        endRoom = rooms[endIdx];

        startPosition = getSafePosition(startRoom);
        exitPosition = getSafePosition(endRoom);
    }

    /**
     * Жёсткая рамка стен.
     * Плюс: если дверь/ключ попали на рамку — удаляем, чтобы не ломали визуализацию.
     */
    private void enforceBorderWalls() {
        // top & bottom
        for (int x = 0; x < WIDTH; x++) {
            tiles[x][0] = TileType.WALL;
            tiles[x][HEIGHT - 1] = TileType.WALL;
        }
        // left & right
        for (int y = 0; y < HEIGHT; y++) {
            tiles[0][y] = TileType.WALL;
            tiles[WIDTH - 1][y] = TileType.WALL;
        }

        // Remove doors/keys on border if any
        doors.keySet().removeIf(p -> isBorder(p.x, p.y));
        keysOnGround.keySet().removeIf(p -> isBorder(p.x, p.y));

        // также чистим doorPositions (иначе можно выбрать для lock дверь на рамке)
        doorPositions.removeIf(p -> isBorder(p.x, p.y));
    }

    private boolean isBorder(int x, int y) {
        return x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1;
    }

    // -------------------- Rooms --------------------

    private void generateRooms() {
        for (int i = 0; i < ROOMS_NUM; i++) {
            int secCol = i % ROOMS_IN_WIDTH;
            int secRow = i / ROOMS_IN_WIDTH;

            int secLeft = secCol * REGION_WIDTH;
            int secTop = secRow * REGION_HEIGHT;

            int w = MIN_ROOM_WIDTH + random.nextInt(MAX_ROOM_WIDTH - MIN_ROOM_WIDTH + 1);
            int h = MIN_ROOM_HEIGHT + random.nextInt(MAX_ROOM_HEIGHT - MIN_ROOM_HEIGHT + 1);

            // Ensure minimum inner floor (w/h include walls)
            if (w < MIN_INNER_SIZE + 2) w = MIN_INNER_SIZE + 2;
            if (h < MIN_INNER_SIZE + 2) h = MIN_INNER_SIZE + 2;

            // Center in region with small jitter
            int x = secLeft + (REGION_WIDTH - w) / 2 + random.nextInt(5) - 2;
            int y = secTop + (REGION_HEIGHT - h) / 2 + random.nextInt(3) - 1;

            // Clamp inside section (but not too tight)
            x = Math.max(secLeft + 2, Math.min(x, secLeft + REGION_WIDTH - w - 2));
            y = Math.max(secTop + 1, Math.min(y, secTop + REGION_HEIGHT - h - 1));

            int x1 = x;
            int y1 = y;
            int x2 = x + w - 1;
            int y2 = y + h - 1;

            // Clamp inside map first
            x2 = Math.min(x2, WIDTH - 1);
            y2 = Math.min(y2, HEIGHT - 1);

            // >>> IMPORTANT FIX: keep rooms strictly inside border (1..WIDTH-2, 1..HEIGHT-2)
            x1 = Math.max(1, x1);
            y1 = Math.max(1, y1);
            x2 = Math.min(WIDTH - 2, x2);
            y2 = Math.min(HEIGHT - 2, y2);

            // If room became too small (need at least wall+floor+wall => size >= 4)
            if (x2 - x1 < 4) x2 = Math.min(WIDTH - 2, x1 + 4);
            if (y2 - y1 < 4) y2 = Math.min(HEIGHT - 2, y1 + 4);

            rooms[i] = new Room(x1, y1, x2, y2);

            // Fill floor inside (leave walls as WALL)
            for (int rx = x1 + 1; rx < x2; rx++) {
                for (int ry = y1 + 1; ry < y2; ry++) {
                    // дополнительная защита: не трогаем рамку
                    if (!isBorder(rx, ry)) {
                        tiles[rx][ry] = TileType.FLOOR;
                    }
                }
            }
        }
    }

    // -------------------- Passages + Doors between room and corridor --------------------

    private void generatePassagesWithDoors() {
        List<Edge> edges = new ArrayList<>();
        generateEdgesForRooms(edges);

        // Connect all neighbors -> connectivity
        for (Edge e : edges) {
            createPassageWithDoor(rooms[e.u], rooms[e.v]);
        }

        // Extra connections
        int extra = random.nextInt(3) + 1;  // 1..3
        Collections.shuffle(edges, random);
        for (int i = 0; i < extra && i < edges.size(); i++) {
            createPassageWithDoor(rooms[edges.get(i).u], rooms[edges.get(i).v]);
        }
    }

    private void generateEdgesForRooms(List<Edge> edges) {
        for (int r = 0; r < ROOMS_IN_HEIGHT; r++) {
            for (int c = 0; c < ROOMS_IN_WIDTH - 1; c++) {
                int u = r * ROOMS_IN_WIDTH + c;
                edges.add(new Edge(u, u + 1));
            }
        }
        for (int r = 0; r < ROOMS_IN_HEIGHT - 1; r++) {
            for (int c = 0; c < ROOMS_IN_WIDTH; c++) {
                int u = r * ROOMS_IN_WIDTH + c;
                edges.add(new Edge(u, u + ROOMS_IN_WIDTH));
            }
        }
    }

    private void createPassageWithDoor(Room a, Room b) {
        int ax = a.getCenterX();
        int ay = a.getCenterY();
        int bx = b.getCenterX();
        int by = b.getCenterY();

        boolean horizontal = Math.abs(ax - bx) > Math.abs(ay - by);

        if (horizontal) {
            int y = (ay + by) / 2;
            y = Math.max(1, Math.min(y, HEIGHT - 2)); // inside border

            int left = Math.min(a.x2, b.x1) + 1;
            int right = Math.max(a.x1, b.x2) - 1;

            left = Math.max(1, left);
            right = Math.min(WIDTH - 2, right);

            for (int x = left; x <= right; x++) {
                if (!isBorder(x, y)) tiles[x][y] = TileType.CORRIDOR;
            }

            // Doors in room walls
            setDoor(a.x2, y);
            setDoor(b.x1, y);

        } else {
            int x = (ax + bx) / 2;
            x = Math.max(1, Math.min(x, WIDTH - 2)); // inside border

            int top = Math.min(a.y2, b.y1) + 1;
            int bottom = Math.max(a.y1, b.y2) - 1;

            top = Math.max(1, top);
            bottom = Math.min(HEIGHT - 2, bottom);

            for (int y = top; y <= bottom; y++) {
                if (!isBorder(x, y)) tiles[x][y] = TileType.CORRIDOR;
            }

            // Doors in room walls
            setDoor(x, a.y2);
            setDoor(x, b.y1);
        }
    }

    private void setDoor(int x, int y) {
        if (!inBounds(x, y)) return;
        if (isBorder(x, y)) return; // never place a door on border

        tiles[x][y] = TileType.DOOR;

        Position p = new Position(x, y);
        if (!doors.containsKey(p)) {
            // Color/locked will be assigned later
            doors.put(p, new Door(DoorColor.RED, false));
            doorPositions.add(p);
        }
    }

    //  Locks + Keys + Anti-softlock validation

    private boolean generateLocksAndKeysNoSoftlock() {

        if (doorPositions.isEmpty()) return false;

        keysOnGround.clear();

        // All doors unlocked first
        for (Position p : new ArrayList<>(doors.keySet())) {
            Door d = doors.get(p);
            if (d == null) continue;
            doors.put(p, new Door(DoorColor.RED, false));
        }

        int possible = Math.min(MAX_LOCKED_DOORS, doorPositions.size());
        int lockedCount = Math.max(MIN_LOCKED_DOORS, 1 + random.nextInt(possible));

        List<Position> candidates = new ArrayList<>(doorPositions);
        Collections.shuffle(candidates, random);
        List<Position> lockedDoors = candidates.subList(0, lockedCount);

        List<DoorColor> colors = new ArrayList<>(Arrays.asList(DoorColor.values()));
        Collections.shuffle(colors, random);
        colors = colors.subList(0, lockedCount);

        int keyMask = 0;

        for (int i = 0; i < lockedCount; i++) {
            Position doorPos = lockedDoors.get(i);
            if (isBorder(doorPos.x, doorPos.y)) return false; // extra safety

            DoorColor color = colors.get(i);

            // lock door
            doors.put(doorPos, new Door(color, true));

            // reachable with current mask (without this new key)
            Set<Position> reachable = reachablePositionsWithMask(keyMask);

            // place key on reachable FLOOR
            Position keyPos = pickKeyPosition(reachable);
            if (keyPos == null) return false;

            keysOnGround.put(keyPos, color);

            // now we can have this key
            keyMask |= (1 << color.ordinal());
        }

        // random colors for unlocked doors (for visualization)
        for (Position p : doorPositions) {
            Door d = doors.get(p);
            if (d == null) continue;
            if (!d.locked) {
                DoorColor any = DoorColor.values()[random.nextInt(DoorColor.values().length)];
                doors.put(p, new Door(any, false));
            }
        }

        return validateNoSoftlockBfs();
    }

    private Position pickKeyPosition(Set<Position> reachable) {
        List<Position> options = new ArrayList<>();
        for (Position p : reachable) {
            if (isBorder(p.x, p.y)) continue;
            if (p.equals(startPosition) || p.equals(exitPosition)) continue;
            if (getTile(p.x, p.y) != TileType.FLOOR) continue;
            options.add(p);
        }
        if (options.isEmpty()) return null;
        return options.get(random.nextInt(options.size()));
    }

    private Set<Position> reachablePositionsWithMask(int keyMask) {
        ArrayDeque<Position> q = new ArrayDeque<>();
        Set<Position> seen = new HashSet<>();

        q.add(startPosition);
        seen.add(startPosition);

        while (!q.isEmpty()) {
            Position cur = q.removeFirst();

            for (int[] d : DIR4) {
                int nx = cur.x + d[0];
                int ny = cur.y + d[1];
                if (!inBounds(nx, ny)) continue;

                if (!canEnter(nx, ny, keyMask)) continue;

                Position np = new Position(nx, ny);
                if (seen.add(np)) q.addLast(np);
            }
        }
        return seen;
    }

    private boolean validateNoSoftlockBfs() {
        int maxMask = 1 << DoorColor.values().length; // 8
        boolean[][][] seen = new boolean[WIDTH][HEIGHT][maxMask];
        ArrayDeque<State> q = new ArrayDeque<>();

        q.add(new State(startPosition.x, startPosition.y, 0));
        seen[startPosition.x][startPosition.y][0] = true;

        while (!q.isEmpty()) {
            State s = q.removeFirst();

            int mask = s.mask;

            DoorColor key = keysOnGround.get(new Position(s.x, s.y));
            if (key != null) {
                mask |= (1 << key.ordinal());
            }

            if (s.x == exitPosition.x && s.y == exitPosition.y) {
                return true;
            }

            for (int[] d : DIR4) {
                int nx = s.x + d[0];
                int ny = s.y + d[1];
                if (!inBounds(nx, ny)) continue;

                if (!canEnter(nx, ny, mask)) continue;

                if (!seen[nx][ny][mask]) {
                    seen[nx][ny][mask] = true;
                    q.addLast(new State(nx, ny, mask));
                }
            }
        }
        return false;
    }

    private record State(int x, int y, int mask) {}

    private boolean canEnter(int x, int y, int mask) {
        TileType t = getTile(x, y);
        if (t == TileType.WALL) return false;
        if (t == TileType.FLOOR || t == TileType.CORRIDOR) return true;

        if (t == TileType.DOOR) {
            Door d = doors.get(new Position(x, y));
            if (d == null) return true;
            if (!d.locked) return true;
            int bit = 1 << d.color.ordinal();
            return (mask & bit) != 0;
        }
        return false;
    }

    private static final int[][] DIR4 = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0}
    };

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    // -------------------- GameMap interface --------------------

    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public TileType getTile(int x, int y) {
        if (!inBounds(x, y)) return TileType.WALL;
        return tiles[x][y];
    }

    @Override
    public boolean isWalkable(int x, int y) {
        TileType t = getTile(x, y);
        return t == TileType.FLOOR || t == TileType.CORRIDOR || t == TileType.DOOR;
    }

    // -------------------- Public helpers for other game logic --------------------

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getExitPosition() {
        return exitPosition;
    }

    public Map<Position, Door> getDoors() {
        return Collections.unmodifiableMap(doors);
    }

    public Map<Position, DoorColor> getKeysOnGround() {
        return Collections.unmodifiableMap(keysOnGround);
    }

    public void loadFromSave(
            TileType[][] newTiles,
            Position newStart,
            Position newExit,
            Map<Position, Door> newDoors,
            Map<Position, DoorColor> newKeysOnGround
    ) {
        this.tiles = newTiles;
        this.startPosition = newStart;
        this.exitPosition = newExit;

        this.doors.clear();
        this.doors.putAll(newDoors);

        this.keysOnGround.clear();
        this.keysOnGround.putAll(newKeysOnGround);

        this.doorPositions.clear();
        this.doorPositions.addAll(newDoors.keySet());
    }

    public Optional<DoorColor> pickupKeyAt(int x, int y) {
        DoorColor c = keysOnGround.remove(new Position(x, y));
        return Optional.ofNullable(c);
    }

    public boolean tryOpenDoor(int x, int y, Set<DoorColor> keys) {
        Door d = doors.get(new Position(x, y));
        if (d == null) return false;
        if (!d.locked) return true;
        if (keys != null && keys.contains(d.color)) {
            d.locked = false;
            return true;
        }
        return false;
    }

    // -------------------- Safe position inside room --------------------

    private Position getSafePosition(Room r) {
        // pick random floor cell inside room
        int innerW = r.x2 - r.x1 - 1;
        int innerH = r.y2 - r.y1 - 1;

        if (innerW < 1 || innerH < 1) {
            int px = Math.min(r.x1 + 1, r.x2);
            int py = Math.min(r.y1 + 1, r.y2);
            px = Math.max(1, Math.min(px, WIDTH - 2));
            py = Math.max(1, Math.min(py, HEIGHT - 2));
            return new Position(px, py);
        }

        int x = random.nextInt(innerW) + r.x1 + 1;
        int y = random.nextInt(innerH) + r.y1 + 1;

        // never return border
        x = Math.max(1, Math.min(x, WIDTH - 2));
        y = Math.max(1, Math.min(y, HEIGHT - 2));
        return new Position(x, y);
    }

    // -------------------- Helper classes --------------------

    private static class Room {
        final int x1, y1, x2, y2;
        Room(int x1, int y1, int x2, int y2) {
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
        }
        int getCenterX() { return (x1 + x2) / 2; }
        int getCenterY() { return (y1 + y2) / 2; }
    }

    private static class Edge {
        final int u, v;
        Edge(int u, int v) { this.u = u; this.v = v; }
    }

//=====================ADD-ONS-SORENLEN====================
        public static int getRoomsNumber()  {
            return ROOMS_NUM;
        }
        //check whether there is a room containing given coordinates
        public int getRoomAt(int x, int y) {
            for (int i = 0; i < rooms.length; i++) {
                Room current = rooms[i];
                if (x > current.x1 && x < current.x2
                        && y > current.y1 && y < current.y2) {
                    return i;
                }
            }
            return -1;
        }
        public int getRandomRoomX(int roomNum) {
            return random.nextInt(rooms[roomNum].x1 + 1, rooms[roomNum].x2);
        }

        public int getRandomRoomY(int roomNum) {
            return random.nextInt(rooms[roomNum].y1 + 1, rooms[roomNum].y2);
        }
}
