package com.aesir.odin.domain.notificaciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/** MOD-04 Cuervos de Odín: tipos, prioridad y límites de los avisos. */
class ModuloCuervosTest {

    private val hoy = LocalDate.of(2026, 11, 17)
    private val prefs = PreferenciasCuervos()
    private val sinRacha = SituacionRacha(0, null)

    private fun decidir(
        ultimaApertura: LocalDate?,
        racha: SituacionRacha = sinRacha,
        ultimoEnvio: LocalDate? = null,
        inactividadAvisada: LocalDate? = null,
        preferencias: PreferenciasCuervos = prefs,
        enPrimerPlano: Boolean = false
    ) = ModuloCuervos.decidir(hoy, EstadoCuervos(ultimaApertura, ultimoEnvio, inactividadAvisada), racha, preferencias, enPrimerPlano)

    @Test
    fun `recompensa diaria si no ha abierto la app ese dia`() {
        assertEquals(TipoNotificacion.RECOMPENSA_DIARIA, decidir(hoy.minusDays(1)))
    }

    @Test
    fun `racha en riesgo tiene mas prioridad que la recompensa diaria`() {
        assertEquals(TipoNotificacion.RACHA_EN_RIESGO, decidir(hoy.minusDays(1), SituacionRacha(4, hoy.minusDays(1))))
        // Abrió hoy pero no ha hecho una actividad que cuente
        assertEquals(TipoNotificacion.RACHA_EN_RIESGO, decidir(hoy, SituacionRacha(4, hoy.minusDays(1))))
    }

    @Test
    fun `inactividad al tercer dia sin abrir y con la mayor prioridad`() {
        assertEquals(TipoNotificacion.RECOMPENSA_DIARIA, decidir(hoy.minusDays(2)))
        assertEquals(TipoNotificacion.INACTIVIDAD, decidir(hoy.minusDays(3), SituacionRacha(5, hoy.minusDays(3))))
    }

    @Test
    fun `E3 nada pendiente si ya abrio y ya hizo una actividad`() {
        assertNull(decidir(hoy, SituacionRacha(2, hoy)))
        assertNull(decidir(hoy))
    }

    @Test
    fun `E2 y E4 desactivadas o con la app abierta no se envia nada`() {
        assertNull(decidir(hoy.minusDays(1), preferencias = prefs.copy(activadas = false)))
        assertNull(decidir(hoy.minusDays(1), enPrimerPlano = true))
    }

    @Test
    fun `RN-21 una notificacion al dia`() {
        assertNull(decidir(hoy.minusDays(1), ultimoEnvio = hoy))
    }

    @Test
    fun `E5 y RN-23 despues del aviso de inactividad no se envia nada hasta volver a abrir`() {
        val apertura = hoy.minusDays(5)
        assertNull(decidir(apertura, inactividadAvisada = apertura))
        // Al abrir de nuevo empieza otro periodo
        assertEquals(TipoNotificacion.RECOMPENSA_DIARIA, decidir(hoy.minusDays(1), inactividadAvisada = apertura))
    }

    @Test
    fun `la proxima hora es hoy si no ha pasado y si no manana`() {
        val p = PreferenciasCuervos(hora = 19, minuto = 0)
        assertEquals(LocalDateTime.of(2026, 11, 17, 19, 0), ModuloCuervos.proximaHora(LocalDateTime.of(2026, 11, 17, 8, 0), p))
        assertEquals(LocalDateTime.of(2026, 11, 18, 19, 0), ModuloCuervos.proximaHora(LocalDateTime.of(2026, 11, 17, 19, 0), p))
        assertEquals("19:00", p.textoHora)
    }

    private class AlmacenMemoria : AlmacenCuervos {
        var prefs = PreferenciasCuervos()
        var estado = EstadoCuervos()
        override fun leerPreferencias() = prefs
        override fun guardarPreferencias(preferencias: PreferenciasCuervos) { prefs = preferencias }
        override fun leerEstado() = estado
        override fun guardarEstado(estado: EstadoCuervos) { this.estado = estado }
    }

    private class ProgramadorMemoria : ProgramadorCuervos {
        var programado: LocalDateTime? = null
        var cancelado = false
        override fun programar(momento: LocalDateTime) { programado = momento; cancelado = false }
        override fun cancelar() { programado = null; cancelado = true }
    }

    @Test
    fun `al abrir registra la fecha y programa el aviso a la hora configurada`() {
        val almacen = AlmacenMemoria()
        val programador = ProgramadorMemoria()
        val modulo = ModuloCuervos(almacen, programador) { LocalDateTime.of(2026, 11, 17, 10, 30) }
        assertTrue(modulo.debeSolicitarPermiso())
        modulo.registrarPermisoSolicitado()
        assertFalse(modulo.debeSolicitarPermiso())
        modulo.alAbrirAplicacion()
        assertEquals(hoy, almacen.estado.ultimaApertura)
        assertEquals(LocalDateTime.of(2026, 11, 17, 19, 0), programador.programado)
        modulo.cambiarPreferencias(PreferenciasCuervos(activadas = false))
        assertTrue("E2: se cancelan los avisos programados", programador.cancelado)
    }

    @Test
    fun `a la hora registra el envio y programa el del dia siguiente`() {
        val almacen = AlmacenMemoria().apply { estado = EstadoCuervos(ultimaApertura = hoy.minusDays(3)) }
        val programador = ProgramadorMemoria()
        val modulo = ModuloCuervos(almacen, programador) { LocalDateTime.of(2026, 11, 17, 19, 0) }
        assertEquals(TipoNotificacion.INACTIVIDAD, modulo.alLlegarLaHora(enPrimerPlano = false, racha = SituacionRacha(0, null)))
        assertEquals(hoy, almacen.estado.ultimoEnvio)
        assertEquals(hoy.minusDays(3), almacen.estado.inactividadAvisada)
        assertEquals(LocalDateTime.of(2026, 11, 18, 19, 0), programador.programado)
        // Al día siguiente ya no se avisa (E5)
        val manana = ModuloCuervos(almacen, programador) { LocalDateTime.of(2026, 11, 18, 19, 0) }
        assertNull(manana.alLlegarLaHora(enPrimerPlano = false, racha = SituacionRacha(0, null)))
    }
}
