package com.aesir.odin.domain.sonido

import com.aesir.odin.domain.hidromiel.AvisoHidromiel
import com.aesir.odin.domain.hidromiel.TipoMovimiento
import com.aesir.odin.domain.model.EstadoRespuesta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** MOD-02: catálogo, preferencias, separación mínima, prioridad y segundo plano. */
class ModuloSonidoTest {

    private class ReproductorFalso : ReproductorEfectos {
        val reproducidos = mutableListOf<EventoSonido>()
        val velocidades = mutableListOf<Float>()
        val detenidos = mutableListOf<Int>()
        var volumen = -1f
        var falla = false
        var detenerTodosLlamado = 0
        var cargados = emptyList<EventoSonido>()
        private var id = 0
        override fun cargar(catalogo: List<EventoSonido>) { cargados = catalogo }
        override fun reproducir(evento: EventoSonido, volumen: Float, velocidad: Float): Int? {
            if (falla) return null
            reproducidos += evento
            velocidades += velocidad
            this.volumen = volumen
            return ++id
        }
        override fun detener(id: Int) { detenidos += id }
        override fun detenerTodos() { detenerTodosLlamado++ }
    }

    private class AlmacenFalso(var guardadas: PreferenciasSonido? = null, val fallaLeer: Boolean = false) : AlmacenPreferenciasSonido {
        override fun leer(): PreferenciasSonido? = if (fallaLeer) error("dañado") else guardadas
        override fun guardar(preferencias: PreferenciasSonido): Boolean { guardadas = preferencias; return true }
    }

    private class MusicaFalsa : ReproductorMusica {
        var sonando = false
        var volumen = -1f
        var pista: PistaMusica? = null
        override fun reproducir(pista: PistaMusica, volumen: Float) { sonando = true; this.volumen = volumen; this.pista = pista }
        override fun pausar() { sonando = false }
        override fun cambiarVolumen(volumen: Float) { this.volumen = volumen }
    }

    private var reloj = 0L
    private lateinit var reproductor: ReproductorFalso
    private lateinit var almacen: AlmacenFalso
    private lateinit var modulo: ModuloSonido

    @Before
    fun preparar() {
        reloj = 1_000
        reproductor = ReproductorFalso()
        almacen = AlmacenFalso()
        musica = MusicaFalsa()
        modulo = ModuloSonido(almacen, reproductor, musica, ahoraMs = { reloj })
        modulo.alAbrirAplicacion()
    }

    private lateinit var musica: MusicaFalsa

    @Test
    fun al_abrir_carga_todo_el_catalogo_con_sonido_activado_al_80() {
        assertEquals(EventoSonido.entries.toList(), reproductor.cargados)
        assertEquals(PreferenciasSonido(true, 0.8f, true, 0.45f), modulo.preferencias.value)
        modulo.avisar(EventoSonido.TOQUE)
        assertEquals(0.8f, reproductor.volumen, 0.0001f)
    }

    @Test
    fun preferencias_ilegibles_usan_las_predeterminadas() {
        val m = ModuloSonido(AlmacenFalso(fallaLeer = true), reproductor, ahoraMs = { reloj })
        m.alAbrirAplicacion()
        assertEquals(PreferenciasSonido(), m.preferencias.value)
    }

    @Test
    fun e1_con_el_sonido_desactivado_no_suena_nada_y_se_guarda() {
        modulo.cambiarPreferencias(PreferenciasSonido(activado = false))
        modulo.avisar(EventoSonido.ACIERTO)
        assertTrue(reproductor.reproducidos.isEmpty())
        assertEquals(false, almacen.guardadas?.activado)
    }

    @Test
    fun e4_el_mismo_efecto_antes_de_100_ms_se_ignora() {
        modulo.avisar(EventoSonido.TOQUE)
        reloj += 50
        modulo.avisar(EventoSonido.TOQUE)
        reloj += 60
        modulo.avisar(EventoSonido.TOQUE)
        assertEquals(2, reproductor.reproducidos.size)
    }

    @Test
    fun paso_5_con_4_sonando_se_calla_el_de_menor_prioridad() {
        listOf(EventoSonido.GIRO_MONEDA, EventoSonido.HIDROMIEL_GANA, EventoSonido.CALDERO, EventoSonido.LOGRO)
            .forEach { modulo.avisar(it); reloj += 10 }
        modulo.avisar(EventoSonido.ACIERTO)
        assertEquals(listOf(1), reproductor.detenidos) // el giro de la moneda (Interfaz)
        assertEquals(4, modulo.efectosSonando())
        // Un efecto de menor prioridad que todos los que suenan no entra
        listOf(EventoSonido.DESBLOQUEO).forEach { modulo.avisar(it) } // reemplaza al de Hidromiel
        reloj += 10
        modulo.avisar(EventoSonido.TOQUE)
        assertEquals(EventoSonido.DESBLOQUEO, reproductor.reproducidos.last())
    }

    @Test
    fun paso_6_varios_efectos_suenan_del_de_mayor_prioridad_al_de_menor() {
        modulo.avisar(listOf(EventoSonido.HIDROMIEL_GANA, EventoSonido.GOLPE_ENEMIGO, EventoSonido.ACIERTO))
        assertEquals(listOf(EventoSonido.ACIERTO, EventoSonido.GOLPE_ENEMIGO, EventoSonido.HIDROMIEL_GANA), reproductor.reproducidos)
    }

    @Test
    fun los_efectos_terminados_dejan_lugar() {
        modulo.avisar(EventoSonido.TOQUE)
        reloj += EventoSonido.TOQUE.duracionMs
        assertEquals(0, modulo.efectosSonando())
    }

