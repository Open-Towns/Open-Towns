package xaos.internal.api.selection;

import java.util.function.Consumer;

import xaos.api.selection.AreaSelection;
import xaos.api.selection.SelectionApi;

public final class DefaultSelectionApi
        implements SelectionApi {

    @Override
    public void selectArea(
            Consumer<AreaSelection> onSelected
    ) {
        SelectionManager.beginAreaSelection(
                onSelected
        );
    }

    @Override
    public boolean isSelecting() {
        return SelectionManager.isSelecting();
    }

    @Override
    public void cancel() {
        SelectionManager.cancel();
    }
}