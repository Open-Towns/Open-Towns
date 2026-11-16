package xaos.api.selection;

import java.util.function.Consumer;

public interface SelectionApi {

    void selectArea(
            Consumer<AreaSelection> onSelected
    );

    boolean isSelecting();

    void cancel();
}