    @Test
    fun e2_y_rn13_si_falla_la_reproduccion_la_actividad_sigue() {
        reproductor.falla = true
        modulo.avisar(EventoSonido.ERROR) // no lanza
        assertEquals(0, modulo.efectosSonando())
    }

    @Test
    fun paso_8_en_segundo_plano_se_detiene_y_no_suena_hasta_volver() {
        modulo.avisar(EventoSonido.CALDERO)
        modulo.alPasarASegundoPlano()
        assertEquals(1, reproductor.detenerTodosLlamado)
        assertEquals(0, modulo.efectosSonando())
        reloj += 500
        modulo.avisar(EventoSonido.TOQUE)
        assertEquals(1, reproductor.reproducidos.size)
        modulo.alVolverAPrimerPlano()
        modulo.avisar(EventoSonido.TOQUE)
        assertEquals(2, reproductor.reproducidos.size)
    }

    @Test
    fun tambor_solo_en_los_ultimos_5_segundos_y_cada_vez_mas_rapido() {
        (10 downTo 0).forEach { modulo.avisarTiempoRestante(it); reloj += 1_000 }
        assertEquals(5, reproductor.reproducidos.count { it == EventoSonido.TAMBOR })
        assertTrue(reproductor.velocidades.zipWithNext().all { (a, b) -> b > a })
    }

    @Test
    fun hidromiel_cuerno_que_se_llena_o_se_vacia() {
        modulo.avisarHidromiel(AvisoHidromiel(TipoMovimiento.RECOMPENSA_NIVEL, 40, 100, ""))
        reloj += 200
        modulo.avisarHidromiel(AvisoHidromiel(TipoMovimiento.ENTRADA_NIVEL, -20, 80, ""))
        reloj += 200
        modulo.avisarHidromiel(AvisoHidromiel(TipoMovimiento.ENTRADA_NIVEL, 0, 80, ""))
        assertEquals(listOf(EventoSonido.HIDROMIEL_GANA, EventoSonido.HIDROMIEL_PIERDE), reproductor.reproducidos)
    }

    @Test
    fun rn14_cualquier_respuesta_incorrecta_suena_igual() {
        assertEquals(EventoSonido.ACIERTO, eventoDeRespuesta(EstadoRespuesta.CORRECTA))
        assertEquals(EventoSonido.INCOMPLETA, eventoDeRespuesta(EstadoRespuesta.INCOMPLETA))
        assertEquals(EventoSonido.ERROR, eventoDeRespuesta(EstadoRespuesta.INCORRECTA))
        assertEquals(EventoSonido.ERROR, eventoDeRespuesta(null))
    }

    @Test
    fun ningun_efecto_pasa_de_su_duracion_maxima() {
        EventoSonido.entries.forEach {
            val maximo = if (it == EventoSonido.CUERNO_ODIN) 5_000L else it.categoria.duracionMaximaMs
            assertTrue("${it.name} dura ${it.duracionMs}", it.duracionMs <= maximo)
        }
    }

    @Test
    fun el_volumen_se_limita_entre_0_y_1() {
        modulo.cambiarPreferencias(PreferenciasSonido(volumen = 1.7f))
        assertEquals(1f, modulo.preferencias.value.volumen, 0f)
    }

    @Test
    fun la_musica_suena_al_frente_se_pausa_atras_y_respeta_ajustes() {
        modulo.alVolverAPrimerPlano()
        assertTrue(musica.sonando)
        assertEquals(0.45f, musica.volumen, 0.0001f)
        modulo.alPasarASegundoPlano()
        assertEquals(false, musica.sonando)
        modulo.alVolverAPrimerPlano()
        modulo.cambiarPreferencias(modulo.preferencias.value.copy(volumenMusica = 0.2f))
        assertEquals(0.2f, musica.volumen, 0.0001f)
        modulo.cambiarPreferencias(modulo.preferencias.value.copy(musica = false))
        assertEquals(false, musica.sonando)
        assertEquals(false, almacen.guardadas?.musica)
        modulo.alInterrumpir()
        assertEquals(false, musica.sonando)
    }

    @Test
    fun cada_pantalla_cambia_a_su_pista_y_sin_musica_no_suena() {
        modulo.alVolverAPrimerPlano()
        assertEquals(PistaMusica.PRINCIPAL, musica.pista)
        modulo.cambiarPista(PistaMusica.BATALLA)
        assertEquals(PistaMusica.BATALLA, musica.pista)
        modulo.cambiarPista(PistaMusica.VOLVA)
        assertEquals(PistaMusica.VOLVA, musica.pista)
        modulo.cambiarPreferencias(modulo.preferencias.value.copy(musica = false))
        modulo.cambiarPista(PistaMusica.CONTRARRELOJ)
        assertEquals(false, musica.sonando)
        assertEquals(PistaMusica.CONTRARRELOJ, modulo.pista)
    }

    @Test
    fun cada_ruta_tiene_su_pista() {
        assertEquals(PistaMusica.PRINCIPAL, pistaParaRuta("mundos"))
        assertEquals(PistaMusica.BATALLA, pistaParaRuta("ragnarok/{mundoId}"))
        assertEquals(PistaMusica.BATALLA, pistaParaRuta("hastaElAlba"))
        assertEquals(PistaMusica.BATALLA, pistaParaRuta("forjaBrokkr"))
        assertEquals(PistaMusica.CONTRARRELOJ, pistaParaRuta("retoContrareloj"))
        assertEquals(PistaMusica.LECCION, pistaParaRuta("leccion/{id}"))
        assertEquals(PistaMusica.LECCION, pistaParaRuta("dueloEnigmas"))
        assertEquals(PistaMusica.VOLVA, pistaParaRuta("volva"))
        assertEquals(PistaMusica.PRINCIPAL, pistaParaRuta(null))
    }
}
