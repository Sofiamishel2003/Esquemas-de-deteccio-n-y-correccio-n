import socket
import random
import emisor

# ====================== CAPA: Aplicación ======================
def solicitar_mensaje():
    mensaje = input("Ingrese el mensaje a enviar: ")
    algoritmo = input(MENU).strip()
    return mensaje, algoritmo


# ====================== CAPA: Presentación ======================
def codificar_ascii(mensaje):
    return ''.join(format(ord(c), '08b') for c in mensaje)


# ====================== CAPA: Enlace ======================
def calcular_integridad(bits, algoritmo):
    if algoritmo == "1":  # Hamming
        return emisor.hamming_emit(bits)

    elif algoritmo == "2":  # CRC-32
        return emisor.crc32_emit(bits)

    elif algoritmo == "3":  # Fletcher
        tipo = input("Seleccione tipo Fletcher (8, 16, 32): ").strip()
        if tipo not in ("8", "16", "32"):
            raise ValueError("Tipo de Fletcher no válido")
        frame, _, _ = emisor.fletcher_emit(bits, int(tipo))
        return frame

    else:
        raise ValueError("Algoritmo no soportado")





# ====================== MAIN ======================
if __name__ == "__main__":
    MENU = """
Seleccione algoritmo:
    1) Hamming (corrección de 1 bit)
    2) CRC-32 (detección)
    3) Fletcher checksum (8/16/32)
Opción: """
    mensaje, algoritmo = solicitar_mensaje()
    bits = codificar_ascii(mensaje)
    # print(f"\nMensaje en binario: {bits}")
    trama = calcular_integridad(bits, algoritmo)
    print(f"Trama generada: {trama}")
    # trama_ruido = aplicar_ruido(trama, prob_error=0.01)
    # enviar_trama(trama_ruido)