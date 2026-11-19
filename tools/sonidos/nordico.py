# MOD-02: efectos de sonido de ODÍN (ver comun.py).
from comun import *

# ---------------------------------------------------------------- efectos
dur = {}

# Interfaz -----------------------------------------------------------
# Toque: cuerda de lira apagada + nudillo sobre madera
x = tocar([(0, 0, n('D4'), 70, 0.12)], 0.15, {0: ARPA}, cola=0.3)
dur['toque'] = guardar('toque', sala(mezclar(x * 0.9, golpe_madera(210, 0.15) * 0.35), 0.8, 0.12), 0.6)

# Volteo de carta: pergamino que roza el aire + nota de lira
pergamino = soplido(0.22, 1800, 5200, 1.6) * 0.5
x = tocar([(0.08, 0, n('A4'), 55, 0.15)], 0.25, {0: ARPA}, cola=0.4)
dur['volteo_carta'] = guardar('volteo_carta', sala(mezclar(pergamino, x * 0.7), 0.8, 0.12), 0.7)

# Soltar pieza: runa de piedra que se asienta
piedra = golpe_madera(130, 0.2, 1800) + filtro(ruido(0.2), 'band', [300, 1500]) * env(0.2, 0.001, 0.02) * 0.6
x = tocar([(0, 9, 45, 60, 0.2)], 0.2, {9: 'drums'}, cola=0.3)  # tom grave muy suave
dur['soltar_pieza'] = guardar('soltar_pieza', sala(mezclar(piedra, mono(x) * 0.25), 0.7, 0.1), 0.6)

# Giro de la moneda: zumbido cálido que acelera (menos agudo) + aire + destellos de arpa
d = 0.95
tt = t(d)
vel = 7 + 26 * (tt / d) ** 1.3
trem = 0.55 + 0.45 * np.sin(2 * np.pi * np.cumsum(vel) / SR)
canto = sum(np.sin(2 * np.pi * f * tt) * a for f, a in ((660, 1), (1043, 0.45), (1530, 0.2)))
canto = filtro(canto * trem, 'low', 2200) * np.minimum(1, tt / 0.06) * np.exp(-tt / 0.75)
aire = soplido(d, 500, 1600, 1.4) * 0.35
arpa = tocar([(0.0, 0, n('D5'), 40, 0.1), (0.12, 0, n('A5'), 38, 0.1), (0.24, 0, n('D6'), 34, 0.1)], d, {0: ARPA}, cola=0.4)
dur['giro_moneda'] = guardar('giro_moneda', sala(mezclar(canto * 0.6, aire, arpa * 0.35), 1.2, 0.2), 1.3)

# Caída de la moneda: cae sobre la mesa de madera (golpe sordo, tintineo apagado y breve)
def tintin(f):
    d_ = 0.35
    return sum(np.sin(2 * np.pi * f * k * t(d_)) * np.exp(-t(d_) / (0.06 / k)) * a for k, a in ((1, 1), (1.5, 0.35), (2.2, 0.15)))
caida = np.zeros(int(SR * 0.9))
for i, (en_s, a) in enumerate(((0, 1.0), (0.13, 0.5), (0.21, 0.28), (0.26, 0.15))):
    trozo = mono(mezclar(golpe_madera(150, 0.25) * a * 0.9, filtro(tintin(1250), "low", 2600) * a * 0.35))
    j = int(en_s * SR)
    caida[j:j + len(trozo)] += trozo[:len(caida) - j]
x = tocar([(0, 9, 41, 70, 0.2)], 0.3, {9: 'drums'}, cola=0.4)  # tom de piso
dur['caida_moneda'] = guardar('caida_moneda', sala(mezclar(caida, mono(x) * 0.35), 1.0, 0.18), 1.2)

# Respuesta ------------------------------------------------------------
# Acierto: lira que sube Re-La-Re y un "oh" grave del coro
x = tocar([(0, 0, n('D4'), 90, 0.5), (0.09, 0, n('A4'), 90, 0.5), (0.18, 0, n('D5'), 100, 0.8),
           (0.15, 1, n('D3'), 65, 0.7), (0.15, 1, n('A3'), 60, 0.7)], 1.0, {0: ARPA, 1: OOHS}, cola=0.6)
