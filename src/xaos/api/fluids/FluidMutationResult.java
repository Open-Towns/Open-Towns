package xaos.api.fluids;

public record FluidMutationResult(
        boolean changed,
        String message
) {

    public static FluidMutationResult success() {
        return new FluidMutationResult(
                true,
                null
        );
    }

    public static FluidMutationResult rejected(
            String message
    ) {
        return new FluidMutationResult(
                false,
                message
        );
    }
}