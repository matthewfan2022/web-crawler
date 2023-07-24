package assignment;

public class Node {
    Node left = null;
    Node right = null;
    String value = null;

    public Node(String value, Node left, Node right)
    {
        this.left = left;
        this.right = right;
        this.value = value;
    }
}
