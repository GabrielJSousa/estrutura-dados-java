package aed.trees;

//this interface is used only to facilitate automated tests with Mooshak
public interface IUAlgTreeNode<Key extends Comparable<Key>,Value> {

    public IUAlgTreeNode<Key,Value> getLeft();
    public IUAlgTreeNode<Key,Value> getRight();
    public int getSize();
    public int getWeight();
    public Key getKey();
    public Value getValue();
}
