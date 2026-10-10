package co.obraplan.agents;

import static co.obraplan.agents.AgentData.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TrackingAgent {
    public List<Insight> inspect(List<Map<String, Object>> quotes) {
        List<Insight> result = new ArrayList<>();
        for (var quote : quotes) {
            String status = str(quote.get("status"));
            LocalDate expected = date(quote.get("expectedDate"));
            if (expected != null
                    && expected.isBefore(LocalDate.now())
                    && List.of("Pedido realizado", "En tránsito").contains(status)) {
                result.add(
                        new Insight(
                                "Seguimiento",
                                "Entrega por revisar",
                                "La fecha estimada "
                                        + expected
                                        + " ya pasó. Confirma manualmente el estado con la"
                                        + " ferretería.",
                                "warning",
                                str(quote.get("materialId")),
                                "OP-Regla-Seguimiento"));
            }
        }
        return result;
    }
}
