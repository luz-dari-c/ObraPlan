package co.obraplan.agents;

import static co.obraplan.agents.AgentData.num;

import co.obraplan.Json;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CostAgent {
    public Map<String, Object> estimate(
            List<Map<String, Object>> materials, List<Map<String, Object>> quotes) {
        double known = 0;
        int missing = 0;
        List<Object> lines = new ArrayList<>();
        for (var material : materials) {
            var valid =
                    quotes.stream()
                            .filter(
                                    quote ->
                                            Objects.equals(
                                                    quote.get("materialId"), material.get("id")))
                            .filter(
                                    quote ->
                                            !List.of("Rechazada", "Cancelado")
                                                    .contains(quote.get("status")))
                            .toList();
            var best =
                    valid.stream()
                            .min(
                                    Comparator.comparingDouble(
                                            quote -> num(quote.get("unitPrice"), Double.MAX_VALUE)))
                            .orElse(null);
            double quantity = num(material.get("quantity"), 0.0);
            Double subtotal = best == null ? null : quantity * num(best.get("unitPrice"), 0.0);
            if (subtotal == null) {
                missing++;
            } else {
                known += subtotal;
            }
            lines.add(
                    Json.map(
                            "materialId",
                            material.get("id"),
                            "material",
                            material.get("name"),
                            "quantity",
                            quantity,
                            "unit",
                            material.get("unit"),
                            "unitPrice",
                            best == null ? null : best.get("unitPrice"),
                            "subtotal",
                            subtotal,
                            "quoteCount",
                            valid.size()));
        }
        return Json.map(
                "knownSubtotal",
                known,
                "missingPriceCount",
                missing,
                "isComplete",
                missing == 0,
                "source",
                "OP-Regla-Costos",
                "message",
                missing == 0
                        ? "Total calculado con los precios registrados."
                        : "Subtotal parcial: "
                                + missing
                                + " material(es) sin cotización; no se cuentan como costo cero.",
                "lines",
                lines);
    }
}
