# Tema principal de ODÍN (música de fondo en bucle). Re dórico, 80 pulsos por minuto,
# 24 compases de 4/4 = 72 s. Lira en ostinato, bordón de cellos, tambor de marco
# "latido", melodía de violín folk, coro grave y cuernos al final.
from comun import *

BPM = 80
PULSO = 60 / BPM
COMPAS = 4 * PULSO
COMPASES = 24

# Acordes por compás (ciclo de 8): Dm Dm C Bb Dm Dm C Am
ACORDES = {
    'Dm': ('D3', 'A3', 'D4', 'F4'),
    'C': ('C3', 'G3', 'C4', 'E4'),
    'Bb': ('Bb2', 'F3', 'Bb3', 'D4'),
    'Am': ('A2', 'E3', 'A3', 'C4'),
}
CICLO = ['Dm', 'Dm', 'C', 'Bb', 'Dm', 'Dm', 'C', 'Am']

# Melodía (nota, pulsos) por compás, para los compases 9-16 y 17-24
MELODIA = [
    [('D4', 1.5), ('E4', .5), ('F4', 1), ('A4', 1)],
    [('G4', 2), ('F4', 1), ('E4', 1)],
    [('E4', 1.5), ('D4', .5), ('C4', 1), ('E4', 1)],
    [('D4', 3), ('C4', 1)],
    [('D4', 1.5), ('E4', .5), ('F4', 1), ('A4', 1)],
    [('C5', 2), ('A4', 1), ('G4', 1)],
    [('A4', 1), ('G4', 1), ('F4', 1), ('E4', 1)],
    [('E4', 2), ('D4', 1), ('E4', 1)],
]

LIRA, BORDON, TAMBOR, VIOLIN, CORO_C, CUERNOS, TIMBAL, FLAUTA, BAJO = 0, 1, 2, 3, 4, 5, 6, 7, 8
PROGRAMAS = {LIRA: ARPA, BORDON: CELLO, TAMBOR: TAIKO, VIOLIN: VIOLIN_F, CORO_C: OOHS,
             CUERNOS: TROMPA, TIMBAL: TIMPANI, FLAUTA: SHAKU, BAJO: CONTRABAJO}


def pieza(desfase=0.0):
    ev = []
    for c in range(COMPASES):
        t0 = desfase + c * COMPAS
        acorde = ACORDES[CICLO[c % 8]]
        seccion = c // 8  # 0 = A, 1 = B, 2 = C

        # Lira: ostinato en corcheas (raíz, quinta, octava, quinta, tercera, quinta, octava, quinta)
        patron = [0, 1, 2, 1, 3, 1, 2, 1]
        for i, k in enumerate(patron):
            vel = 62 + (10 if i in (0, 4) else 0) + 6 * seccion
            ev.append((t0 + i * PULSO / 2, LIRA, n(acorde[k]) + (12 if k == 3 else 0), vel, PULSO * 1.2))

        # Bordón: Re y La graves que respiran cada 4 compases; el bajo sigue la raíz
        if c % 4 == 0:
            ev.append((t0, BORDON, n('D2'), 58 + 6 * seccion, COMPAS * 4))
            ev.append((t0, BORDON, n('A2'), 52 + 6 * seccion, COMPAS * 4))
        ev.append((t0, BAJO, n(acorde[0]) - 12, 60 + 8 * seccion, COMPAS * 0.95))

        # Tambor de marco: latido (1 fuerte, 3 medio, "y" del 4 suave); más intenso en la sección C
        ev.append((t0, TAMBOR, n('D2'), 92 + 10 * seccion, 0.4))
        ev.append((t0 + 2 * PULSO, TAMBOR, n('D2'), 70 + 8 * seccion, 0.4))
        ev.append((t0 + 3.5 * PULSO, TAMBOR, n('D2'), 50 + 8 * seccion, 0.3))
        if seccion == 2:
            ev.append((t0 + 3 * PULSO, TAMBOR, n('D2'), 62, 0.3))
            if c % 2 == 0:
                ev.append((t0, TIMBAL, n('D2'), 85, 0.8))

        # Melodía: violín folk en B; en C la lleva la flauta con los cuernos una octava abajo
        if seccion >= 1:
            pos = 0.0
            for nota, pulsos in MELODIA[c % 8]:
                if seccion == 1:
                    ev.append((t0 + pos * PULSO, VIOLIN, n(nota), 78, pulsos * PULSO * 0.95))
                else:
                    ev.append((t0 + pos * PULSO, FLAUTA, n(nota) + 12, 72, pulsos * PULSO * 0.95))
                    ev.append((t0 + pos * PULSO, CUERNOS, n(nota) - 12, 70, pulsos * PULSO * 0.98))
                pos += pulsos

        # Coro grave: entra en B (raíz y quinta) y se llena en C
        if seccion >= 1:
            ev.append((t0, CORO_C, n(acorde[0]), 50 + 10 * (seccion - 1), COMPAS))
            ev.append((t0, CORO_C, n(acorde[1]), 46 + 10 * (seccion - 1), COMPAS))
            if seccion == 2:
                ev.append((t0, CORO_C, n(acorde[2]), 48, COMPAS))
    return ev


if __name__ == '__main__':
    largo = COMPASES * COMPAS
    # Se tocan dos vueltas y se guarda la segunda: así las colas de la primera
    # suenan al empezar y el bucle no tiene corte.
    eventos = pieza(0.0) + pieza(largo)
    x = tocar(eventos, 2 * largo, PROGRAMAS, cola=4.0, ganancia=-9)
    # Viento del norte, muy bajo
    tt = t(len(x) / SR)
    viento = filtro(ruido(len(x) / SR), 'band', [250, 1400]) * (0.5 + 0.5 * np.sin(2 * np.pi * tt / 9.0)) * 0.05
    x = mezclar(x, viento)
    x = sala(x, 2.8, 0.3, 5000)
    segunda = x[int(largo * SR):int(2 * largo * SR)]
    # Compresión suave y normalización
    pico = np.abs(segunda).max()
    segunda = np.tanh(segunda / pico * 1.4) / np.tanh(1.4) * 0.85
    wavfile.write(os.path.join(OUT, 'tema_principal.wav'), SR, (segunda * 32767).astype(np.int16))
    print('tema_principal %.1f s' % (len(segunda) / SR))
