import os
import re
import sys


def analizza_metodi(java_code, file_type):
    pattern = re.compile(
        r'public\s+FlowGraph\s+visit\s*\(\s*([A-Za-z0-9_]+)\s+\w+\s*,\s*FlowContext\s+\w+\s*\)\s*\{(.*?)}',
        re.DOTALL
    )
    risultati = []
    for m in pattern.finditer(java_code):
        node_type, body = m.group(1), m.group(2)
        # Se il corpo contiene una chiamata a logger.info con "TODO", il metodo non è fatto
        if re.search(r'logger\.info\s*\(\s*".*TODO', body):
            support = "❌"
        else:
            support = "✅"
        risultati.append((node_type, support))
    return risultati


def stampa_tabella_md(risultati):
    totale = len(risultati)
    supportati = sum(1 for _, s in risultati if s == "✅")
    progress_bar_length = 20
    filled_length = int(round(progress_bar_length * supportati / totale)) if totale else 0
    bar = "█" * filled_length + "░" * (progress_bar_length - filled_length)
    print(f"## Progress\nDone: {supportati}/{totale}\n[{bar}]\n")

    risultati_ordinati = sorted(risultati, key=lambda x: 0 if x[1] == "✅" else 1)

    headers = ["Syntax node", "Supported"]
    col1 = max(len(headers[0]), *(len(node) for node, _ in risultati_ordinati))
    col2 = max(len(headers[1]), *(len(s) for _, s in risultati_ordinati))
    print(f"| {headers[0]:<{col1}} | {headers[1]:<{col2}} |")
    print(f"|{'-' * (col1 + 2)}|{'-' * (col2 + 2)}|")
    for node, s in risultati_ordinati:
        print(f"| {node:<{col1}} | {s:<{col2}} |")


def main():
    base_dir = sys.argv[1]
    files = {
        "FlowVisitor": os.path.join(base_dir, "src", "main", "java", "it", "unibo", "tesi", "chorol", "visitor", "flow",
                                    "FlowVisitor.java"),
        "FlowVisitorBase": os.path.join(base_dir, "src", "main", "java", "it", "unibo", "tesi", "chorol", "visitor",
                                        "flow", "FlowVisitorBase.java")
    }
    risultati = []
    for file_type, path in files.items():
        with open(path, "r", encoding="utf-8") as f:
            risultati += analizza_metodi(f.read(), file_type)
    stampa_tabella_md(risultati)


if __name__ == '__main__':
    main()
