"""Comprueba la API local en ejecución usando la cuenta de demostración."""

from http.cookiejar import CookieJar
import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import quote
from urllib.request import HTTPCookieProcessor, Request, build_opener

ROOT = Path(__file__).resolve().parents[1]
BASE = "http://127.0.0.1:8080"


def main() -> None:
    opener = build_opener(HTTPCookieProcessor(CookieJar()))
    checks = []

    def request(path: str, payload: dict | None = None) -> dict:
        data = json.dumps(payload).encode("utf-8") if payload is not None else None
        query = Request(
            BASE + path, data=data, headers={"Content-Type": "application/json"}
        )
        with opener.open(query, timeout=15) as response:
            return json.load(response)

    try:
        request("/api/works/w-demo/knowledge")
        raise AssertionError("La consulta requería autenticación")
    except HTTPError as error:
        assert error.code == 401
        checks.append("Consulta sin sesión devuelve 401")

    request(
        "/api/auth/login", {"email": "demo@obraplan.co", "password": "ObraPlan2026!"}
    )
    checks.append("Inicio demo por HTTP")
    graph = request("/api/works/w-demo/knowledge?q=" + quote("costo sin cotización"))
    assert any(source["id"] == "OP-Regla-Costos" for source in graph["sources"])
    checks.append("Pregunta codificada devuelve fuente de costos")
    assert graph["status"]["ok"] and graph["status"]["vaultConfigured"]
    checks.append("Servidor informa Obsidian conectado")
    assert any(node["id"] == "LangChain" for node in graph["nodes"])
    checks.append("API incluye memoria compartida registrada")
    assert all(
        edge["resolved"] for edge in graph["edges"] if edge["target"].startswith("OP-")
    )
    checks.append("Sin enlaces internos rotos en la obra")
    synced = request("/api/works/w-demo/knowledge/sync", {})
    assert synced["status"]["ok"]
    checks.append("Sincronización autenticada correcta")
    request("/api/auth/logout", {})
    report = {
        "passed": len(checks),
        "checks": checks,
        "nodeCount": graph["nodeCount"],
        "edgeCount": graph["edgeCount"],
        "scope": "Cuenta y obra demostrativas; sin modificar adquisiciones",
    }
    output = ROOT / "output/verification/http-report.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
