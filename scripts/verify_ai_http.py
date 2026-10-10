"""HTTP checks with an isolated server and no cloud credential or production data."""

from http.cookiejar import CookieJar
import json
import os
from pathlib import Path
import shutil
import socket
import subprocess
import time
from urllib.error import HTTPError, URLError
from urllib.request import HTTPCookieProcessor, Request, build_opener
from uuid import uuid4

ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    temp = ROOT / "output/verification" / f"ai-http-{uuid4()}"
    (temp / "vault/.obsidian").mkdir(parents=True)
    shutil.copytree(ROOT / "knowledge", temp / "knowledge")
    (temp / "config").mkdir()
    (temp / "config/integrations.json").write_text(
        '{"sharedKnowledgeFiles":[]}', encoding="utf-8"
    )
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", 0))
        port = probe.getsockname()[1]
    env = os.environ.copy()
    env.pop("GEMINI_API_KEY", None)
    env.pop("OBRAPLAN_GEMINI_MODEL", None)
    env.update(
        OBRAPLAN_PORT=str(port),
        OBRAPLAN_DATA=str(temp / "data.json"),
        OBRAPLAN_STATIC=str(ROOT / "src/main/resources/static"),
        OBRAPLAN_VAULT=str(temp / "vault"),
    )
    base = f"http://127.0.0.1:{port}"
    client = build_opener(HTTPCookieProcessor(CookieJar()))
    checks = []

    def call(path: str, payload: dict | None = None, expected: int = 200) -> dict:
        request = Request(
            base + path,
            data=None if payload is None else json.dumps(payload).encode(),
            headers={"Content-Type": "application/json"},
        )
        try:
            with client.open(request, timeout=15) as response:
                assert response.status == expected
                return json.load(response)
        except HTTPError as error:
            assert error.code == expected, (error.code, expected)
            return json.load(error)

    with (temp / "server.log").open("w", encoding="utf-8") as log:
        process = subprocess.Popen(
            [
                "java", f"-Duser.dir={temp}", "--add-modules", "jdk.httpserver",
                "-cp", str(ROOT / "build/classes"), "co.obraplan.Server",
            ],
            env=env, cwd=ROOT, stdout=log, stderr=log,
            creationflags=subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0,
        )
        try:
            for attempt in range(100):
                if process.poll() is not None:
                    raise RuntimeError(f"Servidor de prueba terminó; revisa {temp / 'server.log'}")
                try:
                    call("/api/health")
                    break
                except URLError:
                    time.sleep(0.1)
            else:
                raise RuntimeError("Servidor de prueba no inició")
            call("/api/assistant/status", expected=401)
            checks.append("Estado IA requiere sesión")
            call("/api/works/w-demo/assistant", {"question": "costos"}, 401)
            checks.append("Consulta IA requiere sesión")
            call("/api/auth/login", {"email": "demo@obraplan.co", "password": "ObraPlan2026!"})
            status = call("/api/assistant/status")
            assert not status["configured"] and len(status["agents"]) == 4
            assert status["billingVerified"] is False and "key" not in json.dumps(status).lower()
            checks.append("Estado distingue cuatro roles pendientes sin exponer clave")
            call("/api/works/w-demo/assistant", {"question": "costos"}, 503)
            checks.append("Sin clave devuelve 503 sin fabricar respuesta IA")
            call("/api/works/obra-ajena/assistant", {"question": "costos"}, 404)
            checks.append("Obra ajena devuelve 404 antes del proveedor")
            call("/api/works/w-demo/assistant/memory", {"runId": "inexistente"}, 404)
            checks.append("No acepta análisis desconocido")
            graph = call("/api/works/w-demo/knowledge?q=costos")
            assert any(n["id"] == "OP-IA-Coordinador" for n in graph["nodes"])
            assert all(e["resolved"] for e in graph["edges"] if e["target"].startswith("OP-"))
            checks.append("Grafo incluye IA sin relaciones internas rotas")
            call("/api/works/w-demo/dashboard")
            checks.append("Tablero disponible sin Gemini")
            call("/api/auth/logout", {})
            call("/api/assistant/status", expected=401)
            checks.append("Cerrar sesión retira acceso")
        finally:
            process.terminate()
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait(timeout=5)
    report = {
        "passed": len(checks), "checks": checks, "data": str(temp / "data.json"),
        "productionDataModified": False, "realProviderVerified": False,
    }
    (ROOT / "output/verification/ai-http-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"{len(checks)} verificaciones HTTP de IA correctas, servidor temporal sin clave.")


if __name__ == "__main__":
    main()
