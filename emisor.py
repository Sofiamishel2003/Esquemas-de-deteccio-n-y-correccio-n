import sys

# ---------------- Utilidades -----------------
def solo_bits(s: str) -> bool:
    return all(c in '01' for c in s) and len(s) > 0

# --------------- Hamming (m+r+1 <= 2^r) --------------
def hamming_emit(msg_bits: str) -> str:
    m = len(msg_bits)
    # calcular r mínimo tal que 2^r >= m + r + 1
    r = 0
    while (2 ** r) < (m + r + 1):
        r += 1

    # construir arreglo 1-indexado con espacios para paridades
    n = m + r
    code = ['0'] * (n + 1)  # índice 0 se ignora

    # colocar datos en posiciones que NO son potencia de 2
    j = 0
    for i in range(1, n + 1):
        if i & (i - 1) != 0:  # no es potencia de 2
            code[i] = msg_bits[j]
            j += 1

    # calcular bits de paridad (paridad par)
    for p in range(r):
        pos = 1 << p  # 1,2,4,8,...
        suma = 0
        for i in range(1, n + 1):
            if i & pos:
                suma ^= int(code[i])
        code[pos] = str(suma)  # con paridad par, asignar el XOR directo

    return ''.join(code[1:])

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

    # representación del polinomio como vector de bits de longitud 33 (coef x^32 .. x^0)
    poly = [(POLY >> i) & 1 for i in range(POLY_DEG, -1, -1)]

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