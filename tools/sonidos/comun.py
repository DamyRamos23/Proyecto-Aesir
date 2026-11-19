# MOD-02: efectos y tema principal de ODÍN con instrumentos reales (GeneralUser GS,
# licencia libre para software) más capas sintetizadas (golpes, madera, líquido).
# Tonalidad de todo el juego: Re eólico/dórico (Dm - C - Bb), como la música nórdica épica.
import os
import numpy as np
from scipy import signal
from scipy.io import wavfile
import tinysoundfont

SR = 44100
SF2 = '/tmp/sf/GeneralUser-GS/GeneralUser-GS.sf2'
BASE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(BASE, 'wav')
os.makedirs(OUT, exist_ok=True)
rng = np.random.default_rng(11)

# Programas General MIDI usados
ARPA, DULCIMER, CELLO, CONTRABAJO, VIOLIN_F = 46, 15, 42, 43, 110
TIMPANI, TAIKO, CORO, OOHS, TROMPA, TROMBON, TUBA = 47, 116, 52, 53, 60, 57, 58
SHAKU, FLAUTA_PAN, CAMPANAS, CUERDAS_LENTAS, VIDRIO = 77, 75, 14, 49, 92


# ---------------------------------------------------------------- utilidades
def n(nombre):
    """'D4' -> número MIDI."""
    notas = {'C': 0, 'D': 2, 'E': 4, 'F': 5, 'G': 7, 'A': 9, 'B': 11}
    base = notas[nombre[0]]
    i = 1
    if nombre[1] in '#b':
        base += 1 if nombre[1] == '#' else -1
        i = 2
    return 12 * (int(nombre[i:]) + 1) + base


def tocar(eventos, duracion, programas, cola=1.5, ganancia=-6):
    """eventos: (inicio_s, canal, nota, velocidad, dur_s[, bend]) -> estéreo float."""
    s = tinysoundfont.Synth(gain=ganancia, samplerate=SR)
    sf = s.sfload(SF2)
    for canal, prog in programas.items():
        if prog == 'drums':
            s.program_select(canal, sf, 128, 0, is_drums=True)
        else:
            s.program_select(canal, sf, 0, prog)
    total = int(SR * (duracion + cola))
    acciones = []
    for ev in eventos:
        t0, c, k, v, d = ev[:5]
        acciones.append((t0, 0, c, k, v))
        acciones.append((t0 + d, 1, c, k, 0))
    acciones.sort(key=lambda a: (a[0], a[1]))
    salida = np.zeros((total, 2), dtype=np.float32)
    pos = 0
    for t, tipo, c, k, v in acciones + [(duracion + cola, 2, 0, 0, 0)]:
        i = min(int(t * SR), total)
        if i > pos:
            buf = s.generate_simple(i - pos)
            salida[pos:i] = np.frombuffer(buf, dtype=np.float32).reshape(-1, 2)
            pos = i
        if tipo == 0:
            s.noteon(c, k, int(v))
        elif tipo == 1:
            s.noteoff(c, k)
    return salida


def mono(x):
    return x.mean(axis=1) if x.ndim == 2 else x


def estereo(x):
    return np.stack([x, x], axis=1) if x.ndim == 1 else x


def t(d):
    return np.arange(int(SR * d)) / SR


def ruido(d):
    return rng.uniform(-1, 1, int(SR * d))


def filtro(x, tipo, f, orden=2):
    b, a = signal.butter(orden, np.array(f) / (SR / 2), tipo)
    return signal.lfilter(b, a, x, axis=0)


def env(d, ataque=0.003, caida=0.1):
    x = t(d)
    return np.minimum(1, x / max(ataque, 1e-4)) * np.exp(-x / caida)


def mezclar(*partes):
    partes = [estereo(p) for p in partes]
    n_ = max(len(p) for p in partes)
    s = np.zeros((n_, 2))
    for p in partes:
        s[:len(p)] += p
    return s


def en(x, segundos, total=None):
    x = estereo(x)
    pad = np.zeros((int(segundos * SR), 2))
    y = np.concatenate([pad, x])
    if total:
        y = np.concatenate([y, np.zeros((max(0, int(total * SR) - len(y)), 2))])[:int(total * SR)]
    return y


_ir_cache = {}


