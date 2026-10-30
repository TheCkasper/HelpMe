package com.helpme.app.feature.map

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MapUiBuilder(private val context: Context) {

    private val isDark: Boolean
        get() = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val bgPrimary: Int get() = if (isDark) Color.parseColor("#121212") else Color.parseColor("#F4F6F8")
    private val bgCard: Int get() = if (isDark) Color.parseColor("#1E1E1E") else Color.WHITE
    private val textPrimary: Int get() = if (isDark) Color.parseColor("#F9FAFB") else Color.parseColor("#212121")
    private val textSecondary: Int get() = if (isDark) Color.parseColor("#9CA3AF") else Color.parseColor("#616161")

    // Main Containers
    val rootScrollView: ScrollView = ScrollView(context)
    val mainLayout: LinearLayout = LinearLayout(context)

    // Back & Location Buttons
    val btnBack: MaterialButton = MaterialButton(context)
    val btnMyLocation: MaterialButton = MaterialButton(context)

    // Loading & Error Views
    val loadingBar: ProgressBar = ProgressBar(context)
    val errorCard: MaterialCardView = MaterialCardView(context)
    val tvErrorMsg: TextView = TextView(context)
    val btnRetry: MaterialButton = MaterialButton(context)

    // Period & Date Views
    val btnPeriodDay: MaterialButton = MaterialButton(context)
    val btnPeriodWeek: MaterialButton = MaterialButton(context)
    val btnPeriodMonth: MaterialButton = MaterialButton(context)
    val btnSelectDate: MaterialButton = MaterialButton(context)

    // Status & Activity Spinners
    val spinnerStatus: Spinner = Spinner(context)
    val spinnerActivity: Spinner = Spinner(context)

    // Type Switches Container
    val typesSwitchesContainer: LinearLayout = LinearLayout(context)

    // WebView Map & Legend
    val webViewMap: WebView = WebView(context)
    val tvEmptyMapNotice: TextView = TextView(context)

    // Selected Report Card
    val selectedReportCard: MaterialCardView = MaterialCardView(context)
    val tvReportTitle: TextView = TextView(context)
    val tvReportBadge: TextView = TextView(context)
    val tvReportDate: TextView = TextView(context)
    val tvReportDesc: TextView = TextView(context)
    val tvReportCoords: TextView = TextView(context)
    val btnCloseReportCard: MaterialButton = MaterialButton(context)

    // Summary Metric Cards
    val tvMetricTotal: TextView = TextView(context)
    val tvMetricActive: TextView = TextView(context)
    val tvMetricResolved: TextView = TextView(context)
    val tvMetricPending: TextView = TextView(context)
    val tvMetricInProcess: TextView = TextView(context)
    val tvMetricNoLocation: TextView = TextView(context)

    // Stats Table
    val statsTableLayout: TableLayout = TableLayout(context)

    // Type Switches Map
    val typeSwitchMap = mutableMapOf<String, SwitchCompat>()

    init {
        buildLayout()
    }

    private fun buildLayout() {
        rootScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        rootScrollView.isFillViewport = true
        rootScrollView.setBackgroundColor(bgPrimary)

        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val p = dpToPx(16)
        mainLayout.setPadding(p, p, p, p)
        rootScrollView.addView(mainLayout)

        buildHeader()
        buildLoadingBar()
        buildErrorCard()
        buildFiltersSection()
        buildMapCard()
        buildSelectedReportCard()
        buildStatisticsSection()
    }

    private fun buildHeader() {
        btnBack.apply {
            text = "← Volver al Inicio"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setBackgroundColor(if (isDark) Color.parseColor("#10B981") else Color.parseColor("#E8EAF6"))
            setTextColor(if (isDark) Color.WHITE else Color.parseColor("#1A237E"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(8) }
        }
        mainLayout.addView(btnBack)

        val tvTitle = TextView(context).apply {
            text = "Mapa de Incidencias y Estadísticas"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
        }
        val tvSubtitle = TextView(context).apply {
            text = "Monitoreo ciudadano en tiempo real"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textSecondary)
            setPadding(0, dpToPx(2), 0, dpToPx(12))
        }
        mainLayout.addView(tvTitle)
        mainLayout.addView(tvSubtitle)
    }

    private fun buildLoadingBar() {
        loadingBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dpToPx(16)
            bottomMargin = dpToPx(16)
        }
        mainLayout.addView(loadingBar)
    }

    private fun buildErrorCard() {
        errorCard.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(12)
        }
        errorCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
        errorCard.cardElevation = dpToPx(2).toFloat()
        errorCard.radius = dpToPx(8).toFloat()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        tvErrorMsg.setTextColor(Color.parseColor("#C62828"))
        tvErrorMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)

        btnRetry.text = "Reintentar"
        btnRetry.setBackgroundColor(Color.parseColor("#C62828"))
        btnRetry.setTextColor(Color.WHITE)
        btnRetry.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(8)
        }

        layout.addView(tvErrorMsg)
        layout.addView(btnRetry)
        errorCard.addView(layout)
        errorCard.visibility = View.GONE
        mainLayout.addView(errorCard)
    }

    private fun buildFiltersSection() {
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
            setCardBackgroundColor(bgCard)
            cardElevation = dpToPx(2).toFloat()
            radius = dpToPx(10).toFloat()
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        val tvFilterHeader = TextView(context).apply {
            text = "Filtros de Búsqueda"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
            setPadding(0, 0, 0, dpToPx(8))
        }
        container.addView(tvFilterHeader)

        val periodLabel = TextView(context).apply {
            text = "Periodo:"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textSecondary)
        }
        container.addView(periodLabel)

        val periodBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(4), 0, dpToPx(8))
        }

        stylePeriodButton(btnPeriodDay, "Día")
        stylePeriodButton(btnPeriodWeek, "Semana")
        stylePeriodButton(btnPeriodMonth, "Mes")

        periodBar.addView(btnPeriodDay)
        periodBar.addView(btnPeriodWeek)
        periodBar.addView(btnPeriodMonth)
        container.addView(periodBar)

        btnSelectDate.apply {
            text = "Fecha de referencia: Hoy"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setBackgroundColor(if (isDark) Color.parseColor("#374151") else Color.parseColor("#E8EAF6"))
            setTextColor(if (isDark) Color.parseColor("#10B981") else Color.parseColor("#3F51B5"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
        }
        container.addView(btnSelectDate)

        val spinnersRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            setPadding(0, 0, 0, dpToPx(8))
        }

        val statusLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = dpToPx(4)
            }
        }
        val tvStatusLbl = TextView(context).apply {
            text = "Estado:"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textSecondary)
        }
        statusLayout.addView(tvStatusLbl)
        statusLayout.addView(spinnerStatus)

        val activityLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dpToPx(4)
            }
        }
        val tvActLbl = TextView(context).apply {
            text = "Actividad:"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textSecondary)
        }
        activityLayout.addView(tvActLbl)
        activityLayout.addView(spinnerActivity)

        spinnersRow.addView(statusLayout)
        spinnersRow.addView(activityLayout)
        container.addView(spinnersRow)

        typesSwitchesContainer.apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dpToPx(4), 0, 0)
        }
        container.addView(typesSwitchesContainer)

        card.addView(container)
        mainLayout.addView(card)
    }

    private fun stylePeriodButton(btn: MaterialButton, label: String) {
        btn.apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = dpToPx(4)
            }
        }
    }

    private fun buildMapCard() {
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
            setCardBackgroundColor(bgCard)
            cardElevation = dpToPx(2).toFloat()
            radius = dpToPx(10).toFloat()
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(8)
            setPadding(p, p, p, p)
        }

        btnMyLocation.apply {
            text = "📍 Centrar en Mi Ubicación"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setBackgroundColor(Color.parseColor("#10B981"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(6) }
        }
        container.addView(btnMyLocation)

        val mapFrame = android.widget.FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(340)
            )
        }

        webViewMap.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
        }
        mapFrame.addView(webViewMap)

        tvEmptyMapNotice.apply {
            text = "No hay incidencias para los filtros seleccionados"
            setTextColor(textSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundColor(if (isDark) Color.parseColor("#CC1E1E1E") else Color.parseColor("#CCFFFFFF"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }
        mapFrame.addView(tvEmptyMapNotice)
        container.addView(mapFrame)

        val legendRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dpToPx(8), 0, dpToPx(4))
        }

        legendRow.addView(createLegendItem("#FFA000", "Pendiente"))
        legendRow.addView(createLegendItem("#1976D2", "En proceso"))
        legendRow.addView(createLegendItem("#388E3C", "Resuelto"))

        container.addView(legendRow)
        card.addView(container)
        mainLayout.addView(card)
    }

    private fun createLegendItem(colorHex: String, label: String): LinearLayout {
        val item = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), 0, dpToPx(8), 0)
        }

        val dot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(10), dpToPx(10)).apply {
                rightMargin = dpToPx(4)
            }
            setBackgroundColor(Color.parseColor(colorHex))
        }

        val tv = TextView(context).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(textSecondary)
        }

        item.addView(dot)
        item.addView(tv)
        return item
    }

    private fun buildSelectedReportCard() {
        selectedReportCard.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
            setCardBackgroundColor(bgCard)
            cardElevation = dpToPx(3).toFloat()
            radius = dpToPx(10).toFloat()
            visibility = View.GONE
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        tvReportTitle.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
        }

        tvReportBadge.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(6)
            }
        }

        tvReportDate.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(textSecondary)
        }

        tvReportDesc.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textPrimary)
            setPadding(0, dpToPx(6), 0, dpToPx(6))
        }

        tvReportCoords.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(textSecondary)
        }

        btnCloseReportCard.apply {
            text = "Cerrar Ficha"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setBackgroundColor(Color.parseColor("#757575"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        }

        container.addView(tvReportTitle)
        container.addView(tvReportBadge)
        container.addView(tvReportDate)
        container.addView(tvReportDesc)
        container.addView(tvReportCoords)
        container.addView(btnCloseReportCard)

        selectedReportCard.addView(container)
        mainLayout.addView(selectedReportCard)
    }

    private fun buildStatisticsSection() {
        val tvStatsHeader = TextView(context).apply {
            text = "Estadísticas del Periodo"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
        mainLayout.addView(tvStatsHeader)

        val row1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            setPadding(0, 0, 0, dpToPx(8))
        }
        row1.addView(createMetricCard("Total", tvMetricTotal, "#1E88E5"))
        row1.addView(createMetricCard("Activas", tvMetricActive, "#FB8C00"))
        row1.addView(createMetricCard("Resueltas", tvMetricResolved, "#43A047"))
        mainLayout.addView(row1)

        val row2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            setPadding(0, 0, 0, dpToPx(12))
        }
        row2.addView(createMetricCard("Pendientes", tvMetricPending, "#FFA000"))
        row2.addView(createMetricCard("En Proceso", tvMetricInProcess, "#1976D2"))
        row2.addView(createMetricCard("Sin Ubic.", tvMetricNoLocation, "#757575"))
        mainLayout.addView(row2)

        val tableCard = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(16) }
            setCardBackgroundColor(bgCard)
            cardElevation = dpToPx(2).toFloat()
            radius = dpToPx(10).toFloat()
        }

        val tableContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        val tvTableTitle = TextView(context).apply {
            text = "Distribución por Tipo y Estado"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
            setPadding(0, 0, 0, dpToPx(8))
        }
        tableContainer.addView(tvTableTitle)

        val hScrollView = HorizontalScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        statsTableLayout.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isStretchAllColumns = true
        }
        hScrollView.addView(statsTableLayout)
        tableContainer.addView(hScrollView)

        tableCard.addView(tableContainer)
        mainLayout.addView(tableCard)
    }

    private fun createMetricCard(title: String, tvValue: TextView, colorHex: String): MaterialCardView {
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dpToPx(3)
                rightMargin = dpToPx(3)
            }
            setCardBackgroundColor(bgCard)
            cardElevation = dpToPx(1).toFloat()
            radius = dpToPx(8).toFloat()
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val p = dpToPx(8)
            setPadding(p, p, p, p)
        }

        val tvTitle = TextView(context).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTextColor(textSecondary)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        tvValue.apply {
            text = "0"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor(colorHex))
            gravity = Gravity.CENTER
        }

        container.addView(tvTitle)
        container.addView(tvValue)
        card.addView(container)
        return card
    }

    fun updateTypesSwitches(
        availableTypes: List<Pair<String, String>>,
        selectedTypeIds: Set<String>,
        onToggle: (String) -> Unit
    ) {
        typesSwitchesContainer.removeAllViews()
        typeSwitchMap.clear()

        for ((typeId, typeName) in availableTypes) {
            val switch = SwitchCompat(context).apply {
                text = typeName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(textPrimary)
                isChecked = selectedTypeIds.isEmpty() || selectedTypeIds.contains(typeId)
                setOnCheckedChangeListener { _, _ ->
                    onToggle(typeId)
                }
            }
            typeSwitchMap[typeId] = switch
            typesSwitchesContainer.addView(switch)
        }
    }

    fun populateStatsTable(stats: StatsSummary) {
        statsTableLayout.removeAllViews()

        val headerRow = TableRow(context).apply {
            setBackgroundColor(if (isDark) Color.parseColor("#374151") else Color.parseColor("#E8EAF6"))
            setPadding(0, dpToPx(6), 0, dpToPx(6))
        }

        headerRow.addView(createTableCell("Tipo", true, if (isDark) "#F9FAFB" else "#1A237E"))
        headerRow.addView(createTableCell("Pend.", true, "#FFA000"))
        headerRow.addView(createTableCell("En Proc.", true, "#1976D2"))
        headerRow.addView(createTableCell("Resuel.", true, "#388E3C"))
        headerRow.addView(createTableCell("Total", true, if (isDark) "#F9FAFB" else "#1A237E"))

        statsTableLayout.addView(headerRow)

        if (stats.typeStats.isEmpty()) {
            val emptyRow = TableRow(context)
            val tv = createTableCell("Sin datos para mostrar", false, if (isDark) "#9CA3AF" else "#757575")
            tv.gravity = Gravity.CENTER
            emptyRow.addView(tv)
            statsTableLayout.addView(emptyRow)
            return
        }

        var alt = false
        for (item in stats.typeStats) {
            val row = TableRow(context).apply {
                if (alt) setBackgroundColor(if (isDark) Color.parseColor("#262626") else Color.parseColor("#F9F9F9"))
                setPadding(0, dpToPx(6), 0, dpToPx(6))
            }
            alt = !alt

            row.addView(createTableCell(item.tipoName, false, if (isDark) "#F9FAFB" else "#212121"))
            row.addView(createTableCell(item.pendingCount.toString(), false, "#FFA000"))
            row.addView(createTableCell(item.inProcessCount.toString(), false, "#1976D2"))
            row.addView(createTableCell(item.resolvedCount.toString(), false, "#388E3C"))
            row.addView(createTableCell(item.totalCount.toString(), true, if (isDark) "#F9FAFB" else "#212121"))

            statsTableLayout.addView(row)
        }

        val totalRow = TableRow(context).apply {
            setBackgroundColor(if (isDark) Color.parseColor("#374151") else Color.parseColor("#E0E0E0"))
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }

        totalRow.addView(createTableCell("TOTALES", true, if (isDark) "#FFFFFF" else "#000000"))
        totalRow.addView(createTableCell(stats.pendingCount.toString(), true, "#FFA000"))
        totalRow.addView(createTableCell(stats.inProcessCount.toString(), true, "#1976D2"))
        totalRow.addView(createTableCell(stats.resolvedCount.toString(), true, "#388E3C"))
        totalRow.addView(createTableCell(stats.totalReportsInPeriod.toString(), true, if (isDark) "#FFFFFF" else "#000000"))

        statsTableLayout.addView(totalRow)
    }

    private fun createTableCell(textVal: String, isBold: Boolean, colorHex: String): TextView {
        return TextView(context).apply {
            text = textVal
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(Color.parseColor(colorHex))
            if (isBold) setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
