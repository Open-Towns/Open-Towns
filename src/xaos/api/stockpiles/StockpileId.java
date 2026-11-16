package xaos.api.stockpiles;

public record StockpileId(int value) {

    public StockpileId {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Stockpile ID cannot be negative"
            );
        }
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}