"""Publica documentación y notas de ObraPlan en un namespace propio del vault."""

from __future__ import annotations

import argparse
from datetime import datetime
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "agents"))
from knowledge_agent import load_graph  # noqa: E402


def sync(vault: Path, check: bool = False) -> dict:
    vault = vault.resolve(strict=True)
    if not (vault / ".obsidian").is_dir():
        raise ValueError("La ruta no es un vault existente de Obsidian")
    target = vault / "Proyectos/ObraPlan"
    if not target.is_relative_to(vault):
        raise ValueError("El namespace debe estar dentro del vault")
    graph = load_graph()
    # Los enlaces a otros proyectos pueden no estar resueltos en el grafo local.
    broken = [
        edge
        for edge in graph["edges"]
        if not edge["resolved"] and edge["target"].startswith("OP-")
    ]
    if broken:
        raise ValueError(f"Relaciones internas sin destino: {broken}")
    copied = 0
    if not check:
        for folder, destination in [
            (ROOT / "knowledge", target / "Conocimiento"),
            (ROOT / "docs", target / "Documentacion"),
            (ROOT / "output/knowledge/analyses", target / "Analisis"),
        ]:
            for source in folder.rglob("*.md"):
                output = destination / source.relative_to(folder)
                output.parent.mkdir(parents=True, exist_ok=True)
                if not output.exists() or source.read_bytes() != output.read_bytes():
                    shutil.copyfile(source, output)
                    copied += 1
        (target / "Aprendizajes").mkdir(parents=True, exist_ok=True)
        for name in ["README.md", "AGENTS.md", "COMO_SE_HACEN_LAS_COSAS.md"]:
            source = ROOT / name
            content = source.read_text(encoding="utf-8").replace(
                "(docs/", "(Documentacion/"
            )
            output = target / name
            if not output.exists() or output.read_text(encoding="utf-8") != content:
                output.write_text(content, encoding="utf-8")
                copied += 1
        index = vault / "Proyectos/Indice_Proyectos.md"
        link = "[[OP-Proyecto|ObraPlan — adquisiciones, agentes y conocimiento]]"
        current = (
            index.read_text(encoding="utf-8")
            if index.exists()
            else "# Índice de Proyectos\n"
        )
        if link not in current:
            if index.exists():
                backups = ROOT / "output/backups"
                backups.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(
                    index,
                    backups / f"Indice_Proyectos-{datetime.now():%Y%m%d-%H%M%S}.md",
                )
            index.write_text(current.rstrip() + f"\n* {link}\n", encoding="utf-8")
    report = {
        "vault": str(vault),
        "namespace": str(target),
        "copied": copied,
        "nodeCount": graph["nodeCount"],
        "edgeCount": graph["edgeCount"],
        "brokenInternalLinks": len(broken),
        "checkOnly": check,
    }
    output_dir = ROOT / "output/knowledge"
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "graph.json").write_text(
        json.dumps(graph, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    (output_dir / "sync-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    return report


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--vault", type=Path)
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    settings = json.loads(
        (ROOT / "config/integrations.json").read_text(encoding="utf-8")
    )
    try:
        print(
            json.dumps(
                sync(arguments.vault or Path(settings["vaultPath"]), arguments.check),
                ensure_ascii=False,
                indent=2,
            )
        )
    except (ValueError, OSError) as error:
        print(f"No se sincronizó: {error}", file=sys.stderr)
        sys.exit(1)
