import pandas as pd
from matplotlib import pyplot as plt

def generar_graficas(df):
    # Gráfico general
    
    # -- Hamming -- #
    # ham = df[df["algoritmo"]=="Hamming"]
    # ham_bar = ham["resultado"].value_counts().reindex(["OK", "FIXED", "ERROR"], fill_value=0) / len(ham)
    # ham_bar.plot(kind="bar", title="Tasa de aciertos: Hamming")
    # plt.grid(axis="y", linestyle='--', alpha=0.6)
    # plt.tight_layout()
    # plt.xticks(ha='right', rotation=30)
    # plt.xlabel("Estatus")
    # plt.show()
    
    # tabla = ham.groupby("prob_error")["resultado"].apply(lambda x: (x=="OK").mean())
    # tabla.plot(marker="o", title=f"Desempeño VS Nivel de Error", label="Ok")
    # tabla2 = ham.groupby("prob_error")["resultado"].apply(lambda x: (x=="FIXED").mean())
    # tabla2.plot(marker="o", label="Fixed")
    # tabla3 = ham.groupby("prob_error")["resultado"].apply(lambda x: (x=="ERROR").mean())
    # tabla3.plot(marker="o", label="Error")
    # plt.grid(axis="y", linestyle='--', alpha=0.6)
    # plt.tight_layout()
    # plt.legend()
    # plt.xticks(ha='right', rotation=30)
    # plt.show()
    
    # -- CRC -- #
    crc = df[df["algoritmo"]=="CRC-32"]
    crc_bar = crc["resultado"].value_counts().reindex(["OK", "ERROR"], fill_value=0) / len(crc)
    crc_bar.plot(kind="bar", title="Tasa de aciertos: CRC-32")
    plt.grid(axis="y", linestyle='--', alpha=0.6)
    plt.tight_layout()
    plt.xticks(ha='right', rotation=30)
    plt.show()
    
    tb4 = crc.groupby("prob_error")["resultado"].apply(lambda x: (x=="OK").mean())
    tb4.plot(marker="o", title=f"Desempeño VS Nivel de Error", label="Ok")
    plt.grid(axis="y", linestyle='--', alpha=0.6)
    plt.tight_layout()
    plt.legend()
    plt.xticks(ha='right', rotation=30)
    plt.show()
    
    # -- Fletcher -- #
    # fletch = df[(df["algoritmo"]!= "CRC-32") & (df["algoritmo"]!= "Hamming")]
    # fle_bar = fletch["resultado"].value_counts().reindex(["OK", "ERROR"], fill_value=0) / len(fletch)
    # fle_bar.plot(kind="bar", title="Tasa de aciertos: Flecther")
    # plt.grid(axis="y", linestyle='--', alpha=0.6)
    # plt.tight_layout()
    # plt.xticks(ha='right', rotation=30)
    # plt.show()
    
    # plt.title("Desempeño VS Nivel de Error")
    # f8 = fletch[fletch["algoritmo"]=="Fletcher x08"]
    # tb5 = f8.groupby("prob_error")["resultado"].apply(lambda x: (x=="OK").mean())
    # tb5.plot(marker="o", label="Fletcher x08")
    
    # f16 = fletch[fletch["algoritmo"]=="Fletcher x16"]
    # tb6 = f16.groupby("prob_error")["resultado"].apply(lambda x: (x=="OK").mean())
    # tb6.plot(marker="o", label="Fletcher x16")
    
    # f32 = fletch[fletch["algoritmo"]=="Fletcher x32"]
    # tb6 = f32.groupby("prob_error")["resultado"].apply(lambda x: (x=="OK").mean())
    # tb6.plot(marker="o", label="Fletcher x32")
    # plt.grid(axis="y", linestyle='--', alpha=0.6)
    # plt.tight_layout()
    # plt.legend()
    # plt.xticks(ha='right', rotation=30)
    # plt.show()
    
    

if __name__=="__main__":
    df = pd.read_csv('resultados.csv')
    generar_graficas(df)