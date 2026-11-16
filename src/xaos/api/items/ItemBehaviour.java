package xaos.api.items;

@FunctionalInterface
public interface ItemBehaviour {

    void onTick(
            ItemBehaviourContext context
    );
}