dur['acierto'] = guardar('acierto', sala(x, 1.6, 0.25), 1.6)

# Error: tambor grave apagado + cello que cae medio tono (no da pistas, RN-14)
x = tocar([(0, 0, n('Bb2'), 95, 0.22), (0.2, 0, n('A2'), 90, 0.4), (0, 9, 36, 100, 0.2)], 0.65, {0: CELLO, 9: 'drums'}, cola=0.4)
dur['error'] = guardar('error', sala(x, 1.0, 0.18), 0.95)

# Incompleta: dos notas de lira que quedan en suspenso
x = tocar([(0, 0, n('D4'), 80, 0.5), (0.12, 0, n('E4'), 80, 0.6)], 0.7, {0: ARPA}, cola=0.5)
dur['incompleta'] = guardar('incompleta', sala(x, 1.4, 0.25), 1.1)

# Tiempo --------------------------------------------------------------
# Tambor de guerra (taiko + timbal) — un golpe por segundo
x = tocar([(0, 0, n('D2'), 120, 0.4), (0, 1, n('D2'), 110, 0.4)], 0.4, {0: TAIKO, 1: TIMPANI}, cola=0.5)
dur['tambor'] = guardar('tambor', sala(x, 1.0, 0.15), 0.75)

# Hidromiel ------------------------------------------------------------
# Gana: hidromiel que llena el cuerno (chorro que sube) + burbujas + lira brillante
d = 1.3
llena = chorro(d, 350, 1300) * 0.55 + burbujas(d, 14, 400, 1000) * 0.35
x = tocar([(0.25, 0, n('F#4'), 75, 0.4), (0.4, 0, n('A4'), 75, 0.4), (0.55, 0, n('D5'), 85, 0.7)], d, {0: ARPA}, cola=0.6)
dur['cuerno_llena'] = guardar('cuerno_llena', sala(mezclar(llena, x * 0.8), 1.4, 0.22), 1.9)

# Pierde: el cuerno se vacía (chorro que baja) + cello grave
d = 1.1
vacia = chorro(d, 1100, 260) * 0.6 + burbujas(d, 8, 200, 500, subir=False) * 0.25
x = tocar([(0.05, 0, n('F3'), 75, 0.5), (0.4, 0, n('D3'), 70, 0.6)], d, {0: CELLO}, cola=0.5)
dur['cuerno_vacia'] = guardar('cuerno_vacia', sala(mezclar(vacia, x * 0.7), 1.3, 0.2), 1.7)

# Progreso ------------------------------------------------------------
# Desbloqueo: cuerno de guerra (Gjallarhorn) con tambores
x = tocar([(0, 0, n('D3'), 110, 2.2), (0, 1, n('A2'), 105, 2.2), (0.6, 0, n('A3'), 100, 1.6), (0, 2, n('D2'), 95, 2.4),
           (0, 3, n('D2'), 127, 0.5), (0.45, 3, n('D2'), 110, 0.5), (0.6, 3, n('D2'), 120, 0.5)],
          2.6, {0: TROMPA, 1: TROMBON, 2: TUBA, 3: TAIKO}, cola=1.5)
dur['cuerno_guerra'] = guardar('cuerno_guerra', sala(x, 2.6, 0.32), 4.8)

# Logro: coro del salón (Re menor que se abre a Re mayor) + arpa
x = tocar([(0, 0, n('D3'), 90, 1.4), (0, 0, n('F3'), 85, 1.4), (0, 0, n('A3'), 85, 1.4),
           (1.4, 0, n('D3'), 95, 1.9), (1.4, 0, n('F#3'), 95, 1.9), (1.4, 0, n('A3'), 95, 1.9), (1.4, 0, n('D4'), 90, 1.9),
           (1.4, 1, n('D4'), 80, 1.5), (1.55, 1, n('F#4'), 80, 1.5), (1.7, 1, n('A4'), 85, 1.5), (1.85, 1, n('D5'), 90, 1.5),
           (1.4, 2, n('D2'), 110, 0.5)], 3.4, {0: CORO, 1: ARPA, 2: TIMPANI}, cola=1.5)
dur['coro_salon'] = guardar('coro_salon', sala(x, 2.8, 0.32), 4.9)

