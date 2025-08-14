import sys

# ---------------- Utilidades -----------------
def solo_bits(s: str) -> bool:
    return all(c in '01' for c in s) and len(s) > 0

# ----------------Hamming (algoritmo con MATRIZ + STACK, paridad par) ----------------
def hamming_emit(msg_bits: str, verbose: bool = True) -> str:
    """
    Implementa el emisor de Hamming siguiendo los pasos de la consigna:
    1) calcular p con 2^p >= m + p + 1
    2) crear matriz m_total x 2 (col 1: posición 1..m_total; col 0: bit)
    3) reservar posiciones de paridad P1,P2,... en potencias de 2
    4) colocar bits del mensaje en el resto
    5) para cada Px, crear un stack con bits de posiciones no-paridad cuya pos tenga 1 en el bit x (1-indexado)
    6) hacer XOR con pops del stack y asignar el resultado a Px
    7) concatenar la palabra código
    """
    m = len(msg_bits)
    # calcular p
    p = 0
    while (2 ** p) < (m + p + 1):
        p += 1
    m_total = m + p
    # matriz [ [bit], [pos] ] como en el ejemplo (col 0 -> bit, col 1 -> pos)
    matriz = [[None, str(i + 1)] for i in range(m_total)]
    # reservar posiciones de paridad (potencias de 2): P1,P2,...
    posiciones_paridad = set()
    for i in range(p):
        pos = 1 << i  # 1,2,4,8,...
        posiciones_paridad.add(pos)
        matriz[pos - 1][0] = f"P{i+1}"
    # colocar mensaje en posiciones no-paridad
    j = 0
    for i in range(m_total):
        pos = i + 1
        if pos not in posiciones_paridad:
            matriz[i][0] = msg_bits[j]
            j += 1
    # calcular cada paridad con stack + XOR
    for k in range(p):
        pos_paridad = 1 << k
        mask = pos_paridad  # bit a testear en la posición

        # construir stack con bits elegibles (no-paridad y pos & mask != 0)
        stack = []
        posiciones_incluidas = []
        for i in range(m_total):
            pos = i + 1
            if (pos not in posiciones_paridad) and (pos & mask):
                stack.append(matriz[i][0][0])  # '0' o '1'
                posiciones_incluidas.append(pos)
        # XOR acumulado con pops (equivalente a XOR de todos los bits)
        res = '0'
        while stack:
            bit = stack.pop()
            res = '0' if res == bit else '1'
        # asignar resultado a la posición de paridad
        matriz[pos_paridad - 1][0] = res
    # palabra código
    codeword = ''.join(matriz[i][0] for i in range(m_total))
    return codeword


# ------------------- CRC-32 (MSB-first, polinomio 0x04C11DB7) -------------------
POLY = 0x04C11DB7  # grado 32
POLY_DEG = 32

def strbits_to_ints(bits: str):
    return [1 if b == '1' else 0 for b in bits]

def ints_to_strbits(v):
    return ''.join('1' if x else '0' for x in v)

def crc32_remainder(bits: str) -> str:
    # división polinomial binaria (MSB primero), sin reflexión, resto de 32 bits
    data = strbits_to_ints(bits)
    # adjuntar 32 ceros
    data += [0] * POLY_DEG

    # representación del polinomio como vector de bits de longitud 33 (coef x^32 .. x^0) incluye el 1 de x^32 seguido de los 32 bit
    poly = [1] + [ (POLY >> i) & 1 for i in range(POLY_DEG - 1, -1, -1) ]

    # copia para operar
    work = data[:]
    for i in range(len(bits)):
        if work[i] == 1:  # restar (XOR) el polinomio alineado
            for j in range(POLY_DEG + 1):
                work[i + j] ^= poly[j]
    # resto = últimos 32 bits
    rem = work[-POLY_DEG:]
    return ints_to_strbits(rem)

def crc32_emit(msg_bits: str) -> str:
    rem = crc32_remainder(msg_bits)
    return msg_bits + rem

# ------------------- Main ---------------------
MENU = """
Seleccione algoritmo del EMISOR:
  1) Hamming (corrección de 1 bit)
  2) CRC-32 (detección)
Opción: """

if __name__ == '__main__':
    try:
        opt = input(MENU).strip()
        msg = input("Ingrese el mensaje en binario (p.ej. 110101): ").strip()
        if not solo_bits(msg):
            print("[Error] Solo se permiten 0s y 1s, y longitud > 0")
            sys.exit(1)

        if opt == '1':
            out = hamming_emit(msg)
            print("\n[EMISOR:Hamming] Trama enviada:", out)
        elif opt == '2':
            out = crc32_emit(msg)
            print("\n[EMISOR:CRC-32] Trama enviada:", out, "(mensaje + 32 bits de CRC)")
        else:
            print("Opción inválida")
            sys.exit(1)
    except KeyboardInterrupt:
        print("\n[EMISOR] Proceso interrumpido")