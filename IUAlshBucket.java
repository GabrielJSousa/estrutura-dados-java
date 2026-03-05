package aed.tables;

public interface IUAlshBucket<Key,Value> {
    public Key getKey();
    public Value getValue();
    public boolean isEmpty();
    public boolean isDeleted();
}
