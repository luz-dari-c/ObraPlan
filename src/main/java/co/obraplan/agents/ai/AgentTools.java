package co.obraplan.agents.ai;

import java.util.Map;

public interface AgentTools {
    Map<String, Object> execute(String name, Map<String, Object> arguments);

    String revision(String question);

    Map<String, Object> save(Map<String, Object> result);
}
