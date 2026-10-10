"""Deriva notas y relaciones del catálogo y de los requisitos documentados."""

from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]


def build() -> int:
    nodes = json.loads((ROOT / "knowledge/catalog.json").read_text(encoding="utf-8"))[
        "nodes"
    ]
    for line in (ROOT / "docs/ANALISIS.md").read_text(encoding="utf-8").splitlines():
        columns = [column.strip() for column in line.split("|")]
        if len(columns) >= 5 and columns[1].startswith(("RF-", "RNF-")):
            identifier, description, criterion = columns[1:4]
            nodes.append(
                {
                    "id": "OP-" + identifier,
                    "title": f"{identifier}: {description}",
                    "type": "requisito",
                    "source": "docs/ANALISIS.md",
                    "summary": f"{description}\n\nCriterio observable: {criterion}",
                    "relations": {
                        "pertenece_a": ["OP-Proyecto"],
                        "se_demuestra_en": ["OP-Trazabilidad"],
                    },
                }
            )
    for node in nodes:
        content = f"---\nid: {node['id']}\ntipo: {node['type']}\ntags: [obraplan, {node['type']}]\n---\n\n# {node['title']}\n\n{node['summary']}\n\n"
        content += f"## Fuente\n\n`{node['source']}`\n\nNota derivada. Actualiza su fuente y ejecuta `python scripts/build_knowledge.py`.\n\n## Relaciones\n\n"
        for relation, targets in node["relations"].items():
            content += "".join(f"- {relation}:: [[{target}]]\n" for target in targets)
        (ROOT / "knowledge" / f"{node['id']}.md").write_text(content, encoding="utf-8")
    return len(nodes)


if __name__ == "__main__":
    print(f"Notas documentales actualizadas: {build()}")
