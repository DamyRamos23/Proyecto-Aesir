"""Convierte las pantallas de inicio aprobadas (Design · «Pantallas de inicio de los modos»)
en escenas animadas autónomas: solo el arte (SVG + animaciones CSS), sin la UI, que la app
dibuja de forma nativa encima. La parallax y la activación se controlan desde JS."""
import re, sys, pathlib

FACTORES = {"pStars": 2, "pMoon": 4, "pFly": 5, "pStones": 7, "pFigure": 10,
            "pThreads": 11, "pCauldron": 13, "pFront": 20}
OVERLAY = {"volva": "LA VÖLVA DESPIERTA…", "duelo": "EL GIGANTE ACEPTA EL RETO",
           "alba": "QUE COMIENCE LA NOCHE", "brokkr": "LA FORJA ARDE"}

def convertir(origen: pathlib.Path, nombre: str) -> str:
    t = origen.read_text(encoding="utf-8")
    estilo = re.search(r"<style>(.*?)</style>", t, re.S).group(1)
    svg = re.search(r'(<svg width="390" height="844".*?\n</svg>)', t, re.S).group(1)
    overlay_bg = re.search(r'class="\{\{overlayClass\}\}"[^>]*background: ([^"]+)"', t).group(1)
    for k, f in FACTORES.items():
        svg = svg.replace("{{%s}}" % k, "calc(var(--tx) * %dpx), calc(var(--ty) * %.1fpx)" % (f, f * .6))
    svg = svg.replace('class="{{actClass}}"', 'class="js-act"')
    svg = svg.replace('class="{{perchL}}"', 'class="v-perch js-perch-l"')
    svg = svg.replace('class="{{perchR}}"', 'class="v-perch js-perch-r"')
    svg = svg.replace('class="{{cauldronClass}}"', 'class="js-boil"')
    svg = svg.replace('<svg width="390" height="844"', '<svg width="100%" height="100%" preserveAspectRatio="xMidYMid slice"', 1)
    resto = re.findall(r"\{\{\w+\}\}", svg)
    if resto:
        sys.exit(f"{nombre}: marcadores sin resolver {set(resto)}")
    return f"""<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
<!-- Generado por tools/escenas.py a partir del diseño aprobado. No editar a mano. -->
<style>
html,body{{margin:0;height:100%;overflow:hidden;background:#06070d}}
:root{{--tx:0;--ty:0}}
svg{{position:absolute;inset:0;display:block}}
{estilo.strip()}
.v-ovl{{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;pointer-events:none;background:{overlay_bg}}}
.v-ovl p{{margin:0;font-family:Georgia,'Times New Roman',serif;font-size:22px;letter-spacing:4px;color:#f1e6ff;text-align:center}}
</style></head>
<body>
{svg}
<div class="v-ovl v-overlay" id="ovl"><p>{OVERLAY[nombre]}</p></div>
<script>
(function(){{
  var r=document.documentElement.style;
  window.odinInclinar=function(x,y){{r.setProperty('--tx',x.toFixed(3));r.setProperty('--ty',y.toFixed(3));}};
  function cambiar(sel,quitar,poner){{document.querySelectorAll(sel).forEach(function(e){{if(quitar)e.classList.remove(quitar);e.classList.add(poner);}});}}
  window.odinActivar=function(){{
    cambiar('.js-act',null,'v-act');cambiar('.js-boil',null,'v-boil');
    cambiar('.js-perch-l','v-perch','v-flyoff-l');cambiar('.js-perch-r','v-perch','v-flyoff-r');
    document.getElementById('ovl').classList.add('v-overlay-on');
  }};
  window.odinReposar=function(){{
    document.querySelectorAll('.v-act,.v-boil').forEach(function(e){{e.classList.remove('v-act');e.classList.remove('v-boil');}});
    cambiar('.js-perch-l','v-flyoff-l','v-perch');cambiar('.js-perch-r','v-flyoff-r','v-perch');
    document.getElementById('ovl').classList.remove('v-overlay-on');
  }};
}})();
</script>
</body></html>
"""

if __name__ == "__main__":
    src, *destinos = map(pathlib.Path, sys.argv[1:])
    for n in OVERLAY:
        html = convertir(src / f"{n}.html", n)
        for d in destinos:
            d.mkdir(parents=True, exist_ok=True)
            (d / f"escena_{n}.html").write_text(html, encoding="utf-8")
        print(n, len(html))
