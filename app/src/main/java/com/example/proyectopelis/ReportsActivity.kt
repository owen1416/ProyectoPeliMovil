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
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Importar Query para ordenar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat // Importar ContextCompat

// Importar el modelo de Peliculas si no está ya
import com.example.proyectopelis.Models.Peliculas
// Puedes importar tu modelo de User aquí si lo necesitas para la gráfica de roles,
// aunque no es estrictamente necesario si solo usas el campo 'role'.
// import com.example.proyectopelis.Models.User

class ReportsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var toolbar: Toolbar
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalMovies: TextView
    private lateinit var pieChartMoviesByGenre: PieChart
    private lateinit var barChartMoviesByYear: BarChart
    private lateinit var pieChartUsersByRole: PieChart
    private lateinit var barChartTopFavoriteMovies: BarChart // ¡NUEVA DECLARACIÓN!
    private lateinit var btnRefreshReports: MaterialButton

    private lateinit var llMoviesByGenreLegend: LinearLayout
    private lateinit var llUsersByRoleLegend: LinearLayout

    // Paleta de colores ampliada para más diversidad en las gráficas
    private val chartColors = listOf(
        Color.parseColor("#8A2BE2"), // purple_primary
        Color.parseColor("#6A1B9A"), // purple_dark
        Color.parseColor("#42A5F5"), // Un azul claro
        Color.parseColor("#FFCA28"), // Un amarillo
        Color.parseColor("#EF5350"), // Un rojo claro
        Color.parseColor("#66BB6A"),  // Un verde
        Color.parseColor("#FF8A65"), // Naranja claro
        Color.parseColor("#7986CB"), // Indigo claro
        Color.parseColor("#4DB6AC"), // Teal
        Color.parseColor("#F06292")  // Rosa
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
        barChartTopFavoriteMovies = findViewById(R.id.bar_chart_top_favorite_movies) // ¡INICIALIZAR AQUÍ!
        btnRefreshReports = findViewById(R.id.btn_refresh_reports)

        llMoviesByGenreLegend = findViewById(R.id.ll_movies_by_genre_legend)
        llUsersByRoleLegend = findViewById(R.id.ll_users_by_role_legend)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reportes y Estadísticas"

        btnRefreshReports.setOnClickListener {
            loadReports()
            Toast.makeText(this, "Reportes actualizados", Toast.LENGTH_SHORT).show()
        }

        setupPieChart(pieChartMoviesByGenre, "Distribución de Géneros")
        setupPieChart(pieChartUsersByRole, "Distribución de Roles")
        setupBarChart(barChartMoviesByYear, "Películas por Año")
        setupBarChart(barChartTopFavoriteMovies, "Top Películas Favoritas") // ¡CONFIGURAR LA NUEVA GRÁFICA!

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
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.black)) // Usar ContextCompat
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

        val xAxis = barChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = ContextCompat.getColor(this, R.color.black) // Usar ContextCompat
        xAxis.textSize = 10f
        xAxis.granularity = 1f

        val leftAxis = barChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.textColor = ContextCompat.getColor(this, R.color.black) // Usar ContextCompat
        leftAxis.textSize = 10f
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.axisMinimum = 0f

        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(1500)
    }

    private fun loadReports() {
        // Cargar el total de usuarios y usuarios por rol
        db.collection("userRoles") // Asumiendo que "userRoles" contiene los roles de usuario
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
                pieChartUsersByRole.setNoDataText("Error al cargar datos de roles.")
                pieChartUsersByRole.setNoDataTextColor(ContextCompat.getColor(this, R.color.red)) // Usar ContextCompat
                pieChartUsersByRole.invalidate()
                llUsersByRoleLegend.removeAllViews()
            }

        // Cargar el total de películas, películas por género, películas por año y TOP FAVORITAS
        db.collection("peliculas")
            .get()
            .addOnSuccessListener { result ->
                val totalMovies = result.size()
                tvTotalMovies.text = "Total de Películas: $totalMovies"

                val moviesByGenre = mutableMapOf<String, Int>()
                val moviesByYear = mutableMapOf<Int, Int>()

                // Cargar géneros para el mapa de nombres
                db.collection("generos")
                    .get()
                    .addOnSuccessListener { genreResult ->
                        val genreNamesMap = mutableMapOf<String, String>()
                        for (doc in genreResult) {
                            genreNamesMap[doc.id] = doc.getString("nombre") ?: "Género Desconocido"
                        }

                        // Procesar películas para todas las gráficas
                        val allPeliculas = mutableListOf<Peliculas>() // Para la gráfica de favoritos
                        for (document in result) {
                            // Para películas por género
                            val genreId = document.getString("generoId")
                            val genreName = genreNamesMap[genreId] ?: "Género Desconocido"
                            moviesByGenre[genreName] = (moviesByGenre[genreName] ?: 0) + 1

                            // Para películas por año
                            val anio = document.getLong("anio")?.toInt()
                            if (anio != null) {
                                moviesByYear[anio] = (moviesByYear[anio] ?: 0) + 1
                            } else {
                                Log.w("ReportsActivity", "Documento de película sin campo 'anio' o no es un número: ${document.id}")
                            }

                            // Para la gráfica de top favoritos
                            val pelicula = document.toObject(Peliculas::class.java)
                            pelicula?.let {
                                it.id = document.id // Asegúrate de asignar el ID del documento
                                allPeliculas.add(it)
                            }
                        }
                        updateMoviesByGenreChart(moviesByGenre)
                        displayMoviesByGenreLegend(moviesByGenre)
                        updateMoviesByYearBarChart(moviesByYear)
                        updateTopFavoriteMoviesChart(allPeliculas) // ¡LLAMAR A LA FUNCIÓN DE TOP FAVORITOS!
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReportsActivity", "Error al cargar nombres de géneros para reportes: ${e.message}", e)
                        Toast.makeText(this, "Error al cargar nombres de géneros para reportes: ${e.message}", Toast.LENGTH_LONG).show()
                        pieChartMoviesByGenre.setNoDataText("Error al cargar géneros.")
                        pieChartMoviesByGenre.setNoDataTextColor(ContextCompat.getColor(this, R.color.red)) // Usar ContextCompat
                        pieChartMoviesByGenre.invalidate()
                        llMoviesByGenreLegend.removeAllViews()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ReportsActivity", "Error al cargar películas para reportes: ${e.message}", e)
                Toast.makeText(this, "Error al cargar reportes de películas: ${e.message}", Toast.LENGTH_LONG).show()
                pieChartMoviesByGenre.setNoDataText("Error al cargar películas.")
                pieChartMoviesByGenre.setNoDataTextColor(ContextCompat.getColor(this, R.color.red)) // Usar ContextCompat
                pieChartMoviesByGenre.invalidate()
                barChartMoviesByYear.setNoDataText("Error al cargar películas.")
                barChartMoviesByYear.setNoDataTextColor(ContextCompat.getColor(this, R.color.red)) // Usar ContextCompat
                barChartMoviesByYear.invalidate()
                barChartTopFavoriteMovies.setNoDataText("Error al cargar películas.") // Para la nueva gráfica
                barChartTopFavoriteMovies.setNoDataTextColor(ContextCompat.getColor(this, R.color.red)) // Para la nueva gráfica
                barChartTopFavoriteMovies.invalidate()
            }
    }

    private fun updateMoviesByGenreChart(moviesByGenre: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val sortedEntries = moviesByGenre.entries.sortedByDescending { it.value } // Ordenar para la leyenda y visualización

        for ((genre, count) in sortedEntries) {
            entries.add(PieEntry(count.toFloat(), genre))
        }

        if (entries.isEmpty()) {
            pieChartMoviesByGenre.clear()
            pieChartMoviesByGenre.setCenterText("Sin datos de géneros")
            pieChartMoviesByGenre.setCenterTextColor(ContextCompat.getColor(this, R.color.gray))
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
        data.setValueTextColor(ContextCompat.getColor(this, R.color.black)) // Usar ContextCompat
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
            barChartMoviesByYear.setNoDataTextColor(ContextCompat.getColor(this, R.color.gray))
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
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.black) // Usar ContextCompat
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.9f

        barChartMoviesByYear.data = data

        barChartMoviesByYear.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChartMoviesByYear.xAxis.setLabelCount(xAxisLabels.size, false)
        barChartMoviesByYear.xAxis.labelRotationAngle = -45f // Rotar etiquetas para mejor legibilidad

        barChartMoviesByYear.invalidate()
        barChartMoviesByYear.animateY(1500)
    }

    private fun updateUsersByRoleChart(usersByRole: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val sortedEntries = usersByRole.entries.sortedByDescending { it.value }

        for ((role, count) in sortedEntries) {
            val formattedRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            entries.add(PieEntry(count.toFloat(), formattedRole))
        }

        if (entries.isEmpty()) {
            pieChartUsersByRole.clear()
            pieChartUsersByRole.setCenterText("Sin datos de roles")
            pieChartUsersByRole.setCenterTextColor(ContextCompat.getColor(this, R.color.gray))
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
        data.setValueTextColor(ContextCompat.getColor(this, R.color.black)) // Usar ContextCompat
        pieChartUsersByRole.data = data
        pieChartUsersByRole.invalidate()
        pieChartUsersByRole.animateY(1400)
    }

    // ¡NUEVA FUNCIÓN: PARA LA GRÁFICA DE TOP PELÍCULAS FAVORITAS!
    private fun updateTopFavoriteMoviesChart(allPeliculas: List<Peliculas>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Ordena por favoritos y toma top 10
        val topFavoriteMovies = allPeliculas
            .sortedByDescending { it.favoriteCount }
            .take(10)

        if (topFavoriteMovies.isEmpty()) {
            barChartTopFavoriteMovies.clear()
            barChartTopFavoriteMovies.setNoDataText("No hay películas con favoritos todavía.")
            barChartTopFavoriteMovies.setNoDataTextColor(ContextCompat.getColor(this, R.color.gray))
            barChartTopFavoriteMovies.invalidate()
            return
        }

        // Prepara entradas y etiquetas
        topFavoriteMovies.forEachIndexed { i, movie ->
            entries.add(BarEntry(i.toFloat(), movie.favoriteCount.toFloat()))
            labels.add(movie.titulo)
        }

        val dataSet = BarDataSet(entries, "N° Favoritos").apply {
            colors = chartColors
            valueTextColor = ContextCompat.getColor(this@ReportsActivity, R.color.black)
            valueTextSize = 10f
        }

        val data = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        // 1) Asigna datos correctamente
        barChartTopFavoriteMovies.setData(data)

        // 2) Refresca gráfico
        barChartTopFavoriteMovies.notifyDataSetChanged()
        barChartTopFavoriteMovies.invalidate()

        // Configura eje X
        barChartTopFavoriteMovies.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            setLabelCount(labels.size, false)
            textColor = ContextCompat.getColor(this@ReportsActivity, R.color.black)
            textSize = 10f
            labelRotationAngle = -45f
        }

        // Configura eje Y izquierdo
        barChartTopFavoriteMovies.axisLeft.apply {
            axisMinimum = 0f
            textColor = ContextCompat.getColor(this@ReportsActivity, R.color.black)
            gridColor = ContextCompat.getColor(this@ReportsActivity, R.color.light_gray)
        }

        barChartTopFavoriteMovies.axisRight.isEnabled = false
        barChartTopFavoriteMovies.setFitBars(true)

        // Animación
        barChartTopFavoriteMovies.animateY(1500)
    }
    // Funciones para mostrar la leyenda manualmente
    private fun displayMoviesByGenreLegend(moviesByGenre: Map<String, Int>) {
        llMoviesByGenreLegend.removeAllViews()
        if (moviesByGenre.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No hay datos de géneros para mostrar."
            tv.setTextColor(ContextCompat.getColor(this, R.color.dark_gray)) // Usar ContextCompat
            tv.textSize = 16f
            llMoviesByGenreLegend.addView(tv)
            return
        }

        var colorIndex = 0
        // Ordenar el mapa para que la leyenda coincida con el orden de la gráfica (descendente por valor)
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
            // Asegúrate de tener un drawable `circle_shape.xml` en `res/drawable/`
            // Ejemplo de circle_shape.xml:
            // <shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
            //    <solid android:color="#FF0000"/>
            // </shape>
            colorCircle.background = ContextCompat.getDrawable(this, R.drawable.circle_shape) // Usar ContextCompat
            colorCircle.background.setTint(chartColors[colorIndex % chartColors.size])

            val tv = TextView(this)
            tv.text = "$genre: $count películas"
            tv.setTextColor(ContextCompat.getColor(this, R.color.black)) // Usar ContextCompat
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
            tv.setTextColor(ContextCompat.getColor(this, R.color.dark_gray)) // Usar ContextCompat
            tv.textSize = 16f
            llUsersByRoleLegend.addView(tv)
            return
        }

        var colorIndex = 0
        // Ordenar el mapa para que la leyenda coincida con el orden de la gráfica (descendente por valor)
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
            colorCircle.background = ContextCompat.getDrawable(this, R.drawable.circle_shape) // Usar ContextCompat
            colorCircle.background.setTint(chartColors[colorIndex % chartColors.size])

            val tv = TextView(this)
            val formattedRole = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            tv.text = "$formattedRole: $count usuarios"
            tv.setTextColor(ContextCompat.getColor(this, R.color.black)) // Usar ContextCompat
            tv.textSize = 16f

            rowLayout.addView(colorCircle)
            rowLayout.addView(tv)
            llUsersByRoleLegend.addView(rowLayout)

            colorIndex++
        }
    }
}