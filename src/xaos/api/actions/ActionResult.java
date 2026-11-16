package xaos.api.actions;

public record ActionResult(
        boolean accepted,
        String message
) {

    public static ActionResult success() {
        return new ActionResult(
                true,
                null
        );
    }

    public static ActionResult rejected(
            String message
    ) {
        return new ActionResult(
                false,
                message
        );
    }
}