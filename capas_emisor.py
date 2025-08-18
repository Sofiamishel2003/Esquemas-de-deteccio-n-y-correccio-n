import socket
import random
import emisor
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import analysis

# ====================== CAPA: Aplicación ======================
def solicitar_modo():
    opcion = input("\nSeleccione modo:\n1) Prueba manual\n2) Prueba masiva\nOpción: ").strip()
    return opcion

def solicitar_mensaje():
    mensaje = input("Ingrese el mensaje a enviar: ")
    algoritmo = input(MENU).strip()
    return mensaje, algoritmo


# ====================== CAPA: Presentación ======================
def codificar_ascii(mensaje):
    return ''.join(format(ord(c), '08b') for c in mensaje)


# ====================== CAPA: Enlace ======================
def calcular_integridad(bits, algoritmo, fletcher_tipo=None):
    if algoritmo == "1":  # Hamming
        return format(1, '08b'), emisor.hamming_emit(bits)
    elif algoritmo == "2":  # CRC-32
        return format(2, '08b'), emisor.crc32_emit(bits)
    elif algoritmo == "3":  # Fletcher
        if fletcher_tipo is None:  # modo manual
            tipo = input("Seleccione tipo Fletcher (8, 16, 32): ").strip()
            if tipo not in ("8", "16", "32"):
                raise ValueError("Tipo de Fletcher no válido")
        else:  # modo masivo
            tipo = fletcher_tipo
        frame, _, _ = emisor.fletcher_emit(bits, int(tipo))
        return format(int(np.log2(int(tipo))), '08b'), frame
    else:
        raise ValueError("Algoritmo no soportado")



# ====================== CAPA: Ruido ======================
def aplicar_ruido(trama, prob_error=0.01):
    bits = list(trama)
    for i in range(len(bits)):
        if random.random() < prob_error:
            bits[i] = '0' if bits[i] == '1' else '1'
    return ''.join(bits)


# ====================== CAPA: Transmisión ======================
def enviar_trama(trama, host="127.0.0.1", puerto=5000):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((host, puerto))
        s.sendall((trama + "\n").encode("utf-8"))

        respuesta = s.recv(1024).decode().strip()
    return respuesta


# ====================== PRUEBAS MANUALES ======================
def prueba_manual():
    mensaje, algoritmo = solicitar_mensaje()
    bits = codificar_ascii(mensaje)
    header, trama = calcular_integridad(bits, algoritmo)
    print(header)
    trama_ruido = aplicar_ruido(trama, prob_error=0)
    
    trama_final = header + trama_ruido
    respuesta = enviar_trama(trama_final)
    print(f"\nTrama enviada: {trama_final[:8]}-{trama_final[8:]}")
    print(f"Respuesta del receptor: {respuesta}")


# ====================== PRUEBAS MASIVAS ======================
def prueba_masiva(num_pruebas=10000):
    resultados = []
    alg_names = {
        "00000001": "Hamming",
        "00000010": "CRC-32",
        "00000011": "Fletcher x08",
        "00000100": "Fletcher x16",
        "00000101": "Fletcher x32"
    }
    algoritmos = ["1", "2", "3"]
    tamanos = [8, 16, 32, 64, 128]
    probabilidades = [0.001, 0.01, 0.05, 0.1, 0.2]

    for algoritmo in algoritmos:
        for tam in tamanos:
            for p in probabilidades:
                for _ in range(num_pruebas // (len(algoritmos) * len(tamanos) * len(probabilidades))):
                    
                    # Generar mensaje aleatorio
                    mensaje = ''.join(random.choice("01") for _ in range(tam))
                    fletcher_tipo = random.choice(["8", "16", "32"]) if algoritmo == "3" else None
                    header, trama = calcular_integridad(mensaje, algoritmo, fletcher_tipo)
                    trama_ruido = aplicar_ruido(trama, prob_error=p)
                    trama_final = header+trama_ruido
                    # Enviar al receptor y obtener respuesta
                    rslt = enviar_trama(trama_final).split(",")
                    
                    respuesta = rslt[0]
                    if respuesta == mensaje or respuesta == "OK":
                        respuesta = "OK"
                    elif respuesta == mensaje or respuesta == "FIXED":
                        respuesta = "FIXED"
                    else:
                        respuesta = "ERROR"

                    # Guardar resultados (el receptor debe enviar "OK" o "ERROR")
                    resultados.append({
                        "algoritmo": alg_names[header],
                        "tamano": tam,
                        "prob_error": p,
                        "tiempo": float(rslt[1]),
                        "resultado": respuesta
                    })

    df = pd.DataFrame(resultados)
    df.to_csv("resultados.csv", index=False)
    print("\nResultados guardados en resultados.csv")
    analysis.generar_graficas(df)


# ====================== MAIN ======================
if __name__ == "__main__":
    MENU = """
Seleccione algoritmo:
    1) Hamming (corrección de 1 bit)
    2) CRC-32 (detección)
    3) Fletcher checksum (8/16/32)
Opción: """
    modo = solicitar_modo()
    if modo == "1":
        prueba_manual()
    elif modo == "2":
        prueba_masiva(12000)
    else:
        print("Opción no válida.")