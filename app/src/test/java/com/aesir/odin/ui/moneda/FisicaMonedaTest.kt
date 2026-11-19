package com.aesir.odin.ui.moneda

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.cos

class FisicaMonedaTest {

    private fun ladoVisible(angulo: Float) = if (cos(Math.toRadians(angulo.toDouble())) >= 0) "frente" else "reverso"

    @Test
    fun lanzamientos_siempre_dan_un_numero_impar_de_medias_vueltas() {
        listOf(0f, 100f, 449f, 450f, 1000f, 3000f, 50_000f).forEach { v ->
            val n = VueloMoneda.medioGirosPorVelocidad(v)
            assertTrue("impar para $v", n % 2 == 1)
            assertTrue(n in 3..41)
        }
        assertTrue(VueloMoneda.medioGirosPorVelocidad(4000f) > VueloMoneda.medioGirosPorVelocidad(500f))
    }

    @Test
    fun al_caer_un_lanzamiento_se_ve_el_reverso_y_queda_quieta() {
        val vuelo = VueloMoneda(medioGiros = 7, alturaPx = 300f, rebotePx = 30f)
        var t = vuelo.enElAire
        while (t <= vuelo.duracion) {
            val pose = vuelo.pose(t)
            assertEquals("reverso", ladoVisible(pose.anguloVisible))
            t += 1f / 120f
        }
        val final = vuelo.pose(vuelo.duracion + 1f)
        assertEquals(0f, final.alturaPx, 0.001f)
        assertEquals(0f, final.bamboleo, 0.001f)
        assertEquals(180f * 7, final.angulo, 0.001f)
    }

    @Test
    fun la_caida_de_entrada_termina_en_la_misma_cara_y_empieza_fuera_de_pantalla() {
        val vuelo = VueloMoneda(VueloMoneda.MEDIO_GIROS_ENTRADA, alturaPx = 900f, rebotePx = 40f, caidaDelCielo = true)
        assertEquals(-900f, vuelo.pose(0f).alturaPx, 0.001f)
        assertEquals("frente", ladoVisible(vuelo.pose(vuelo.duracion).anguloVisible))
        assertTrue(vuelo.pose(vuelo.enElAire * 0.5f).desdeAterrizaje < 0f)
    }

    @Test
    fun la_pose_es_continua_al_tocar_el_suelo() {
        val vuelo = VueloMoneda(medioGiros = 11, alturaPx = 400f, rebotePx = 30f)
        val antes = vuelo.pose(vuelo.enElAire - 0.0005f)
        val despues = vuelo.pose(vuelo.enElAire + 0.0005f)
        assertTrue(abs(antes.alturaPx - despues.alturaPx) < 5f)
        assertTrue(abs(antes.anguloVisible - despues.anguloVisible) < 2f)
    }

    @Test
    fun el_ciclo_pasa_por_las_cinco_caras_y_vuelve_al_escudo_de_mundos() {
        var i = 0
        val vistas = mutableListOf(CaraNombres[i])
        repeat(5) { i = siguienteCara(i, CaraNombres.size); vistas += CaraNombres[i] }
        assertEquals(listOf("MUNDOS", "HASTA_EL_ALBA", "APUESTA_BROKKR", "VOLVA", "POR_REVELAR", "MUNDOS"), vistas)
    }

    @Test
    fun soltar_despacio_regresa_a_su_cara_si_no_paso_de_canto() {
        val asentar = AsentarMoneda(anguloInicial = 60f, alturaInicialPx = -40f, escalaInicial = 1.06f)
        assertTrue(!asentar.cambiaCara)
        val final = asentar.pose(asentar.duracion + 1f)
        assertEquals(0f, final.angulo, 0.001f)
        assertEquals(0f, final.alturaPx, 0.001f)
        assertEquals("frente", ladoVisible(final.anguloVisible))
    }

    @Test
    fun soltar_despacio_termina_de_voltearse_si_ya_paso_de_canto() {
        listOf(120f, -130f).forEach { a ->
            val asentar = AsentarMoneda(anguloInicial = a, alturaInicialPx = -40f, escalaInicial = 1.06f)
            assertTrue(asentar.cambiaCara)
            var t = asentar.enElAire
            while (t <= asentar.duracion) {
                assertEquals("reverso", ladoVisible(asentar.pose(t).anguloVisible))
                t += 1f / 120f
            }
        }
    }

    @Test
    fun lanzar_desde_la_mano_empieza_donde_esta_y_cae_en_la_otra_cara() {
        listOf(1f, -1f).forEach { dir ->
            val vuelo = VueloMoneda(7, 300f, 30f, anguloInicial = 40f * dir, alturaInicialPx = -30f, escalaInicial = 1.05f, direccion = dir)
            val inicio = vuelo.pose(0f)
            assertEquals(40f * dir, inicio.angulo, 0.001f)
            assertEquals(-30f, inicio.alturaPx, 0.001f)
            assertEquals("reverso", ladoVisible(vuelo.pose(vuelo.duracion).anguloVisible))
        }
    }

