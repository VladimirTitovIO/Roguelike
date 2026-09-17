package com.roguegame.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Backpack {
    private final int capacity;
    private final List<Item> items;
    private final Map<ItemTypes.Type, Integer> typeLimits;

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public long getCount(ItemTypes.Type type) {
        return countPerType(type);
    }

    public int getEmptySlots(ItemTypes.Type type) {
        return typeLimits.get(type) - (int) countPerType(type);
    }

    public Backpack(int capacity) {
        this.capacity = capacity;
        items = new ArrayList<Item>(capacity);
        this.typeLimits = Map.of(
                ItemTypes.Type.WEAPON, 9,
                ItemTypes.Type.MEDKIT, 9,
                ItemTypes.Type.SCROLLS, 9,
                ItemTypes.Type.FOOD, 9,
                ItemTypes.Type.ELIXIR, 9,
                ItemTypes.Type.TREASURE, 9
        );
    }

    private long countPerType(ItemTypes.Type type) {
        return items.stream().filter(i -> i.getType().equals(type)).count();
    }

    public boolean addItem(Item item) {
        if (items.size() >= capacity) {
            return false;
        }
        long sizePerType = countPerType(item.getType());
        if (sizePerType >= typeLimits.get(item.getType())) {
            return false;
        }
        items.add(item);
        return true;
    }

    public boolean useItem(Item item, Character target) {
        for (Item i : getItems()) {
            if (i.getType() != item.getType() || i.getSubtype() != item.getSubtype()) {
                continue;
            }
            item.use(target);
            items.remove(i);
            return true;
        }
        return false;
    }
    public void clear() {
        items.clear();
    }


}
