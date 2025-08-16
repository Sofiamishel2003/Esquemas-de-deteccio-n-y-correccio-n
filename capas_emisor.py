import socket
import random
import binascii

# ====================== CAPA: Aplicación ======================
def solicitar_mensaje():
    mensaje = input("Ingrese el mensaje a enviar: ")
    algoritmo = input("Algoritmo de integridad (hamming, crc32, fletcher8): ").lower()
    return mensaje, algoritmo


# ====================== CAPA: Presentación ======================
def codificar_ascii(mensaje):
    return ''.join(format(ord(c), '08b') for c in mensaje)





# ====================== MAIN ======================
if __name__ == "__main__":
    mensaje, algoritmo = solicitar_mensaje()
    bits = codificar_ascii(mensaje)
    print(f"Mensaje codificado en bits: {bits}")