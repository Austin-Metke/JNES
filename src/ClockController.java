public class ClockController {
    private long lastTime = System.nanoTime();
    private final int targetCyclesPerSecond;

    public ClockController(int targetHz) {
        this.targetCyclesPerSecond = targetHz;
    }

    public void throttle(int cyclesExecuted) {
        long expectedTime = (long)((1_000_000_000L * (double) cyclesExecuted) / targetCyclesPerSecond);
        long now = System.nanoTime();
        long elapsed = now - lastTime;

        if (expectedTime > elapsed) {
            try {
                long sleepTime = (expectedTime - elapsed) / 1_000_000L;
                if (sleepTime > 0) Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        lastTime = System.nanoTime();
    }
}