def sala(x, duracion=2.2, humedad=0.28, oscuro=4500):
    """Reverb de sala de piedra (respuesta impulsiva sintética, estéreo)."""
    clave = (duracion, oscuro)
    if clave not in _ir_cache:
        tt = t(duracion)
        irs = []
        for _ in range(2):
            ir = rng.normal(0, 1, len(tt)) * np.exp(-tt * 6.9 / duracion)
            ir = filtro(ir, 'low', oscuro)
            ir[:int(0.012 * SR)] *= np.linspace(0, 1, int(0.012 * SR))
            irs.append(ir / np.sqrt(np.sum(ir ** 2)))
        _ir_cache[clave] = irs
    x = estereo(x)
    irs = _ir_cache[clave]
    mojado = np.stack([signal.fftconvolve(x[:, i], irs[i]) for i in range(2)], axis=1)
    seco = np.concatenate([x, np.zeros((len(mojado) - len(x), 2))])
    return seco * (1 - humedad) + mojado * humedad * 0.6


def guardar(nombre, x, maximo, pico=0.9):
    x = estereo(np.asarray(x, dtype=float))
    x = x[:int(maximo * SR)]
    # recorta el silencio final y desvanece
    nivel = np.abs(x).max(axis=1)
    umbral = nivel.max() * 0.002
    ult = np.nonzero(nivel > umbral)[0]
    if len(ult):
        x = x[:min(len(x), ult[-1] + int(0.05 * SR))]
    f = min(len(x) // 3, int(0.25 * SR))
    x[-f:] *= np.linspace(1, 0, f)[:, None]
    x -= x.mean(axis=0)
    x = x / (np.abs(x).max() + 1e-9) * pico
    wavfile.write(os.path.join(OUT, nombre + '.wav'), SR, (x * 32767).astype(np.int16))
    return len(x) / SR


# ---------------------------------------------------------------- capas sintetizadas
def golpe_madera(f=180, d=0.18, brillo=2500):
    cuerpo = sum(np.sin(2 * np.pi * f * k * t(d)) * np.exp(-t(d) / (0.05 / k)) / k for k in (1, 2.3, 3.9))
    clic = filtro(ruido(d), 'band', [600, brillo]) * env(d, 0.0005, 0.008)
    return cuerpo * 0.8 + clic * 0.6


def soplido(d, f0, f1, ancho=1.3):
    """Silbido de aire que barre de f0 a f1 (giros, volteos)."""
    x = ruido(d)
    out = np.zeros_like(x)
    pasos = 24
    trozo = len(x) // pasos
    for i in range(pasos):
        fc = f0 * (f1 / f0) ** (i / pasos)
        seg = filtro(x, 'band', [fc / ancho, min(fc * ancho, SR / 2 - 100)])[i * trozo:(i + 1) * trozo]
        out[i * trozo:(i + 1) * trozo] = seg
    return out * np.sin(np.pi * t(d)[:len(out)] / d) ** 1.5


def burbujas(d, cuantas, f_lo=300, f_hi=900, subir=True):
    out = np.zeros(int(SR * d))
    for _ in range(cuantas):
        f0 = rng.uniform(f_lo, f_hi)
        dd = rng.uniform(0.03, 0.08)
        tt = t(dd)
        f = f0 * (1 + (1.6 if subir else -0.4) * tt / dd)
        b = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-tt / (dd / 3)) * np.minimum(1, tt / 0.003)
        i = int(rng.uniform(0, d - dd) * SR)
        out[i:i + len(b)] += b * rng.uniform(0.3, 1)
    return out


def chorro(d, f0, f1):
    """Líquido que cae (hidromiel): ruido resonante que sube o baja de tono."""
    return soplido(d, f0, f1, 1.15) * 0.8 + filtro(ruido(d), 'low', 500) * 0.3 * np.sin(np.pi * t(d) / d)


def crujido(d=0.6):
    x = filtro(ruido(d), 'band', [250, 3500]) * env(d, 0.001, 0.06)
    for _ in range(18):
        i = int(rng.uniform(0.01, d * 0.6) * SR)
        a = filtro(ruido(0.025), 'band', [1200, 6000]) * env(0.025, 0.0005, 0.005) * rng.uniform(0.2, 0.7)
        x[i:i + len(a)] += a[:len(x) - i]
    return x


def zumbido(d=1.6):
    tt = t(d)
    f = 205 * (1 + 0.06 * np.sin(2 * np.pi * 7 * tt) + 0.04 * np.sin(2 * np.pi * 1.1 * tt))
    z = 2 * ((np.cumsum(f) / SR) % 1) - 1
    pan = np.sin(2 * np.pi * 0.7 * tt)
    z = filtro(z, 'band', [180, 3200]) * (0.65 + 0.35 * np.sin(2 * np.pi * 26 * tt)) * np.minimum(1, tt / 0.15) * np.minimum(1, (d - tt) / 0.3)
    return np.stack([z * (0.6 - 0.4 * pan), z * (0.6 + 0.4 * pan)], axis=1)


