"""Prueba relaciones, recuperación, conservación e independencia de entornos."""

from pathlib import Path
import json
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "agents"))
sys.path.insert(0, str(ROOT / "scripts"))
import knowledge_agent as agent
from sync_knowledge import sync


def main() -> None:
    graph = agent.load_graph()
    checks = []

    def check(name: str, condition: bool) -> None:
        if not condition:
            raise AssertionError(name)
        checks.append(name)

    check(
        "IDs únicos",
        len({node["id"] for node in graph["nodes"]}) == len(graph["nodes"]),
    )
    check(
        "Sin relaciones internas rotas",
        all(
            edge["resolved"]
            for edge in graph["edges"]
            if edge["target"].startswith("OP-")
        ),
    )
    check(
        "30 requisitos documentales",
        sum(node["type"] == "requisito" for node in graph["nodes"]) == 30,
    )
    response = agent.consult("costo sin cotización", None, False)
    check(
        "Consulta devuelve regla con fuente",
        any(source["id"] == "OP-Regla-Costos" for source in response["sources"]),
    )
    check("Recuperación usa relaciones", bool(response["relationships"]))
    check(
        "Sin evidencia no inventa",
        not agent.consult("xylophoniummmm", None, False)["sources"],
    )
    folder = ROOT / "output/verification"
    folder.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=folder, prefix="python-") as temporary:
        temp = Path(temporary)
        vault = temp / "vault"
        (vault / ".obsidian").mkdir(parents=True)
        (vault / "Proyectos").mkdir()
        index = vault / "Proyectos/Indice_Proyectos.md"
        index.write_text("# Índice\n* [[ProyectoAnterior]]\n", encoding="utf-8")
        learning = vault / "Proyectos/ObraPlan/Aprendizajes/OP-Aprendizaje-Prueba.md"
        learning.parent.mkdir(parents=True)
        learning.write_text(
            "# Aprendizaje manual\nConservar este contenido.\n", encoding="utf-8"
        )
        sync(vault)
        sync(vault)
        check(
            "Índice anterior conservado",
            "[[ProyectoAnterior]]" in index.read_text(encoding="utf-8"),
        )
        check(
            "Sin enlace duplicado",
            index.read_text(encoding="utf-8").count("[[OP-Proyecto|") == 1,
        )
        check(
            "Aprendizajes conservados",
            learning.read_text(encoding="utf-8")
            == "# Aprendizaje manual\nConservar este contenido.\n",
        )
        shared = vault / "Conocimiento/MemoriaCompartida.md"
        shared.parent.mkdir(parents=True)
        shared.write_text(
            "# Memoria compartida\nReferencia xyloshared.\n", encoding="utf-8"
        )
        saved_config = agent.config
        try:
            agent.config = lambda: {
                "vaultPath": str(vault),
                "sharedKnowledgeFiles": ["Conocimiento/MemoriaCompartida.md"],
            }
            check(
                "Recupera memoria compartida del vault",
                any(
                    note["id"] == "MemoriaCompartida"
                    for note in agent.consult("xyloshared", None, False)["sources"]
                ),
            )
            agent.config = lambda: {
                "vaultPath": str(vault),
                "sharedKnowledgeFiles": ["../fuera.md"],
            }
            try:
                agent.load_graph()
                raise AssertionError("Debía rechazar una referencia fuera del vault")
            except ValueError:
                checks.append("Referencia fuera del vault rechazada")
        finally:
            agent.config = saved_config
        external_root = temp / "agente independiente"
        external_root.mkdir()
        entrypoint = external_root / "agent.py"
        entrypoint.write_text(
            "import json, os, sys\ndata=json.load(sys.stdin)\nprint(json.dumps({'value':data['value']*2,'cwd':os.getcwd(),'python':sys.executable}))\n",
            encoding="utf-8",
        )
        payload = temp / "request.json"
        payload.write_text('{"value": 21}', encoding="utf-8")
        original_config = agent.config
        agent.config = lambda: {
            "externalProjects": [
                {
                    "id": "prueba",
                    "kind": "python-agent",
                    "enabled": True,
                    "root": str(external_root),
                    "python": sys.executable,
                    "entrypoint": "agent.py",
                }
            ]
        }
        try:
            result = agent.external("prueba", payload)
            check("Agente externo devuelve JSON", result["value"] == 42)
            check("Agente externo usa su cwd", Path(result["cwd"]) == external_root)
            check(
                "Agente externo usa intérprete registrado",
                Path(result["python"]) == Path(sys.executable),
            )
            try:
                agent.external("deshabilitado", payload)
                raise AssertionError("Debía rechazar agente no registrado")
            except ValueError:
                checks.append("Agente no registrado rechazado")
        finally:
            agent.config = original_config
    (folder / "python-report.json").write_text(
        json.dumps(
            {"passed": len(checks), "checks": checks}, ensure_ascii=False, indent=2
        ),
        encoding="utf-8",
    )
    print(f"Verificación Python: {len(checks)} comprobaciones correctas.")


if __name__ == "__main__":
    main()
