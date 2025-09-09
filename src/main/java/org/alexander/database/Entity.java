package org.alexander.database;

public abstract class Entity<T> {
    private T key;

    public Entity(T key) {
        this.key = key;
    }

    public T getKey() {
        return key;
    }

    protected void setKey(T key) {
        this.key = key;
    }
}
