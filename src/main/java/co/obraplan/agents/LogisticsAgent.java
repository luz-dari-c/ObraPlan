package co.obraplan.agents;

import static co.obraplan.agents.AgentData.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LogisticsAgent {
    public List<Insight> plan(List<Map<String, Object>> materials) {
        List<Insight> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (var material : materials) {
            LocalDate needed = date(material.get("neededDate"));
            if (needed == null || "Recibido".equals(material.get("status"))) {
                continue;
            }
            int lead = num(material.get("leadDays"), 0);
            LocalDate latest = needed.minusDays(lead);
            long days = ChronoUnit.DAYS.between(now, latest);
            if (days < 0) {
                result.add(
                        new Insight(
                                "Logística",
                                "Fecha de compra superada",
                                str(material.get("name"))
                                        + " necesita gestión inmediata; con el plazo de "
                                        + lead
                                        + " días, la fecha recomendada era "
                                        + latest
                                        + ".",
                                "critical",
                                str(material.get("id")),
                                "OP-Regla-Logistica"));
            } else if (days <= 3) {
                result.add(
                        new Insight(
                                "Logística",
                                "Compra próxima",
                                str(material.get("name"))
                                        + " debe gestionarse en los próximos "
                                        + days
                                        + " día(s) para llegar antes de "
                                        + needed
                                        + ".",
                                "warning",
                                str(material.get("id")),
                                "OP-Regla-Logistica"));
            }
        }
        return result;
    }
}
