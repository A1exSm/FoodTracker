package org.alexander.database;

public class CompoundEntity<K1, K2> extends Entity<CompoundKey<K1,K2>>{
    public CompoundEntity(K1 keyOne, K2 keyTwo) {
        super(new CompoundKey<>(keyOne, keyTwo));
    }

    protected void updateKey(K1 keyOne, K2 keyTwo) {
        setKey(new CompoundKey<>(keyOne, keyTwo));
    }
}
