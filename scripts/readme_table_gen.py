import os
import re
import sys


def analizza_metodi(java_code):
    """
    Cerca i metodi visit nella classe Java e restituisce una lista di tuple:
    (nome_della_syntaxnode, support_status)
    """
    risultati = []
    pattern = re.compile(
        r'public\s+FlowGraph\s+visit\s*\(\s*([A-Za-z0-9_]+)\s+\w+\s*,\s*FlowContext\s+\w+\s*\)\s*\{(.*?)\}',
        re.DOTALL
    )

    for match in pattern.finditer(java_code):
        node_type = match.group(1)
        body = match.group(2)
        # Suddividi il corpo in righe, rimuovendo spazi bianchi
        lines = [line.strip() for line in body.splitlines() if line.strip()]

        # Se l'ultima riga è "return null;" il metodo non è implementato
        support = "❌" if lines and lines[-1] == "return null;" else "✅"
        risultati.append((node_type, support))

    return risultati


def stampa_tabella_md(risultati):
    # Calcola il progresso generale
    totale = len(risultati)
    supportati = sum(1 for _, support in risultati if support == "✅")
    percentuale = (supportati / totale * 100) if totale > 0 else 0

    # Definisce la lunghezza della barra di caricamento
    progress_bar_length = 30
    # Calcola quanti blocchi pieni devono essere mostrati
    filled_length = int(round(progress_bar_length * supportati / totale)) if totale > 0 else 0
    # Crea la barra con blocchi pieni e blocchi vuoti
    bar = "█" * filled_length + "░" * (progress_bar_length - filled_length)

    # Stampa l'indicatore di progresso con la barra di caricamento
    print(f"Progresso generale: {supportati}/{totale} ({percentuale:.2f}%)")
    print(f"[{bar}]\n")

    headers = ["Syntax node", "Supported"]
    col1_width = max(len(headers[0]), max((len(node) for node, _ in risultati), default=0))
    col2_width = max(len(headers[1]), max((len(support) for _, support in risultati), default=0))

    print(f"| {headers[0]:<{col1_width}} | {headers[1]:<{col2_width}} |")
    print(f"|{'-' * (col1_width + 2)}|{'-' * (col2_width + 2)}|")
    for node_type, support in risultati:
        print(f"| {node_type:<{col1_width}} | {support:<{col2_width}} |")


def main():
    base_dir = sys.argv[1]
    file_path = os.path.join(base_dir, "src", "main", "java", "it", "unibo", "tesi", "chorol", "controlflow",
                             "FlowVisitor.java")
    with open(file_path, "r", encoding="utf-8") as f:
        java_code = f.read()
    risultati = analizza_metodi(java_code)
    stampa_tabella_md(risultati)


if __name__ == '__main__':
    main()
