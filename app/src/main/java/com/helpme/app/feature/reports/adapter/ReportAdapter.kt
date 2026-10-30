package com.helpme.app.feature.reports.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.helpme.app.R
import com.helpme.app.databinding.ItemReportBinding
import com.helpme.app.domain.model.Report

class ReportAdapter(
    private val onItemClick: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.tvReportTitle.text = if (report.tipo.isNotBlank()) report.tipo else "Incidencia"
            binding.tvReportDate.text = "${report.getFormattedDate()} - ${report.getFormattedCode()}"

            // Status Badge Color
            val (badgeBg, badgeText) = when (report.estado.trim().lowercase()) {
                "pendiente" -> Color.parseColor("#FFA000") to "Pendiente"
                "en proceso", "proceso" -> Color.parseColor("#1976D2") to "En proceso"
                "resuelto" -> Color.parseColor("#388E3C") to "Resuelto"
                else -> Color.parseColor("#757575") to report.estado
            }

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(badgeBg)
            }
            binding.tvStatusBadge.background = drawable
            binding.tvStatusBadge.text = badgeText

            // Category Icon
            val iconRes = when (report.tipoId) {
                "INC_ALUMBRADO", "INC_SERVICIOS" -> android.R.drawable.ic_menu_compass
                "INC_BACHEO", "INC_INFRA" -> android.R.drawable.ic_menu_directions
                "INC_AGUA" -> android.R.drawable.ic_menu_rotate
                "INC_BASURA" -> android.R.drawable.ic_menu_delete
                "INC_SEGURIDAD" -> android.R.drawable.ic_lock_lock
                else -> android.R.drawable.ic_dialog_alert
            }
            binding.ivCategoryIcon.setImageResource(iconRes)

            binding.root.setOnClickListener {
                onItemClick(report)
            }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.reporteId == newItem.reporteId
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}