# Valhalla ------------------------------------------------------------
# Golpe al enemigo: tajo en el aire + golpe en el escudo + tambor
tajo = soplido(0.16, 900, 4500, 1.5) * 0.6
x = tocar([(0.12, 0, n('D2'), 127, 0.3), (0.12, 9, 49, 70, 0.3)], 0.5, {0: TAIKO, 9: 'drums'}, cola=0.6)
impacto = en(golpe_madera(110, 0.3, 2000) * 0.7, 0.12)
dur['golpe_enemigo'] = guardar('golpe_enemigo', sala(mezclar(tajo, x, impacto), 1.2, 0.2), 1.3)

# Escudo roto: madera que se astilla + cuerdas en sforzando
x = tocar([(0, 0, n('D2'), 110, 0.35), (0, 0, n('A2'), 100, 0.35), (0, 1, n('D2'), 115, 0.3)], 0.5, {0: CELLO, 1: TAIKO}, cola=0.5)
dur['escudo_roto'] = guardar('escudo_roto', sala(mezclar(filtro(crujido(0.7), 'low', 4500) * 0.8, x * 0.8), 1.2, 0.18), 1.4)

# Cuerno de Odín: cuernos y tuba muy graves, coro y redoble de timbal
eventos = [(0, 0, n('D2'), 115, 3.4), (0, 0, n('A2'), 110, 3.4), (0, 1, n('D1'), 105, 3.6),
           (0.8, 2, n('D3'), 85, 2.8), (0.8, 2, n('A3'), 80, 2.8), (0.8, 2, n('F3'), 80, 2.8)]
eventos += [(0.0 + i * 0.07, 3, n('D2'), 50 + i * 3, 0.08) for i in range(18)]
eventos += [(1.3, 3, n('D2'), 127, 0.6), (1.3, 4, n('D2'), 127, 0.6)]
x = tocar(eventos, 3.8, {0: TROMPA, 1: TUBA, 2: CORO, 3: TIMPANI, 4: TAIKO}, cola=1.4)
dur['cuerno_odin'] = guardar('cuerno_odin', sala(x, 3.0, 0.35), 4.9)

# Martillo en la forja: yunque cálido (menos agudo) + golpe sordo + chispas
d = 1.2
yunque = sum(np.sin(2 * np.pi * f * t(d)) * np.exp(-t(d) / c) * a for f, a, c in ((620, 1, .35), (1310, .5, .2), (2140, .25, .1)))
yunque = filtro(yunque, 'low', 3000)
chispas = filtro(filtro(ruido(d), 'high', 3000), 'low', 7000) * env(d, 0.001, 0.04) * 0.07
x = tocar([(0, 0, n('D2'), 110, 0.3)], 0.4, {0: TAIKO}, cola=0.5)
dur['martillo'] = guardar('martillo', sala(mezclar(yunque * 0.55, chispas, golpe_madera(95, 0.3) * 0.7, x * 0.6), 1.3, 0.22), 1.6)

# Mosca de Loki: zumbido que da vueltas + violín que se burla
x = tocar([(0.2, 0, n('A5'), 50, 0.12), (0.35, 0, n('Bb5'), 50, 0.12), (0.5, 0, n('A5'), 50, 0.12)], 1.6, {0: VIOLIN_F}, cola=0.4)
dur['mosca_loki'] = guardar('mosca_loki', sala(mezclar(zumbido(1.6) * 0.8, x * 0.3), 1.0, 0.15), 1.9)

# Caldero de la Völva: burbujeo + coro murmurado + vidrio
d = 2.6
cal = filtro(ruido(d), 'low', 160) * 1.5 + burbujas(d, 30, 180, 420) * 0.6
cal *= np.minimum(1, t(d) / 0.3)[:len(cal)]
x = tocar([(0, 0, n('D3'), 60, 2.4), (0, 0, n('A3'), 55, 2.4), (0.3, 1, n('D5'), 40, 2.0)], d, {0: OOHS, 1: VIDRIO}, cola=0.8)
dur['caldero'] = guardar('caldero', sala(mezclar(cal * 0.6, x * 0.7), 2.0, 0.25), 2.95)

for k, v in dur.items():
    print('%-14s %.2f s' % (k, v))
