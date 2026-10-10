package co.obraplan.agents;

/** Resultado auditable: agente, mensaje, material y nota que sustenta la regla. */
public record Insight(
        String agent,
        String title,
        String detail,
        String severity,
        String materialId,
        String source) {}
