package main.java.edu.tue.dsvis.core.mvc;

/**
 * Minimal contract for controllers responsible for validating user input,
 * instantiating the appropriate {@link Model}, and connecting it to one or
 * more {@link View}s.
 */
public interface Controller {

    /**
     * Perform any necessary preprocessing, create the correct {@link Model},
     * and wire it to the {@link View}. Implementations should avoid heavy
     * computation here; long-running work should happen on background threads
     * or inside the {@link Model#run()} method.
     */
    void initialise();
} 