package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

public final class StockpileCopyToAllCommandHandler implements CommandHandler {

    private final StockpileCommandService stockpileService;

    public StockpileCopyToAllCommandHandler(StockpileCommandService stockpileService) {
        this.stockpileService = stockpileService;
    }

    @Override
    public void execute(CommandContext context) {
        stockpileService.copySettingsToMatchingStockpiles(context.getParameter());
    }
}