    @Test
    fun al_arrastrar_se_levanta_poco_a_poco_y_se_voltea_con_el_dedo() {
        assertTrue(ArrastreMoneda.alturaDp(10f) < ArrastreMoneda.alturaDp(60f))
        assertEquals(44f, ArrastreMoneda.alturaDp(1000f), 0.001f)
        assertTrue(ArrastreMoneda.angulo(-50f) > 0f)
        assertTrue(ArrastreMoneda.angulo(50f) < 0f)
        assertEquals(170f, ArrastreMoneda.angulo(-1000f), 0.001f)
    }

    @Test
    fun impulsar_a_medio_vuelo_sigue_hacia_adelante_y_cae_en_la_otra_cara() {
        listOf(1f, -1f).forEach { dir ->
            listOf(0f, 37f, 900f, 2523f).forEach { a ->
                val desde = a * dir
                val vuelo = VueloMoneda(5, 300f, 30f, anguloInicial = desde, alturaInicialPx = -120f, direccion = dir)
                val final = vuelo.pose(vuelo.duracion).angulo
                assertTrue("avanza: $desde -> $final", (final - desde) * dir >= 180f * 4)
                assertEquals("reverso", ladoVisible(vuelo.pose(vuelo.duracion).anguloVisible))
            }
        }
    }

    private companion object {
        // Mismo orden que CaraMoneda (se evita depender de Compose en la prueba).
        val CaraNombres = listOf("MUNDOS", "HASTA_EL_ALBA", "APUESTA_BROKKR", "VOLVA", "POR_REVELAR")
    }

    @Test
    fun hacia_abajo_sale_la_siguiente_y_hacia_arriba_la_anterior() {
        assertEquals(1, caraAlVoltear(0, 5, -1f))
        assertEquals(4, caraAlVoltear(0, 5, 1f))
        assertEquals(0, caraAlVoltear(4, 5, SENTIDO_SIGUIENTE))
        assertEquals(2, caraAlVoltear(3, 5, 30f))
        assertEquals(4, anteriorCara(0, 5))
    }

    @Test
    fun lanzada_hacia_abajo_gira_en_negativo_y_cae_en_el_reverso() {
        val vuelo = VueloMoneda(medioGiros = 5, alturaPx = 300f, rebotePx = 30f, direccion = -1f)
        val final = vuelo.pose(vuelo.duracion + 1f)
        assertTrue(final.angulo < 0f)
        assertEquals("reverso", ladoVisible(final.angulo))
        assertTrue(vuelo.cambiaCara)
    }

    @Test
    fun soltada_despacio_hacia_abajo_pasada_de_canto_termina_en_menos_180() {
        val asentar = AsentarMoneda(-120f, -30f, 1.05f)
        assertTrue(asentar.cambiaCara)
        assertEquals(-180f, asentar.pose(asentar.duracion + 1f).angulo, 0.001f)
        assertEquals(0f, AsentarMoneda(-60f, -30f, 1f).pose(5f).angulo, 0.001f)
    }

    @Test
    fun el_amago_no_llega_a_voltearla_y_alterna_el_sentido() {
        var t = 0f
        var maxArriba = 0f
        var maxAbajo = 0f
        while (t < PistaMoneda.PAUSA_INICIAL + PistaMoneda.CADA * 2) {
            val p = PistaMoneda.poseQuieta(t)
            assertTrue("nunca pasa de canto", abs(p.angulo) < 90f)
            assertEquals(0f, p.alturaPx, 0.001f)
            if (t < PistaMoneda.PAUSA_INICIAL + PistaMoneda.CADA) maxAbajo = minOf(maxAbajo, p.angulo)
            else maxArriba = maxOf(maxArriba, p.angulo)
            t += 1f / 60f
        }
        assertEquals(PoseMoneda.Reposo, PistaMoneda.poseQuieta(1f))
        assertTrue(maxArriba > 40f)
        assertTrue(maxAbajo < -40f)
        assertEquals(PoseMoneda.Reposo, PistaMoneda.pose(PistaMoneda.DURACION + .1f))
    }

    @Test
    fun el_martillo_sube_despacio_y_cae_de_golpe_justo_cuando_destella() {
        assertEquals(0f, curvaGolpe(0f), 0.001f)
        assertEquals(1f, curvaGolpe(0.6f), 0.001f)
        assertEquals(0f, curvaGolpe(0.76f), 0.001f)
        assertTrue("sube despacio", curvaGolpe(0.1f) < 0.4f)
        assertTrue("cae rapido", curvaGolpe(0.72f) > curvaGolpe(0.75f) + 0.3f)
        assertEquals(0f, curvaDestello(0.7f), 0.001f)
        assertEquals(1f, curvaDestello(0.76f), 0.001f)
        assertTrue(curvaDestello(0.95f) < 0.1f)
    }
}
