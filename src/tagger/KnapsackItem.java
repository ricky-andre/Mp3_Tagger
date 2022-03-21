package tagger;


public interface KnapsackItem
{
    float getWeight ();
    float getGain ();
    boolean moveTo (ContainerItem container);
}
