package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

public final class StockpileCopyToAllCommandHandler implements CommandHandler {

    private final StockpileCommandService stockpileCommandService;

    public StockpileCopyToAllCommandHandler() {
        this.stockpileCommandService = new StockpileCommandService();
    }

    @Override
    public void execute(CommandContext context) {
        stockpileCommandService.copyToAllMatchingStockpiles(
                context.getParameter()
        );
    }
}