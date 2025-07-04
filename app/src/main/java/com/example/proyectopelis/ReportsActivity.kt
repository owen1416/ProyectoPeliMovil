package com.example.proyectopelis

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart // Importar BarChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarData // Importar BarData
import com.github.mikephil.charting.data.BarDataSet // Importar BarDataSet
import com.github.mikephil.charting.data.BarEntry // Importar BarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter // Para formatear etiquetas del eje X
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View // Importar View para los círculos de la leyenda
import java.util.Collections // Para ordenar mapas

class ReportsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var toolbar: Toolbar
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalMovies: TextView
    private lateinit var pieChartMoviesByGenre: PieChart
    private lateinit var barChartMoviesByYear: BarChart // Declarar el BarChart
    private lateinit var pieChartUsersByRole: PieChart
    private lateinit var btnRefreshReports: MaterialButton

    // Referencias a los LinearLayouts para las leyendas
    private lateinit var llMoviesByGenreLegend: LinearLayout
    private lateinit var llUsersByRoleLegend: LinearLayout

    // Colores personalizados para las gráficas (puedes ajustarlos)
    private val chartColors = listOf(
        Color.parseColor("#8A2BE2"), // purple_primary
        Color.parseColor("#6A1B9A"), // purple_dark
        Color.parseColor("#42A5F5"), // Un azul claro
        Color.parseColor("#FFCA28"), // Un amarillo
        Color.parseColor("#EF5350"), // Un rojo claro
        Color.parseColor("#66BB6A")  // Un verde
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        db = Firebase.firestore

        toolbar = findViewById(R.id.toolbar_reports)
        tvTotalUsers = findViewById(R.id.tv_total_users)
        tvTotalMovies = findViewById(R.id.tv_total_movies)
        pieChartMoviesByGenre = findViewById(R.id.pie_chart_movies_by_genre)
        barChartMoviesByYear = findViewById(R.id.bar_chart_movies_by_year) // Inicializar el BarChart
        pieChartUsersByRole = findViewById(R.id.pie_chart_users_by_role)
        btnRefreshReports = findViewById(R.id.btn_refresh_reports)

        // Inicialización de los LinearLayouts de leyenda con los IDs CORRECTOS
        llMoviesByGenreLegend = findViewById(R.id.ll_movies_by_genre_legend)
        llUsersByRoleLegend = findViewById(R.id.ll_users_by_role_legend)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reportes y Estadísticas"

        // Configurar el botón de refrescar
        btnRefreshReports.setOnClickListener {
            loadReports()
            Toast.makeText(this, "Reportes actualizados", Toast.LENGTH_SHORT).show()
        }

        // Configuración inicial de las gráficas
        setupPieChart(pieChartMoviesByGenre, "Distribución de Géneros")
        setupPieChart(pieChartUsersByRole, "Distribución de Roles")
        setupBarChart(barChartMoviesByYear, "Películas por Año") // Configurar el BarChart

        loadReports()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupPieChart(pieChart: PieChart, descriptionText: String) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE) // Fondo del centro del pastel
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.setCenterText(descriptionText) // Texto en el centro del pastel
        pieChart.setCenterTextColor(Color.BLACK) // Color del texto central
        pieChart.setCenterTextSize(18f) // Tamaño del texto central
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        // pieChart.highlightPerTapEnabled = true // Línea eliminada

        // Configuración de la leyenda
        pieChart.legend.isEnabled = false // Deshabilitar la leyenda por defecto de la gráfica
        // Si necesitas una leyenda personalizada, la puedes construir en el LinearLayout

        // Deshabilitar la entrada táctil si no quieres interactividad
        // pieChart.setTouchEnabled(false)
    }

    private fun setupBarChart(barChart: BarChart, descriptionText: String) {
        barChart.description.isEnabled = false
        barChart.setPinchZoom(false) // Deshabilitar el zoom de pellizco
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)

        // Configuración del eje X
        val xAxis = barChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 10f
        xAxis.granularity = 1f // Solo mostrar valores enteros

        // Configuración del eje Y izquierdo
        val leftAxis = barChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.textColor = Color.BLACK
        leftAxis.textSize = 10f
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f // Empezar desde 0

        // Deshabilitar el eje Y derecho
        barChart.axisRight.isEnabled = false

        // Configuración de la leyenda
        barChart.legend.isEnabled = false // Deshabilitar la leyenda por defecto

        // Animación
        barChart.animateY(1500)
    }

    private fun loadReports() {
        // Cargar el total de usuarios y usuarios por rol
        db.collection("userRoles")
            .get()
            .addOnSuccessListener { result ->
                val totalUsers = result.size()
                tvTotalUsers.text = "Total de Usuarios: $totalUsers"

                val usersByRole = mutableMapOf<String, Int>()
                for (document in result) {
                    val role = document.getString("role") ?: "desconocido"
                    usersByRole[role] = (usersByRole[role] ?: 0) + 1
                }
                updateUsersByRoleChart(usersByRole)
                displayUsersByRoleLegend(usersByRole) // Actualizar la leyenda manual
            }
            .addOnFailureListener { e ->
                Log.e("ReportsActivity", "Error al cargar usuarios para reportes: ${e.message}", e)
                Toast.makeText(this, "Error al cargar reportes de usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Cargar el total de películas, películas por género y películas por año
        db.collection("peliculas")
            .get()
            .addOnSuccessListener { result ->
                val totalMovies = result.size()
                tvTotalMovies.text = "Total de Películas: $totalMovies"

                val moviesByGenre = mutableMapOf<String, Int>()
                val moviesByYear = mutableMapOf<Int, Int>() // Nuevo mapa para años

                db.collection("generos")
                    .get()
                    .addOnSuccessListener { genreResult ->
                        val genreNamesMap = mutableMapOf<String, String>()
                        for (doc in genreResult) {
                            genreNamesMap[doc.id] = doc.getString("nombre") ?: "Género Desconocido"
                        }

                        for (document in result) {
                            // Para películas por género
                            val genreId = document.getString("generoId")
                            val genreName = genreNamesMap[genreId] ?: "Género Desconocido"
                            moviesByGenre[genreName] = (moviesByGenre[genreName] ?: 0) + 1

                            // Para películas por año (asumiendo campo 'anio' como Long o Int)
                            val releaseYear = document.getLong("anio")?.toInt() // Usar "anio" como el campo del año
                            if (releaseYear != null) {
                                moviesByYear[releaseYear] = (moviesByYear[releaseYear] ?: 0) + 1
                            } else {
                                Log.w("ReportsActivity", "Documento de película sin campo 'anio' o no es un número: ${document.id}")
                            }
                        }
                        updateMoviesByGenreChart(moviesByGenre)
                        displayMoviesByGenreLegend(moviesByGenre) // Actualizar la leyenda manual
                        updateMoviesByYearBarChart(moviesByYear) // Actualizar el BarChart
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReportsActivity", "Error al cargar nombres de géneros para reportes: ${e.message}", e)
                        Toast.makeText(this, "Error al cargar nombres de géneros para reportes: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ReportsActivity", "Error al cargar películas para reportes: ${e.message}", e)
                Toast.makeText(this, "Error al cargar reportes de películas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateMoviesByGenreChart(moviesByGenre: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((genre, count) in moviesByGenre) {
            entries.add(PieEntry(count.toFloat(), genre))
        }

        if (entries.isEmpty()) {
            pieChartMoviesByGenre.clear()
            pieChartMoviesByGenre.setCenterText("Sin datos de géneros")
            pieChartMoviesByGenre.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Géneros de Películas")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = chartColors // Usar los colores personalizados
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartMoviesByGenre))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK) // Color del texto de los valores
        pieChartMoviesByGenre.data = data
        pieChartMoviesByGenre.invalidate() // Refrescar la gráfica
        pieChartMoviesByGenre.animateY(1400) // Animación
    }

    private fun updateMoviesByYearBarChart(moviesByYear: Map<Int, Int>) {
        val entries = ArrayList<BarEntry>()
        val years = moviesByYear.keys.sorted() // Ordenar años para el eje X

        if (years.isEmpty()) {
            barChartMoviesByYear.clear()
            barChartMoviesByYear.setNoDataText("Sin datos de películas por año.")
            barChartMoviesByYear.invalidate()
            return
        }

        val xAxisLabels = ArrayList<String>()
        for ((index, year) in years.withIndex()) {
            entries.add(BarEntry(index.toFloat(), moviesByYear[year]?.toFloat() ?: 0f))
            xAxisLabels.add(year.toString())
        }

        val dataSet = BarDataSet(entries, "Películas por Año")
        dataSet.colors = chartColors // Reutilizar los colores
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.9f // Ancho de las barras

        barChartMoviesByYear.data = data

        // Formatear las etiquetas del eje X para mostrar los años
        barChartMoviesByYear.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChartMoviesByYear.xAxis.setLabelCount(xAxisLabels.size, false) // Asegurarse de que todas las etiquetas se muestren

        barChartMoviesByYear.invalidate() // Refrescar la gráfica
        barChartMoviesByYear.animateY(1500) // Animación
    }

    private fun updateUsersByRoleChart(usersByRole: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((role, count) in usersByRole) {
            val formattedRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            entries.add(PieEntry(count.toFloat(), formattedRole))
        }

        if (entries.isEmpty()) {
            pieChartUsersByRole.clear()
            pieChartUsersByRole.setCenterText("Sin datos de roles")
            pieChartUsersByRole.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Roles de Usuarios")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = chartColors // Usar los colores personalizados
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartUsersByRole))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK) // Color del texto de los valores
        pieChartUsersByRole.data = data
        pieChartUsersByRole.invalidate() // Refrescar la gráfica
        pieChartUsersByRole.animateY(1400) // Animación
    }

    // Funciones para mostrar la leyenda manualmente
    private fun displayMoviesByGenreLegend(moviesByGenre: Map<String, Int>) {
        llMoviesByGenreLegend.removeAllViews() // Limpiar vistas anteriores
        if (moviesByGenre.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No hay datos de géneros para mostrar."
            tv.setTextColor(resources.getColor(R.color.dark_gray))
            tv.textSize = 16f
            llMoviesByGenreLegend.addView(tv)
            return
        }

        var colorIndex = 0
        for ((genre, count) in moviesByGenre.entries.sortedByDescending { it.value }) {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = android.view.Gravity.CENTER_VERTICAL
            rowLayout.setPadding(0, 0, 0, 8) // Padding inferior para cada fila

            val colorCircle = View(this)
            val size = (resources.displayMetrics.density * 16).toInt() // 16dp
            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.marginEnd = (resources.displayMetrics.density * 8).toInt() // 8dp margin
            colorCircle.layoutParams = layoutParams
            colorCircle.background = resources.getDrawable(R.drawable.circle_shape, null)
            colorCircle.background.setTint(chartColors[colorIndex % chartColors.size])

            val tv = TextView(this)
            tv.text = "$genre: $count películas"
            tv.setTextColor(resources.getColor(R.color.black))
            tv.textSize = 16f

            rowLayout.addView(colorCircle)
            rowLayout.addView(tv)
            llMoviesByGenreLegend.addView(rowLayout)

            colorIndex++
        }
    }

    private fun displayUsersByRoleLegend(usersByRole: Map<String, Int>) {
        llUsersByRoleLegend.removeAllViews() // Limpiar vistas anteriores
        if (usersByRole.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No hay datos de roles para mostrar."
            tv.setTextColor(resources.getColor(R.color.dark_gray))
            tv.textSize = 16f
            llUsersByRoleLegend.addView(tv)
            return
        }

        var colorIndex = 0
        for ((role, count) in usersByRole.entries.sortedByDescending { it.value }) {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = android.view.Gravity.CENTER_VERTICAL
            rowLayout.setPadding(0, 0, 0, 8) // Padding inferior para cada fila

            val colorCircle = View(this)
            val size = (resources.displayMetrics.density * 16).toInt() // 16dp
            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.marginEnd = (resources.displayMetrics.density * 8).toInt() // 8dp margin
            colorCircle.layoutParams = layoutParams
            colorCircle.background = resources.getDrawable(R.drawable.circle_shape, null)
            colorCircle.background.setTint(chartColors[colorIndex % chartColors.size])

            val tv = TextView(this)
            val formattedRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            tv.text = "$formattedRole: $count usuarios"
            tv.setTextColor(resources.getColor(R.color.black))
            tv.textSize = 16f

            rowLayout.addView(colorCircle)
            rowLayout.addView(tv)
            llUsersByRoleLegend.addView(rowLayout)

            colorIndex++
        }
    }
}
