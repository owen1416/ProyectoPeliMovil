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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View
import java.util.Collections // Para ordenar mapas
import com.example.proyectopelis.Models.Peliculas // Asegúrate de que esta ruta sea correcta para tu modelo Peliculas
 // Asegúrate de que esta ruta sea correcta para tu modelo User, si lo usas en otros reportes

class ReportsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var toolbar: Toolbar
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalMovies: TextView
    private lateinit var pieChartMoviesByGenre: PieChart
    private lateinit var barChartMoviesByYear: BarChart
    private lateinit var pieChartUsersByRole: PieChart
    private lateinit var btnRefreshReports: MaterialButton

    // Referencias a los LinearLayouts para las leyendas
    private lateinit var llMoviesByGenreLegend: LinearLayout
    private lateinit var llUsersByRoleLegend: LinearLayout

    // NUEVO: Declarar el BarChart para películas favoritas
    private lateinit var barChartTopFavoriteMovies: BarChart

    // Colores personalizados para las gráficas (puedes ajustarlos)
    private val chartColors = listOf(
        Color.parseColor("#8A2BE2"), // purple_primary
        Color.parseColor("#6A1B9A"), // purple_dark
        Color.parseColor("#42A5F5"), // Un azul claro
        Color.parseColor("#FFCA28"), // Un amarillo
        Color.parseColor("#EF5350"), // Un rojo claro
        Color.parseColor("#66BB6A"), // Un verde
        Color.parseColor("#FFC107"), // Amarillo ámbar
        Color.parseColor("#00BCD4"), // Cian
        Color.parseColor("#FF5722")  // Naranja oscuro
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        db = Firebase.firestore

        toolbar = findViewById(R.id.toolbar_reports)
        tvTotalUsers = findViewById(R.id.tv_total_users)
        tvTotalMovies = findViewById(R.id.tv_total_movies)
        pieChartMoviesByGenre = findViewById(R.id.pie_chart_movies_by_genre)
        barChartMoviesByYear = findViewById(R.id.bar_chart_movies_by_year)
        pieChartUsersByRole = findViewById(R.id.pie_chart_users_by_role)
        btnRefreshReports = findViewById(R.id.btn_refresh_reports)

        llMoviesByGenreLegend = findViewById(R.id.ll_movies_by_genre_legend)
        llUsersByRoleLegend = findViewById(R.id.ll_users_by_role_legend)

        // NUEVO: Inicializar el BarChart para películas favoritas
        barChartTopFavoriteMovies = findViewById(R.id.bar_chart_top_favorite_movies)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reportes y Estadísticas"

        btnRefreshReports.setOnClickListener {
            loadReports()
            Toast.makeText(this, "Reportes actualizados", Toast.LENGTH_SHORT).show()
        }

        // Configuración inicial de las gráficas
        setupPieChart(pieChartMoviesByGenre, "Distribución de Géneros")
        setupPieChart(pieChartUsersByRole, "Distribución de Roles")
        setupBarChart(barChartMoviesByYear, "Películas por Año")
        setupBarChart(barChartTopFavoriteMovies, "Top Películas Favoritas") // NUEVO: Configurar el BarChart de favoritos

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
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.setCenterText(descriptionText)
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.setCenterTextSize(18f)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.legend.isEnabled = false
    }

    private fun setupBarChart(barChart: BarChart, descriptionText: String) {
        barChart.description.isEnabled = false
        barChart.setPinchZoom(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)
        barChart.setBackgroundColor(Color.WHITE) // Establecer el fondo de la gráfica a blanco

        // Configuración del eje X
        val xAxis = barChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 10f
        xAxis.granularity = 1f

        // Configuración del eje Y izquierdo
        val leftAxis = barChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.textColor = Color.BLACK
        leftAxis.textSize = 10f
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY // Color de las líneas de la cuadrícula
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f

        // Deshabilitar el eje Y derecho
        barChart.axisRight.isEnabled = false

        // Configuración de la leyenda
        barChart.legend.isEnabled = false

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
                displayUsersByRoleLegend(usersByRole)
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
                val moviesByYear = mutableMapOf<Int, Int>()

                // NUEVO: Mapa para contar favoritos
                val favoriteMovieCounts = mutableMapOf<String, Int>()


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

                            // Para películas por año
                            val releaseYear = document.getLong("anio")?.toInt()
                            if (releaseYear != null) {
                                moviesByYear[releaseYear] = (moviesByYear[releaseYear] ?: 0) + 1
                            } else {
                                Log.w("ReportsActivity", "Documento de película sin campo 'anio' o no es un número: ${document.id}")
                            }

                            // NUEVO: Para películas favoritas
                            val pelicula = document.toObject(Peliculas::class.java)
                            if (pelicula.isFavorite) { // Asegúrate de que Peliculas.kt tiene 'isFavorite: Boolean'
                                val title = pelicula.titulo ?: "Título Desconocido"
                                favoriteMovieCounts[title] = favoriteMovieCounts.getOrDefault(title, 0) + 1
                            }
                        }
                        updateMoviesByGenreChart(moviesByGenre)
                        displayMoviesByGenreLegend(moviesByGenre)
                        updateMoviesByYearBarChart(moviesByYear)

                        // ¡NUEVO: Llamar a la función para actualizar la gráfica de favoritos!
                        updateTopFavoriteMoviesChart(favoriteMovieCounts)

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
        dataSet.colors = chartColors
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartMoviesByGenre))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        pieChartMoviesByGenre.data = data
        pieChartMoviesByGenre.invalidate()
        pieChartMoviesByGenre.animateY(1400)
    }

    private fun updateMoviesByYearBarChart(moviesByYear: Map<Int, Int>) {
        val entries = ArrayList<BarEntry>()
        val years = moviesByYear.keys.sorted()

        if (years.isEmpty()) {
            barChartMoviesByYear.clear()
            barChartMoviesByYear.setNoDataText("Sin datos de películas por año.")
            barChartMoviesByYear.setNoDataTextColor(Color.GRAY)
            barChartMoviesByYear.invalidate()
            return
        }

        val xAxisLabels = ArrayList<String>()
        for ((index, year) in years.withIndex()) {
            entries.add(BarEntry(index.toFloat(), moviesByYear[year]?.toFloat() ?: 0f))
            xAxisLabels.add(year.toString())
        }

        val dataSet = BarDataSet(entries, "Películas por Año")
        dataSet.colors = chartColors
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.9f

        barChartMoviesByYear.data = data

        barChartMoviesByYear.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChartMoviesByYear.xAxis.setLabelCount(xAxisLabels.size, false)
        barChartMoviesByYear.invalidate()
        barChartMoviesByYear.animateY(1500)
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
        dataSet.colors = chartColors
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartUsersByRole))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        pieChartUsersByRole.data = data
        pieChartUsersByRole.invalidate()
        pieChartUsersByRole.animateY(1400)
    }

    // Funciones para mostrar la leyenda manualmente (ya existentes)
    private fun displayMoviesByGenreLegend(moviesByGenre: Map<String, Int>) {
        llMoviesByGenreLegend.removeAllViews()
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
            rowLayout.setPadding(0, 0, 0, 8)

            val colorCircle = View(this)
            val size = (resources.displayMetrics.density * 16).toInt()
            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.marginEnd = (resources.displayMetrics.density * 8).toInt()
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
        llUsersByRoleLegend.removeAllViews()
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
            rowLayout.setPadding(0, 0, 0, 8)

            val colorCircle = View(this)
            val size = (resources.displayMetrics.density * 16).toInt()
            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.marginEnd = (resources.displayMetrics.density * 8).toInt()
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

    // NUEVO: Función para actualizar la gráfica de Top Películas Favoritas
    private fun updateTopFavoriteMoviesChart(favoriteMovieCounts: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Ordenar las películas por el número de favoritos (descendente)
        val sortedFavoriteMovies = favoriteMovieCounts.entries.sortedByDescending { it.value }

        // Limitar a las 10 películas más favoritas
        val topMovies = sortedFavoriteMovies.take(10)

        if (topMovies.isEmpty()) {
            barChartTopFavoriteMovies.clear()
            barChartTopFavoriteMovies.setNoDataText("No hay películas marcadas como favoritas todavía.")
            barChartTopFavoriteMovies.setNoDataTextColor(Color.GRAY)
            barChartTopFavoriteMovies.invalidate()
            return
        }

        for ((index, entry) in topMovies.withIndex()) {
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        val dataSet = BarDataSet(entries, "Número de Favoritos")
        dataSet.colors = chartColors // Reutilizar tus colores personalizados
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.9f

        barChartTopFavoriteMovies.data = data

        // Configuración específica del eje X para las etiquetas de las películas
        val xAxis = barChartTopFavoriteMovies.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true) // Centrar etiquetas si es apropiado
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 10f
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(labels.size, false) // Asegura que todas las etiquetas se muestren

        // Configuración del eje Y (ya debería estar configurado por setupBarChart)
        val leftAxis = barChartTopFavoriteMovies.axisLeft
        leftAxis.axisMinimum = 0f // Asegurarse de que el mínimo sea 0

        barChartTopFavoriteMovies.invalidate()
        barChartTopFavoriteMovies.animateY(1500)
    }
}