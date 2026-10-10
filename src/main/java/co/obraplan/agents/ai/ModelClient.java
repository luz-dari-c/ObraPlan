package co.obraplan.agents.ai;

import java.util.Map;

public interface ModelClient {
    Map<String, Object> generate(Map<String, Object> request);

    boolean configured();

    String model();
}
